package devatron.company.libraryapp;

import java.io.IOException;

import devatron.company.libraryapp.dao.BookDAO;
import devatron.company.libraryapp.model.Book;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
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
    private TextField txtGenre;
    @FXML
    private TextField txtQuantity;
    @FXML
    private TextField txtSold;

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
        colGenre.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getGenre()));
        colQuantity.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getQuantity()).asObject());
        colSold.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getSold()).asObject());

        tableBooks.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                txtIsbn.setText(newSel.getIsbn());
                txtTitle.setText(newSel.getTitle());
                txtPublisher.setText(newSel.getPublisher());
                txtAuthor.setText(newSel.getAuthor());
                spYear.getValueFactory().setValue(newSel.getYearPublished());
                txtPrice.setText(String.valueOf(newSel.getPrice()));
                txtGenre.setText(newSel.getGenre());
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
        String genre = txtGenre.getText().trim();
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
        String genre = txtGenre.getText().trim();
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
    private void handleLogout() {LibraryApp.switchToLoginView();}


    @FXML
    private void handleShowStatistics() {
        try {
            FXMLLoader loader = new FXMLLoader(LibraryApp.class.getResource("/devatron/company/libraryapp/stats-view.fxml"));
            Scene scene = new Scene(loader.load());
            LibraryApp.getPrimaryStage().setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleShowLoans() {
        try {
            FXMLLoader loader = new FXMLLoader(LibraryApp.class.getResource("/devatron/company/libraryapp/loan-view.fxml"));
            Scene scene = new Scene(loader.load());
            LibraryApp.getPrimaryStage().setScene(scene);
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
        txtGenre.clear();
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
