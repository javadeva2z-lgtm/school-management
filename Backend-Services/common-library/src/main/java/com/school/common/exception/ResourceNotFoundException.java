package com.school.common.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResourceNotFoundException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String resourceName;
    private String fieldName;
    private Object fieldValue;

    public ResourceNotFoundException(String message) {
        super(message);
    }

    @Override
    public String getMessage() {
        if (resourceName != null && fieldName != null) {
            return String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue);
        }
        return super.getMessage();
    }
}
