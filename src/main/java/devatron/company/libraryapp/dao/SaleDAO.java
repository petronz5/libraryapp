package devatron.company.libraryapp.dao;

import devatron.company.libraryapp.DBUtil;
import devatron.company.libraryapp.Lang;
import devatron.company.libraryapp.model.Sale;
import javafx.util.Pair;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SaleDAO {

    public void addSale(Sale s) {
        String sql = "INSERT INTO sales (isbn, qty, unit_price, sale_date) VALUES (?,?,?,?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString (1, s.isbn());
            ps.setInt    (2, s.qty());
            ps.setDouble (3, s.unitPrice());
            ps.setDate   (4, Date.valueOf(s.saleDate()));
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    /** Quantità e ricavi per mese (yyyy-MM). */
    public List<SaleMonthAgg> getMonthlyStats() {
        String sql = """
            SELECT date_trunc('month', sale_date) AS m,
                   SUM(qty)              AS total_qty,
                   SUM(qty*unit_price)   AS revenue
            FROM sales
            GROUP BY m
            ORDER BY m
        """;
        List<SaleMonthAgg> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new SaleMonthAgg(
                    rs.getDate("m").toLocalDate(),
                    rs.getInt ("total_qty"),
                    rs.getDouble("revenue")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }


    public List<Pair<String,Integer>> getTopTitlesByYear(int year, int limit) {
        List<Pair<String,Integer>> result = new ArrayList<>();
        String sql = """
            SELECT b.title, SUM(s.qty) AS total_qty
            FROM sales s
            JOIN books b ON s.isbn = b.isbn
            WHERE EXTRACT(YEAR FROM s.sale_date) = ?
            GROUP BY b.title
            ORDER BY total_qty DESC
            LIMIT ?
            """;
        try (Connection conn = DBUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, year);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String title = rs.getString("title");
                    int qty     = rs.getInt("total_qty");
                    result.add(new Pair<>(title, qty));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<Pair<String,Double>> getRevenueByGenre() {
        List<Pair<String,Double>> result = new ArrayList<>();
        String sql = """
            SELECT b.genre, SUM(s.qty * s.unit_price) AS revenue
            FROM sales s
            JOIN books b ON s.isbn = b.isbn
            GROUP BY b.genre
            """;
        try (Connection conn = DBUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String genre    = rs.getString("genre");
                double revenue  = rs.getDouble("revenue");
                // se il genere è vuoto, puoi sostituirlo con un placeholder:
                if (genre == null || genre.isBlank()) {
                    genre = Lang.get("stats.genre.unknown"); // definisci questa chiave nel JSON, es. "Senza genere"
                }
                result.add(new Pair<>(genre, revenue));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public record SaleMonthAgg(LocalDate month, int qty, double revenue) {}
}
