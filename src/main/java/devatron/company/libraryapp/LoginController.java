package devatron.company.libraryapp;

import devatron.company.libraryapp.dao.UserDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {
    @FXML
    private TextField txtEmail;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private TextField txtPasswordVisible;
    @FXML
    private Button btnTogglePassword;

    private final UserDAO userDAO;

    public LoginController() {
        userDAO = new UserDAO();
    }
    @FXML
    private void initialize() {
        txtPassword.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!txtPasswordVisible.isVisible()) {
                txtPasswordVisible.setText(newVal);
            }
        });
        txtPasswordVisible.textProperty().addListener((obs, oldVal, newVal) -> {
            if (txtPasswordVisible.isVisible()) {
                txtPassword.setText(newVal);
            }
        });
    }
    @FXML
    private void togglePasswordVisibility() {
        if (txtPasswordVisible.isVisible()) {
            txtPasswordVisible.setVisible(false);
            txtPasswordVisible.setManaged(false);
            txtPassword.setText(txtPasswordVisible.getText());
            txtPassword.setVisible(true);
            txtPassword.setManaged(true);
            btnTogglePassword.setText("👁");
        } else {
            txtPasswordVisible.setText(txtPassword.getText());
            txtPasswordVisible.setVisible(true);
            txtPasswordVisible.setManaged(true);
            txtPassword.setVisible(false);
            txtPassword.setManaged(false);
            btnTogglePassword.setText("🙈");
        }
    }
    @FXML
    private void handleLogin() {
        String email = txtEmail.getText().trim();
        String password = txtPassword.isVisible() ? txtPassword.getText() : txtPasswordVisible.getText();
        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Campi vuoti", "Compila tutti i campi!");
            return;
        }
        boolean valid = userDAO.validateUser(email, password);
        if (valid) {
            LibraryApp.switchToMainView();
        } else {
            showAlert("Login Fallito", "Email o password errati, oppure dominio non valido.");
        }
    }
    @FXML
    private void handleExit() {
        Stage stage = (Stage) txtEmail.getScene().getWindow();
        stage.close();
    }
    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
