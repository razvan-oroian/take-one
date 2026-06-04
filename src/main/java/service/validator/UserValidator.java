package service.validator;

import service.dtos.UserDto;

import java.util.StringJoiner;
import java.util.regex.Pattern;

public class UserValidator implements Validator<UserDto> {
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final Pattern  EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    @Override
    public void validate(UserDto elem) {
        StringJoiner errors = new StringJoiner("\n");

        if (elem.getUsername() == null || elem.getUsername().isEmpty()) {
            errors.add("username is null or empty.");
        }
        if (elem.getEmail() == null || elem.getEmail().isEmpty()) {
            errors.add("email is null or empty.");
        }
        if (!EMAIL_PATTERN.matcher(elem.getEmail()).matches()) {
            errors.add("email is not valid.");
        }
        if (elem.getPassword() == null || elem.getPassword().isEmpty()) {
            errors.add("password is null or empty.");
        }
        if (elem.getJoinedDate().isAfter(elem.getJoinedDate())) {
            errors.add("user can't join in the future");
        }

        if (errors.length() > 0) {
            throw new ValidationException(errors.toString());
        }
    }
}
