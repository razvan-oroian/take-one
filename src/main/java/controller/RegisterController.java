package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import service.CastingService;
import service.UserService;
import service.validator.ValidationException;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class RegisterController {
    @FXML
    private TextField usernameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;

    private UserService userService;

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @FXML
    public void handleRegister(ActionEvent actionEvent) {
        try {
            userService.addUser(usernameField.getText(), emailField.getText(),
                    passwordField.getText(), LocalDate.now());
            goToLogin();
        } catch (ValidationException ex) {
            errorLabel.setText(ex.getMessage());
            errorLabel.setVisible(true);
        } catch (Exception ex) {
            errorLabel.setText("An unexpected error ocurred");
            errorLabel.setVisible(true);
            ex.printStackTrace();
        }
    }

    @FXML
    public void goToLogin() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login-view.fxml"));
        Parent root = loader.load();

        LoginController loginController = loader.getController();
        loginController.setUserService(userService);

        Stage stage = (Stage) emailField.getScene().getWindow();
        stage.getScene().setRoot(root);
    }
}
