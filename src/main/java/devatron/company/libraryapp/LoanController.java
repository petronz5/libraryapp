package devatron.company.libraryapp;

import devatron.company.libraryapp.dao.BookDAO;
import devatron.company.libraryapp.dao.LoanDAO;
import devatron.company.libraryapp.model.Book;
import devatron.company.libraryapp.model.Loan;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import java.time.LocalDate;
import java.util.List;
import java.util.function.UnaryOperator;

public class LoanController {

    @FXML private TableView<Loan>           tableLoans;
    @FXML private TableColumn<Loan,String>  colUser;
    @FXML private TableColumn<Loan,String>  colBook;
    @FXML private TableColumn<Loan,String>  colPhone;
    @FXML private TableColumn<Loan,LocalDate> colStart;
    @FXML private TableColumn<Loan,LocalDate> colDue;
    @FXML private TableColumn<Loan,Boolean> colExpired;

    @FXML private TextField   txtUserName;
    @FXML private TextField   txtUserSurname;
    @FXML private TextField   txtUserPhone;
    @FXML private ComboBox<Book> comboBook;
    @FXML private DatePicker  dpLoanStart;
    @FXML private DatePicker  dpLoanDue;

    @FXML private Label   lblNewLoan;
    @FXML private Button  btnRegister;
    @FXML private Button  btnReturn;
    @FXML private Button  btnCancel;

    private final LoanDAO loanDAO = new LoanDAO();
    private final BookDAO bookDAO = new BookDAO();

    @FXML
    public void initialize() {
        applyTranslations();

        // formatter telefono XXX YYY ZZZZ (solo cifre)
        txtUserPhone.setTextFormatter(createPhoneFormatter());

        // popola combo con libri disponibili
        reloadBookCombo();

        // setup colonne
        colUser.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUserFullName()));
        colBook.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getBookTitle()));
        colPhone.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUserPhone()));
        colStart.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getStartDate()));
        colDue.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getDueDate()));

        // colonna “Scaduto” editabile + styling rosso se scaduto e non restituito
        tableLoans.setEditable(true);
        colExpired.setCellValueFactory(d ->
            new SimpleBooleanProperty(
                !d.getValue().isReturned() &&
                d.getValue().getDueDate().isBefore(LocalDate.now())
            )
        );
        colExpired.setCellFactory(tc -> new CheckBoxTableCell<>() {
            @Override
            public void updateItem(Boolean expired, boolean empty) {
                super.updateItem(expired, empty);
                if (empty) {
                    setGraphic(null);
                    setStyle("");
                } else {
                    setGraphic(getGraphic());
                    if (expired) {
                        setStyle("-fx-background-color: tomato;");
                    } else {
                        setStyle("");
                    }
                    // click restituisce il libro
                    this.setOnMouseClicked(evt -> {
                        Loan loan = getTableView().getItems().get(getIndex());
                        loanDAO.markAsReturned(loan.getId());
                        Book b = bookDAO.getByIsbn(loan.getBookIsbn());
                        if (b != null) {
                            b.setQuantity(b.getQuantity() + 1);
                            bookDAO.updateBook(b);
                        }
                        refreshLoans();
                        reloadBookCombo();
                    });
                }
            }
        });
        colExpired.setEditable(true);

        tableLoans.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        refreshLoans();

        // ➞ la notifica di scadenze viene mostrata da MainController dopo il load della view
    }

    /** Mostra l’alert per i prestiti in scadenza oggi, chiamato da MainController. */
    public void showDueNotification() {
        List<Loan> dueToday = loanDAO.getLoansDueOn(LocalDate.now());
        if (!dueToday.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(Lang.get("loan.notify.title"));
            alert.setHeaderText(Lang.get("loan.notify.header"));
            alert.setContentText(String.format(
                Lang.get("loan.notify.content"),
                dueToday.size()
            ));
            alert.showAndWait();
        }
    }

    @FXML
    private void handleRegisterLoan() {
        Book selected = comboBook.getValue();
        String name    = txtUserName.getText().trim();
        String surname = txtUserSurname.getText().trim();
        String phone   = txtUserPhone.getText().trim();
        LocalDate start= dpLoanStart.getValue();
        LocalDate due  = dpLoanDue.getValue();

        if (selected == null || name.isEmpty() || surname.isEmpty() || start == null || due == null) {
            new Alert(Alert.AlertType.ERROR, Lang.get("loan.error.missing")).showAndWait();
            return;
        }
        if (selected.getQuantity() <= 0) {
            new Alert(Alert.AlertType.ERROR, Lang.get("loan.error.unavailable")).showAndWait();
            return;
        }

        Loan loan = new Loan(
            0,
            selected.getIsbn(),
            selected.getTitle(),
            name, surname, phone,
            start, due,
            false
        );
        loanDAO.addLoan(loan);
        selected.setQuantity(selected.getQuantity() - 1);
        bookDAO.updateBook(selected);

        refreshLoans();
        reloadBookCombo();
        clearForm();
    }

    @FXML
    private void handleReturn() {
        Loan sel = tableLoans.getSelectionModel().getSelectedItem();
        if (sel == null) {
            new Alert(Alert.AlertType.WARNING, Lang.get("loan.return.noSelection")).showAndWait();
            return;
        }
        loanDAO.markAsReturned(sel.getId());
        Book b = bookDAO.getByIsbn(sel.getBookIsbn());
        if (b != null) {
            b.setQuantity(b.getQuantity() + 1);
            bookDAO.updateBook(b);
        }
        refreshLoans();
        reloadBookCombo();
    }

    @FXML
    private void handleCancel() {
        LibraryApp.switchToMainView();
    }

    // — UTILITIES —

    private void refreshLoans() {
        tableLoans.setItems(FXCollections.observableArrayList(
            loanDAO.getActiveLoans()
        ));
    }

    private void reloadBookCombo() {
        comboBook.setItems(FXCollections.observableArrayList(
            bookDAO.getAllBooks().stream()
                   .filter(b -> b.getQuantity() > 0)
                   .toList()
        ));
    }

    private void clearForm() {
        txtUserName.clear();
        txtUserSurname.clear();
        txtUserPhone.clear();
        comboBook.setValue(null);
        dpLoanStart.setValue(null);
        dpLoanDue.setValue(null);
    }

    private TextFormatter<String> createPhoneFormatter() {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String digits = change.getControlNewText().replaceAll("\\D", "");
            if (digits.length() > 10) digits = digits.substring(0, 10);
            StringBuilder sb = new StringBuilder(digits);
            if (digits.length() > 3) sb.insert(3, ' ');
            if (digits.length() > 6) sb.insert(7, ' ');
            change.setRange(0, change.getControlText().length());
            change.setText(sb.toString());
            return change;
        };
        return new TextFormatter<>(filter);
    }

    private void applyTranslations() {
        lblNewLoan     .setText(Lang.get("loan.title"));
        txtUserName    .setPromptText(Lang.get("loan.userName.prompt"));
        txtUserSurname .setPromptText(Lang.get("loan.userSurname.prompt"));
        txtUserPhone   .setPromptText(Lang.get("loan.userPhone.prompt"));
        comboBook      .setPromptText(Lang.get("loan.book.prompt"));
        dpLoanStart    .setPromptText(Lang.get("loan.date.start.prompt"));
        dpLoanDue      .setPromptText(Lang.get("loan.date.due.prompt"));
        btnRegister    .setText(Lang.get("loan.button.register"));
        btnReturn      .setText(Lang.get("loan.button.return"));
        btnCancel      .setText(Lang.get("loan.button.cancel"));

        colUser    .setText(Lang.get("loan.col.user"));
        colBook    .setText(Lang.get("loan.col.book"));
        colPhone   .setText(Lang.get("loan.col.phone"));
        colStart   .setText(Lang.get("loan.col.start"));
        colDue     .setText(Lang.get("loan.col.due"));
        colExpired .setText(Lang.get("loan.col.expired"));
    }
}
