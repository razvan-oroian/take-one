package controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Movie;
import model.User;
import model.WatchlistEntry;
import service.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class WatchlistController {

    @FXML private VBox watchlistContainer;

    private User loggedUser;
    private Parent previousView;

    private WatchlistService watchlistService;
    private MovieService movieService;
    private CastingService castingService;
    private DirectorService directorService;
    private GenreService genreService;
    private ReviewService reviewService;

    public void setLoggedUser(User loggedUser) {
        this.loggedUser = loggedUser;
    }

    public void setPreviousView(Parent previousView) {
        this.previousView = previousView;
    }

    public void setWatchlistService(WatchlistService watchlistService) {
        this.watchlistService = watchlistService;
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

    public void setReviewService(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    private Parent cachedMovieInfoRoot;
    private MovieInfoController cachedMovieInfoController;

    private final Image borderImage = new Image(getClass().getResourceAsStream("/images/movie-border.png"));
    private final double POSTER_WIDTH = 110;
    private final double BORDER_WIDTH = POSTER_WIDTH * (160.0 / 140.0);
    private final double BORDER_HEIGHT = BORDER_WIDTH * (230.0 / 160.0);


    public void loadData() {
        if (loggedUser == null) return;

        CompletableFuture.runAsync(() -> {
            try {
                List<WatchlistEntry> watchlist = watchlistService.getUserWatchlist(loggedUser.getId());

                Platform.runLater(() -> {
                    watchlistContainer.getChildren().clear();
                    for (WatchlistEntry entry : watchlist) {
                        watchlistContainer.getChildren().add(createWatchlistItem(entry));
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private HBox createWatchlistItem(WatchlistEntry entry) {
        Movie movie = entry.getMovie();
        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-border-color: rgba(0,0,0,0.2); -fx-border-width: 0 0 1 0;");
        row.setPadding(new Insets(15, 20, 15, 0));

        StackPane card = new StackPane();
        ImageView poster = new ImageView(new Image(movie.getPosterPath(), true));
        poster.setFitWidth(POSTER_WIDTH);
        poster.setPreserveRatio(true);
        ImageView border = new ImageView(borderImage);
        border.setFitWidth(BORDER_WIDTH);
        border.setFitHeight(BORDER_HEIGHT);
        card.getChildren().addAll(poster, border);

        card.setOnMouseClicked(e -> navigateToMovie(movie, card));
        card.setOnMouseEntered(e -> card.setStyle("-fx-cursor: hand; -fx-scale-x: 1.05; -fx-scale-y: 1.05;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-cursor: default; -fx-scale-x: 1.0; -fx-scale-y: 1.0;"));

        VBox centerContent = new VBox(5);
        centerContent.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(movie.getTitle().toUpperCase());
        titleLabel.setStyle("-fx-font-family: 'Impact'; -fx-font-size: 20px; -fx-text-fill: black;");

        String desc = movie.getOverview();
        if (desc.length() > 220) {
            desc = desc.substring(0, 217) + "...";
        }

        Label descLabel = new Label(desc);
        descLabel.setWrapText(true);
        descLabel.setMinWidth(0);
        descLabel.setStyle("-fx-font-family: 'Helvetica'; -fx-font-size: 14px; -fx-text-fill: black;");

        centerContent.getChildren().addAll(titleLabel, descLabel);
        HBox.setHgrow(centerContent, Priority.ALWAYS);

        VBox rightContent = new VBox(10);
        rightContent.setAlignment(Pos.CENTER_RIGHT);
        rightContent.setMinWidth(130);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM. yyyy", Locale.ENGLISH);
        Label dateLabel = new Label("ADDED ON\n" + entry.getAddedAt().format(formatter).toUpperCase());
        dateLabel.setStyle("-fx-font-family: 'Impact'; -fx-font-size: 16px; -fx-text-fill: black; -fx-text-alignment: right;");

        Button removeBtn = new Button("REMOVE");
        removeBtn.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        removeBtn.setOnAction(e -> {
            watchlistService.removeFromWatchlist(loggedUser.getId(), movie.getId());
            watchlistContainer.getChildren().remove(row);
        });

        rightContent.getChildren().addAll(dateLabel, removeBtn);

        row.getChildren().addAll(card, centerContent, rightContent);
        return row;
    }

    private void navigateToMovie(Movie movie, StackPane card) {
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
    }

    public void handleBack(ActionEvent actionEvent) {
        try {
            if (previousView != null) {
                watchlistContainer.getScene().setRoot(previousView);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Parent cachedProfileRoot;
    private ProfileController cachedProfileController;

    @FXML public void goToProfile(ActionEvent event) {
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

            Parent currentRoot = watchlistContainer.getScene().getRoot();
            cachedProfileController.setPreviousView(currentRoot);

            cachedProfileController.loadData();
            watchlistContainer.getScene().setRoot(cachedProfileRoot);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}