package devatron.company.libraryapp;

import devatron.company.libraryapp.dao.BookDAO;
import devatron.company.libraryapp.dao.LoanDAO;
import devatron.company.libraryapp.model.Book;
import devatron.company.libraryapp.model.Loan;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import java.time.LocalDate;
import java.util.List;
import java.util.function.UnaryOperator;

public class LoanController {

    /* ---------- FXML ---------- */
    @FXML private TableView<Loan>         tableLoans;
    @FXML private TableColumn<Loan,String>     colUser;
    @FXML private TableColumn<Loan,String>     colBook;
    @FXML private TableColumn<Loan,String>     colPhone;
    @FXML private TableColumn<Loan,LocalDate>  colStart;
    @FXML private TableColumn<Loan,LocalDate>  colDue;
    @FXML private TableColumn<Loan,Boolean>    colExpired;

    @FXML private TextField   txtUserName;
    @FXML private TextField   txtUserSurname;
    @FXML private TextField   txtUserPhone;
    @FXML private ComboBox<Book> comboBook;
    @FXML private DatePicker  dpLoanStart;
    @FXML private DatePicker  dpLoanDue;

    @FXML private Label  lblNewLoan;
    @FXML private Button btnRegister;
    @FXML private Button btnReturn;
    @FXML private Button btnCancel;

    /* ---------- DAO ---------- */
    private final LoanDAO loanDAO = new LoanDAO();
    private final BookDAO bookDAO = new BookDAO();

    /* ---------- Inizializzazione ---------- */
    @FXML
    public void initialize() {

        applyTranslations();

        /* formatter telefono -> XXX YYY ZZZZ */
        txtUserPhone.setTextFormatter(createPhoneFormatter());

        reloadBookCombo();

        /* colonne tabella */
        colUser .setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getUserFullName()));
        colBook .setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getBookTitle()));
        colPhone.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getUserPhone()));
        colStart.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue().getStartDate()));
        colDue  .setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue().getDueDate()));

        /* colonna scadenza */
        colExpired.setCellValueFactory(d ->
                new javafx.beans.property.SimpleBooleanProperty(
                        !d.getValue().isReturned() &&
                        d.getValue().getDueDate().isBefore(LocalDate.now()))
        );
        colExpired.setCellFactory(CheckBoxTableCell.forTableColumn(colExpired));
        colExpired.setEditable(false);

        tableLoans.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        refreshLoans();

        /* notifica prestiti in scadenza oggi */
        List<Loan> dueToday = loanDAO.getLoansDueOn(LocalDate.now());
        if (!dueToday.isEmpty()) {
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle      (Lang.get("loan.notify.title"));
            info.setHeaderText (Lang.get("loan.notify.header"));
            info.setContentText(String.format(
                    Lang.get("loan.notify.content"), dueToday.size()));
            info.showAndWait();
        }
    }

    /* ---------- Formatter telefono ---------- */
    private TextFormatter<String> createPhoneFormatter() {

        UnaryOperator<TextFormatter.Change> filter = change -> {

            String digits = change.getControlNewText().replaceAll("\\D", "");
            if (digits.length() > 10) digits = digits.substring(0, 10);

            StringBuilder sb = new StringBuilder(digits);
            if (digits.length() > 3) sb.insert(3, ' ');
            if (digits.length() > 6) sb.insert(7, ' ');

            /* sostituisco tutto il testo attuale */
            change.setRange(0, change.getControlText().length());
            change.setText(sb.toString());

            /* caret alla fine */
            change.setCaretPosition(sb.length());
            change.setAnchor(sb.length());
            return change;
        };
        return new TextFormatter<>(filter);
    }

    /* ---------- Combo libri ---------- */
    private void reloadBookCombo() {
        List<Book> available = bookDAO.getAllBooks().stream()
                                      .filter(b -> b.getQuantity() > 0)
                                      .toList();

        comboBook.setItems(FXCollections.observableArrayList(available));

        /* titolo nel menu a discesa */
        comboBook.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Book item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getTitle());
            }
        });
        /* titolo anche quando la combo è chiusa */
        comboBook.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Book item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getTitle());
            }
        });
    }

    /* ---------- Registrazione prestito ---------- */
    @FXML
    private void handleRegisterLoan() {

        Book      selectedBook = comboBook.getValue();
        String    name    = txtUserName.getText().trim();
        String    surname = txtUserSurname.getText().trim();
        String    phone   = txtUserPhone.getText().trim();
        LocalDate start   = dpLoanStart.getValue();
        LocalDate due     = dpLoanDue.getValue();

        if (selectedBook == null || name.isEmpty() || surname.isEmpty()
            || start == null || due == null) {
            new Alert(Alert.AlertType.ERROR,
                    Lang.get("loan.error.missing")).showAndWait();
            return;
        }
        if (selectedBook.getQuantity() <= 0) {
            new Alert(Alert.AlertType.ERROR,
                    Lang.get("loan.error.unavailable")).showAndWait();
            return;
        }

        Loan loan = new Loan(0, selectedBook.getIsbn(), selectedBook.getTitle(),
                             name, surname, phone, start, due, false);
        loanDAO.addLoan(loan);

        /* -1 alla quantità */
        selectedBook.setQuantity(selectedBook.getQuantity() - 1);
        bookDAO.updateBook(selectedBook);

        refreshLoans();
        reloadBookCombo();
        clearForm();
    }

    /* ---------- Restituzione prestito ---------- */
    @FXML
    private void handleReturn() {

        Loan sel = tableLoans.getSelectionModel().getSelectedItem();
        if (sel == null) {
            new Alert(Alert.AlertType.WARNING,
                    Lang.get("loan.return.noSelection")).showAndWait();
            return;
        }

        loanDAO.markAsReturned(sel.getId());

        Book b = bookDAO.getByIsbn(sel.getIsbn());
        if (b != null) {
            b.setQuantity(b.getQuantity() + 1);
            bookDAO.updateBook(b);
        }

        refreshLoans();
        reloadBookCombo();
    }

    /* ---------- Utility ---------- */
    private void refreshLoans() {
        tableLoans.setItems(FXCollections.observableArrayList(
                loanDAO.getActiveLoans()));
    }

    private void clearForm() {
        txtUserName.clear();
        txtUserSurname.clear();
        txtUserPhone.clear();
        comboBook.setValue(null);
        dpLoanStart.setValue(null);
        dpLoanDue  .setValue(null);
    }

    /* ---------- Traduzioni ---------- */
    private void applyTranslations() {
        lblNewLoan  .setText(Lang.get("loan.title"));
        txtUserName .setPromptText(Lang.get("loan.userName.prompt"));
        txtUserSurname.setPromptText(Lang.get("loan.userSurname.prompt"));
        txtUserPhone.setPromptText(Lang.get("loan.userPhone.prompt"));
        comboBook   .setPromptText(Lang.get("loan.book.prompt"));
        dpLoanStart .setPromptText(Lang.get("loan.date.start.prompt"));
        dpLoanDue   .setPromptText(Lang.get("loan.date.due.prompt"));
        btnRegister .setText(Lang.get("loan.button.register"));
        btnReturn   .setText(Lang.get("loan.button.return"));
        btnCancel   .setText(Lang.get("loan.button.cancel"));

        colUser   .setText(Lang.get("loan.col.user"));
        colBook   .setText(Lang.get("loan.col.book"));
        colPhone  .setText(Lang.get("loan.col.phone"));
        colStart  .setText(Lang.get("loan.col.start"));
        colDue    .setText(Lang.get("loan.col.due"));
        colExpired.setText(Lang.get("loan.col.expired"));
    }

    /* ---------- Torna al main ---------- */
    @FXML
    private void handleCancel() {
        LibraryApp.switchToMainView();
    }
}
