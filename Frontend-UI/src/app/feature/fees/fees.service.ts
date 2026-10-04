import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { paymentServiceApiUrl } from '../../core/config/api.config';

interface ApiResponse<T> {
  status: 'SUCCESS' | 'ERROR';
  code: number;
  message: string;
  data: T;
  timestamp: string;
}

export interface MonthlyFee {
  id: number;
  studentId: number;
  monthYear: string;
  baseAmount: number;
  waiverAmount: number;
  penaltyAmount: number;
  totalPayable: number;
  paidAmount: number;
  status: 'PENDING' | 'PARTIAL' | 'PAID' | 'EXEMPT' | 'OVERDUE' | 'CANCELLED';
}

export interface PaymentRecord {
  id: number;
  studentId: number;
  monthlyFeeId: number;
  monthYear: string;
  transactionId: string;
  paymentMethod: string;
  amountPaid: number;
  paymentDate: string;
  status: MonthlyFee['status'];
}

export interface FeeItem {
  id: number;
  serviceName: string;
  classId: number | null;
  mandatory: boolean;
  defaultAmount: number;
  active: boolean;
}

export interface PaymentReminder {
  id: number;
  monthlyFeeId: number;
  studentId: number;
  reminderType: 'DUE_DATE_COMMING_TWO_DAUS' | 'DUE_DATE' | 'OVERDUE_7DAYS' | 'OVERDUE_14DAYS';
  amount: number;
  dueDate: string;
  sent: boolean;
  sentAt: string | null;
}

export interface GatewayPaymentRequest {
  provider: 'RAZORPAY' | 'PAYU';
  studentId: number;
  monthlyFeeIds: number[];
  customerName: string;
  customerEmail: string;
  customerPhone: string;
}

export interface GatewayPaymentOrder {
  reference: string;
  provider: 'RAZORPAY' | 'PAYU';
  status: 'PENDING' | 'PAID' | 'FAILED';
  amountPaise: number;
  currency: 'INR';
  qrImageUrl: string | null;
  qrPayload: string | null;
  providerReference: string;
  expiresAt: string;
}

export type PaymentReminderRequest = Pick<
  PaymentReminder,
  'monthlyFeeId' | 'studentId' | 'reminderType' | 'amount' | 'dueDate'
>;

@Injectable({ providedIn: 'root' })
export class FeesService {
  private readonly http = inject(HttpClient);

  getMonthlyFees(studentId: number): Observable<MonthlyFee[]> {
    return this.http.get<ApiResponse<MonthlyFee[]>>(
      paymentServiceApiUrl(`/monthly-fees/student/${studentId}`)
    ).pipe(map(response => response.data ?? []));
  }

  getPayments(studentId: number): Observable<PaymentRecord[]> {
    return this.http.get<ApiResponse<PaymentRecord[]>>(
      paymentServiceApiUrl(`/payments/student/${studentId}`)
    ).pipe(map(response => response.data ?? []));
  }

  getFeeItemsByClass(classId: number): Observable<FeeItem[]> {
    return this.http.get<ApiResponse<FeeItem[]>>(
      paymentServiceApiUrl(`/fee-items/class/${classId}`)
    ).pipe(map(response => response.data ?? []));
  }

  createGatewayOrder(request: GatewayPaymentRequest): Observable<GatewayPaymentOrder> {
    return this.http.post<ApiResponse<GatewayPaymentOrder>>(
      paymentServiceApiUrl('/payments/gateway-orders'),
      request
    ).pipe(map(response => response.data));
  }

  refreshGatewayOrder(reference: string): Observable<GatewayPaymentOrder> {
    return this.http.get<ApiResponse<GatewayPaymentOrder>>(
      paymentServiceApiUrl(`/payments/gateway-orders/${encodeURIComponent(reference)}/status`)
    ).pipe(map(response => response.data));
  }

  createReminder(request: PaymentReminderRequest): Observable<PaymentReminder> {
    return this.http.post<ApiResponse<PaymentReminder>>(
      paymentServiceApiUrl('/payment-reminders'),
      request
    ).pipe(map(response => response.data));
  }
}
