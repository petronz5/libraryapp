module devatron.company.libraryapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens devatron.company.libraryapp to javafx.fxml;

    exports devatron.company.libraryapp;
}
