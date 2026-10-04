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

export interface PaymentRequest {
  studentId: number;
  monthlyFeeId: number;
  monthYear: string;
  transactionId: string;
  paymentMethod: string;
  amountPaid: number;
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

  recordPayment(request: PaymentRequest): Observable<PaymentRecord> {
    return this.http.post<ApiResponse<PaymentRecord>>(
      paymentServiceApiUrl('/payments'),
      request
    ).pipe(map(response => response.data));
  }

  createReminder(request: PaymentReminderRequest): Observable<PaymentReminder> {
    return this.http.post<ApiResponse<PaymentReminder>>(
      paymentServiceApiUrl('/payment-reminders'),
      request
    ).pipe(map(response => response.data));
  }
}
