package controller;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Movie;
import model.User;
import repository.hibernate.pagination.Pageable;
import service.*;
import service.dtos.MovieFilter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class SearchController {
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> runtimeFilter;
    @FXML
    private ComboBox<String> periodFilter;
    @FXML
    private ComboBox<String> genreFilter;
    @FXML
    private TilePane movieGrid;
    @FXML
    private ScrollPane scrollPane;

    private WatchlistService watchlistService;

    public void setWatchlistService(WatchlistService watchlistService) {
        this.watchlistService = watchlistService;
    }

    private DirectorService directorService;

    public void setDirectorService(DirectorService directorService) {
        this.directorService = directorService;
    }

    private CastingService castingService;

    public void setCastingService(CastingService castingService) {
        this.castingService = castingService;
    }

    private PauseTransition timer = new  PauseTransition(Duration.millis(2000));

    private MovieService movieService;

    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean hasMoreMovies = true;
    private MovieFilter movieFilter;

    private final double POSTER_WIDTH = 140;
    private final double BORDER_WIDTH = POSTER_WIDTH * (160.0 / 140.0);
    private final double BORDER_HEIGHT = BORDER_WIDTH * (230.0 / 160.0);
    private final double GRID_GAP = 10.0;

    private ReviewService reviewService;

    public void setReviewService(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    public void setMovieService(MovieService movieService) {
        this.movieService = movieService;
    }

    private GenreService genreService;

    public void setGenreService(GenreService genreService) {
        this.genreService = genreService;
    }

    public void initFilters() {
        runtimeFilter.getItems().clear();
        runtimeFilter.getItems().addAll("Any", "Under 90 min", "90 - 120 min", "Over 120 min");
        runtimeFilter.getSelectionModel().selectFirst();

        periodFilter.getItems().clear();
        periodFilter.getItems().addAll("Any", "2020s", "2010s", "2000s", "1990s", "1980s", "1970s", "before 1970");
        periodFilter.getSelectionModel().selectFirst();

        CompletableFuture.runAsync(() -> {
            var genres = genreService.findAllDistinct();
            Platform.runLater(() -> {
                genreFilter.getItems().clear();
                genreFilter.getItems().add("Any");
                for (var genre : genres) {
                    genreFilter.getItems().add(genre.getName());
                }
                genreFilter.getSelectionModel().selectFirst();
                attachListeners();
            });
        });
    }

    private void attachListeners() {
        runtimeFilter.setOnAction(e -> handleSearch());
        periodFilter.setOnAction(e -> handleSearch());
        genreFilter.setOnAction(e -> handleSearch());
        timer.setOnFinished(e -> handleSearch());

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            timer.playFromStart();
        });

        scrollPane.vvalueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.doubleValue() >= 0.9 && !isLoading && hasMoreMovies) {
                loadNextPage();
                System.out.println("I loaded page number " + currentPage + " with " + getPageSize() + " movies");
            }
        });
    }

    private User loggedUser;
    public void setLoggedUser(User loggedUser) {
        this.loggedUser = loggedUser;
    }

    private Image borderImage = new Image(getClass().getResourceAsStream("/images/movie-border.png"));

    private Parent cachedMovieInfoRoot;
    private MovieInfoController cachedMovieInfoController;

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
                    if (reviewService == null)
                        System.out.println("dkasd");
                }

                cachedMovieInfoController.setSelectedMovie(movie);
                cachedMovieInfoController.setLoggedUser(loggedUser);


                Parent currentRoot = card.getScene().getRoot();
                cachedMovieInfoController.setPreviousView(currentRoot);

                var currentScene = card.getScene();

                var currentStage = (Stage)currentScene.getWindow();
                currentStage.setWidth(900);
                currentStage.setHeight(900);
                currentStage.centerOnScreen();

                currentScene.setRoot(cachedMovieInfoRoot);

                cachedMovieInfoController.loadData();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        card.setOnMouseEntered(e -> card.setStyle("-fx-cursor: hand; -fx-scale-x: 1.05; -fx-scale-y: 1.05;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-cursor: default; -fx-scale-x: 1.0; -fx-scale-y: 1.0;"));

        return card;
    }

    public void startBackgroundPreloading() {
        if (cachedMovieInfoRoot != null) return;

        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(500);

                var loader = new FXMLLoader(getClass().getResource("/views/movie-info-view.fxml"));
                Parent root = loader.load();
                MovieInfoController controller = loader.getController();

                javafx.application.Platform.runLater(() -> {
                    cachedMovieInfoRoot = root;
                    cachedMovieInfoController = controller;

                    cachedMovieInfoController.setLoggedUser(loggedUser);
                    cachedMovieInfoController.setCastingService(castingService);
                    cachedMovieInfoController.setDirectorService(directorService);
                    cachedMovieInfoController.setGenreService(genreService);
                    cachedMovieInfoController.setMovieService(movieService);
                    cachedMovieInfoController.setReviewService(reviewService);
                    cachedMovieInfoController.setWatchlistService(watchlistService);


                    cachedMovieInfoController.startBackgroundPreloading();

                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        CompletableFuture.runAsync(() -> {
            try {
                var loader = new FXMLLoader(getClass().getResource("/views/profile-view.fxml"));
                Parent root = loader.load();
                ProfileController controller = loader.getController();

                javafx.application.Platform.runLater(() -> {
                    cachedProfileRoot = root;
                    cachedProfileController = controller;

                    cachedProfileController.setMovieService(movieService);
                    cachedProfileController.setCastingService(castingService);
                    cachedProfileController.setDirectorService(directorService);
                    cachedProfileController.setGenreService(genreService);
                    cachedProfileController.setReviewService(reviewService);
                    cachedProfileController.setWatchlistService(watchlistService);
                });
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


    }

    private int getPageSize() {
        double width = scrollPane.getViewportBounds().getWidth();
        double height = scrollPane.getViewportBounds().getHeight();

        double cardWidth = BORDER_WIDTH + GRID_GAP;
        double cardHeight = BORDER_HEIGHT + GRID_GAP;

        int columns = (int) Math.max(1, Math.floor(width / cardWidth));
        int rows = (int) Math.max(1, Math.floor(height / cardHeight));

        int visibleMovies = columns * rows;

        return Math.max(15,  (int) (visibleMovies * 1.2));
    }

    @FXML
    public void handleSearch() {
        scrollPane.setVvalue(0);
        currentPage = 0;
        hasMoreMovies = true;
        movieGrid.getChildren().clear();

        movieFilter = buildMovieFilter();
        loadNextPage();
    }

    private void loadNextPage() {
        if (isLoading || !hasMoreMovies) {
            return;
        }

        isLoading = true;

        int currentSize = getPageSize();
        CompletableFuture.runAsync(() -> {
            var pageable = new Pageable(currentPage, currentSize);
            var page = movieService.findPageByFilter(movieFilter, pageable);

            Platform.runLater(() -> {
                hasMoreMovies = page.hasNext();
                appendMovies(page.getContent());
                currentPage++;
                isLoading = false;
            });
        });
    }

    private void appendMovies(List<Movie> movies) {
        for (var movie : movies) {
            var card = createMovieCard(movie);
            movieGrid.getChildren().add(card);
        }
    }

    private MovieFilter buildMovieFilter() {
        String title = null;
        if (searchField.getText() == null || !searchField.getText().trim().isEmpty()) {
            title = searchField.getText();
        }

        String genre = null;
        if (!Objects.equals(genreFilter.getValue(), "Any")) {
            genre = genreFilter.getValue();

        }
        Integer minRuntime = null;
        Integer maxRuntime = null;
        String runtime = runtimeFilter.getValue();
        if (runtime != null && !runtime.equals("Any")) {
            switch (runtime) {
                case "Under 90 min": maxRuntime = 90; break;
                case "90 - 120 min": minRuntime = 90; maxRuntime = 120; break;
                case "Over 120 min": minRuntime = 120; break;
            }
        }

        Integer minYear = null;
        Integer maxYear = null;
        String period = periodFilter.getValue();

        if (period != null && !period.equals("Any")) {
            switch (period) {
                case "2020s": minYear = 2020; maxYear = 2029; break;
                case "2010s": minYear = 2010; maxYear = 2019; break;
                case "2000s": minYear = 2000; maxYear = 2009; break;
                case "1990s": minYear = 1990; maxYear = 1999; break;
                case "1980s": minYear = 1980; maxYear = 1989; break;
                case "1970s": minYear = 1970; maxYear = 1979; break;
                case "before 1970": maxYear = 1969; break;
            }
        }

        MovieFilter filter = new MovieFilter();
        filter.setTitle(title);
        filter.setGenre(genre);
        filter.setMinRuntime(minRuntime);
        filter.setMaxRuntime(maxRuntime);
        filter.setMinYear(minYear);
        filter.setMaxYear(maxYear);

        return filter;
    }

    private Parent cachedProfileRoot;
    private ProfileController cachedProfileController;

    @FXML
    public void goToProfile() {
        try {
            if (cachedProfileRoot == null) {
                var loader = new FXMLLoader(getClass().getResource("/views/profile-view.fxml"));
                cachedProfileRoot = loader.load();
                cachedProfileController = loader.getController();

                cachedProfileController.setMovieService(movieService);
                cachedProfileController.setCastingService(castingService);
                cachedProfileController.setDirectorService(directorService);
                cachedProfileController.setGenreService(genreService);
                cachedProfileController.setReviewService(reviewService);
                cachedMovieInfoController.setWatchlistService(watchlistService);
            }

            cachedProfileController.setLoggedUser(loggedUser);

            Parent currentRoot = scrollPane.getScene().getRoot();
            cachedProfileController.setPreviousView(currentRoot);

            cachedProfileController.loadData();
            scrollPane.getScene().setRoot(cachedProfileRoot);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
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

            Parent currentRoot = scrollPane.getScene().getRoot();
            cachedWatchlistController.setPreviousView(currentRoot);

            cachedWatchlistController.loadData();
            scrollPane.getScene().setRoot(cachedWatchlistRoot);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
