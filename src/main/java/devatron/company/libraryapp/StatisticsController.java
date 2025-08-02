package devatron.company.libraryapp;

import devatron.company.libraryapp.dao.BookDAO;
import devatron.company.libraryapp.model.Book;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.*;
import java.util.stream.Collectors;

public class StatisticsController {

    @FXML
    private PieChart pieChart;
    @FXML
    private TableView<Book> tableSoldBooks;
    @FXML
    private TableColumn<Book, String> colIsbn;
    @FXML
    private TableColumn<Book, String> colTitle;
    @FXML
    private TableColumn<Book, Integer> colSold;
    @FXML
    private TextField txtSearchBook;

    private final BookDAO bookDAO = new BookDAO();

    @FXML
    public void initialize() {
        List<Book> allBooks = bookDAO.getAllBooks();

        // PieChart per generi
        Map<String, Integer> genreCount = new HashMap<>();
        for (Book book : allBooks) {
            genreCount.put(book.getGenre(), genreCount.getOrDefault(book.getGenre(), 0) + 1);
        }
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> entry : genreCount.entrySet()) {
            pieData.add(new PieChart.Data(entry.getKey().isEmpty() ? "Senza genere" : entry.getKey(), entry.getValue()));
        }
        pieChart.setData(pieData);

        // Tabella: top 10 libri più venduti
        List<Book> topSold = allBooks.stream()
                .sorted(Comparator.comparingInt(Book::getSold).reversed())
                .limit(10)
                .collect(Collectors.toList());
        ObservableList<Book> topSoldObs = FXCollections.observableArrayList(topSold);

        colIsbn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colSold.setCellValueFactory(new PropertyValueFactory<>("sold"));
        tableSoldBooks.setItems(topSoldObs);

        // Autocompletamento ricerca
        txtSearchBook.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                tableSoldBooks.setItems(topSoldObs);
            } else {
                String lower = newVal.toLowerCase();
                List<Book> filtered = allBooks.stream()
                        .filter(b -> b.getTitle().toLowerCase().contains(lower) || b.getIsbn().contains(lower))
                        .sorted(Comparator.comparingInt(Book::getSold).reversed())
                        .limit(10)
                        .collect(Collectors.toList());
                tableSoldBooks.setItems(FXCollections.observableArrayList(filtered));
            }
        });
    }

    @FXML
    private void handleBack() {
        LibraryApp.switchToMainView();
    }
}