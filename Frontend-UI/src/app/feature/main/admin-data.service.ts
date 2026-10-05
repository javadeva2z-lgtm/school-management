import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { bulkApiUrl } from '../../core/config/api.config';
import { Observable } from 'rxjs';

export type AdminDataType = 'Student' | 'Teacher' | 'Section' | 'Subject';

@Injectable({ providedIn: 'root' })
export class AdminDataService {
  private readonly http = inject(HttpClient);

  importFile(file: File, dataType: AdminDataType): Observable<void> {
    const formData = new FormData();
    formData.append('file', file);
    if (dataType === 'Subject') {
      return this.http.post<void>(bulkApiUrl('/import-csv/Subject'), formData);
    }
    return this.http.post<void>(bulkApiUrl('/import-csv/' + dataType), formData);
  }

  exportRecords(dataType: AdminDataType, classId = '0', sectionName = '0'): Observable<Blob> {
    if (dataType === 'Teacher') {
      return this.http.get(bulkApiUrl('/export/teacher'), {
        responseType: 'blob'
      });
    }
    else if (dataType === 'Student') {
      return this.http.get(bulkApiUrl(`/export/student/class/${encodeURIComponent(classId)}/section/${encodeURIComponent(sectionName)}`), {
        responseType: 'blob'
      });
    }
    else if (dataType === 'Subject') {
      return this.http.get(bulkApiUrl(`/export/subject?classId=${encodeURIComponent(classId)}`), { responseType: 'blob' });
    }
    else {
      return this.http.get(bulkApiUrl('/export/section'), {
        responseType: 'blob'
      });
    }
  }
  exportSample(dataType: AdminDataType): Observable<Blob> {
    if (dataType === 'Subject') {
      return this.http.get(bulkApiUrl('/template/Subject'), { responseType: 'blob' });
    }
    return this.http.get(bulkApiUrl('/template/' + dataType), {
      responseType: 'blob'
    });
  }
}
