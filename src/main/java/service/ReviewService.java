package service;

import model.Review;
import repository.MovieRepository;
import repository.ReviewRepository;
import repository.UserRepository;
import service.dtos.ReviewDto;
import service.validator.ReviewValidator;

import java.time.LocalDateTime;
import java.util.List;

public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private ReviewValidator reviewValidator;

    public ReviewService(ReviewRepository reviewRepository,
             UserRepository userRepository, MovieRepository movieRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
        this.reviewValidator = new ReviewValidator();
    }

    public Review addReview(Integer rating, String comment,
                LocalDateTime createdAt, Integer userId, Integer movieId) {

        reviewValidator.validate(new ReviewDto(rating, comment, createdAt, userId, movieId));

        var user = userRepository.findOne(userId);
        if (user == null) {
            throw new IllegalArgumentException("can't add review: user does not exist");
        }
        var movie = movieRepository.findOne(movieId);
        if (movie == null) {
            throw new IllegalArgumentException("can't add review: movie does not exist");
        }

        return reviewRepository.add(new Review(rating, comment, createdAt, user, movie));
    }

    public void updateReview(Integer id, Integer rating, String comment,
                LocalDateTime createdAt, Integer userId, Integer movieId) {

        reviewValidator.validate(new ReviewDto(rating, comment, createdAt, userId, movieId));

        var user = userRepository.findOne(userId);
        if (user == null) {
            throw new IllegalArgumentException("can't update review: user does not exist");
        }
        var movie = movieRepository.findOne(movieId);
        if (movie == null) {
            throw new IllegalArgumentException("can't update review: movie does not exist");
        }

        var review = new Review(rating, comment, createdAt, user, movie);
        review.setId(id);
        reviewRepository.update(id, review);
    }

    public Review findByUserIdAndMovieId(Integer userId,  Integer movieId) {
        return reviewRepository.findByUserIdAndMovieId(userId, movieId);
    }

    public void deleteReview(Integer id) {
        reviewRepository.delete(id);
    }

    public Review findOne(Integer id) {
        return reviewRepository.findOne(id);
    }

    public List<Review> findAll() {
        return (List<Review>)reviewRepository.findAll();
    }

    public Long getTotalMoviesWatched(Integer userId) {
        return reviewRepository.countMoviesWatched(userId);
    }

    public Long getTotalHoursWatched(Integer userId) {
        return reviewRepository.sumRuntimeMinutes(userId) / 60; // Convert to hours
    }

    public List<model.Movie> getRecentMovies(Integer userId, int limit) {
        return reviewRepository.findRecentMovies(userId, limit);
    }

    public List<Object[]> getGenreStats(Integer userId) {
        return reviewRepository.getGenreStats(userId);
    }

    public List<Object[]> getReleaseYearStats(Integer userId) {
        return reviewRepository.getReleaseYearStats(userId);
    }
}
