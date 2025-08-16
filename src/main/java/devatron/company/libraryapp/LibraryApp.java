package devatron.company.libraryapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.input.KeyCombination; // <--- aggiunta
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import java.io.IOException;

public class LibraryApp extends Application {
    private static Stage primaryStage;
    @Override
    public void start(Stage stage) throws IOException {
        String saved = SettingsManager.loadLang();
        Lang.load(saved);
        primaryStage = stage;

        // Creiamo UNA sola Scene vuota e la impostiamo subito: poi cambieremo solo il root
        Scene scene = new Scene(new Pane(), 800, 600);
        primaryStage.setScene(scene);

        // Impedisci uscita accidentale dal fullscreen
        primaryStage.setFullScreenExitHint("");
        primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);

        // Metti fullscreen UNA SOLA VOLTA (all'avvio)
        primaryStage.setFullScreen(true);

        String sessionEmail = SessionManager.getValidSessionEmail();

        if (sessionEmail != null) {
            setRootFromFXML("/devatron/company/libraryapp/main-view.fxml");
        } else {
            setRootFromFXML("/devatron/company/libraryapp/login-view.fxml");
        }

        primaryStage.show();
    }

    // Helper che sostituisce solo il root della Scene (evita di cambiare la Scene stessa)
    private static void setRootFromFXML(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(LibraryApp.class.getResource(fxmlPath));
            primaryStage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void switchToLoginView() {
        // non usare setScene, usa il helper
        setRootFromFXML("/devatron/company/libraryapp/login-view.fxml");
        primaryStage.setTitle("Library App - Login");
    }

    public static void switchToMainView() {
        // non usare setScene, usa il helper
        setRootFromFXML("/devatron/company/libraryapp/main-view.fxml");
        primaryStage.setTitle("Library Manager");
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}