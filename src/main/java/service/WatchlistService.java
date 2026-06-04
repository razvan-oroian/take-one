package service;

import model.Movie;
import model.User;
import model.WatchlistEntry;
import repository.MovieRepository;
import repository.UserRepository;
import repository.WatchlistEntryRepository;

import java.time.LocalDateTime;
import java.util.List;

public class WatchlistService {

    private final WatchlistEntryRepository watchlistRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;

    public WatchlistService(WatchlistEntryRepository watchlistRepository,
                            UserRepository userRepository,
                            MovieRepository movieRepository) {
        this.watchlistRepository = watchlistRepository;
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
    }

    public List<WatchlistEntry> getUserWatchlist(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        return watchlistRepository.findByUserId(userId);
    }

    public boolean isInWatchlist(Integer userId, Integer movieId) {
        if (userId == null || movieId == null) {
            return false;
        }
        return watchlistRepository.existsByUserIdAndMovieId(userId, movieId);
    }

    public WatchlistEntry addToWatchlist(Integer userId, Integer movieId) {
        if (userId == null || movieId == null) {
            throw new IllegalArgumentException("User ID and Movie ID cannot be null");
        }

        if (isInWatchlist(userId, movieId)) {
            throw new IllegalStateException("Movie is already in the watchlist");
        }

        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new IllegalArgumentException("User does not exist");
        }

        Movie movie = movieRepository.findOne(movieId);
        if (movie == null) {
            throw new IllegalArgumentException("Movie does not exist");
        }

        WatchlistEntry entry = new WatchlistEntry(LocalDateTime.now(), user, movie);

        return watchlistRepository.add(entry);
    }

    public void removeFromWatchlist(Integer userId, Integer movieId) {
        if (userId == null || movieId == null) {
            throw new IllegalArgumentException("User ID and Movie ID cannot be null");
        }
        watchlistRepository.deleteByUserIdAndMovieId(userId, movieId);
    }
}