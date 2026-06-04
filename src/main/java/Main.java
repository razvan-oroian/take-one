import controller.LoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import repository.*;
import repository.hibernate.*;
import service.*;

public class Main extends Application {
    private UserService userService;
    private MovieService movieService;
    private GenreService genreService;
    private CastingService castingService;
    private DirectorService directorService;
    private ReviewService reviewService;
    private WatchlistService watchlistService;

    @Override
    public void init() throws Exception {
        UserRepository userRepository = new UserDbRepository();
        this.userService = new UserService(userRepository);

        MovieRepository movieRepository = new MovieDbRepository();
        this.movieService = new MovieService(movieRepository);

        GenreRepository genreRepository = new GenreDbRepository();
        this.genreService = new GenreService(genreRepository);

        CastingRepository castingRepository = new CastingDbRepository();
        this.castingService = new CastingService(castingRepository);

        DirectorRepository directorRepository = new DirectorDbRepository();
        this.directorService = new DirectorService(directorRepository);

        ReviewRepository reviewRepository = new ReviewDbRepository();
        this.reviewService = new ReviewService(reviewRepository, userRepository, movieRepository);

        WatchlistEntryRepository watchlistEntryRepository = new WatchlistEntryDbRepository();
        this.watchlistService = new WatchlistService(watchlistEntryRepository, userRepository, movieRepository);
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("views/login-view.fxml"));
        Parent root = loader.load();

        LoginController loginController = loader.getController();
        loginController.setUserService(userService);
        loginController.setMovieService(movieService);
        loginController.setGenreService(genreService);
        loginController.setCastingService(castingService);
        loginController.setDirectorService(directorService);
        loginController.setReviewService(reviewService);
        loginController.setWatchlistService(watchlistService);

        loginController.startBackgroundPreloading();

        Scene scene = new Scene(root, 400, 500);
        stage.setTitle("Take One");
        stage.setScene(scene);
        stage.show();
    }
}
