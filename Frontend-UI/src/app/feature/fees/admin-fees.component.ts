import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ClassSectionService } from '../class-section/class-section.service';
import { ClassSectionOption, Student } from '../../common/model/models';
import { FormsModule } from '@angular/forms';
import { FeesService, MonthlyFee } from './fees.service';
import { PeopleService } from '../people/people.service';
import { forkJoin, map, of, switchMap } from 'rxjs';

interface PendingFee {
  id: number;
  studentId: number;
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
  private readonly classSectionService = inject(ClassSectionService);
  private readonly peopleService = inject(PeopleService);
  private readonly feesService = inject(FeesService);
  protected activeTab: 'structure' | 'pending' = 'structure';
  protected readonly classOptions = signal<ClassSectionOption[]>([]);
  protected readonly selectedClass = signal('');
  protected readonly isLoadingPending = signal(false);
  protected readonly pendingError = signal('');
  protected readonly reminderCreatedFor = signal<number[]>([]);
  protected statusError = false;
  protected readonly feeTypes = [
    { key: 'tuition', label: 'Tuition fee' },
    { key: 'transport', label: 'Transport fee' },
    { key: 'activities', label: 'Activities fee' },
    { key: 'examination', label: 'Examination fee' },
    { key: 'other', label: 'Other fee' }
  ];
  protected readonly feeAmounts = signal<Record<string, number>>({
    tuition: 5000,
    transport: 1200,
    activities: 500,
    examination: 300,
    other: 0
  });
  protected readonly monthlyTotal = computed(() =>
    Object.values(this.feeAmounts()).reduce((total, amount) => total + amount, 0)
  );
  protected readonly pendingFees = signal<PendingFee[]>([]);
  protected readonly pendingStudentCount = computed(() =>
    new Set(this.pendingFees().map(fee => fee.studentId)).size
  );
  protected statusMessage = '';

  constructor() {
    this.classSectionService.getAll().subscribe(options => {
      this.classOptions.set(options);
      this.selectedClass.set(options[0]?.classId ?? '');
      this.loadPendingFees();
    }, () => {
      this.pendingError.set('Unable to load classes for fee management.');
    });
  }

  protected selectClass(value: string | number): void {
    this.selectedClass.set(String(value ?? ''));
    this.statusMessage = '';
    this.statusError = false;
    this.loadPendingFees();
  }

  protected selectTab(tab: 'structure' | 'pending'): void {
    this.activeTab = tab;
    this.statusMessage = '';
    this.statusError = false;
  }

  protected updateFee(key: string, event: Event): void {
    const amount = Number((event.target as HTMLInputElement).value);
    this.feeAmounts.update(current => ({ ...current, [key]: Number.isFinite(amount) && amount >= 0 ? amount : 0 }));
    this.statusMessage = '';
    this.statusError = false;
  }

  protected saveClassFees(): void {
    this.statusMessage = `${this.selectedClass()} monthly fee of ${this.formatCurrency(this.monthlyTotal())} is ready to be saved.`;
  }

  protected isOverdue(fee: PendingFee): boolean {
    return fee.amount > 0 && fee.dueDate < this.today();
  }

  protected createReminder(fee: PendingFee): void {
    if (!this.isOverdue(fee) || this.reminderCreatedFor().includes(fee.id)) {
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
      monthlyFeeId: fee.id,
      studentId: fee.studentId,
      reminderType,
      amount: fee.amount,
      dueDate: fee.dueDate
    }).subscribe({
      next: () => {
        this.reminderCreatedFor.update(ids => [...ids, fee.id]);
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

  private loadPendingFees(): void {
    const classOption = this.classOptions().find(option => option.classId === this.selectedClass());
    if (!classOption) {
      this.pendingFees.set([]);
      this.isLoadingPending.set(false);
      return;
    }

    this.isLoadingPending.set(true);
    this.pendingError.set('');
    this.reminderCreatedFor.set([]);
    const className = classOption.classId;
    const sections = classOption.sections;
    const rosters$ = sections.length
      ? forkJoin(sections.map(section =>
        this.peopleService.getStudentsByClassAndSection(Number(className), section.sectionName)
      )).pipe(map(rosters => [...new Map(rosters.flat().map(student => [student.id, student])).values()]))
      : of([] as Student[]);

    rosters$.pipe(
      switchMap(roster => {
        const students = roster.filter((student): student is Student & { id: number } => student.id !== null);
        return students.length
        ? forkJoin(students.map(student =>
          this.feesService.getMonthlyFees(student.id).pipe(
            map(fees => fees
              .filter(fee => fee.status !== 'PAID' && fee.status !== 'EXEMPT' && this.outstanding(fee) > 0)
              .map(fee => ({
                id: fee.id,
                studentId: student.id ?? 0,
                studentName: student.name,
                className: `Class ${student.classId}`,
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
