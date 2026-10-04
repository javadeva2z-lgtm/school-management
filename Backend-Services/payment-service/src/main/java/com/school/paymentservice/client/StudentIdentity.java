package com.school.paymentservice.client;

import java.time.LocalDate;

public record StudentIdentity(Long admissionNumber, Long classId, LocalDate admissionDate) {
}
