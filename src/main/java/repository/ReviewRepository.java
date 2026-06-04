package repository;

import model.Movie;
import model.Review;

import java.util.List;

public interface ReviewRepository extends Repository<Integer, Review> {
    Review findByUserIdAndMovieId(Integer userId, Integer movieId);

    public Long countMoviesWatched(Integer userId);

    public Long sumRuntimeMinutes(Integer userId);

    public List<Movie> findRecentMovies(Integer userId, int limit);

    public List<Object[]> getGenreStats(Integer userId);

    public List<Object[]> getReleaseYearStats(Integer userId);
}