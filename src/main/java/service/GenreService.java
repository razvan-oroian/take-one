package service;

import model.Genre;
import repository.GenreRepository;

import java.util.List;

public class GenreService {
    private GenreRepository genreRepository;

    public GenreService(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    public List<Genre> findAllDistinct() {
        return genreRepository.findDistinct();
    }
}
