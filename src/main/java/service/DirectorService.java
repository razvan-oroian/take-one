package service;

import model.Director;
import repository.DirectorRepository;

public class DirectorService {
    private DirectorRepository directorRepository;

    public DirectorService(DirectorRepository directorRepository) {
        this.directorRepository = directorRepository;
    }

    public Director findDirectorByMovieId(Integer movieId) {
        return directorRepository.findDirectorsByMovieId(movieId);
    }
}
