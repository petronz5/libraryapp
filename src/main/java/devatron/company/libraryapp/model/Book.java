package devatron.company.libraryapp.model;

public class Book {
    private String isbn;
    private String title;
    private String publisher;
    private String author;
    private int yearPublished;
    private double price;

    public Book(String isbn, String title, String publisher, String author, int yearPublished, double price) {
        this.isbn = isbn;
        this.title = title;
        this.publisher = publisher;
        this.author = author;
        this.yearPublished = yearPublished;
        this.price = price;
    }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public int getYearPublished() { return yearPublished; }
    public void setYearPublished(int yearPublished) { this.yearPublished = yearPublished; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}
