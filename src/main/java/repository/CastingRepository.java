package repository;

import model.Casting;
import model.Movie;

import java.util.List;

public interface CastingRepository extends Repository<Integer, Casting> {
    List<Casting> findCastByMovieId(Integer movieId);
}
