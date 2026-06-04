package service;

import model.Casting;
import repository.CastingRepository;

import java.util.List;

public class CastingService {
    private CastingRepository castingRepository;

    public CastingService(CastingRepository castingRepository) {
        this.castingRepository = castingRepository;
    }

    public List<Casting> findCastByMovieId(int movieId) {
        return castingRepository.findCastByMovieId(movieId);
    }
}
