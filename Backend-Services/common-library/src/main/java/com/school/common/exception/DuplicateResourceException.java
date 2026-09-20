package com.school.common.exception;

public class DuplicateResourceException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String resourceName;
    private String fieldName;
    private Object fieldValue;

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    @Override
    public String getMessage() {
        if (resourceName != null && fieldName != null) {
            return String.format("%s with %s '%s' already exists", resourceName, fieldName, fieldValue);
        }
        return super.getMessage();
    }
}
