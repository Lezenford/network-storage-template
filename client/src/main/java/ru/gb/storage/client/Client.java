package ru.gb.storage.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Client  extends Application {
    private final int PORT = 9000;
    private final String HOST = "localhost";

    public static void main(String[] args) {
        Application.launch(args);
    }

    public void start (Stage stage) throws Exception {

        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 620);
        stage.setTitle("MyDropRockerok");
        stage.setScene(scene);
        stage.setResizable(true);

        stage.show();
    }
}
