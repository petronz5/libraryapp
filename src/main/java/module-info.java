module devatron.company.libraryapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires java.prefs;

    opens devatron.company.libraryapp to javafx.fxml, com.fasterxml.jackson.databind;
    opens devatron.company.libraryapp.model to javafx.base; 

    exports devatron.company.libraryapp;
}