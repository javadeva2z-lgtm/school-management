import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { SPECIAL_CLASS_NAMES } from '../../common/model/models';
import { ClassManagementService, ManagedClass, ManagedSection } from './class-management.service';

type ManagementTab = 'new' | 'existing';
type RecordType = 'classes' | 'sections';

interface ClassForm {
  classId: number | null;
  className: string;
  academicYear: string;
  isActive: boolean;
}

interface SectionForm {
  classId: string;
  sectionName: string;
  capacity: number | null;
  isActive: boolean;
}

@Component({
  selector: 'app-admin-class-section',
  imports: [FormsModule, RouterLink],
  templateUrl: './admin-class-section.component.html',
  styleUrl: './admin-class-section.component.css'
})
export class AdminClassSectionComponent {
  private readonly classManagementService = inject(ClassManagementService);

  protected readonly activeTab = signal<ManagementTab>('new');
  protected readonly activeRecordType = signal<RecordType>('classes');
  protected readonly classes = signal<ManagedClass[]>([]);
  protected readonly sections = signal<ManagedSection[]>([]);
  protected readonly isLoading = signal(false);
  protected readonly isSaving = signal(false);
  protected readonly deletingClassId = signal<number | null>(null);
  protected readonly deletingSectionId = signal<number | null>(null);
  protected readonly message = signal('');
  protected readonly error = signal('');
  protected readonly editingClassId = signal<number | null>(null);
  protected readonly editingSectionId = signal<number | null>(null);
  protected readonly classOptions = computed(() => [...this.classes()].sort((left, right) =>
    Number(left.classId) - Number(right.classId)
  ));
  protected readonly orderedSections = computed(() => [...this.sections()].sort((left, right) =>
    Number(left.classId) - Number(right.classId) || left.sectionName.localeCompare(right.sectionName)
  ));

  protected classForm: ClassForm = this.emptyClassForm();
  protected sectionForm: SectionForm = this.emptySectionForm();

  constructor() {
    this.loadCatalog();
  }

  protected selectTab(tab: ManagementTab): void {
    this.activeTab.set(tab);
    this.clearFeedback();
  }

  protected selectRecordType(recordType: RecordType): void {
    this.activeRecordType.set(recordType);
    this.clearFeedback();
  }

  protected saveClass(): void {
    const classId = Number(this.classForm.classId);
    const className = this.classForm.className.trim();
    const academicYear = this.classForm.academicYear.trim();

    if (!Number.isInteger(classId) || classId === 0 || (classId < 0 && !Object.hasOwn(SPECIAL_CLASS_NAMES, classId))) {
      this.setValidationError('Enter a non-zero class ID. Negative IDs are reserved for UKG (-1), LKG (-2), and Nursery (-3).');
      return;
    }
    if (!className || !academicYear) {
      this.setValidationError('Enter a class name and academic year.');
      return;
    }

    this.clearFeedback();
    this.isSaving.set(true);
    const payload = { className, academicYear, isActive: this.classForm.isActive };
    const request = this.editingClassId() === null
      ? this.classManagementService.createClass({ classId, ...payload })
      : this.classManagementService.updateClass(this.editingClassId()!, payload);

    request.subscribe({
      next: response => {
        this.isSaving.set(false);
        this.message.set(response.message || (this.editingClassId() === null ? 'Class created successfully.' : 'Class updated successfully.'));
        this.resetClassForm();
        this.loadCatalog();
      },
      error: error => {
        this.isSaving.set(false);
        this.error.set(this.readableError(error, 'The class could not be saved. Check the details and try again.'));
      }
    });
  }

  protected editClass(clazz: ManagedClass): void {
    this.editingClassId.set(Number(clazz.classId));
    this.classForm = {
      classId: Number(clazz.classId),
      className: clazz.className,
      academicYear: clazz.academicYear,
      isActive: clazz.isActive
    };
    this.clearFeedback();
    this.activeRecordType.set('classes');
    this.activeTab.set('new');
  }

  protected resetClassForm(): void {
    this.editingClassId.set(null);
    this.classForm = this.emptyClassForm();
  }

  protected deleteClass(clazz: ManagedClass): void {
    const classId = Number(clazz.classId);
    if (this.isSaving() || this.deletingClassId() !== null || this.deletingSectionId() !== null) {
      return;
    }
    if (!window.confirm(`Delete class ${this.displayClassName(clazz.classId)} (ID ${classId})? This removes every class record with this ID.`)) {
      return;
    }

    this.clearFeedback();
    this.deletingClassId.set(classId);
    this.classManagementService.deleteClass(classId).subscribe({
      next: response => {
        this.deletingClassId.set(null);
        if (this.editingClassId() === classId) {
          this.resetClassForm();
        }
        this.message.set(response.message || 'Class deleted successfully.');
        this.loadCatalog();
      },
      error: error => {
        this.deletingClassId.set(null);
        this.error.set(this.readableError(error, 'The class could not be deleted. Please try again.'));
      }
    });
  }

  protected saveSection(): void {
    const classId = Number(this.sectionForm.classId);
    const sectionName = this.sectionForm.sectionName.trim();
    const capacity = this.sectionForm.capacity;

    if (!this.classes().some(clazz => Number(clazz.classId) === classId)) {
      this.setValidationError('Select a class before saving the section.');
      return;
    }
    if (!sectionName) {
      this.setValidationError('Enter a section name.');
      return;
    }
    if (capacity !== null && (!Number.isInteger(capacity) || capacity < 0)) {
      this.setValidationError('Capacity must be a non-negative whole number.');
      return;
    }

    this.clearFeedback();
    this.isSaving.set(true);
    const payload = {
      classId,
      sectionName,
      capacity,
      isActive: this.sectionForm.isActive
    };
    const request = this.editingSectionId() === null
      ? this.classManagementService.createSection(payload)
      : this.classManagementService.updateSection(this.editingSectionId()!, payload);

    request.subscribe({
      next: response => {
        this.isSaving.set(false);
        this.message.set(response.message || (this.editingSectionId() === null ? 'Section created successfully.' : 'Section updated successfully.'));
        this.resetSectionForm();
        this.loadCatalog();
      },
      error: error => {
        this.isSaving.set(false);
        this.error.set(this.readableError(error, 'The section could not be saved. Check the details and try again.'));
      }
    });
  }

  protected editSection(section: ManagedSection): void {
    this.editingSectionId.set(section.id);
    this.sectionForm = {
      classId: String(section.classId),
      sectionName: section.sectionName,
      capacity: section.capacity,
      isActive: section.isActive
    };
    this.clearFeedback();
    this.activeRecordType.set('sections');
    this.activeTab.set('new');
  }

  protected deleteSection(section: ManagedSection): void {
    if (this.isSaving() || this.deletingClassId() !== null || this.deletingSectionId() !== null) {
      return;
    }
    if (!window.confirm(`Delete section "${section.sectionName}" from class ${this.displayClassName(section.classId)}?`)) {
      return;
    }

    this.clearFeedback();
    this.deletingSectionId.set(section.id);
    this.classManagementService.deleteSection(section.id).subscribe({
      next: response => {
        this.deletingSectionId.set(null);
        if (this.editingSectionId() === section.id) {
          this.resetSectionForm();
        }
        this.message.set(response.message || 'Section deleted successfully.');
        this.loadCatalog();
      },
      error: error => {
        this.deletingSectionId.set(null);
        this.error.set(this.readableError(error, 'The section could not be deleted. Please try again.'));
      }
    });
  }

  protected resetSectionForm(): void {
    this.editingSectionId.set(null);
    this.sectionForm = this.emptySectionForm();
  }

  protected displayClassName(classId: number | string): string {
    const specialName = SPECIAL_CLASS_NAMES[Number(classId)];
    return specialName ?? this.classes().find(clazz => Number(clazz.classId) === Number(classId))?.className ?? String(classId);
  }

  protected isDeletingClass(clazz: ManagedClass): boolean {
    return this.deletingClassId() === Number(clazz.classId);
  }

  private loadCatalog(): void {
    this.isLoading.set(true);
    this.error.set('');
    this.classManagementService.getCatalog().subscribe({
      next: catalog => {
        this.classes.set(catalog.classes);
        this.sections.set(catalog.sections);
        this.isLoading.set(false);
      },
      error: error => {
        this.isLoading.set(false);
        this.error.set(this.readableError(error, 'Classes and sections could not be loaded. Please retry.'));
      }
    });
  }

  private emptyClassForm(): ClassForm {
    return { classId: null, className: '', academicYear: '', isActive: true };
  }

  private emptySectionForm(): SectionForm {
    return { classId: '', sectionName: '', capacity: null, isActive: true };
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
