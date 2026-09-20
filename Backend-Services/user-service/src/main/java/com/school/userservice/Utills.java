package com.school.userservice;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Utills {
	private final static DateTimeFormatter DD_MM_YYYY = DateTimeFormatter.ofPattern("dd-MM-yyyy");
	private final static DateTimeFormatter DDMMYYYY = DateTimeFormatter.ofPattern("ddMMyyyy");
	
    public static LocalDate getDateFromString(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateString, DD_MM_YYYY);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date format. Expected format: yyyy-MM-dd");
        }
    }

    public static String generatePasswordFromDateOfBirth(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            return "123456";
        }
        return dateOfBirth.format(DDMMYYYY);
    }

    public static String generateRandomPassword(int length) {
        // Implementation for generating random password
        return "defaultPassword"; // Placeholder implementation
    }
}
