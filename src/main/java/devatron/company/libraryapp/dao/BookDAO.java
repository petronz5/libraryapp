package devatron.company.libraryapp.dao;
import devatron.company.libraryapp.DBUtil;
import devatron.company.libraryapp.model.Book;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class BookDAO {

    // CREATE (INSERT)
    public void addBook(Book book) {
        String sql = "INSERT INTO books (isbn, title, publisher, author, year_published, price) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, book.getIsbn());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getPublisher());
            stmt.setString(4, book.getAuthor());
            stmt.setInt(5, book.getYearPublished());
            stmt.setDouble(6, book.getPrice());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // READ (SELECT)
    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT isbn, title, publisher, author, year_published, price FROM books ORDER BY title ASC";
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Book book = new Book(
                        rs.getString("isbn"),
                        rs.getString("title"),
                        rs.getString("publisher"),
                        rs.getString("author"),
                        rs.getInt("year_published"),
                        rs.getDouble("price")
                );
                books.add(book);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }

    // SEARCH
    public List<Book> searchBooks(String keyword) {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT isbn, title, publisher, author, year_published, price " +
                "FROM books " +
                "WHERE isbn ILIKE ? OR title ILIKE ? OR publisher ILIKE ? OR author ILIKE ? " +
                "ORDER BY title ASC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String like = "%" + keyword + "%";
            stmt.setString(1, like);
            stmt.setString(2, like);
            stmt.setString(3, like);
            stmt.setString(4, like);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Book book = new Book(
                            rs.getString("isbn"),
                            rs.getString("title"),
                            rs.getString("publisher"),
                            rs.getString("author"),
                            rs.getInt("year_published"),
                            rs.getDouble("price")
                    );
                    books.add(book);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }

    // UPDATE
    public void updateBook(Book book) {
        String sql = "UPDATE books SET title=?, publisher=?, author=?, year_published=?, price=? WHERE isbn=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getPublisher());
            stmt.setString(3, book.getAuthor());
            stmt.setInt(4, book.getYearPublished());
            stmt.setDouble(5, book.getPrice());
            stmt.setString(6, book.getIsbn());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // DELETE
    public void deleteBook(String isbn) {
        String sql = "DELETE FROM books WHERE isbn=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, isbn);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
