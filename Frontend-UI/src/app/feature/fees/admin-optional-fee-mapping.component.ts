import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin, map, of, switchMap } from 'rxjs';

import { ClassSectionOption, Student, classDisplayName } from '../../common/model/models';
import { ClassSectionService } from '../class-section/class-section.service';
import { PeopleService } from '../people/people.service';
import { FeeItem, FeesService, StudentServiceSubscription } from './fees.service';

interface StudentEnrollment {
  student: Student;
  subscriptions: StudentServiceSubscription[];
}

@Component({
  selector: 'app-admin-optional-fee-mapping',
  imports: [RouterLink],
  templateUrl: './admin-optional-fee-mapping.component.html',
  styleUrl: './admin-optional-fee-mapping.component.css'
})
export class AdminOptionalFeeMappingComponent {
  private readonly classSectionService = inject(ClassSectionService);
  private readonly peopleService = inject(PeopleService);
  private readonly feesService = inject(FeesService);

  protected readonly classOptions = signal<ClassSectionOption[]>([]);
  protected readonly selectedClassId = signal('');
  protected readonly selectedSectionName = signal('');
  protected readonly optionalFeeItems = signal<FeeItem[]>([]);
  protected readonly enrollments = signal<StudentEnrollment[]>([]);
  protected readonly sections = computed(() =>
    this.classOptions().find(option => option.classId === this.selectedClassId())?.sections ?? []
  );
  protected readonly isLoadingClasses = signal(true);
  protected readonly isLoadingFeeItems = signal(false);
  protected readonly isLoadingStudents = signal(false);
  protected readonly savingEnrollment = signal('');
  protected readonly errorMessage = signal('');
  protected readonly statusMessage = signal('');

  constructor() {
    this.loadClasses();
  }

  protected selectClass(classId: string): void {
    this.selectedClassId.set(classId);
    this.selectedSectionName.set('');
    this.optionalFeeItems.set([]);
    this.enrollments.set([]);
    this.isLoadingStudents.set(false);
    this.clearFeedback();
    this.loadOptionalFeeItems(classId);
  }

  protected selectSection(sectionName: string): void {
    this.selectedSectionName.set(sectionName);
    this.enrollments.set([]);
    this.isLoadingStudents.set(false);
    this.clearFeedback();
    this.loadStudents();
  }

  protected isSubscribed(admissionNumber: number, feeItemId: number): boolean {
    return this.enrollments()
      .find(enrollment => enrollment.student.admissionNumber === admissionNumber)
      ?.subscriptions.some(subscription => subscription.feeItemId === feeItemId) ?? false;
  }

  protected toggleEnrollment(student: Student, feeItem: FeeItem, checked: boolean): void {
    const key = `${student.admissionNumber}:${feeItem.id}`;
    if (this.savingEnrollment() || this.isSubscribed(student.admissionNumber, feeItem.id) === checked) {
      return;
    }

    const existingSubscription = this.enrollments()
      .find(enrollment => enrollment.student.admissionNumber === student.admissionNumber)
      ?.subscriptions.find(subscription => subscription.feeItemId === feeItem.id);
    this.savingEnrollment.set(key);
    this.clearFeedback();

    if (checked) {
      this.feesService.createStudentServiceSubscription(student.admissionNumber, feeItem.id).subscribe({
        next: subscription => {
          this.updateSubscriptions(student.admissionNumber, feeItem.id, subscription);
          this.statusMessage.set(`${feeItem.serviceName} applied to ${student.name}.`);
        },
        error: () => this.failEnrollment(student.name)
      });
      return;
    }

    if (!existingSubscription) {
      this.savingEnrollment.set('');
      return;
    }
    this.feesService.deleteStudentServiceSubscription(existingSubscription.id).subscribe({
      next: () => {
        this.updateSubscriptions(student.admissionNumber, feeItem.id);
        this.statusMessage.set(`${feeItem.serviceName} removed from ${student.name}.`);
      },
      error: () => this.failEnrollment(student.name)
    });
  }

  protected displayClassName(classId: string): string {
    return classDisplayName(classId, this.classOptions());
  }

  private loadClasses(): void {
    this.isLoadingClasses.set(true);
    this.classSectionService.getAll().subscribe({
      next: options => {
        this.classOptions.set(options);
        this.isLoadingClasses.set(false);
        const firstClass = options[0];
        if (firstClass) {
          this.selectedClassId.set(firstClass.classId);
          this.loadOptionalFeeItems(firstClass.classId);
        }
      },
      error: () => {
        this.isLoadingClasses.set(false);
        this.errorMessage.set('Unable to load classes and sections. Please retry.');
      }
    });
  }

  private loadOptionalFeeItems(classId: string): void {
    if (!classId) {
      this.optionalFeeItems.set([]);
      this.isLoadingFeeItems.set(false);
      return;
    }
    this.isLoadingFeeItems.set(true);
    this.feesService.getFeeItemsByClass(Number(classId)).subscribe({
      next: feeItems => {
        if (this.selectedClassId() !== classId) {
          return;
        }
        this.optionalFeeItems.set(feeItems.filter(item => item.active && !item.mandatory));
        this.isLoadingFeeItems.set(false);
        if (this.selectedSectionName()) {
          this.loadStudents();
        }
      },
      error: () => {
        if (this.selectedClassId() !== classId) {
          return;
        }
        this.optionalFeeItems.set([]);
        this.isLoadingFeeItems.set(false);
        this.errorMessage.set('Unable to load optional fee items for this class.');
      }
    });
  }

  private loadStudents(): void {
    const classId = this.selectedClassId();
    const sectionName = this.selectedSectionName();
    if (!classId || !sectionName || this.isLoadingFeeItems()) {
      this.enrollments.set([]);
      return;
    }
    if (!this.optionalFeeItems().length) {
      this.enrollments.set([]);
      return;
    }

    this.isLoadingStudents.set(true);
    this.peopleService.getStudentsByClassAndSection(Number(classId), sectionName).pipe(
      switchMap(students => students.length
        ? forkJoin(students.map(student =>
          this.feesService.getStudentServiceSubscriptions(student.admissionNumber).pipe(
            map(subscriptions => ({ student, subscriptions }))
          )
        ))
        : of([] as StudentEnrollment[])
      )
    ).subscribe({
      next: enrollments => {
        if (this.selectedClassId() !== classId || this.selectedSectionName() !== sectionName) {
          return;
        }
        this.enrollments.set(enrollments);
        this.isLoadingStudents.set(false);
      },
      error: () => {
        if (this.selectedClassId() !== classId || this.selectedSectionName() !== sectionName) {
          return;
        }
        this.enrollments.set([]);
        this.isLoadingStudents.set(false);
        this.errorMessage.set('Unable to load students or their optional fee assignments.');
      }
    });
  }

  private updateSubscriptions(
    admissionNumber: number,
    feeItemId: number,
    addedSubscription?: StudentServiceSubscription
  ): void {
    this.enrollments.update(enrollments => enrollments.map(enrollment => {
      if (enrollment.student.admissionNumber !== admissionNumber) {
        return enrollment;
      }
      const subscriptions = addedSubscription
        ? [...enrollment.subscriptions, addedSubscription]
        : enrollment.subscriptions.filter(subscription => subscription.feeItemId !== feeItemId);
      return { ...enrollment, subscriptions };
    }));
    this.savingEnrollment.set('');
  }

  private failEnrollment(studentName: string): void {
    this.errorMessage.set(`Unable to update optional fee assignment for ${studentName}.`);
    this.savingEnrollment.set('');
  }

  private clearFeedback(): void {
    this.errorMessage.set('');
    this.statusMessage.set('');
  }
}
