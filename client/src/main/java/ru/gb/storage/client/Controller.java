package ru.gb.storage.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import ru.gb.storage.message.AuthMessage;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    public PanelController panelController;
    private Network myNetwork;
    private String nick;
    private String pass;

    @FXML
    VBox leftPanel, rightPanel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        myNetwork = new Network(this);
        myNetwork.start();
    }

    public void btnExitAction(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void btnCopyAction(ActionEvent actionEvent) {
        PanelController leftPanContr = (PanelController) leftPanel.getProperties().get("contrLeft");
        PanelController rightPanContr = (PanelController) rightPanel.getProperties().get("contrRight");
        if (leftPanContr.getSelectedFileName() == null && rightPanContr.getSelectedFileName() == null ){
            Alert alert= new Alert(Alert.AlertType.ERROR, "File not select", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        PanelController scrPanContr = null, dstPanContr = null;
        if (leftPanContr.getSelectedFileName() != null){
            scrPanContr = leftPanContr;
            dstPanContr = rightPanContr;
        }
        if (rightPanContr.getSelectedFileName() != null){
            scrPanContr = rightPanContr;
            dstPanContr = leftPanContr;
        }

        Path srcPath = Paths.get(scrPanContr.getCurrentPath(),scrPanContr.getSelectedFileName());
        Path dstPath = Paths.get(dstPanContr.getCurrentPath()).resolve(srcPath.getFileName().toString());
            String srcPathStr = String.valueOf(srcPath);
            String dstPathStr = String.valueOf(dstPath);

        dstPanContr.updateList(Paths.get(dstPanContr.getCurrentPath()));

//        try {
//            Files.copy(srcPath,dstPath);
//            new ClientHandler(srcPath,dstPath);
//            dstPanContr.updateList(Paths.get(dstPanContr.getCurrentPath()));
//        } catch (IOException e) {
//            Alert alert= new Alert(Alert.AlertType.ERROR, "File is NOT Copy", ButtonType.OK);
//            alert.showAndWait();
//        }

        //Удаление и перемещение https://youtu.be/LILeZhSHf1k?t=5450

    }

    public void menuItemDialogLogin(ActionEvent actionEvent) {

        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Окно авторизации");
        dialog.setHeaderText("Введите Ваши Логин и Пароль:");
// Set the button types.
        ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);
// Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        TextField username = new TextField();
        username.setPromptText("Username");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");
        grid.add(new Label("Username:"), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(password, 1, 1);
        // Enable/Disable login button depending on whether a username was entered.
        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);
        // Do some validation (using the Java 8 lambda syntax).
        username.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty());
        });
        dialog.getDialogPane().setContent(grid);
        // Request focus on the username field by default.
        Platform.runLater(() -> username.requestFocus());
        // Convert the result to a username-password-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(username.getText(), password.getText());
            }
            return null;
        });
        Optional<Pair<String, String>> result = dialog.showAndWait();
        result.ifPresent(usernamePassword -> {
            nick = username.getText();
            pass = password.getText();
        });

        AuthMessage message = new AuthMessage();
        message.setLogin(nick);
        message.setPass(pass);
        myNetwork.auth(message);
    }

    public void closeAuth() {
    }

    public void errorAuth() {
    }
}
