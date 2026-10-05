import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, forkJoin, map, Observable, of, switchMap } from 'rxjs';

import { classesApiUrl, classSectionApiUrl, classTeacherApiUrl } from '../../core/config/api.config';
import { ClassApiResponse, ClassSectionApiResponse, ClassSectionOption, ClassTeacherApiResponse, ClassTeacherAssignment, classDisplayName, SPECIAL_CLASS_NAMES } from '../../common/model/models';
import { ProfileService } from '../profile/profile.service';

@Injectable({ providedIn: 'root' })
export class ClassSectionService {
  private readonly http = inject(HttpClient);
  private readonly profileService = inject(ProfileService);

  getAll(): Observable<ClassSectionOption[]> {
    return forkJoin({
      sectionsResponse: this.http.get<ClassSectionApiResponse>(classSectionApiUrl('')),
      classes: this.http.get<ClassApiResponse>(classesApiUrl('/active'))
    }).pipe(
      map(({ sectionsResponse, classes }) => {
        const classNames = new Map(classes.data.map(clazz => [String(clazz.classId), clazz.className]));
        const classOptions = new Map<string, ClassSectionOption>();

        classes.data.forEach(clazz => {
          const classId = String(clazz.classId);
          classOptions.set(classId, {
            classId,
            className: SPECIAL_CLASS_NAMES[Number(classId)] ?? clazz.className,
            sections: []
          });
        });

        sectionsResponse.data.forEach(classSection => {
          const classId = String(classSection.classId);
          const section = {
            sectionId: classSection.id,
            sectionName: classSection.sectionName,
            capacity: classSection.capacity
          };
          const option = classOptions.get(classId);

          if (option) {
            if (!option.sections.some(item => item.sectionId === section.sectionId)) {
              option.sections.push(section);
            }
          } else {
            classOptions.set(classId, {
              classId,
              className: SPECIAL_CLASS_NAMES[Number(classId)] ?? classNames.get(classId) ?? classDisplayName(classId),
              sections: [section]
            });
          }
        });
        const sorted = [...classOptions.values()]
          .map(option => ({
            ...option,
            sections: option.sections.sort((a, b) => a.sectionName.localeCompare(b.sectionName))
          }))
          .sort((a, b) => Number(a.classId) - Number(b.classId));
        return sorted;
      })
    );
  }

  getClassTeacherAssignments(): Observable<ClassTeacherAssignment[]> {
    return this.http.get<ClassTeacherApiResponse>(classTeacherApiUrl('')).pipe(
      map(response => response.data ?? []),
      catchError(() => of([]))
    );
  }

  getTeacherDefaultClassSection(): Observable<{ classId: string; sectionName: string } | null> {
    return this.profileService.getProfile().pipe(
      switchMap(profile => this.getClassTeacherAssignments().pipe(
        map(assignments => {
          const assignment = assignments.find(item => item.teacherId === profile.id);
          if (!assignment) return null;
          return { classId: String(assignment.classId), sectionName: assignment.sectionName };
        })
      )),
      catchError(() => of(null))
    );
  }

  getAllWithTeacherDefaultSelection(): Observable<{ classOptions: ClassSectionOption[]; defaultSelection: { classId: string; sectionName: string } | null }> {
    return this.getAll().pipe(
      switchMap(classOptions => this.getTeacherDefaultClassSection().pipe(
        map(defaultSelection => ({ classOptions, defaultSelection }))
      ))
    );
  }

  resolveDefaultClassSection(
    options: ClassSectionOption[],
    defaultSelection: { classId: string; sectionName: string } | null
  ): { classId: string; sectionName: string } {
    const fallbackClass = options[0]?.classId ?? '';
    const fallbackSection = options[0]?.sections[0]?.sectionName ?? '';

    if (!defaultSelection) {
      return { classId: fallbackClass, sectionName: fallbackSection };
    }

    const matchedClass = options.find(option => option.classId === defaultSelection.classId);
    if (!matchedClass) {
      return { classId: fallbackClass, sectionName: fallbackSection };
    }

    const matchedSection = matchedClass.sections.find(section => section.sectionName === defaultSelection.sectionName);
    return {
      classId: matchedClass.classId,
      sectionName: matchedSection?.sectionName ?? matchedClass.sections[0]?.sectionName ?? fallbackSection
    };
  }
}
