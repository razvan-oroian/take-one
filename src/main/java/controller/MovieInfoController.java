package controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import model.*;
import service.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class MovieInfoController {
    @FXML
    private Button watchlistButton;
    @FXML
    private StackPane posterContainer;
    @FXML
    private ImageView moviePosterImage;
    @FXML
    private ImageView movieBorderImage;
    @FXML
    private Label movieTitleLabel;
    @FXML
    private Label releaseDateLabel;
    @FXML
    private Label runtimeLabel;
    @FXML
    private Label overviewTextLabel;
    @FXML
    private ImageView directorImage;
    @FXML
    private Label directorNameLabel;
    @FXML
    private Label actor1Label;
    @FXML
    private Label actor2Label;
    @FXML
    private Label actor3Label;
    @FXML
    private Label role1Label;
    @FXML
    private Label role2Label;
    @FXML
    private Label role3Label;
    @FXML
    private ImageView actor1Image;
    @FXML
    private ImageView actor2Image;
    @FXML
    private ImageView actor3Image;

    private ReviewService reviewService;

    public void setReviewService(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @FXML
    private HBox starRatingBox;
    private int currentRating = 0;
    private Review existingUserReview = null;
    @FXML
    private TextArea reviewTextArea;
    @FXML
    private Button saveReviewButton;
    @FXML
    private Label reviewStatusLabel;

    private GenreService genreService;

    public void setGenreService(GenreService genreService) {
        this.genreService = genreService;
    }

    private WatchlistService watchlistService;

    public void setWatchlistService(WatchlistService watchlistService) {
        this.watchlistService = watchlistService;
    }

    private CastingService castingService;

    public void setCastingService(CastingService castingService) {
        this.castingService = castingService;
    }

    private Movie selectedMovie;
    private User loggedUser;
    private MovieService movieService;
    private ActorService actorService;
    private DirectorService directorService;

    public void setSelectedMovie(Movie selectedMovie) {
        this.selectedMovie = selectedMovie;
    }

    public void setLoggedUser(User loggedUser) {
        this.loggedUser = loggedUser;
    }

    public void setMovieService(MovieService movieService) {
        this.movieService = movieService;
    }

    public void setActorService(ActorService actorService) {
        this.actorService = actorService;
    }

    public void setDirectorService(DirectorService directorService) {
        this.directorService = directorService;
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

            Parent currentRoot = posterContainer.getScene().getRoot();
            cachedProfileController.setPreviousView(currentRoot);

            cachedProfileController.loadData();
            posterContainer.getScene().setRoot(cachedProfileRoot);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Parent previousView;

    public void setPreviousView(Parent previousView) {
        this.previousView = previousView;
    }

    private Parent cachedPersonInfoRoot;
    private PersonInfoController cachedPersonInfoController;

    private void initializeStarRating() {
        starRatingBox.getChildren().clear();

        for (int i = 1; i <= 10; i++) {
            Label star = new Label("★");
            star.getStyleClass().add("star");

            final int ratingValue = i;

            star.setOnMouseEntered(e -> updateStarUI(ratingValue));

            star.setOnMouseClicked(e -> {
                currentRating = ratingValue;
                updateStarUI(currentRating);
            });

            starRatingBox.getChildren().add(star);
        }

        starRatingBox.setOnMouseExited(e -> updateStarUI(currentRating));
    }


    private void updateStarUI(int ratingValue) {
        for (int i = 0; i < starRatingBox.getChildren().size(); i++) {
            Label star = (Label) starRatingBox.getChildren().get(i);

            if (i < ratingValue) {
                if (!star.getStyleClass().contains("star-filled")) {
                    star.getStyleClass().add("star-filled");
                }
            } else {
                star.getStyleClass().remove("star-filled");
            }
        }
    }


    public void startBackgroundPreloading() {
        if (cachedPersonInfoRoot != null) return;

        CompletableFuture.runAsync(() -> {
           try {
               Thread.sleep(300);

               var loader = new FXMLLoader(getClass().getResource("/views/person-info-view.fxml"));
               Parent root = loader.load();
               PersonInfoController personInfoController = loader.getController();

               Platform.runLater(() -> {
                   cachedPersonInfoRoot = root;
                   cachedPersonInfoController = personInfoController;

                   cachedPersonInfoController.setLoggedUser(loggedUser);
                   cachedPersonInfoController.setMovieService(movieService);
                   cachedPersonInfoController.setCastingService(castingService);
                   cachedPersonInfoController.setDirectorService(directorService);
                   cachedPersonInfoController.setGenreService(genreService);
                   cachedPersonInfoController.setReviewService(reviewService);
                   cachedPersonInfoController.setWatchlistService(watchlistService);

                   cachedPersonInfoController.startBackgroundPreloading();
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

    private void setupActorInteraction(ImageView imageView, Actor actor) {
        imageView.setOnMouseEntered(e -> imageView.setStyle("-fx-cursor: hand; -fx-scale-x: 1.05; -fx-scale-y: 1.05;"));
        imageView.setOnMouseExited(e -> imageView.setStyle("-fx-cursor: default; -fx-scale-x: 1.0; -fx-scale-y: 1.0;"));

        imageView.setOnMouseClicked(e -> {
            try {
                if (cachedPersonInfoRoot == null) {
                    var loader = new FXMLLoader(getClass().getResource("/views/person-info-view.fxml"));
                    cachedPersonInfoRoot = loader.load();
                    cachedPersonInfoController = loader.getController();

                    cachedPersonInfoController.setMovieService(movieService);
                    cachedPersonInfoController.setCastingService(castingService);
                    cachedPersonInfoController.setDirectorService(directorService);
                    cachedPersonInfoController.setGenreService(genreService);
                    cachedPersonInfoController.setReviewService(reviewService);
                }

                cachedPersonInfoController.setLoggedUser(loggedUser);
                cachedPersonInfoController.setSelectedActor(actor);

                Parent currentRoot = imageView.getScene().getRoot();
                cachedPersonInfoController.setPreviousView(currentRoot);

                cachedPersonInfoController.loadActorData();
                imageView.getScene().setRoot(cachedPersonInfoRoot);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void setupDirectorInteraction(ImageView imageView, Director director) {
        imageView.setOnMouseEntered(e -> imageView.setStyle("-fx-cursor: hand; -fx-scale-x: 1.05; -fx-scale-y: 1.05;"));
        imageView.setOnMouseExited(e -> imageView.setStyle("-fx-cursor: default; -fx-scale-x: 1.0; -fx-scale-y: 1.0;"));

        imageView.setOnMouseClicked(e -> {
            try {
                if (cachedPersonInfoRoot == null) {
                    var loader = new FXMLLoader(getClass().getResource("/views/person-info-view.fxml"));
                    cachedPersonInfoRoot = loader.load();
                    cachedPersonInfoController = loader.getController();

                    cachedPersonInfoController.setMovieService(movieService);
                    cachedPersonInfoController.setCastingService(castingService);
                    cachedPersonInfoController.setDirectorService(directorService);
                    cachedPersonInfoController.setGenreService(genreService);
                    cachedPersonInfoController.setWatchlistService(watchlistService);
                }

                cachedPersonInfoController.setLoggedUser(loggedUser);
                cachedPersonInfoController.setSelectedDirector(director);

                Parent currentRoot = imageView.getScene().getRoot();
                cachedPersonInfoController.setPreviousView(currentRoot);

                cachedPersonInfoController.loadDirectorData();
                imageView.getScene().setRoot(cachedPersonInfoRoot);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void loadData() {
        currentRating = 0;
        initializeStarRating();
        reviewTextArea.clear();
        saveReviewButton.setText("SAVE");
        reviewStatusLabel.setVisible(false);
        existingUserReview = null;


        var formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
        releaseDateLabel.setText("RELEASE DATE: " + selectedMovie.getReleaseDate().format(formatter).toUpperCase());
        runtimeLabel.setText("RUNTIME: " + selectedMovie.getRuntime() + " MIN");
        movieTitleLabel.setText(selectedMovie.getTitle());
        overviewTextLabel.setText(selectedMovie.getOverview());
        moviePosterImage.setImage(new Image(selectedMovie.getPosterPath(), true));

        actor1Label.setText("Loading...");
        actor2Label.setText("Loading...");
        actor3Label.setText("Loading...");
        directorNameLabel.setText("Loading...");

        CompletableFuture.runAsync(() -> {
            try {
                var cast = castingService.findCastByMovieId(selectedMovie.getId());
                var director = directorService.findDirectorByMovieId(selectedMovie.getId());

                var review = reviewService.findByUserIdAndMovieId(loggedUser.getId(), selectedMovie.getId());

                boolean inWatchlist = watchlistService.isInWatchlist(loggedUser.getId(), selectedMovie.getId());

                javafx.application.Platform.runLater(() -> {
                    if (!cast.isEmpty() && cast.size() >= 3) {
                        actor1Label.setText(cast.get(0).getActor().getName());
                        role1Label.setText(cast.get(0).getRole());
                        actor1Image.setImage(new Image(cast.get(0).getActor().getProfilePath(), true));
                        setupActorInteraction(actor1Image, cast.get(0).getActor());

                        actor2Label.setText(cast.get(1).getActor().getName());
                        role2Label.setText(cast.get(1).getRole());
                        actor2Image.setImage(new Image(cast.get(1).getActor().getProfilePath(), true));
                        setupActorInteraction(actor2Image, cast.get(1).getActor());

                        actor3Label.setText(cast.get(2).getActor().getName());
                        role3Label.setText(cast.get(2).getRole());
                        actor3Image.setImage(new Image(cast.get(2).getActor().getProfilePath(), true));
                        setupActorInteraction(actor3Image, cast.get(2).getActor());

                    }

                    if (director != null) {
                        directorNameLabel.setText(director.getName());
                        directorImage.setImage(new Image(director.getProfilePath(), true));
                        setupDirectorInteraction(directorImage, director);
                    }

                    if (review != null) {
                        existingUserReview = review;
                        currentRating = review.getRating();
                        updateStarUI(currentRating);
                        reviewTextArea.setText(review.getComment());
                        saveReviewButton.setText("UPDATE");
                    }

                    if (inWatchlist) {
                        watchlistButton.setText("-WATCHLIST");
                        watchlistButton.setStyle("-fx-background-color: #555555; -fx-text-fill: white;");
                    } else {
                        watchlistButton.setText("+ WATCHLIST");
                        watchlistButton.setStyle("");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
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

    @FXML
    public void handleBack(ActionEvent actionEvent) {
        try {
            if (previousView != null) {
                posterContainer.getScene().setRoot(previousView);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showReviewStatus(String message, boolean isError) {
        reviewStatusLabel.setText(message);
        if (isError) {
            reviewStatusLabel.setStyle("-fx-text-fill: #dfa1a1;");
        } else {
            reviewStatusLabel.setStyle("-fx-text-fill: #b4d6b7;");
        }
        reviewStatusLabel.setVisible(true);
    }

    @FXML
    public void handleSaveReview(ActionEvent actionEvent) {
        if (currentRating == 0) {
            showReviewStatus("Please select a star rating.", true);
            return;
        }

        String comment = reviewTextArea.getText();

        try {
            if (existingUserReview == null) {
                existingUserReview = reviewService.addReview(
                        currentRating,
                        comment,
                        LocalDateTime.now(),
                        loggedUser.getId(),
                        selectedMovie.getId()
                );
                saveReviewButton.setText("UPDATE");
                showReviewStatus("Review saved successfully", false);
            } else {
                reviewService.updateReview(
                        existingUserReview.getId(),
                        currentRating,
                        comment,
                        LocalDateTime.now(),
                        loggedUser.getId(),
                        selectedMovie.getId()
                );
            }
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

            Parent currentRoot = posterContainer.getScene().getRoot();
            cachedWatchlistController.setPreviousView(currentRoot);

            cachedWatchlistController.loadData();
            posterContainer.getScene().setRoot(cachedWatchlistRoot);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    public void handleToggleWatchlist(ActionEvent actionEvent) {
        if (loggedUser == null || selectedMovie == null) return;

        watchlistButton.setDisable(true);

        CompletableFuture.runAsync(() -> {
            try {
                boolean currentlyInWatchlist = watchlistService.isInWatchlist(loggedUser.getId(), selectedMovie.getId());

                if (currentlyInWatchlist) {
                    watchlistService.removeFromWatchlist(loggedUser.getId(), selectedMovie.getId());

                    Platform.runLater(() -> {
                        watchlistButton.setText("+ WATCHLIST");
                        watchlistButton.setStyle("");
                        watchlistButton.setDisable(false);
                    });
                } else {
                    watchlistService.addToWatchlist(loggedUser.getId(), selectedMovie.getId());

                    Platform.runLater(() -> {
                        watchlistButton.setText("-WATCHLIST");
                        watchlistButton.setStyle("-fx-background-color: #555555; -fx-text-fill: white;");
                        watchlistButton.setDisable(false);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> watchlistButton.setDisable(false));
            }
        });
    }
}
