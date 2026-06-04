package service;

import model.Movie;
import repository.MovieRepository;
import repository.hibernate.pagination.Page;
import repository.hibernate.pagination.Pageable;
import service.dtos.MovieFilter;

import java.util.List;

public class MovieService {
    private MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public List<Movie> findAll() {
        return (List<Movie>)movieRepository.findAll();
    }

    public List<Movie> findByFilter(MovieFilter filter) {
        return movieRepository.findByFilter(filter);
    }

    public Page<Movie> findPageByFilter(MovieFilter filter, Pageable pageable) {
        return  movieRepository.findPageByFilter(filter, pageable);
    }

    public List<Movie> findByActorId(Integer actorId) {
        return movieRepository.findMoviesByActorId(actorId);
    }

    public List<Movie> findByDirectorId(Integer directorId) {
        return movieRepository.findMoviesByDirectorId(directorId);
    }
}
