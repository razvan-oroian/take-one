package controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import model.Movie;
import model.User;
import service.*;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

public class ProfileController {

    @FXML private ImageView profileImageView;
    @FXML private Label usernameLabel;
    @FXML private Label moviesWatchedLabel;
    @FXML private Label hoursWatchedLabel;
    @FXML private Label favGenreLabel;
    @FXML private HBox recentMoviesBox;
    @FXML private BarChart<String, Number> genreChart;
    @FXML private BarChart<String, Number> periodChart;

    private User loggedUser;
    private Parent previousView;
    private ReviewService reviewService;
    private MovieService movieService;
    private CastingService castingService;
    private DirectorService directorService;
    private GenreService genreService;

    private final Image borderImage = new Image(getClass().getResourceAsStream("/images/movie-border.png"));
    private final double POSTER_WIDTH = 110;

    private final double BORDER_WIDTH = POSTER_WIDTH * (160.0 / 140.0);
    private final double BORDER_HEIGHT = BORDER_WIDTH * (230.0 / 160.0);

    private WatchlistService watchlistService;

    public void setWatchlistService(WatchlistService watchlistService) {
        this.watchlistService = watchlistService;
    }

    private Parent cachedMovieInfoRoot;
    private MovieInfoController cachedMovieInfoController;

    public void setLoggedUser(User loggedUser) { this.loggedUser = loggedUser; }
    public void setPreviousView(Parent previousView) { this.previousView = previousView; }
    public void setReviewService(ReviewService reviewService) { this.reviewService = reviewService; }
    public void setMovieService(MovieService movieService) { this.movieService = movieService; }
    public void setCastingService(CastingService castingService) { this.castingService = castingService; }
    public void setDirectorService(DirectorService directorService) { this.directorService = directorService; }
    public void setGenreService(GenreService genreService) { this.genreService = genreService; }


    public void startBackgroundPreloading() {
        if (cachedMovieInfoRoot != null) return;

        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(300);

                var loader = new FXMLLoader(getClass().getResource("/views/movie-info-view.fxml"));
                Parent root = loader.load();
                MovieInfoController controller = loader.getController();

                Platform.runLater(() -> {
                    cachedMovieInfoRoot = root;
                    cachedMovieInfoController = controller;

                    cachedMovieInfoController.setCastingService(castingService);
                    cachedMovieInfoController.setDirectorService(directorService);
                    cachedMovieInfoController.setGenreService(genreService);
                    cachedMovieInfoController.setMovieService(movieService);
                    cachedMovieInfoController.setReviewService(reviewService);
                    cachedMovieInfoController.setWatchlistService(watchlistService);
                });

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }


    private StackPane createMovieCard(Movie movie) {
        StackPane card = new StackPane();

        ImageView poster = new ImageView(new Image(movie.getPosterPath(), true));
        poster.setFitWidth(POSTER_WIDTH);
        poster.setPreserveRatio(true);

        ImageView border = new ImageView(borderImage);
        border.setFitWidth(BORDER_WIDTH);
        border.setFitHeight(BORDER_HEIGHT);
        border.setPickOnBounds(false);

        card.getChildren().addAll(poster, border);

        card.setOnMouseEntered(e -> card.setStyle("-fx-cursor: hand; -fx-scale-x: 1.05; -fx-scale-y: 1.05;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-cursor: default; -fx-scale-x: 1.0; -fx-scale-y: 1.0;"));

        card.setOnMouseClicked(e -> {
            try {
                if (cachedMovieInfoRoot == null) {
                    var loader = new FXMLLoader(getClass().getResource("/views/movie-info-view.fxml"));
                    cachedMovieInfoRoot = loader.load();

                    cachedMovieInfoController = loader.getController();
                    cachedMovieInfoController.setCastingService(castingService);
                    cachedMovieInfoController.setDirectorService(directorService);
                    cachedMovieInfoController.setGenreService(genreService);
                    cachedMovieInfoController.setMovieService(movieService);
                    cachedMovieInfoController.setReviewService(reviewService);
                    cachedMovieInfoController.setWatchlistService(watchlistService);
                }

                cachedMovieInfoController.setSelectedMovie(movie);
                cachedMovieInfoController.setLoggedUser(loggedUser);

                Parent currentRoot = card.getScene().getRoot();
                cachedMovieInfoController.setPreviousView(currentRoot);

                var currentScene = card.getScene();
                var currentStage = (Stage) currentScene.getWindow();
                currentStage.setWidth(900);
                currentStage.setHeight(900);
                currentStage.centerOnScreen();

                currentScene.setRoot(cachedMovieInfoRoot);

                cachedMovieInfoController.loadData();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        CompletableFuture.runAsync(() -> {
            try {
                var loader = new FXMLLoader(getClass().getResource("/views/watchlist-view.fxml"));
                Parent root = loader.load();
                WatchlistController controller = loader.getController();

                javafx.application.Platform.runLater(() -> {
                    cachedWatchlistRoot = root;
                    cachedWatchlistController = controller;

                    cachedWatchlistController.setMovieService(movieService);
                    cachedWatchlistController.setCastingService(castingService);
                    cachedWatchlistController.setDirectorService(directorService);
                    cachedWatchlistController.setGenreService(genreService);
                    cachedWatchlistController.setReviewService(reviewService);
                    cachedWatchlistController.setWatchlistService(watchlistService);
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        return card;
    }


    public void loadData() {
        if (loggedUser == null) return;

        usernameLabel.setText(loggedUser.getUsername().toUpperCase());

        CompletableFuture.runAsync(() -> {
            try {
                Long totalMovies = reviewService.getTotalMoviesWatched(loggedUser.getId());
                Long totalHours = reviewService.getTotalHoursWatched(loggedUser.getId());
                List<Movie> recentMovies = reviewService.getRecentMovies(loggedUser.getId(), 4);
                List<Object[]> genreStats = reviewService.getGenreStats(loggedUser.getId());
                List<Object[]> yearStats = reviewService.getReleaseYearStats(loggedUser.getId());

                Platform.runLater(() -> {
                    moviesWatchedLabel.setText(totalMovies + " MOVIES WATCHED");
                    hoursWatchedLabel.setText(totalHours + " HOURS WATCHED");

                    if (!genreStats.isEmpty()) {
                        String topGenre = (String) genreStats.get(0)[0];
                        favGenreLabel.setText("FAVORITE GENRE: " + topGenre.toUpperCase());
                    } else {
                        favGenreLabel.setText("FAVORITE GENRE: N/A");
                    }

                    recentMoviesBox.getChildren().clear();
                    for (Movie m : recentMovies) {
                        recentMoviesBox.getChildren().add(createMovieCard(m));
                    }

                    populateGenreChart(genreStats);
                    populatePeriodChart(yearStats);

                    startBackgroundPreloading();
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void populateGenreChart(List<Object[]> genreStats) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        int limit = Math.min(5, genreStats.size());
        for (int i = 0; i < limit; i++) {
            String genreName = (String) genreStats.get(i)[0];
            Long count = (Long) genreStats.get(i)[1];
            series.getData().add(new XYChart.Data<>(genreName, count));
        }

        genreChart.getData().clear();
        genreChart.getData().add(series);
    }

    private void populatePeriodChart(List<Object[]> yearStats) {
        Map<String, Long> decadeBuckets = new TreeMap<>();

        for (Object[] row : yearStats) {
            Integer year = (Integer) row[0];
            Long count = (Long) row[1];

            if (year != null) {
                String decade = (year / 10 * 10) + "s";
                decadeBuckets.put(decade, decadeBuckets.getOrDefault(decade, 0L) + count);
            }
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Map.Entry<String, Long> entry : decadeBuckets.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        periodChart.getData().clear();
        periodChart.getData().add(series);
    }


    @FXML
    public void handleBack(ActionEvent actionEvent) {
        if (previousView != null) {
            usernameLabel.getScene().setRoot(previousView);
        }
    }

    @FXML
    public void goHome(MouseEvent event) {
    }

    private Parent cachedWatchlistRoot;
    private WatchlistController cachedWatchlistController;

    @FXML
    public void goToWatchlist() {
        try {
            if (cachedWatchlistRoot == null) {
                var loader = new FXMLLoader(getClass().getResource("/views/watchlist-view.fxml"));
                cachedWatchlistRoot = loader.load();
                cachedWatchlistController = loader.getController();

                cachedWatchlistController.setMovieService(movieService);
                cachedWatchlistController.setCastingService(castingService);
                cachedWatchlistController.setDirectorService(directorService);
                cachedWatchlistController.setGenreService(genreService);
                cachedWatchlistController.setReviewService(reviewService);
                cachedWatchlistController.setWatchlistService(watchlistService);
            }

            cachedWatchlistController.setLoggedUser(loggedUser);

            Parent currentRoot = favGenreLabel.getScene().getRoot();
            cachedWatchlistController.setPreviousView(currentRoot);

            cachedWatchlistController.loadData();
            favGenreLabel.getScene().setRoot(cachedWatchlistRoot);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}