import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { defaultIfEmpty, forkJoin, from, concatMap, finalize, toArray } from 'rxjs';

import { ProfileService } from '../profile/profile.service';
import { FeeItem, FeesService, MonthlyFee, PaymentRecord } from './fees.service';

type FeeTab = 'dues' | 'receipts';

@Component({
  selector: 'app-student-fee',
  imports: [RouterLink],
  templateUrl: './student-fee.component.html',
  styleUrl: './student-fee.component.css'
})
export class StudentFeeComponent {
  private readonly feesService = inject(FeesService);
  private readonly profileService = inject(ProfileService);

  protected readonly profile = signal<{ id: number; name: string; classId: number } | null>(null);
  protected readonly activeTab = signal<FeeTab>('dues');
  protected readonly monthlyFees = signal<MonthlyFee[]>([]);
  protected readonly payments = signal<PaymentRecord[]>([]);
  protected readonly feeItems = signal<FeeItem[]>([]);
  protected readonly selectedMonth = signal('');
  protected readonly receiptMonth = signal('');
  protected readonly isLoading = signal(true);
  protected readonly isPaying = signal(false);
  protected readonly isLoadingReceiptItems = signal(false);
  protected readonly receiptItemsLoaded = signal(false);
  protected readonly paymentMethod = signal('UPI');
  protected readonly message = signal('');
  protected readonly errorMessage = signal('');
  protected readonly receiptError = signal('');

  protected readonly payableFees = computed(() =>
    this.monthlyFees()
      .filter(fee => fee.status !== 'PAID' && fee.status !== 'EXEMPT' && this.outstanding(fee) > 0)
      .sort((left, right) => left.monthYear.localeCompare(right.monthYear))
  );
  protected readonly selectedFees = computed(() => {
    const selectedMonth = this.selectedMonth();
    return selectedMonth
      ? this.payableFees().filter(fee => fee.monthYear <= selectedMonth)
      : [];
  });
  protected readonly selectedAmount = computed(() =>
    this.selectedFees().reduce((sum, fee) => sum + this.outstanding(fee), 0)
  );
  protected readonly outstandingAmount = computed(() =>
    this.payableFees().reduce((sum, fee) => sum + this.outstanding(fee), 0)
  );
  protected readonly overdueAmount = computed(() =>
    this.payableFees()
      .filter(fee => this.isOverdue(fee))
      .reduce((sum, fee) => sum + this.outstanding(fee), 0)
  );
  protected readonly receiptMonths = computed(() =>
    [...new Set(this.payments().filter(payment => payment.status === 'PAID').map(payment => payment.monthYear))]
      .sort((left, right) => right.localeCompare(left))
  );
  protected readonly receiptPayments = computed(() =>
    this.payments()
      .filter(payment => payment.monthYear === this.receiptMonth() && payment.status === 'PAID')
      .sort((left, right) => left.paymentDate.localeCompare(right.paymentDate))
  );
  protected readonly receiptTotal = computed(() =>
    this.receiptPayments().reduce((sum, payment) => sum + payment.amountPaid, 0)
  );

  constructor() {
    this.profileService.getProfile().pipe(defaultIfEmpty(null)).subscribe({
      next: profile => {
        if (!profile || profile.role !== 'Student' || profile.id <= 0) {
          this.isLoading.set(false);
          this.errorMessage.set('Unable to identify the signed-in student. Please sign in again.');
          return;
        }

        this.profile.set({
          id: profile.id,
          name: profile.name,
          classId: Number(profile.className)
        });
        this.loadFees();
      },
      error: () => {
        this.isLoading.set(false);
        this.errorMessage.set('Unable to load your student profile.');
      }
    });
  }

  protected selectTab(tab: FeeTab): void {
    this.activeTab.set(tab);
    this.message.set('');
    if (tab === 'receipts' && !this.receiptMonth() && this.receiptMonths().length) {
      this.selectReceiptMonth(this.receiptMonths()[0]);
    }
  }

  protected selectMonth(month: string): void {
    this.selectedMonth.set(month);
    this.message.set('');
  }

  protected selectPaymentMethod(method: string): void {
    this.paymentMethod.set(method);
  }

  protected selectReceiptMonth(month: string): void {
    this.receiptMonth.set(month);
    this.feeItems.set([]);
    this.receiptItemsLoaded.set(false);
    this.receiptError.set('');
    const classId = this.profile()?.classId;
    if (!classId || !month) {
      return;
    }

    this.isLoadingReceiptItems.set(true);
    this.feesService.getFeeItemsByClass(classId).subscribe({
      next: items => {
        this.feeItems.set(items.filter(item => item.active));
        this.receiptItemsLoaded.set(true);
        this.isLoadingReceiptItems.set(false);
      },
      error: () => {
        this.receiptError.set('Unable to load the fee items for this receipt.');
        this.isLoadingReceiptItems.set(false);
      }
    });
  }

  protected paySelectedFees(): void {
    const studentId = this.profile()?.id;
    const fees = this.selectedFees();
    if (!studentId || !fees.length || this.selectedAmount() <= 0 || this.isPaying()) {
      return;
    }

    this.isPaying.set(true);
    this.message.set('');
    this.errorMessage.set('');
    from(fees).pipe(
      concatMap((fee, index) => this.feesService.recordPayment({
        studentId,
        monthlyFeeId: fee.id,
        monthYear: fee.monthYear,
        transactionId: `FEE-${studentId}-${Date.now()}-${index}`,
        paymentMethod: this.paymentMethod(),
        amountPaid: this.outstanding(fee)
      })),
      toArray(),
      finalize(() => this.isPaying.set(false))
    ).subscribe({
      next: () => {
        this.message.set(`Payment recorded for ${fees.length} month${fees.length === 1 ? '' : 's'}.`);
        this.loadFees();
      },
      error: () => {
        this.errorMessage.set('The payment could not be fully recorded. Your latest fee balance is being refreshed.');
        this.loadFees();
      }
    });
  }

  protected outstanding(fee: MonthlyFee): number {
    return Math.max(0, (fee.totalPayable ?? 0) - (fee.paidAmount ?? 0));
  }

  protected isOverdue(fee: MonthlyFee): boolean {
    return this.dueDate(fee.monthYear) < this.today();
  }

  protected dueDate(monthYear: string): string {
    return `${monthYear}-08`;
  }

  protected formatMonth(monthYear: string): string {
    const [year, month] = monthYear.split('-').map(Number);
    return new Intl.DateTimeFormat('en-IN', { month: 'long', year: 'numeric' }).format(new Date(year, month - 1, 1));
  }

  protected formatDate(date: string): string {
    return new Intl.DateTimeFormat('en-IN', { day: 'numeric', month: 'long', year: 'numeric' })
      .format(new Date(`${date}T00:00:00`));
  }

  protected formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 2
    }).format(amount);
  }

  protected printReceipt(): void {
    if (this.receiptPayments().length && this.receiptItemsLoaded()) {
      window.print();
    }
  }

  private loadFees(): void {
    const studentId = this.profile()?.id;
    if (!studentId) {
      return;
    }

    this.isLoading.set(true);
    forkJoin({
      monthlyFees: this.feesService.getMonthlyFees(studentId),
      payments: this.feesService.getPayments(studentId)
    }).subscribe({
      next: ({ monthlyFees, payments }) => {
        this.monthlyFees.set(monthlyFees);
        this.payments.set(payments);
        if (!this.payableFees().some(fee => fee.monthYear === this.selectedMonth())) {
          this.selectedMonth.set(this.payableFees()[0]?.monthYear ?? '');
        }
        if (this.receiptMonth() && !payments.some(payment =>
          payment.monthYear === this.receiptMonth() && payment.status === 'PAID'
        )) {
          this.receiptMonth.set('');
          this.feeItems.set([]);
          this.receiptItemsLoaded.set(false);
        }
        this.isLoading.set(false);
      },
      error: () => {
        this.errorMessage.set('Unable to load fee balances and payment history.');
        this.isLoading.set(false);
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
