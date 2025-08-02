package devatron.company.libraryapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class LibraryApp extends Application {
    private static Stage primaryStage;
    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        switchToLoginView();
        primaryStage.setTitle("Library App - Login");
        primaryStage.centerOnScreen();
        primaryStage.show();
    }
    public static void switchToLoginView() {
        try {
            FXMLLoader loader = new FXMLLoader(LibraryApp.class.getResource("/devatron/company/libraryapp/login-view.fxml"));
            Scene scene = new Scene(loader.load());
            primaryStage.setTitle("Library App - Login");
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void switchToMainView() {
        try {
            FXMLLoader loader = new FXMLLoader(LibraryApp.class.getResource("/devatron/company/libraryapp/main-view.fxml"));
            Scene scene = new Scene(loader.load());
            primaryStage.setTitle("Library Manager");
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
