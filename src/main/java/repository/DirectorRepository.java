package repository;

import model.Director;

import java.util.List;

public interface DirectorRepository extends Repository<Integer, Director> {
    Director findDirectorsByMovieId(Integer movieId);
}
