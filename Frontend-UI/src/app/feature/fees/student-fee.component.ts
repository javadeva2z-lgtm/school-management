import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { DestroyRef } from '@angular/core';
import { defaultIfEmpty, forkJoin, interval, startWith, switchMap, takeWhile, catchError, of } from 'rxjs';
import { toDataURL } from 'qrcode';

import { ProfileService } from '../profile/profile.service';
import { FeeItem, FeesService, GatewayPaymentOrder, MonthlyFee, PaymentRecord } from './fees.service';

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
  private readonly destroyRef = inject(DestroyRef);

  protected readonly profile = signal<{
    admissionNumber: number;
    name: string;
    classId: number;
    email: string;
    mobile: string;
  } | null>(null);
  protected readonly activeTab = signal<FeeTab>('dues');
  protected readonly monthlyFees = signal<MonthlyFee[]>([]);
  protected readonly payments = signal<PaymentRecord[]>([]);
  protected readonly feeItems = signal<FeeItem[]>([]);
  protected readonly selectedMonth = signal('');
  protected readonly receiptMonth = signal('');
  protected readonly isLoading = signal(true);
  protected readonly isPaying = signal(false);
  protected readonly isCheckingPayment = signal(false);
  protected readonly paymentProvider = signal<'RAZORPAY' | 'PAYU'>('RAZORPAY');
  protected readonly gatewayOrder = signal<GatewayPaymentOrder | null>(null);
  protected readonly qrDataUrl = signal('');
  protected readonly isLoadingReceiptItems = signal(false);
  protected readonly receiptItemsLoaded = signal(false);
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
        const admissionNumber = Number(profile?.username);
        if (!profile || profile.role !== 'Student'
            || !Number.isSafeInteger(admissionNumber) || admissionNumber <= 0) {
          this.isLoading.set(false);
          this.errorMessage.set('Unable to identify the signed-in student. Please sign in again.');
          return;
        }

        this.profile.set({
          admissionNumber,
          name: profile.name,
          classId: Number(profile.className),
          email: profile.email,
          mobile: profile.mobile
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

  protected selectPaymentProvider(provider: 'RAZORPAY' | 'PAYU'): void {
    this.paymentProvider.set(provider);
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

  protected createQrPayment(): void {
    const profile = this.profile();
    const fees = this.selectedFees();
    if (!profile || !fees.length || this.selectedAmount() <= 0 || this.isPaying()) {
      return;
    }
    if (!profile.email.includes('@') || profile.mobile.replace(/\D/g, '').length < 10) {
      this.errorMessage.set('A valid email address and phone number are required for gateway payment.');
      return;
    }

    this.isPaying.set(true);
    this.message.set('');
    this.errorMessage.set('');
    this.gatewayOrder.set(null);
    this.qrDataUrl.set('');
    this.feesService.createGatewayOrder({
      provider: this.paymentProvider(),
      admissionNumber: profile.admissionNumber,
      monthlyFeeIds: fees.map(fee => fee.id),
      customerName: profile.name,
      customerEmail: profile.email,
      customerPhone: profile.mobile
    }).subscribe({
      next: order => {
        this.gatewayOrder.set(order);
        this.isPaying.set(false);
        if (order.qrImageUrl) {
          this.qrDataUrl.set(order.qrImageUrl);
        } else if (order.qrPayload) {
          toDataURL(order.qrPayload, { errorCorrectionLevel: 'M', margin: 2, width: 256 })
            .then(dataUrl => this.qrDataUrl.set(dataUrl))
            .catch(() => this.errorMessage.set('Payment QR was created, but it could not be rendered.'));
        }
        this.message.set('Scan the QR with your UPI app to complete the payment. We will verify the result automatically.');
        this.watchGatewayOrder(order);
      },
      error: () => {
        this.isPaying.set(false);
        this.errorMessage.set('Unable to create the payment QR. Check that the gateway is configured and enabled for this merchant account.');
      }
    });
  }

  protected checkPaymentNow(): void {
    const order = this.gatewayOrder();
    if (order && order.status === 'PENDING') {
      this.refreshGatewayOrder(order);
    }
  }

  protected amountFromPaise(amountPaise: number): number {
    return amountPaise / 100;
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
    const profile = this.profile();
    if (!profile) {
      return;
    }

    this.isLoading.set(true);
    forkJoin({
      monthlyFees: this.feesService.getMonthlyFees(profile.admissionNumber),
      payments: this.feesService.getPayments(profile.admissionNumber)
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

  private watchGatewayOrder(order: GatewayPaymentOrder): void {
    this.isCheckingPayment.set(true);
    interval(5000).pipe(
      startWith(0),
      switchMap(() => this.feesService.refreshGatewayOrder(order.reference).pipe(
        catchError(() => {
          this.errorMessage.set('Could not check the payment status. Retrying...');
          return of(order);
        })
      )),
      takeWhile(result =>
        result.status === 'PENDING' && Date.parse(result.expiresAt) > Date.now(), true
      ),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: result => this.updateGatewayOrder(result),
      complete: () => this.isCheckingPayment.set(false)
    });
  }

  private refreshGatewayOrder(order: GatewayPaymentOrder): void {
    this.feesService.refreshGatewayOrder(order.reference).subscribe({
      next: result => this.updateGatewayOrder(result),
      error: () => this.errorMessage.set('Unable to check payment status right now.')
    });
  }

  private updateGatewayOrder(order: GatewayPaymentOrder): void {
    this.gatewayOrder.set(order);
    if (order.status === 'PAID') {
      this.message.set('Payment verified and recorded. Your fee balance has been updated.');
      this.errorMessage.set('');
      this.isCheckingPayment.set(false);
      this.loadFees();
    } else if (Date.parse(order.expiresAt) <= Date.now()) {
      this.message.set('This QR has expired. Create a new QR to try again.');
      this.isCheckingPayment.set(false);
    }
  }

  private today(): string {
    const now = new Date();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    return `${now.getFullYear()}-${month}-${day}`;
  }
}
