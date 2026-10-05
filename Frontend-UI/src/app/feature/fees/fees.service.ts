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
  admissionNumber: number;
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
  admissionNumber: number;
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

export type FeeItemRequest = Omit<FeeItem, 'id'>;

export interface StudentServiceSubscription {
  id: number;
  admissionNumber: number;
  feeItemId: number;
}

export interface PaymentReminder {
  id: number;
  monthlyFeeId: number;
  admissionNumber: number;
  reminderType: 'DUE_DATE_COMMING_TWO_DAUS' | 'DUE_DATE' | 'OVERDUE_7DAYS' | 'OVERDUE_14DAYS';
  amount: number;
  dueDate: string;
  sent: boolean;
  sentAt: string | null;
}

export interface GatewayPaymentRequest {
  provider: 'RAZORPAY' | 'PAYU';
  admissionNumber: number;
  monthlyFeeIds: number[];
  customerName: string;
  customerEmail: string;
  customerPhone: string;
}

export interface GatewayPaymentOrder {
  reference: string;
  provider: 'RAZORPAY' | 'PAYU';
  admissionNumber: number;
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
  'monthlyFeeId' | 'admissionNumber' | 'reminderType' | 'amount' | 'dueDate'
>;

@Injectable({ providedIn: 'root' })
export class FeesService {
  private readonly http = inject(HttpClient);

  getMonthlyFees(admissionNumber: number): Observable<MonthlyFee[]> {
    return this.http.get<ApiResponse<MonthlyFee[]>>(
      paymentServiceApiUrl(`/monthly-fees/student/${admissionNumber}`)
    ).pipe(map(response => response.data ?? []));
  }

  getPayments(admissionNumber: number): Observable<PaymentRecord[]> {
    return this.http.get<ApiResponse<PaymentRecord[]>>(
      paymentServiceApiUrl(`/payments/admission/${admissionNumber}`)
    ).pipe(map(response => response.data ?? []));
  }

  getFeeItemsByClass(classId: number): Observable<FeeItem[]> {
    return this.http.get<ApiResponse<FeeItem[]>>(
      paymentServiceApiUrl(`/fee-items/class/${classId}`)
    ).pipe(map(response => response.data ?? []));
  }

  createFeeItem(feeItem: FeeItemRequest): Observable<FeeItem> {
    return this.http.post<ApiResponse<FeeItem>>(
      paymentServiceApiUrl('/fee-items'),
      feeItem
    ).pipe(map(response => response.data));
  }

  updateFeeItem(id: number, feeItem: FeeItemRequest): Observable<FeeItem> {
    return this.http.put<ApiResponse<FeeItem>>(
      paymentServiceApiUrl(`/fee-items/${id}`),
      feeItem
    ).pipe(map(response => response.data));
  }

  deleteFeeItem(id: number): Observable<void> {
    return this.http.delete<ApiResponse<void>>(
      paymentServiceApiUrl(`/fee-items/${id}`)
    ).pipe(map(response => response.data));
  }

  getStudentServiceSubscriptions(admissionNumber: number): Observable<StudentServiceSubscription[]> {
    return this.http.get<ApiResponse<StudentServiceSubscription[]>>(
      paymentServiceApiUrl(`/student-service-subscriptions/student/${admissionNumber}`)
    ).pipe(map(response => response.data ?? []));
  }

  createStudentServiceSubscription(
    admissionNumber: number,
    feeItemId: number
  ): Observable<StudentServiceSubscription> {
    return this.http.post<ApiResponse<StudentServiceSubscription>>(
      paymentServiceApiUrl('/student-service-subscriptions'),
      { admissionNumber, feeItemId }
    ).pipe(map(response => response.data));
  }

  deleteStudentServiceSubscription(id: number): Observable<void> {
    return this.http.delete<ApiResponse<void>>(
      paymentServiceApiUrl(`/student-service-subscriptions/${id}`)
    ).pipe(map(response => response.data));
  }

  generateMonthlyFees(admissionNumber: number): Observable<MonthlyFee[]> {
    return this.http.post<ApiResponse<MonthlyFee[]>>(
      paymentServiceApiUrl(`/monthly-fees/student/${admissionNumber}/generate`),
      {}
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
