package devatron.company.libraryapp.model;

import java.time.LocalDate;

public class Loan {
    private int id;
    private String bookIsbn;
    private String bookTitle;
    private String userName;
    private String userSurname;
    private String userPhone;
    private LocalDate startDate;
    private LocalDate dueDate;
    private boolean returned;

    // Costruttori, getter e setter

    public Loan() {}

    public Loan(int id, String bookIsbn, String bookTitle, String userName, String userSurname, String userPhone, LocalDate startDate, LocalDate dueDate, boolean returned) {
        this.id = id;
        this.bookIsbn = bookIsbn;
        this.bookTitle = bookTitle;
        this.userName = userName;
        this.userSurname = userSurname;
        this.userPhone = userPhone;
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.returned = returned;
    }

    public int getId() { return id; }
    public String getBookIsbn() { return bookIsbn; }
    public String getBookTitle() { return bookTitle; }
    public String getUserName() { return userName; }
    public String getUserSurname() { return userSurname; }
    public String getUserPhone() { return userPhone; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getDueDate() { return dueDate; }
    public boolean isReturned() { return returned; }

    public void setId(int id) { this.id = id; }
    public void setBookIsbn(String bookIsbn) { this.bookIsbn = bookIsbn; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setUserSurname(String userSurname) { this.userSurname = userSurname; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public void setReturned(boolean returned) { this.returned = returned; }

    public String getUserFullName() {
        return userName + " " + userSurname;
    }
}