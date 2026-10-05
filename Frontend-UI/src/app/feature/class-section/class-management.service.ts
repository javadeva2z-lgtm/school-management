import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, forkJoin, map } from 'rxjs';

import { classesApiUrl, classSectionApiUrl, classSubjectsApiUrl } from '../../core/config/api.config';

export interface ManagedClass {
  classId: number | string;
  className: string;
  academicYear: string;
  isActive: boolean;
}

export interface ManagedSection {
  id: number;
  classId: number | string;
  sectionName: string;
  capacity: number | null;
  isActive: boolean;
}

export interface ManagedSubject {
  id: number;
  classId: number | string;
  subjectName: string;
  subjectCode: string;
  isActive: boolean;
}

interface ApiResponse<T> {
  status: string;
  code: number;
  message: string;
  data: T;
  timestamp: string;
}

export interface ClassSectionCatalog {
  classes: ManagedClass[];
  sections: ManagedSection[];
}

@Injectable({ providedIn: 'root' })
export class ClassManagementService {
  private readonly http = inject(HttpClient);

  getClasses(): Observable<ManagedClass[]> {
    return this.http.get<ApiResponse<ManagedClass[]>>(classesApiUrl('')).pipe(map(response => response.data));
  }

  getCatalog(): Observable<ClassSectionCatalog> {
    return forkJoin({
      classes: this.http.get<ApiResponse<ManagedClass[]>>(classesApiUrl('')),
      sections: this.http.get<ApiResponse<ManagedSection[]>>(classSectionApiUrl(''))
    }).pipe(map(({ classes, sections }) => ({
      classes: classes.data,
      sections: sections.data
    })));
  }

  createClass(payload: Omit<ManagedClass, 'isActive'> & { isActive: boolean }): Observable<ApiResponse<ManagedClass>> {
    return this.http.post<ApiResponse<ManagedClass>>(classesApiUrl(''), payload);
  }

  updateClass(classId: number, payload: Omit<ManagedClass, 'classId'>): Observable<ApiResponse<ManagedClass>> {
    return this.http.put<ApiResponse<ManagedClass>>(classesApiUrl(`/${classId}`), payload);
  }

  createSection(payload: Omit<ManagedSection, 'id'>): Observable<ApiResponse<ManagedSection>> {
    return this.http.post<ApiResponse<ManagedSection>>(classSectionApiUrl(''), payload);
  }

  updateSection(id: number, payload: Omit<ManagedSection, 'id'>): Observable<ApiResponse<ManagedSection>> {
    return this.http.put<ApiResponse<ManagedSection>>(classSectionApiUrl(`/${id}`), payload);
  }

  getSubjects(classId: number): Observable<ApiResponse<ManagedSubject[]>> {
    return this.http.get<ApiResponse<ManagedSubject[]>>(classSubjectsApiUrl(`/class/${classId}`));
  }

  createSubject(payload: Omit<ManagedSubject, 'id'>): Observable<ApiResponse<ManagedSubject>> {
    return this.http.post<ApiResponse<ManagedSubject>>(classSubjectsApiUrl(''), payload);
  }

  updateSubject(id: number, payload: Omit<ManagedSubject, 'id'>): Observable<ApiResponse<ManagedSubject>> {
    return this.http.put<ApiResponse<ManagedSubject>>(classSubjectsApiUrl(`/${id}`), payload);
  }

  deleteSubject(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(classSubjectsApiUrl(`/${id}`));
  }
}
