package service.dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReviewDto {
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private Integer userId;
    private Integer movieId;

    public ReviewDto(Integer rating, String comment, LocalDateTime createdAt, Integer userId, Integer movieId) {
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
        this.userId = userId;
        this.movieId = movieId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getMovieId() {
        return movieId;
    }

    public void setMovieId(Integer movieId) {
        this.movieId = movieId;
    }
}
