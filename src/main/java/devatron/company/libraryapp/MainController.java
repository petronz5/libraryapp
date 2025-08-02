package devatron.company.libraryapp;

import devatron.company.libraryapp.dao.BookDAO;
import devatron.company.libraryapp.model.Book;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;

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
    private final BookDAO bookDAO;

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

        tableBooks.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                txtIsbn.setText(newSel.getIsbn());
                txtTitle.setText(newSel.getTitle());
                txtPublisher.setText(newSel.getPublisher());
                txtAuthor.setText(newSel.getAuthor());
                spYear.getValueFactory().setValue(newSel.getYearPublished());
                txtPrice.setText(String.valueOf(newSel.getPrice()));
            }
        });
        tableBooks.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE || event.getCode() == KeyCode.BACK_SPACE) {
                handleDeleteBook();
            }
        });
        loadBooks();
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

        if (isbn.isEmpty() || title.isEmpty() || publisher.isEmpty() ||
                author.isEmpty() || priceStr.isEmpty()) {
            showAlert("Campi Vuoti", "Compila tutti i campi!");
            return;
        }
        if (isbn.length() != 13) {
            showAlert("Errore ISBN", "L'ISBN deve contenere esattamente 13 cifre!");
            return;
        }
        double price;
        try {price = Double.parseDouble(priceStr);}
        catch (NumberFormatException e) {
            showAlert("Errore Prezzo", "Il prezzo inserito non è valido.");
            return;
        }
        Book newBook = new Book(isbn, title, publisher, author, year, price);
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
        bookDAO.updateBook(selectedBook);
        loadBooks();
        clearFields();
    }

    @FXML
    private void handleDeleteBook() {
        Book selectedBook = tableBooks.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {return;}
        bookDAO.deleteBook(selectedBook.getIsbn());
        loadBooks();
        clearFields();
    }
    @FXML
    private void handleClearFields() {clearFields();}

    @FXML
    private void handleLogout() {LibraryApp.switchToLoginView();}

    private void clearFields() {
        txtIsbn.clear();
        txtTitle.clear();
        txtPublisher.clear();
        txtAuthor.clear();
        spYear.getValueFactory().setValue(2025);
        txtPrice.clear();
        txtSearch.clear();
    }
    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
