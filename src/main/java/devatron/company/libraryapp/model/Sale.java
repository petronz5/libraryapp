package devatron.company.libraryapp.model;

import java.time.LocalDate;

public record Sale(
    int id,
    String isbn,
    int qty,
    double unitPrice,
    LocalDate saleDate
) {}
