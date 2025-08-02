module devatron.company.libraryapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens devatron.company.libraryapp to javafx.fxml, javafx.base;
    opens devatron.company.libraryapp.model to javafx.base; 

    exports devatron.company.libraryapp;
}