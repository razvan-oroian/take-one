package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.User;
import service.*;

import java.util.concurrent.CompletableFuture;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private WatchlistService watchlistService;

    public void setWatchlistService(WatchlistService watchlistService) {
        this.watchlistService = watchlistService;
    }

    private DirectorService directorService;

    public void  setDirectorService(DirectorService directorService) {
        this.directorService = directorService;
    }

    private CastingService castingService;

    public void setCastingService(CastingService castingService) {
        this.castingService = castingService;
    }

    private UserService userService;

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    private MovieService movieService;

    public void setMovieService(MovieService movieService) {
        this.movieService = movieService;
    }

    private GenreService genreService;

    public void setGenreService(GenreService genreService) {
        this.genreService = genreService;
    }

    private ReviewService reviewService;

    public void setReviewService(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    private Parent cachedSearchRoot;
    private SearchController cachedSearchController;

    public void startBackgroundPreloading() {
        if (cachedSearchRoot != null) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(500);

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/search-view.fxml"));
                Parent root = loader.load();
                SearchController controller = loader.getController();

                Platform.runLater(() -> {
                    cachedSearchRoot = root;
                    cachedSearchController = controller;

                    cachedSearchController.setMovieService(movieService);
                    cachedSearchController.setGenreService(genreService);
                    cachedSearchController.setCastingService(castingService);
                    cachedSearchController.setDirectorService(directorService);
                    cachedSearchController.setReviewService(reviewService);
                    cachedSearchController.setWatchlistService(watchlistService);

                    cachedSearchController.initFilters();
                    cachedSearchController.handleSearch();

                    cachedSearchController.startBackgroundPreloading();
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    @FXML
    private void handleLogin() {
        try {
            User loggedUser = userService.loginUser(emailField.getText(), passwordField.getText());
            errorLabel.setVisible(false);

            Parent root;
            SearchController controller;

            if (cachedSearchController != null) {
                root = cachedSearchRoot;
                controller = cachedSearchController;
            } else {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/search-view.fxml"));
                root = loader.load();
                controller = loader.getController();

                controller.setMovieService(movieService);
                controller.setGenreService(genreService);
                controller.setCastingService(castingService);
                controller.setDirectorService(directorService);
                controller.setWatchlistService(watchlistService);
            }

            controller.setLoggedUser(loggedUser);

            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setMinWidth(900);
            stage.setMinHeight(800);
            stage.centerOnScreen();

            stage.getScene().setRoot(root);

        } catch (Exception ex) {
            System.out.printf(ex.getMessage());
            errorLabel.setText(ex.getMessage());
            errorLabel.setVisible(true);
        }
    }

    @FXML
    private void goToRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/register-view.fxml"));
            Parent root = loader.load();

            RegisterController registerController = loader.getController();
            registerController.setUserService(userService);

            Stage stage = (Stage) emailField.getScene().getWindow();

            stage.getScene().setRoot(root);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
