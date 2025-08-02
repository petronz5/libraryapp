package devatron.company.libraryapp;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

public class LoanController {

    @FXML
    private TextField txtUserEmail;
    @FXML
    private TextField txtBookIsbn;
    @FXML
    private DatePicker dpLoanStart;
    @FXML
    private DatePicker dpLoanDue;
    
    @FXML
    private void handleRegisterLoan() {
        String userEmail = txtUserEmail.getText().trim();
        String bookIsbn = txtBookIsbn.getText().trim();
        if (userEmail.isEmpty() || bookIsbn.isEmpty() || dpLoanStart.getValue() == null || dpLoanDue.getValue() == null) {
            showAlert("Errore", "Compila tutti i campi del prestito!");
            return;
        }
        // Qui inserisci la logica per registrare il prestito nel database:
        // ad esempio creando un oggetto Loan e usando un LoanDAO (da implementare)
        showAlert("Prestito", "Prestito registrato per " + bookIsbn + " all'utente " + userEmail);
    }
    
    @FXML
    private void handleCancel() {
        LibraryApp.switchToMainView();
    }
    
    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}