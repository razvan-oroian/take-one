package repository;

import model.Movie;
import repository.hibernate.pagination.Page;
import repository.hibernate.pagination.Pageable;
import service.dtos.MovieFilter;

import java.util.List;

public interface MovieRepository extends Repository<Integer, Movie> {
    List<Movie> findByFilter(MovieFilter filter);
    Page<Movie> findPageByFilter(MovieFilter filter, Pageable pageable);
    List<Movie> findMoviesByActorId(Integer actorId);
    List<Movie> findMoviesByDirectorId(Integer directorId);
}
