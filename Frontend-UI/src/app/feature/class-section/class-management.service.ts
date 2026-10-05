import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, forkJoin, map } from 'rxjs';

import { classesApiUrl, classSectionApiUrl } from '../../core/config/api.config';

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
}
