import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { HomeworkService } from './homework.service';
import { ClassSectionService } from '../class-section/class-section.service';
import { PeopleService } from '../people/people.service';
import { classDisplayName, ClassSectionOption, HomeworkRecord, Student } from '../../common/model/models';
import { ProfileService } from '../profile/profile.service';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';

type HomeworkTab = 'new' | 'list';

function defaultDueDate(): string {
  const tomorrow = new Date();
  tomorrow.setDate(tomorrow.getDate() + 1);
  const year = tomorrow.getFullYear();
  const month = String(tomorrow.getMonth() + 1).padStart(2, '0');
  const day = String(tomorrow.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

@Component({
  selector: 'app-teacher-homework',
  imports: [RouterLink, FormsModule],
  templateUrl: './teacher-homework.component.html',
  styleUrl: './teacher-homework.component.css'
})
export class TeacherHomeworkComponent {
  protected readonly classDisplayName = classDisplayName;
  private readonly homeworkService = inject(HomeworkService);
  private readonly classSectionService = inject(ClassSectionService);
  private readonly peopleService = inject(PeopleService);
  protected readonly profileService = inject(ProfileService);

  protected readonly activeTab = signal<HomeworkTab>('new');
  protected readonly title = signal('');
  protected readonly description = signal('');
  protected readonly dueDate = signal(defaultDueDate());
  protected readonly workType = signal<'CLASSWORK' | 'HOMEWORK'>('HOMEWORK');
  protected readonly editingAssignment = signal<HomeworkRecord | null>(null);
  protected readonly deletingAssignmentId = signal<number | null>(null);
  protected readonly selectedFiles = signal<File[]>([]);
  protected readonly isUploading = signal(false);
  protected readonly uploadMessage = signal('');
  protected readonly classOptions = signal<ClassSectionOption[]>([]);
  protected readonly selectedClass = signal('');
  protected readonly selectedSection = signal('');
  protected readonly sectionOptions = computed(() => this.classOptions().find(option => option.classId === this.selectedClass())?.sections ?? []);
  protected readonly students = signal<Student[]>([]);
  protected readonly assignments = signal<HomeworkRecord[]>([]);
  protected readonly groupedAssignments = computed(() => {
    const groups = new Map<string, { homework: HomeworkRecord[]; classwork: HomeworkRecord[] }>();

    this.assignments().forEach(assignment => {
      const date = assignment.dueDate || 'Unknown date';
      const bucket = groups.get(date) ?? { homework: [], classwork: [] };

      if (assignment.workType === 'HOMEWORK') {
        bucket.homework.push(assignment);
      } else {
        bucket.classwork.push(assignment);
      }

      groups.set(date, bucket);
    });

    return Array.from(groups.entries()).map(([date, value]) => ({
      date,
      homework: [...value.homework].sort((a, b) => a.title.localeCompare(b.title)),
      classwork: [...value.classwork].sort((a, b) => a.title.localeCompare(b.title))
    })).sort((a, b) => b.date.localeCompare(a.date));
  });
  protected readonly isLoadingStudents = signal(false);
  protected readonly isLoadingAssignments = signal(false);
  protected readonly profile = toSignal(this.profileService.getProfile());
  protected readonly teacherId = computed(() => this.profile()?.role === 'TEACHER' ? this.profile()?.id ?? null : null);

  constructor() {
    this.loadClasses();
  }

  protected loadClasses(): void {
    this.classSectionService.getAllWithTeacherDefaultSelection().subscribe({
      next: ({ classOptions, defaultSelection }) => {
        this.classOptions.set(classOptions);
        const resolved = this.classSectionService.resolveDefaultClassSection(classOptions, defaultSelection);
        this.selectedClass.set(resolved.classId);
        this.selectedSection.set(resolved.sectionName);
        this.loadStudents();
        this.loadAssignments();
      },
      error: () => {
        this.uploadMessage.set('Unable to load classes and sections.');
      }
    });
  }

  protected onClassChange(value: string | number): void {
    const className = String(value ?? '');
    this.selectedClass.set(className);
    const firstSection = this.classOptions().find(option => option.classId === className)?.sections[0]?.sectionName ?? '';
    this.selectedSection.set(firstSection);
    this.loadStudents();
    this.loadAssignments();
  }

  protected onSectionChange(value: string | number): void {
    this.selectedSection.set(String(value ?? ''));
    this.loadStudents();
    this.loadAssignments();
  }

  protected switchTab(tab: HomeworkTab): void {
    this.activeTab.set(tab);
    this.uploadMessage.set('');
    if (tab === 'list') {
      this.loadAssignments();
    }
  }

  protected onTitleChange(event: Event): void {
    this.title.set((event.target as HTMLInputElement).value);
    this.uploadMessage.set('');
  }

  protected onDueDateChange(event: Event): void {
    this.dueDate.set((event.target as HTMLInputElement).value);
    this.uploadMessage.set('');
  }

  protected onDescriptionChange(event: Event): void {
    this.description.set((event.target as HTMLTextAreaElement).value);
    this.uploadMessage.set('');
  }

  protected onWorkTypeChange(event: Event): void {
    this.workType.set((event.target as HTMLSelectElement).value as 'CLASSWORK' | 'HOMEWORK');
    this.uploadMessage.set('');
  }

  protected onFileChange(event: Event): void {
    const files = Array.from((event.target as HTMLInputElement).files ?? []);
    this.selectedFiles.set(files);
    this.uploadMessage.set('');
  }

  protected downloadAttachment(file: { fileName: string; downloadUrl?: string }): void {
    if (!file.downloadUrl) {
      return;
    }

    this.homeworkService.downloadFile(file.downloadUrl, file.fileName).subscribe();
  }

  protected editAssignment(assignment: HomeworkRecord): void {
    this.editingAssignment.set(assignment);
    this.selectedClass.set(String(assignment.classId));
    this.selectedSection.set(assignment.sectionName);
    this.title.set(assignment.title);
    this.description.set(assignment.description ?? '');
    this.dueDate.set(assignment.dueDate);
    this.workType.set(assignment.workType);
    this.selectedFiles.set([]);
    this.uploadMessage.set('');
    this.activeTab.set('new');
    this.loadStudents();
  }

  protected cancelEdit(): void {
    this.editingAssignment.set(null);
    this.title.set('');
    this.description.set('');
    this.dueDate.set(defaultDueDate());
    this.workType.set('HOMEWORK');
    this.selectedFiles.set([]);
    this.uploadMessage.set('');
  }

  protected deleteAssignment(assignment: HomeworkRecord): void {
    if (!window.confirm(`Delete "${assignment.title}"? This action cannot be undone.`)) {
      return;
    }

    this.deletingAssignmentId.set(assignment.id);
    this.uploadMessage.set('');
    this.homeworkService.deleteWork(assignment.id).subscribe({
      next: () => {
        this.deletingAssignmentId.set(null);
        this.uploadMessage.set('Work deleted successfully.');
        this.loadAssignments();
      },
      error: () => {
        this.deletingAssignmentId.set(null);
        this.uploadMessage.set('Work could not be deleted. Please try again.');
      }
    });
  }

  protected uploadWork(): void {
    if (!this.title().trim() || !this.description().trim()) {
      this.uploadMessage.set('Enter a title and description before uploading.');
      return;
    }

    if (!this.selectedClass() || !this.selectedSection()) {
      this.uploadMessage.set('Select a class and section before uploading.');
      return;
    }

    this.isUploading.set(true);
    this.uploadMessage.set('');
    this.homeworkService.uploadWork({
      id: this.editingAssignment()?.id,
      title: this.title().trim(),
      description: this.description().trim(),
      workType: this.workType(),
      classId: Number(this.selectedClass()),
      sectionName: this.selectedSection(),
      files: this.selectedFiles(),
      teacherId: this.profile()?.id ?? 0,
      dueDate: this.dueDate()
    }).subscribe({
      next: response => {
        this.isUploading.set(false);
        const successMessage = response.message;
        this.cancelEdit();
        this.uploadMessage.set(successMessage);
        this.loadAssignments();
      },
      error: () => {
        this.isUploading.set(false);
        this.uploadMessage.set(this.editingAssignment()
          ? 'Work could not be updated. Please try again.'
          : 'Work could not be uploaded. Please try again.');
      }
    });
  }

  private loadStudents(): void {
    const className = this.selectedClass();
    const section = this.selectedSection();

    if (!className || !section) {
      this.students.set([]);
      return;
    }

    this.isLoadingStudents.set(true);
    this.peopleService.getStudentsByClassAndSection(Number(className), section).subscribe(res => {
      if (this.selectedClass() !== className || this.selectedSection() !== section) {
        return;
      }
      this.students.set(res ?? []);
      this.isLoadingStudents.set(false);
    });
  }

  private loadAssignments(): void {
    const className = this.selectedClass();
    const section = this.selectedSection();

    if (!className || !section) {
      this.assignments.set([]);
      return;
    }

    this.isLoadingAssignments.set(true);
    this.homeworkService.getByClassAndSection(className, section).subscribe(assignments => {
      if (this.selectedClass() !== className || this.selectedSection() !== section) {
        return;
      }
      this.assignments.set(assignments);
      this.isLoadingAssignments.set(false);
    });
  }
}