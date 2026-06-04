package service.validator;

public interface Validator<T> {
    void validate(T elem);
}
