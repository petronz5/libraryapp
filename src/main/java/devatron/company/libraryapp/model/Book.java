package devatron.company.libraryapp.model;

public class Book {
    private String isbn;
    private String title;
    private String publisher;
    private String author;
    private int yearPublished;
    private double price;
    private String genre;
    private int quantity;
    private int sold;

    public Book(String isbn, String title, String publisher, String author, int yearPublished, double price, String genre, int quantity, int sold) {
        this.isbn = isbn;
        this.title = title;
        this.publisher = publisher;
        this.author = author;
        this.yearPublished = yearPublished;
        this.price = price;
        this.genre = genre;
        this.quantity = quantity;
        this.sold = sold;
    }

    public Book(String isbn, String title, String publisher, String author, int yearPublished, double price) {
        this(isbn, title, publisher, author, yearPublished, price, "", 0, 0);
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

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public int getSold() { return sold; }
    public void setSold(int sold) { this.sold = sold; }
}
