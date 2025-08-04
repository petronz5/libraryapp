package devatron.company.libraryapp;

import devatron.company.libraryapp.dao.BookDAO;
import devatron.company.libraryapp.dao.SaleDAO;
import devatron.company.libraryapp.model.Book;
import devatron.company.libraryapp.dao.SaleDAO.SaleMonthAgg;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Pair;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class StatisticsController {

    // — GENERE —
    @FXML private PieChart pieChart;
    @FXML private TableView<Book> tableSoldBooks;
    @FXML private TableColumn<Book,String> colIsbn;
    @FXML private TableColumn<Book,String> colTitle;
    @FXML private TableColumn<Book,Integer> colSold;

    // — VENDITE MENSILI —
    @FXML private LineChart<String,Number> chartQty;
    @FXML private LineChart<String,Number> chartRevenue;
    @FXML private TableView<SaleMonthAgg> tableMonth;
    @FXML private TableColumn<SaleMonthAgg,String>  colMonth;
    @FXML private TableColumn<SaleMonthAgg,Integer> colMQty;
    @FXML private TableColumn<SaleMonthAgg,Double>  colRev;

    // — NAV & SEARCH —
    @FXML private TextField txtSearchBook;
    @FXML private Label     lblStatsTitle;
    @FXML private Button    btnBack;

    @FXML private BarChart<String,Number>  barChartBestseller;
    @FXML private PieChart                 pieChartRevenue;

    @FXML private CategoryAxis xAxisQty, xAxisRev, bestsellerXAxis;
    @FXML private NumberAxis   yAxisQty, yAxisRev, bestsellerYAxis;

    private final BookDAO bookDAO = new BookDAO();
    private final SaleDAO saleDAO = new SaleDAO();

    @FXML
    public void initialize() {
        applyTranslations();

        List<Book> all = bookDAO.getAllBooks();

        // 1) PieChart + top venduti
        var byGenre = new HashMap<String,Integer>();
        for (var b: all) byGenre.merge(
            b.getGenre().isBlank() ? Lang.get("stats.genre.none") : b.getGenre(),
            b.getSold(), Integer::sum);
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        byGenre.forEach((g,q) -> pieData.add(new PieChart.Data(g,q)));
        pieChart.setData(pieData);

        var top10 = all.stream()
                       .sorted(Comparator.comparingInt(Book::getSold).reversed())
                       .limit(10).toList();
        ObservableList<Book> obs = FXCollections.observableArrayList(top10);
        colIsbn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colSold.setCellValueFactory(new PropertyValueFactory<>("sold"));
        tableSoldBooks.setItems(obs);

        // ricerca live
        txtSearchBook.textProperty().addListener((o,oldv,newv)-> {
            if (newv.isBlank()) {
                tableSoldBooks.setItems(obs);
            } else {
                String lw = newv.toLowerCase();
                var filt = all.stream()
                    .filter(b->b.getTitle().toLowerCase().contains(lw)
                              || b.getIsbn().contains(lw))
                    .sorted(Comparator.comparingInt(Book::getSold).reversed())
                    .limit(10)
                    .collect(Collectors.toList());
                tableSoldBooks.setItems(FXCollections.observableArrayList(filt));
            }
        });

        // 2) vendite mensili
        loadMonthly();
        loadYearlyBestseller();
        loadRevenueByGenre();
    }

    private void loadMonthly() {
        List<SaleMonthAgg> data = saleDAO.getMonthlyStats();

        // tabella
        ObservableList<SaleMonthAgg> mObs = FXCollections.observableArrayList(data);
        colMonth.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().month().toString().substring(0,7)));
        colMQty .setCellValueFactory(d ->
            new SimpleIntegerProperty(d.getValue().qty()).asObject());
        colRev  .setCellValueFactory(d ->
            new SimpleDoubleProperty(d.getValue().revenue()).asObject());
        tableMonth.setItems(mObs);

        // grafico quantità
        var seriesQty = new XYChart.Series<String,Number>();
        data.forEach(rec->
            seriesQty.getData().add(
                new XYChart.Data<>(rec.month().toString().substring(0,7), rec.qty())));
        chartQty.getData().setAll(seriesQty);
        xAxisQty.setLabel( Lang.get("stats.monthly.col.month") );
        yAxisQty.setLabel( Lang.get("stats.monthly.col.qty") );
        chartQty.setTitle(  Lang.get("stats.monthly.qtyTitle") );
        xAxisRev.setLabel( Lang.get("stats.monthly.col.month") );
        yAxisRev.setLabel( Lang.get("stats.monthly.col.rev") );
        chartRevenue.setTitle( Lang.get("stats.monthly.revTitle") );

        bestsellerXAxis.setLabel( Lang.get("stats.bestseller.axis.title") );
        bestsellerYAxis.setLabel( Lang.get("stats.bestseller.axis.qty") );
        barChartBestseller.setTitle( Lang.get("stats.bestseller.chart.title") );

        

        // grafico ricavi
        var seriesRev = new XYChart.Series<String,Number>();
        data.forEach(rec->
            seriesRev.getData().add(
                new XYChart.Data<>(rec.month().toString().substring(0,7), rec.revenue())));
        chartRevenue.getData().setAll(seriesRev);
    }


    private void loadYearlyBestseller() {
        int currentYear = LocalDate.now().getYear();
        // SaleDAO ti restituisce lista di (title, qty)
        List<Pair<String,Integer>> top = saleDAO.getTopTitlesByYear(currentYear, 10);
        CategoryAxis xAxis = (CategoryAxis)barChartBestseller.getXAxis();
        NumberAxis   yAxis = (NumberAxis)barChartBestseller.getYAxis();
        xAxis.setLabel(Lang.get("stats.bestseller.axis.title"));
        yAxis.setLabel(Lang.get("stats.bestseller.axis.qty"));
        XYChart.Series<String,Number> series = new XYChart.Series<>();
        for (var rec : top) {
            series.getData().add(new XYChart.Data<>(rec.getKey(), rec.getValue()));
        }
        barChartBestseller.getData().setAll(series);
    }

    private void loadRevenueByGenre() {
        // SaleDAO ti restituisce lista di (genre, revenue)
        List<Pair<String,Double>> rev = saleDAO.getRevenueByGenre();
        ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
        for (var r : rev) {
            data.add(new PieChart.Data(r.getKey(), r.getValue()));
        }
        pieChartRevenue.setData(data);
    }


    private void applyTranslations() {
        lblStatsTitle .setText(Lang.get("stats.title"));
        txtSearchBook .setPromptText(Lang.get("stats.search.prompt"));
        btnBack       .setText(Lang.get("stats.button.back"));

        // colonna “Genere”
        colIsbn       .setText(Lang.get("stats.col.isbn"));
        colTitle      .setText(Lang.get("stats.col.title"));
        colSold       .setText(Lang.get("stats.col.sold"));

        // mensili
        colMonth      .setText(Lang.get("stats.monthly.col.month"));
        colMQty       .setText(Lang.get("stats.monthly.col.qty"));
        colRev        .setText(Lang.get("stats.monthly.col.rev"));
    }

    @FXML
    private void handleBack() {
        LibraryApp.switchToMainView();
    }
}
