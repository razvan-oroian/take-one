package controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;
import model.Actor;
import model.Director;
import model.Movie;
import model.User;
import service.*;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class PersonInfoController {

    @FXML
    private ImageView profileImage;
    @FXML
    private Label nameLabel;
    @FXML
    private Label birthDateLabel;
    @FXML
    private Label biographyLabel;
    @FXML
    private TilePane movieGrid;

    private User loggedUser;
    private MovieService movieService;
    private CastingService castingService;
    private DirectorService directorService;
    private GenreService genreService;
    private Actor selectedActor;
    private Director selectedDirector;

    private Parent previousView;

    public void setSelectedActor(Actor selectedActor) {
        this.selectedActor = selectedActor;
    }

    public void setSelectedDirector(Director selectedDirector) {
        this.selectedDirector = selectedDirector;
    }

    public void setLoggedUser(User loggedUser) {
        this.loggedUser = loggedUser;
    }

    public void setMovieService(MovieService movieService) {
        this.movieService = movieService;
    }

    public void setCastingService(CastingService castingService) {
        this.castingService = castingService;
    }

    public void setDirectorService(DirectorService directorService) {
        this.directorService = directorService;
    }

    public void setGenreService(GenreService genreService) {
        this.genreService = genreService;
    }

    public void setPreviousView(Parent previousView) {
        this.previousView = previousView;
    }

    private WatchlistService watchlistService;

    public void setWatchlistService(WatchlistService watchlistService) {
        this.watchlistService = watchlistService;
    }

    private ReviewService reviewService;
    public void setReviewService(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    public void startBackgroundPreloading() {
        if (cachedMovieInfoRoot != null) return;

        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(300);

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
    }

    private Image borderImage = new Image(getClass().getResourceAsStream("/images/movie-border.png"));
    private final double POSTER_WIDTH = 140;
    private final double BORDER_WIDTH = POSTER_WIDTH * (160.0 / 140.0);
    private final double BORDER_HEIGHT = BORDER_WIDTH * (230.0 / 160.0);
    private final double GRID_GAP = 10.0;

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

                cachedMovieInfoController.setLoggedUser(loggedUser);
                cachedMovieInfoController.setSelectedMovie(movie);

                Parent currentRoot = card.getScene().getRoot();
                cachedMovieInfoController.setPreviousView(currentRoot);

                var currentScene = card.getScene();
                currentScene.setRoot(cachedMovieInfoRoot);

                cachedMovieInfoController.loadData();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        return card;
    }

    public void loadActorData() {
        if (selectedActor == null) return;

        nameLabel.setText(selectedActor.getName().toUpperCase());
        biographyLabel.setText(selectedActor.getBiography());
        profileImage.setImage(new Image(selectedActor.getProfilePath(), true));

        if (selectedActor.getBirthDate() != null) {
            var formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
            birthDateLabel.setText("BORN: " + selectedActor.getBirthDate().format(formatter).toUpperCase());
        }

        CompletableFuture.runAsync(() -> {
            try {
                var movies = movieService.findByActorId(selectedActor.getId());

                Platform.runLater(() -> {
                    movieGrid.getChildren().clear();

                    for (var movie: movies) {
                        movieGrid.getChildren().add(createMovieCard(movie));
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void loadDirectorData() {
        if (selectedDirector == null) return;

        nameLabel.setText(selectedDirector.getName().toUpperCase());
        biographyLabel.setText(selectedDirector.getBiography());
        profileImage.setImage(new Image(selectedDirector.getProfilePath(), true));

        if (selectedDirector.getBirthDate() != null) {
            var formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
            birthDateLabel.setText("BORN: " + selectedDirector.getBirthDate().format(formatter).toUpperCase());
        }

        CompletableFuture.runAsync(() -> {
            try {
                var movies = movieService.findByDirectorId(selectedDirector.getId());

                Platform.runLater(() -> {
                    movieGrid.getChildren().clear();

                    for (var movie: movies) {
                        movieGrid.getChildren().add(createMovieCard(movie));
                    }

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
                cachedProfileController.setWatchlistService(watchlistService);
            }

            cachedProfileController.setLoggedUser(loggedUser);

            Parent currentRoot = nameLabel.getScene().getRoot();
            cachedProfileController.setPreviousView(currentRoot);

            cachedProfileController.loadData();
            nameLabel.getScene().setRoot(cachedProfileRoot);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    public void handleBack(ActionEvent actionEvent) {
        if (previousView != null) {
            profileImage.getScene().setRoot(previousView);
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

            Parent currentRoot = nameLabel.getScene().getRoot();
            cachedWatchlistController.setPreviousView(currentRoot);

            cachedWatchlistController.loadData();
            nameLabel.getScene().setRoot(cachedWatchlistRoot);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
