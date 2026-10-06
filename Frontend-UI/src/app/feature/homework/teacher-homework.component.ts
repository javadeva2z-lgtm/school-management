import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { HomeworkService } from './homework.service';
import { ClassSectionService } from '../class-section/class-section.service';
import { ClassManagementService, ManagedSubject } from '../class-section/class-management.service';
import { PeopleService } from '../people/people.service';
import { classDisplayName, ClassSectionOption, HomeworkRecord, Student } from '../../common/model/models';
import { ProfileService } from '../profile/profile.service';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { concatMap, from, tap, toArray } from 'rxjs';

type HomeworkTab = 'new' | 'list';
type HomeworkLine = { id: number; subjectValue: string; work: string };
type HomeworkSubjectOption = { value: string; subjectId: number | null; subjectName: string };

let nextHomeworkLineId = 0;

function createHomeworkLine(): HomeworkLine {
  return { id: nextHomeworkLineId++, subjectValue: '', work: '' };
}

function defaultDueDate(): string {
  const tomorrow = new Date();
  tomorrow.setDate(tomorrow.getDate());
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
  private readonly classManagementService = inject(ClassManagementService);
  private readonly peopleService = inject(PeopleService);
  protected readonly profileService = inject(ProfileService);

  protected readonly activeTab = signal<HomeworkTab>('new');
  protected readonly homeworkLines = signal<HomeworkLine[]>([createHomeworkLine()]);
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
  protected readonly subjects = signal<ManagedSubject[]>([]);
  protected readonly isLoadingSubjects = signal(false);
  protected readonly subjectOptions = computed<HomeworkSubjectOption[]>(() => [
    ...this.subjects()
      .filter(subject => subject.isActive)
      .map(subject => ({
        value: String(subject.id),
        subjectId: subject.id,
        subjectName: subject.subjectName
      })),
    { value: 'special:others', subjectId: null, subjectName: 'Others' },
    { value: 'special:diary', subjectId: null, subjectName: 'Diary' }
  ]);
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
  protected readonly workspaceRole = computed(() => this.profile()?.role === 'ADMIN' ? 'Admin' : 'Teacher');

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
        this.loadSubjects(resolved.classId);
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
    if (!this.editingAssignment()) {
      this.homeworkLines.set([createHomeworkLine()]);
    }
    this.loadSubjects(className);
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

  protected onDueDateChange(event: Event): void {
    this.dueDate.set((event.target as HTMLInputElement).value);
    this.uploadMessage.set('');
  }

  protected updateHomeworkLine(id: number, field: 'subjectValue' | 'work', value: string): void {
    this.homeworkLines.update(lines => lines.map(line => line.id === id ? { ...line, [field]: value } : line));
    this.uploadMessage.set('');
  }

  protected addHomeworkLine(): void {
    this.homeworkLines.update(lines => [...lines, createHomeworkLine()]);
  }

  protected removeHomeworkLine(id: number): void {
    this.homeworkLines.update(lines => lines.length > 1 ? lines.filter(line => line.id !== id) : lines);
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
    const subjectValue = assignment.subjectId
      ? String(assignment.subjectId)
      : assignment.title.toLowerCase() === 'diary'
        ? 'special:diary'
        : 'special:others';
    this.homeworkLines.set([{ id: nextHomeworkLineId++, subjectValue, work: assignment.description ?? '' }]);
    this.dueDate.set(assignment.dueDate);
    this.workType.set(assignment.workType);
    this.selectedFiles.set([]);
    this.uploadMessage.set('');
    this.activeTab.set('new');
    this.loadSubjects(String(assignment.classId));
    this.loadStudents();
  }

  protected cancelEdit(): void {
    this.editingAssignment.set(null);
    this.homeworkLines.set([createHomeworkLine()]);
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
    if (!this.selectedClass() || !this.selectedSection()) {
      this.uploadMessage.set('Select a class and section before uploading.');
      return;
    }

    const lines = this.homeworkLines();
    const invalidLine = lines.find(line =>
      !this.subjectOptions().some(option => option.value === line.subjectValue) || !line.work.trim()
    );
    if (invalidLine) {
      this.uploadMessage.set('Select a subject and enter its homework for every line.');
      return;
    }

    if (this.isLoadingSubjects()) {
      this.uploadMessage.set('Wait for the class subjects to finish loading.');
      return;
    }

    this.isUploading.set(true);
    this.uploadMessage.set('');
    let uploadedCount = 0;
    const editingAssignment = this.editingAssignment();
    const upload$ = editingAssignment
      ? this.homeworkService.uploadWork(this.buildHomework(lines[0], editingAssignment.id)).pipe(toArray())
      : from(lines).pipe(
        concatMap(line => this.homeworkService.uploadWork(this.buildHomework(line)).pipe(
          tap(() => uploadedCount++)
        )),
        toArray()
      );

    upload$.subscribe({
      next: () => {
        this.isUploading.set(false);
        this.cancelEdit();
        this.uploadMessage.set(editingAssignment ? 'Work updated successfully.' : 'Homework added for all subjects.');
        this.loadAssignments();
      },
      error: () => {
        this.isUploading.set(false);
        this.uploadMessage.set(editingAssignment
          ? 'Work could not be updated. Please try again.'
          : `Could not upload all homework lines. ${uploadedCount} of ${lines.length} were saved; review the list before retrying.`);
      }
    });
  }

  private buildHomework(line: HomeworkLine, id?: number): {
    id?: number;
    title: string;
    description: string;
    workType: 'CLASSWORK' | 'HOMEWORK';
    classId: number;
    sectionName: string;
    subjectId: number | null;
    files: File[];
    teacherId: number;
    dueDate: string;
  } {
    const subject = this.subjectOptions().find(option => option.value === line.subjectValue);
    if (!subject) {
      throw new Error(`Unknown homework subject: ${line.subjectValue}`);
    }
    return {
      id,
      title: subject.subjectName,
      description: line.work.trim(),
      workType: this.workType(),
      classId: Number(this.selectedClass()),
      sectionName: this.selectedSection(),
      subjectId: subject.subjectId,
      files: this.selectedFiles(),
      teacherId: this.profile()?.id ?? 0,
      dueDate: this.dueDate()
    };
  }

  private loadSubjects(classId: string): void {
    const parsedClassId = Number(classId);
    if (!Number.isFinite(parsedClassId) || parsedClassId === 0) {
      this.subjects.set([]);
      this.isLoadingSubjects.set(false);
      return;
    }

    this.isLoadingSubjects.set(true);
    this.classManagementService.getSubjects(parsedClassId).subscribe({
      next: response => {
        if (this.selectedClass() !== classId) {
          return;
        }
        this.subjects.set(response.data);
        this.isLoadingSubjects.set(false);
      },
      error: () => {
        if (this.selectedClass() !== classId) {
          return;
        }
        this.subjects.set([]);
        this.isLoadingSubjects.set(false);
        this.uploadMessage.set('Subjects could not be loaded. Please retry before adding homework.');
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