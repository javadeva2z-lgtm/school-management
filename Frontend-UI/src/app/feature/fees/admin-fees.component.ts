import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ClassSectionService } from '../class-section/class-section.service';
import { classDisplayName, ClassSectionOption, Student } from '../../common/model/models';
import { FormsModule } from '@angular/forms';
import { FeeItemRequest, FeesService, MonthlyFee } from './fees.service';
import { PeopleService } from '../people/people.service';
import { forkJoin, map, of, switchMap } from 'rxjs';

interface PendingFee {
  monthlyFeeId: number;
  admissionNumber: number;
  studentName: string;
  className: string;
  section: string;
  monthYear: string;
  dueDate: string;
  amount: number;
  parentMobile: string;
  status: MonthlyFee['status'];
}

@Component({
  selector: 'app-admin-fees',
  imports: [RouterLink, FormsModule],
  templateUrl: './admin-fees.component.html',
  styleUrl: './admin-fees.component.css'
})
export class AdminFeesComponent {
  protected readonly classDisplayName = classDisplayName;
  private readonly classSectionService = inject(ClassSectionService);
  private readonly peopleService = inject(PeopleService);
  private readonly feesService = inject(FeesService);
  protected activeTab: 'structure' | 'pending' = 'structure';
  protected readonly classOptions = signal<ClassSectionOption[]>([]);
  protected readonly selectedClass = signal('');
  protected readonly isLoadingStructure = signal(false);
  protected readonly isSavingStructure = signal(false);
  protected readonly structureError = signal('');
  protected readonly isLoadingPending = signal(false);
  protected readonly pendingError = signal('');
  protected readonly pendingWarning = signal('');
  protected readonly reminderCreatedForMonthlyFeeIds = signal<number[]>([]);
  protected statusError = false;
  protected readonly feeTypes = [
    { key: 'tuition', serviceName: 'TUITION', label: 'Tuition fee' },
    { key: 'transport', serviceName: 'TRANSPORT', label: 'Transport fee' },
    { key: 'activities', serviceName: 'ACTIVITIES', label: 'Activities fee' },
    { key: 'examination', serviceName: 'EXAMINATION', label: 'Examination fee' },
    { key: 'other', serviceName: 'OTHER', label: 'Other fee' }
  ];
  protected readonly feeAmounts = signal<Record<string, number>>({});
  protected readonly monthlyTotal = computed(() =>
    Object.values(this.feeAmounts()).reduce((total, amount) => total + amount, 0)
  );
  protected readonly pendingFees = signal<PendingFee[]>([]);
  protected readonly pendingStudentCount = computed(() =>
    new Set(this.pendingFees().map(fee => fee.admissionNumber)).size
  );
  protected statusMessage = '';

  constructor() {
    this.classSectionService.getAll().subscribe(options => {
      this.classOptions.set(options);
      this.selectedClass.set(options[0]?.classId ?? '');
      this.loadFeeStructure();
    }, () => {
      this.pendingError.set('Unable to load classes for fee management.');
      this.structureError.set('Unable to load classes for fee management.');
    });
  }

  protected selectClass(value: string | number): void {
    this.selectedClass.set(String(value ?? ''));
    this.statusMessage = '';
    this.statusError = false;
    this.loadFeeStructure();
    if (this.activeTab === 'pending') {
      this.loadPendingFees();
    }
  }

  protected selectTab(tab: 'structure' | 'pending'): void {
    this.activeTab = tab;
    this.statusMessage = '';
    this.statusError = false;
    if (tab === 'pending') {
      this.loadPendingFees();
    }
  }

  protected updateFee(key: string, event: Event): void {
    const amount = Number((event.target as HTMLInputElement).value);
    this.feeAmounts.update(current => ({ ...current, [key]: Number.isFinite(amount) && amount >= 0 ? amount : 0 }));
    this.statusMessage = '';
    this.statusError = false;
  }

  protected reloadFeeStructure(): void {
    this.loadFeeStructure();
  }

  protected saveClassFees(): void {
    const selectedClass = this.selectedClass();
    if (!selectedClass || this.isSavingStructure()) {
      return;
    }
    const classId = Number(selectedClass);
    this.isSavingStructure.set(true);
    this.structureError.set('');
    this.statusMessage = '';
    this.statusError = false;

    this.feesService.getFeeItemsByClass(classId).pipe(
      switchMap(existingItems => forkJoin(this.feeTypes.map(feeType => {
        const existing = existingItems.find(item =>
          item.serviceName.trim().toUpperCase() === feeType.serviceName
        );
        const request: FeeItemRequest = {
          serviceName: feeType.serviceName,
          classId,
          mandatory: true,
          defaultAmount: this.feeAmounts()[feeType.key] ?? 0,
          active: true
        };
        return existing
          ? this.feesService.updateFeeItem(existing.id, request)
          : this.feesService.createFeeItem(request);
      })))
    ).subscribe({
      next: () => {
        if (this.selectedClass() === selectedClass) {
          this.statusMessage = `Monthly fee structure saved for Class ${classDisplayName(selectedClass)}.`;
          this.loadFeeStructure();
        }
        this.isSavingStructure.set(false);
      },
      error: () => {
        this.structureError.set('Unable to save the monthly fee structure. Please try again.');
        this.isSavingStructure.set(false);
      }
    });
  }

  protected isOverdue(fee: PendingFee): boolean {
    return fee.amount > 0 && fee.dueDate < this.today();
  }

  protected createReminder(fee: PendingFee): void {
    if (!this.isOverdue(fee) || this.reminderCreatedForMonthlyFeeIds().includes(fee.monthlyFeeId)) {
      return;
    }

    const overdueDays = Math.floor((Date.parse(`${this.today()}T00:00:00`) -
      Date.parse(`${fee.dueDate}T00:00:00`)) / 86_400_000);
    const reminderType = overdueDays >= 14
      ? 'OVERDUE_14DAYS'
      : overdueDays >= 7
        ? 'OVERDUE_7DAYS'
        : 'DUE_DATE';
    this.statusMessage = '';
    this.statusError = false;
    this.feesService.createReminder({
      monthlyFeeId: fee.monthlyFeeId,
      admissionNumber: fee.admissionNumber,
      reminderType,
      amount: fee.amount,
      dueDate: fee.dueDate
    }).subscribe({
      next: () => {
        this.reminderCreatedForMonthlyFeeIds.update(ids => [...ids, fee.monthlyFeeId]);
        this.statusMessage = `Reminder created for ${fee.studentName} (${this.formatMonth(fee.monthYear)}).`;
      },
      error: () => {
        this.statusError = true;
        this.statusMessage = 'Unable to create the payment reminder. Please try again.';
      }
    });
  }

  protected formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 2 }).format(amount);
  }

  protected formatMonth(monthYear: string): string {
    const [year, month] = monthYear.split('-').map(Number);
    return new Intl.DateTimeFormat('en-IN', { month: 'long', year: 'numeric' })
      .format(new Date(year, month - 1, 1));
  }

  protected outstanding(fee: MonthlyFee): number {
    return Math.max(0, (fee.totalPayable ?? 0) - (fee.paidAmount ?? 0));
  }

  private loadFeeStructure(): void {
    const selectedClass = this.selectedClass();
    if (!selectedClass) {
      this.feeAmounts.set({});
      this.isLoadingStructure.set(false);
      return;
    }
    this.isLoadingStructure.set(true);
    this.structureError.set('');
    this.feeAmounts.set({});
    this.feesService.getFeeItemsByClass(Number(selectedClass)).subscribe({
      next: items => {
        if (this.selectedClass() !== selectedClass) {
          return;
        }
        this.feeAmounts.set(Object.fromEntries(this.feeTypes.map(feeType => {
          const item = items.find(candidate =>
            candidate.serviceName.trim().toUpperCase() === feeType.serviceName
          );
          return [feeType.key, item?.defaultAmount ?? 0];
        })));
        this.isLoadingStructure.set(false);
      },
      error: () => {
        if (this.selectedClass() !== selectedClass) {
          return;
        }
        this.structureError.set('Unable to load the monthly fee structure for this class.');
        this.isLoadingStructure.set(false);
      }
    });
  }

  private loadPendingFees(): void {
    const classOption = this.classOptions().find(option => option.classId === this.selectedClass());
    if (!classOption) {
      this.pendingFees.set([]);
      this.isLoadingPending.set(false);
      return;
    }

    this.isLoadingPending.set(true);
    this.pendingError.set('');
    this.pendingWarning.set('');
    this.reminderCreatedForMonthlyFeeIds.set([]);
    const className = classOption.classId;
    const sections = classOption.sections;
    const rosters$ = sections.length
      ? forkJoin(sections.map(section =>
        this.peopleService.getStudentsByClassAndSection(Number(className), section.sectionName)
      )).pipe(map(rosters =>
        [...new Map(rosters.flat().map(student => [student.admissionNumber, student])).values()]
      ))
      : of([] as Student[]);

    rosters$.pipe(
      switchMap(roster => {
        const students = roster.filter((student): student is Student =>
          Number.isSafeInteger(student.admissionNumber) && student.admissionNumber > 0
        );
        const missingAdmissionDateCount = students.filter(student => !student.admissionDate).length;
        this.pendingWarning.set(missingAdmissionDateCount
          ? `${missingAdmissionDateCount} student(s) need an admission date before missing monthly fees can be generated.`
          : '');
        return students.length
        ? forkJoin(students.map(student =>
          (student.admissionDate && this.activeTab === 'pending'
            ? this.feesService.generateMonthlyFees(student.admissionNumber)
            : this.feesService.getMonthlyFees(student.admissionNumber)).pipe(
            map(fees => fees
              .filter(fee => fee.status !== 'PAID' && fee.status !== 'EXEMPT' && this.outstanding(fee) > 0)
              .map(fee => ({
                monthlyFeeId: fee.id,
                admissionNumber: student.admissionNumber,
                studentName: student.name,
                className: `Class ${classDisplayName(student.classId)}`,
                section: student.sectionName,
                monthYear: fee.monthYear,
                dueDate: `${fee.monthYear}-08`,
                amount: this.outstanding(fee),
                parentMobile: student.parentPhone,
                status: fee.status
              } satisfies PendingFee))
            )
          )
        )).pipe(map(feesByStudent => feesByStudent.flat()))
        : of([] as PendingFee[])
      })
    ).subscribe({
      next: fees => {
        if (this.selectedClass() !== className) {
          return;
        }
        this.pendingFees.set(fees.sort((left, right) =>
          left.studentName.localeCompare(right.studentName) || left.monthYear.localeCompare(right.monthYear)
        ));
        this.isLoadingPending.set(false);
      },
      error: () => {
        if (this.selectedClass() !== className) {
          return;
        }
        this.pendingError.set('Unable to load pending fees for this class.');
        this.isLoadingPending.set(false);
      }
    });
  }

  private today(): string {
    const now = new Date();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    return `${now.getFullYear()}-${month}-${day}`;
  }
}
