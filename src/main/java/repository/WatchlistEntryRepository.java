package repository;

import model.WatchlistEntry;

import java.util.List;

public interface WatchlistEntryRepository extends Repository<Integer, WatchlistEntry> {
    List<WatchlistEntry> findByUserId(Integer userId);
    boolean existsByUserIdAndMovieId(Integer userId, Integer movieId);
    void deleteByUserIdAndMovieId(Integer userId, Integer movieId);
}
