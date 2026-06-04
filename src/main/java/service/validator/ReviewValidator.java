package service.validator;

import service.dtos.ReviewDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.StringJoiner;

public class ReviewValidator implements Validator<ReviewDto> {
    @Override
    public void validate(ReviewDto elem) {
        StringJoiner errors = new StringJoiner("\n");

        if (elem.getRating() == null || elem.getRating() < 1 || elem.getRating() > 10) {
            errors.add("rating should be between 1 and 10");
        }
        if (elem.getComment() == null || elem.getComment().length() > 1000) {
            errors.add("comment cannot exceed 1000 characters");
        }
        if (elem.getCreatedAt().isAfter(LocalDateTime.now())) {
            errors.add("review can't be created in the future");
        }
        if (elem.getUserId() == null) {
            errors.add("no user binded to this review");
        }
        if (elem.getMovieId() == null) {
            errors.add("no movie binded to this review");
        }

        if  (errors.length() > 0) {
            throw new ValidationException(errors.toString());
        }
    }
}
