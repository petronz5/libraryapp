package devatron.company.libraryapp;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

import javafx.util.Pair;
import javafx.util.StringConverter;
import devatron.company.libraryapp.dao.BookDAO;
import devatron.company.libraryapp.dao.SaleDAO;
import devatron.company.libraryapp.model.Book;
import devatron.company.libraryapp.model.Sale;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;

public class MainController {

    @FXML
    private TableView<Book> tableBooks;
    @FXML
    private TableColumn<Book, String> colIsbn;
    @FXML
    private TableColumn<Book, String> colTitle;
    @FXML
    private TableColumn<Book, String> colPublisher;
    @FXML
    private TableColumn<Book, String> colAuthor;
    @FXML
    private TableColumn<Book, Integer> colYear;
    @FXML
    private TableColumn<Book, Double> colPrice;
    @FXML
    private TableColumn<Book, String> colGenre;
    @FXML
    private TableColumn<Book, Integer> colQuantity;
    @FXML
    private TableColumn<Book, Integer> colSold;
    @FXML
    private TextField txtIsbn;
    @FXML
    private TextField txtTitle;
    @FXML
    private TextField txtPublisher;
    @FXML
    private TextField txtAuthor;
    @FXML
    private Spinner<Integer> spYear;
    @FXML
    private TextField txtPrice;
    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<String> comboGenre;
    @FXML
    private TextField txtQuantity;
    @FXML
    private TextField txtSold;
    @FXML
    private ComboBox<String> localeCombo;

    @FXML private Label  lblSearch;
    @FXML private Button btnSearch;
    @FXML private Button btnStats;
    @FXML private Button btnLoans;
    @FXML private Button btnLogout;

    private final BookDAO bookDAO;
    private final SaleDAO saleDAO = new SaleDAO();

    @FXML private Label lblIsbn;
    @FXML private Label lblTitle;
    @FXML private Label lblPublisher;
    @FXML private Label lblAuthor;
    @FXML private Label lblYear;
    @FXML private Label lblPrice;
    @FXML private Label lblGenre;
    @FXML private Label lblQuantity;
    @FXML private Label lblSold;
    @FXML private Button btnAdd;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;
    @FXML private Button btnClear;
    @FXML private Button btnReload;

    public MainController() {
        bookDAO = new BookDAO();
    }
    @FXML
    public void initialize() {
        SpinnerValueFactory<Integer> yearFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1900, 2100, 2025);
        spYear.setValueFactory(yearFactory);
        txtIsbn.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d{0,13}")) {txtIsbn.setText(oldVal);}
        });

        txtPrice.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d{0,7}([\\.]\\d{0,2})?")) {txtPrice.setText(oldVal);}
        });

        colIsbn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getIsbn()));
        colTitle.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTitle()));
        colPublisher.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPublisher()));
        colAuthor.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getAuthor()));
        colYear.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getYearPublished()).asObject());
        colPrice.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getPrice()).asObject());
        colGenre.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getGenre()));
        comboGenre.setItems(FXCollections.observableArrayList(
            "Giallo", "Romanzo", "Horror", "Thriller", "Romanzo storico", "Saggio", "Biografia", "Autobiografia", "Manuale"
        ));
        colQuantity.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getQuantity()).asObject());
        colSold.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getSold()).asObject());
        localeCombo.setItems(FXCollections.observableArrayList("IT", "EN", "FR", "SP"));
        // ---- visualizza il nome della lingua nel Combo anziché il codice ----
        Map<String,String> langMap = Map.of(
        "IT", Lang.get("lang.it"),
        "EN", Lang.get("lang.en"),
        "FR", Lang.get("lang.fr"),
        "SP", Lang.get("lang.sp")
        );
        localeCombo.setConverter(new StringConverter<>() {
        @Override 
        public String toString(String code) {
            return langMap.getOrDefault(code, code);
        }
        @Override 
        public String fromString(String s) {
            return s;
        }
        });
        // imposta anche il prompt traducibile
        localeCombo.setPromptText(Lang.get("main.combo.locale.prompt"));
        localeCombo.setValue(Lang.current().toUpperCase());  

        localeCombo.setOnAction(e -> {
            String code = localeCombo.getValue().toLowerCase();
            Lang.load(code);
            // ricarichiamo tutta la scena per applicare le nuove stringhe
            LibraryApp.switchToMainView();
        });
        
        tableBooks.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableBooks.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                Book selected = tableBooks.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    showSaleDialog(selected);
                    event.consume();   // preveniamo ulteriori gestioni di ENTER
                }
            }
        });

        applyTranslations();

        tableBooks.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                txtIsbn.setText(newSel.getIsbn());
                txtTitle.setText(newSel.getTitle());
                txtPublisher.setText(newSel.getPublisher());
                txtAuthor.setText(newSel.getAuthor());
                spYear.getValueFactory().setValue(newSel.getYearPublished());
                txtPrice.setText(String.valueOf(newSel.getPrice()));
                comboGenre.setValue(newSel.getGenre());
                txtQuantity.setText(String.valueOf(newSel.getQuantity()));
                txtSold.setText(String.valueOf(newSel.getSold()));
            }
        });
        tableBooks.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE || event.getCode() == KeyCode.BACK_SPACE) {
                Book selectedBook = tableBooks.getSelectionModel().getSelectedItem();
                if (selectedBook != null) {
                    Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Sei sicuro di voler eliminare questo libro?", ButtonType.YES, ButtonType.NO);
                    confirmation.setTitle("Conferma eliminazione");
                    confirmation.setHeaderText("Eliminazione libro");
                    if (confirmation.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                        bookDAO.deleteBook(selectedBook.getIsbn());
                        loadBooks();
                        clearFields();
                    }
                }
            }
        });
        
        loadBooks();
    }

    private void applyTranslations() {
        // — Navbar —
        lblSearch .setText(Lang.get("main.search.label"));
        txtSearch .setPromptText(Lang.get("main.search.prompt"));
        btnSearch .setText(Lang.get("main.button.search"));
        btnStats  .setText(Lang.get("main.button.stats"));
        btnLoans  .setText(Lang.get("main.button.loans"));
        btnLogout .setText(Lang.get("main.button.logout"));
        localeCombo.setPromptText(Lang.get("main.combo.locale.prompt"));

        // — Colonne tabella —
        colIsbn     .setText(Lang.get("col.isbn"));
        colTitle    .setText(Lang.get("col.title"));
        colPublisher.setText(Lang.get("col.publisher"));
        colAuthor   .setText(Lang.get("col.author"));
        colYear     .setText(Lang.get("col.year"));
        colPrice    .setText(Lang.get("col.price"));
        colGenre    .setText(Lang.get("col.genre"));
        colQuantity .setText(Lang.get("col.quantity"));
        colSold     .setText(Lang.get("col.sold"));

        // — Form in basso —
        lblIsbn   .setText(Lang.get("label.isbn"));
        lblTitle  .setText(Lang.get("label.title"));
        lblPublisher.setText(Lang.get("label.publisher"));
        lblAuthor .setText(Lang.get("label.author"));
        lblYear   .setText(Lang.get("label.year"));
        lblPrice  .setText(Lang.get("label.price"));
        lblGenre  .setText(Lang.get("label.genre"));
        lblQuantity.setText(Lang.get("label.quantity"));
        lblSold   .setText(Lang.get("label.sold"));

        btnAdd   .setText(Lang.get("button.add"));
        btnUpdate.setText(Lang.get("button.update"));
        btnDelete.setText(Lang.get("button.delete"));
        btnClear .setText(Lang.get("button.clear"));
        btnReload.setText(Lang.get("button.reload"));
    }
    @FXML
    private void loadBooks() {
        ObservableList<Book> bookList = FXCollections.observableArrayList(bookDAO.getAllBooks());
        tableBooks.setItems(bookList);
    }
    @FXML
    private void handleSearch() {
        String keyword = txtSearch.getText().trim();
        if (!keyword.isEmpty()) {
            ObservableList<Book> searchResults = FXCollections.observableArrayList(bookDAO.searchBooks(keyword));
            tableBooks.setItems(searchResults);
        } else {loadBooks();}
    }
    @FXML
    private void handleAddBook() {
        String isbn = txtIsbn.getText().trim();
        String title = txtTitle.getText().trim();
        String publisher = txtPublisher.getText().trim();
        String author = txtAuthor.getText().trim();
        int year = spYear.getValue();
        String priceStr = txtPrice.getText().trim();
        String genre = comboGenre.getValue() != null ? comboGenre.getValue() : "";
        int quantity, sold;
        try {
            quantity = Integer.parseInt(txtQuantity.getText().trim());
            sold = Integer.parseInt(txtSold.getText().trim());
        } catch (NumberFormatException e) {
            showAlert("Errore", "Quantità e venduti devono essere numeri interi!");
            return;
        }
        if (isbn.isEmpty() || title.isEmpty() || publisher.isEmpty() ||
                author.isEmpty() || priceStr.isEmpty()) {
            showAlert("Campi Vuoti", "Compila tutti i campi!");
            return;
        }
        if (isbn.length() != 13) {
            showAlert("Errore ISBN", "L'ISBN deve contenere esattamente 13 cifre!");
            return;
        }
        if (!bookDAO.isIsbnUnico(isbn)) {
            showAlert("Errore ISBN", "L'ISBN esiste già!");
            return;
        }
        double price;
        try {price = Double.parseDouble(priceStr);}
        catch (NumberFormatException e) {
            showAlert("Errore Prezzo", "Il prezzo inserito non è valido.");
            return;
        }
        Book newBook = new Book(isbn, title, publisher, author, year, price, genre, quantity, sold);
        bookDAO.addBook(newBook);
        loadBooks();
        clearFields();
    }

    @FXML
    private void handleUpdateBook() {
        Book selectedBook = tableBooks.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            showAlert("Nessuna selezione", "Seleziona un libro dalla tabella.");
            return;
        }

        String isbn = txtIsbn.getText().trim();
        String title = txtTitle.getText().trim();
        String publisher = txtPublisher.getText().trim();
        String author = txtAuthor.getText().trim();
        int year = spYear.getValue();
        String priceStr = txtPrice.getText().trim();
        String genre = comboGenre.getValue() != null ? comboGenre.getValue() : "";
        int quantity, sold;
        try {
            quantity = Integer.parseInt(txtQuantity.getText().trim());
            sold = Integer.parseInt(txtSold.getText().trim());
        } catch (NumberFormatException e) {
            showAlert("Errore", "Quantità e venduti devono essere numeri interi!");
            return;
        }

        if (isbn.isEmpty() || title.isEmpty() || publisher.isEmpty() ||
                author.isEmpty() || priceStr.isEmpty()) {
            showAlert("Campi Vuoti", "Compila tutti i campi!");
            return;
        }

        if (isbn.length() != 13) {
            showAlert("Errore ISBN", "L'ISBN deve contenere 13 cifre!");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            showAlert("Errore Prezzo", "Il prezzo inserito non è valido.");
            return;
        }

        selectedBook.setIsbn(isbn);
        selectedBook.setTitle(title);
        selectedBook.setPublisher(publisher);
        selectedBook.setAuthor(author);
        selectedBook.setYearPublished(year);
        selectedBook.setPrice(price);
        selectedBook.setGenre(genre);
        selectedBook.setQuantity(quantity);
        selectedBook.setSold(sold);
        bookDAO.updateBook(selectedBook);
        loadBooks();
        clearFields();
    }

    @FXML
    private void handleDeleteBook() {
        Book selectedBook = tableBooks.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {return;}
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Sei sicuro di voler eliminare il libro?", ButtonType.YES, ButtonType.NO);
        if (confirmation.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) {
            return;
        }
        bookDAO.deleteBook(selectedBook.getIsbn());
        loadBooks();
        clearFields();
    }
    @FXML
    private void handleClearFields() {clearFields();}

    @FXML
    private void handleLogout() {
        SessionManager.clearSession();
        LibraryApp.switchToLoginView();
    }


    @FXML
    private void handleShowStatistics() {
        try {
            FXMLLoader loader = new FXMLLoader(LibraryApp.class.getResource("/devatron/company/libraryapp/stats-view.fxml"));
            Scene scene = new Scene(loader.load());
            LibraryApp.getPrimaryStage().setScene(scene);
            LibraryApp.getPrimaryStage().setTitle("Statistiche");
            LibraryApp.getPrimaryStage().centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void showSaleDialog(Book book) {
        Dialog<Pair<Integer,LocalDate>> dlg = new Dialog<>();
        dlg.setTitle(Lang.get("sale.dialog.title"));
        dlg.setHeaderText(String.format(Lang.get("sale.dialog.header"), book.getTitle()));

        // bottoni
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField qtyField = new TextField("1");
        qtyField.setPromptText(Lang.get("sale.dialog.prompt"));
        DatePicker dpSale   = new DatePicker(LocalDate.now());

        grid.add(new Label(Lang.get("sale.dialog.qty")),  0, 0);
        grid.add(qtyField,                                 1, 0);
        grid.add(new Label(Lang.get("sale.dialog.date")), 1, 1);
        grid.add(dpSale,                                  2, 1);

        dlg.getDialogPane().setContent(grid);

        // risultato
        dlg.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    int q = Integer.parseInt(qtyField.getText().trim());
                    return new Pair<>(q, dpSale.getValue());
                } catch (NumberFormatException e) { /* ignore */ }
            }
            return null;
        });

        dlg.showAndWait().ifPresent(pair -> {
            int soldQty    = pair.getKey();
            LocalDate date = pair.getValue();
            if (soldQty<=0 || soldQty>book.getQuantity()) {
                showWarning( soldQty<=0
                    ? Lang.get("sale.error.invalidNumber")
                    : Lang.get("sale.error.exceeds"));
            } else {
                processSale(book, soldQty, date);
            }
        });
    }

    private void processSale(Book book, int qtySold, LocalDate saleDate) {
        saleDAO.addSale(new Sale(0, book.getIsbn(), qtySold, book.getPrice(), saleDate));
        book.setQuantity(book.getQuantity() - qtySold);
        book.setSold(book.getSold() + qtySold);
        bookDAO.updateBook(book);
        loadBooks();
    }

    /** Mostra un warning semplice */
    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    @FXML
    private void handleShowLoans() {
        try {
            FXMLLoader loader = new FXMLLoader(LibraryApp.class.getResource(
                "/devatron/company/libraryapp/loan-view.fxml"));
            Parent root = loader.load();
            // imposta scena
            Scene scene = new Scene(root);
            LibraryApp.getPrimaryStage().setScene(scene);
            LibraryApp.getPrimaryStage().setTitle(Lang.get("main.button.loans"));
            LibraryApp.getPrimaryStage().centerOnScreen();
            // **ATTENZIONE**: qui mostriamo la view, poi…
            // …prendiamo il controller e invochiamo la notifica
            LoanController loanCtrl = loader.getController();
            loanCtrl.showDueNotification();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void clearFields() {
        txtIsbn.clear();
        txtTitle.clear();
        txtPublisher.clear();
        txtAuthor.clear();
        spYear.getValueFactory().setValue(2025);
        txtPrice.clear();
        txtSearch.clear();
        comboGenre.setValue(null);
        txtQuantity.clear();
        txtSold.clear();
    }
    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
