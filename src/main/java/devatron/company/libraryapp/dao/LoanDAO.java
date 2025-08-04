package devatron.company.libraryapp.dao;

import devatron.company.libraryapp.DBUtil;
import devatron.company.libraryapp.model.Loan;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LoanDAO {

    public List<Loan> getActiveLoans() {
        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT l.*, b.title as book_title FROM loans l JOIN books b ON l.book_isbn = b.isbn WHERE l.returned = FALSE";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Loan loan = new Loan(
                    rs.getInt("id"),
                    rs.getString("book_isbn"),
                    rs.getString("book_title"),
                    rs.getString("user_name"),
                    rs.getString("user_surname"),
                    rs.getString("user_phone"),
                    rs.getDate("start_date").toLocalDate(),
                    rs.getDate("due_date").toLocalDate(),
                    rs.getBoolean("returned")
                );
                loans.add(loan);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return loans;
    }

    
    public List<Loan> getLoansDueOn(LocalDate date) {
        List<Loan> list = new ArrayList<>();
        String sql = """
            SELECT l.id,
                l.book_isbn,
                b.title AS book_title,
                l.user_name,
                l.user_surname,
                l.user_phone,
                l.start_date,
                l.due_date,
                l.returned
            FROM loans l
            JOIN books b ON l.book_isbn = b.isbn
            WHERE l.due_date = ?
            AND l.returned = FALSE
            ORDER BY l.due_date
        """;

        try (Connection c = DBUtil.getConnection();
            PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Loan(
                        rs.getInt("id"),
                        rs.getString("book_isbn"),           // prende il campo corretto
                        rs.getString("book_title"),          // titolo dal join
                        rs.getString("user_name"),
                        rs.getString("user_surname"),
                        rs.getString("user_phone"),
                        rs.getDate("start_date").toLocalDate(),
                        rs.getDate("due_date").toLocalDate(),
                        rs.getBoolean("returned")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void addLoan(Loan loan) {
        String sql = "INSERT INTO loans (book_isbn, user_name, user_surname, user_phone, start_date, due_date, returned) VALUES (?, ?, ?, ?, ?, ?, FALSE)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, loan.getBookIsbn());
            stmt.setString(2, loan.getUserName());
            stmt.setString(3, loan.getUserSurname());
            stmt.setString(4, loan.getUserPhone());
            stmt.setDate(5, Date.valueOf(loan.getStartDate()));
            stmt.setDate(6, Date.valueOf(loan.getDueDate()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void markAsReturned(int loanId) {
        String sql = "UPDATE loans SET returned = true WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
            PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, loanId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}