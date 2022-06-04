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
    private Network myNetwork;
    private boolean signUp;
    private String nick;
    private String pass;

    @FXML
    VBox leftPanel, rightPanel;

    @FXML
    Button btnCopy, btnMove, btnDel;

    public PanelController leftPanContr;
    public PanelController rightPanContr;

    public boolean isSignUp() {
        return signUp;
    }

    public void setSignUp(boolean signUp) {
        this.signUp = signUp;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        myNetwork = new Network(this);
        myNetwork.start();
    }

    public String getNick() {
        return nick;
    }

    public void btnExitAction(ActionEvent actionEvent) {
        if (myNetwork.sChannel.isActive()){
            myNetwork.sChannel.close();
        }
        Platform.exit();
    }

    public void authTrueReq(){
        btnCopy.setDisable(false);
        btnDel.setDisable(false);
        btnMove.setDisable(false);
    }

    public void updateListPanel(String pathRight, String pathLeft)  {
        try {
            rightPanContr = (PanelController) rightPanel.getProperties().get("control");
            rightPanContr.updateList(Path.of(pathRight));
            leftPanContr = (PanelController) leftPanel.getProperties().get("control");
            leftPanContr.updateList(Path.of(pathLeft));
        } catch (NullPointerException nullE){
//            System.out.println("Error rightPannelController and leftPannnelController");
            Alert alert= new Alert(Alert.AlertType.ERROR, "Error rightPannelController and leftPannnelController", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void btnCopyAction(ActionEvent actionEvent) {
        if (leftPanContr.getSelectedFileName() == null && rightPanContr.getSelectedFileName() == null ){
            Alert alert= new Alert(Alert.AlertType.ERROR, "File not select", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        PanelController scrPanContr = null; // from
        PanelController dstPanContr = null; // to
        if (leftPanContr.getSelectedFileName() != null){
            scrPanContr = leftPanContr;
            dstPanContr = rightPanContr;
//            System.out.println("file transfer to Local PC");
        }
        if (rightPanContr.getSelectedFileName() != null){
            scrPanContr = rightPanContr;
            dstPanContr = leftPanContr;
//            System.out.println("file transfer to Network");
        }
        Path srcPath = Paths.get(scrPanContr.getCurrentPath(),scrPanContr.getSelectedFileName());
        Path dstPath = Paths.get(dstPanContr.getCurrentPath()).resolve(srcPath.getFileName().toString());

        String srcPathStr = String.valueOf(srcPath);
        String dstPathStr = String.valueOf(dstPath);

        myNetwork.setPathFile(dstPathStr);

            if (!dstPath.toFile().exists()) {
                myNetwork.myCopyFile(srcPath);
            }else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "File transfer failed, because the file exists", ButtonType.OK);
                alert.showAndWait();
            }
            dstPanContr.updateList(Paths.get(dstPanContr.getCurrentPath()));
    }

    public void menuItemDialogLogin(ActionEvent actionEvent) {
//        try {
//            myNetwork.thread1.start();
//        }catch (NullPointerException ne){
//            Alert alert = new Alert(Alert.AlertType.ERROR,"Network is wrong. Please check yor network connection, or Server is stop",ButtonType.OK);
//            alert.showAndWait();
//        }catch (ConnectException ce){
//            Alert alert = new Alert(Alert.AlertType.ERROR,"Network is wrong. Please check yor network connection, or Server is stop",ButtonType.OK);
//            alert.showAndWait();
//        }
        if (myNetwork.sChannel != null){
            Dialog<Pair<String, String>> dialog = new Dialog<>();
            dialog.setTitle("Окно авторизации");
            dialog.setHeaderText("Введите Ваши Логин и Пароль:");
// Set the button types.
            ButtonType signUpButtonType = new ButtonType("SignUp", ButtonBar.ButtonData.OK_DONE);
            ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(signUpButtonType, loginButtonType, ButtonType.CANCEL);
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
            Node signUpButton = dialog.getDialogPane().lookupButton(signUpButtonType);
            signUpButton.setDisable(true);
            loginButton.setDisable(true);
            // Do some validation (using the Java 8 lambda syntax).
            username.textProperty().addListener((observable, oldValue, newValue) -> {
                loginButton.setDisable(newValue.trim().isEmpty());
                signUpButton.setDisable(newValue.trim().isEmpty());
            });
            dialog.getDialogPane().setContent(grid);
            // Request focus on the username field by default.
            Platform.runLater(() -> username.requestFocus());
            // Convert the result to a username-password-pair when the login button is clicked.
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == loginButtonType) {
                    setSignUp(false);
                    return new Pair<>(username.getText(), password.getText());
                }
                if (dialogButton == signUpButtonType) {
                    setSignUp(true);
                    return new Pair<>(username.getText(), password.getText());
                }
                return null;
            });
            Optional<Pair<String, String>> result = dialog.showAndWait();
            result.ifPresent(usernamePassword -> {
                nick = username.getText();
                pass = password.getText();
            });

            AuthMessage authMessage = new AuthMessage();
            authMessage.setSignUp(isSignUp());
            authMessage.setLogin(nick);
            authMessage.setPass(pass);
            myNetwork.auth(authMessage);
            //            System.out.println(authMessage);
        }
    }

    public void btnDelAction(ActionEvent actionEvent) {

    }
}
