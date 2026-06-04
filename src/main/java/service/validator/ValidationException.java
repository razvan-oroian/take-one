package service.validator;

public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
    public ValidationException(Exception e) { super(e); }
}
