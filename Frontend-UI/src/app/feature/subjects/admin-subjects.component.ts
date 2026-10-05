import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { SPECIAL_CLASS_NAMES } from '../../common/model/models';
import { ClassManagementService, ManagedClass, ManagedSubject } from '../class-section/class-management.service';

type SubjectTab = 'new' | 'existing';

interface SubjectForm {
  classId: string;
  subjectName: string;
  subjectCode: string;
  isActive: boolean;
}

@Component({
  selector: 'app-admin-subjects',
  imports: [FormsModule, RouterLink],
  templateUrl: './admin-subjects.component.html',
  styleUrl: './admin-subjects.component.css'
})
export class AdminSubjectsComponent {
  private readonly classManagementService = inject(ClassManagementService);

  protected readonly activeTab = signal<SubjectTab>('new');
  protected readonly classes = signal<ManagedClass[]>([]);
  protected readonly subjects = signal<ManagedSubject[]>([]);
  protected readonly classOptions = computed(() => [...this.classes()].sort((left, right) =>
    Number(left.classId) - Number(right.classId)
  ));
  protected readonly selectedClassId = signal('');
  protected readonly editingSubjectId = signal<number | null>(null);
  protected readonly isLoading = signal(false);
  protected readonly isLoadingSubjects = signal(false);
  protected readonly isSaving = signal(false);
  protected readonly message = signal('');
  protected readonly error = signal('');
  protected subjectForm: SubjectForm = this.emptyForm();

  constructor() {
    this.loadClasses();
  }

  protected selectTab(tab: SubjectTab): void {
    this.activeTab.set(tab);
    this.clearFeedback();
    if (tab === 'existing' && this.selectedClassId()) {
      this.loadSubjects(Number(this.selectedClassId()));
    }
  }

  protected saveSubject(): void {
    const classId = Number(this.subjectForm.classId);
    const subjectName = this.subjectForm.subjectName.trim();
    const subjectCode = this.subjectForm.subjectCode.trim();

    if (!this.classes().some(clazz => Number(clazz.classId) === classId)) {
      this.setValidationError('Select a class before saving the subject.');
      return;
    }
    if (!subjectName || !subjectCode) {
      this.setValidationError('Enter both a subject name and subject code.');
      return;
    }

    this.clearFeedback();
    this.isSaving.set(true);
    const payload = { classId, subjectName, subjectCode, isActive: this.subjectForm.isActive };
    const editingId = this.editingSubjectId();
    const request = editingId === null
      ? this.classManagementService.createSubject(payload)
      : this.classManagementService.updateSubject(editingId, payload);

    request.subscribe({
      next: response => {
        this.isSaving.set(false);
        this.message.set(response.message || (editingId === null ? 'Subject added successfully.' : 'Subject updated successfully.'));
        this.selectedClassId.set(String(classId));
        this.resetForm();
        this.loadSubjects(classId);
      },
      error: error => {
        this.isSaving.set(false);
        this.error.set(this.readableError(error, 'The subject could not be saved. Check the details and try again.'));
      }
    });
  }

  protected editSubject(subject: ManagedSubject): void {
    this.editingSubjectId.set(subject.id);
    this.subjectForm = {
      classId: String(subject.classId),
      subjectName: subject.subjectName,
      subjectCode: subject.subjectCode,
      isActive: subject.isActive
    };
    this.clearFeedback();
    this.activeTab.set('new');
  }

  protected resetForm(): void {
    this.editingSubjectId.set(null);
    this.subjectForm = { ...this.emptyForm(), classId: this.selectedClassId() };
    this.clearFeedback();
  }

  protected onClassChange(classId: string): void {
    this.selectedClassId.set(classId);
    if (!this.editingSubjectId()) {
      this.subjectForm.classId = classId;
    }
    this.clearFeedback();
    this.loadSubjects(Number(classId));
  }

  protected deleteSubject(subject: ManagedSubject): void {
    if (!window.confirm(`Delete ${subject.subjectName} (${subject.subjectCode}) from this class?`)) {
      return;
    }

    this.clearFeedback();
    this.isSaving.set(true);
    this.classManagementService.deleteSubject(subject.id).subscribe({
      next: response => {
        this.isSaving.set(false);
        this.message.set(response.message || 'Subject deleted successfully.');
        this.loadSubjects(Number(this.selectedClassId()));
      },
      error: error => {
        this.isSaving.set(false);
        this.error.set(this.readableError(error, 'The subject could not be deleted. Please try again.'));
      }
    });
  }

  protected displayClassName(classId: number | string): string {
    return SPECIAL_CLASS_NAMES[Number(classId)]
      ?? this.classes().find(clazz => Number(clazz.classId) === Number(classId))?.className
      ?? String(classId);
  }

  private loadClasses(): void {
    this.isLoading.set(true);
    this.classManagementService.getClasses().subscribe({
      next: classes => {
        this.classes.set(classes);
        this.isLoading.set(false);
        if (!this.selectedClassId() && classes.length) {
          this.selectedClassId.set(String(this.classOptions()[0].classId));
          this.subjectForm.classId = this.selectedClassId();
        }
        if (this.selectedClassId()) {
          this.loadSubjects(Number(this.selectedClassId()));
        }
      },
      error: error => {
        this.isLoading.set(false);
        this.error.set(this.readableError(error, 'Classes could not be loaded. Please retry.'));
      }
    });
  }

  private loadSubjects(classId: number): void {
    if (!Number.isFinite(classId) || classId === 0) {
      this.subjects.set([]);
      return;
    }
    this.isLoadingSubjects.set(true);
    this.classManagementService.getSubjects(classId).subscribe({
      next: response => {
        if (Number(this.selectedClassId()) !== classId) {
          return;
        }
        this.subjects.set(response.data);
        this.isLoadingSubjects.set(false);
      },
      error: error => {
        this.isLoadingSubjects.set(false);
        this.subjects.set([]);
        this.error.set(this.readableError(error, 'Subjects could not be loaded. Please retry.'));
      }
    });
  }

  private emptyForm(): SubjectForm {
    return { classId: '', subjectName: '', subjectCode: '', isActive: true };
  }

  private clearFeedback(): void {
    this.message.set('');
    this.error.set('');
  }

  private setValidationError(message: string): void {
    this.message.set('');
    this.error.set(message);
  }

  private readableError(error: unknown, fallback: string): string {
    if (error instanceof HttpErrorResponse && typeof error.error?.message === 'string' && error.error.message.trim()) {
      return error.error.message;
    }
    return fallback;
  }
}
