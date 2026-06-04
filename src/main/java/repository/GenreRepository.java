package repository;

import model.Genre;

import java.util.List;

public interface GenreRepository extends Repository<Integer, Genre> {
    List<Genre> findDistinct();
}
