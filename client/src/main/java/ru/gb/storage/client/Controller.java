package ru.gb.storage.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Controller {
    private final int PORT = 9000;
    private final String HOST = "localhost";

    private String nick;
    public void onSendButtonClick(ActionEvent actionEvent) {
    }

    @FXML
    VBox leftPanel, rightPanel;

    public void btnExitAction(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void btnCopyAction(ActionEvent actionEvent) {
        PanelController leftPanContr = (PanelController) leftPanel.getProperties().get("contr");
        PanelController rightPanContr = (PanelController) rightPanel.getProperties().get("contr");
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

        try {
            Files.copy(srcPath,dstPath);
            dstPanContr.updateList(Paths.get(dstPanContr.getCurrentPath()));
        } catch (IOException e) {
            Alert alert= new Alert(Alert.AlertType.ERROR, "File is NOT Copy", ButtonType.OK);
            alert.showAndWait();
        }

        //Удаление и перемещение https://youtu.be/LILeZhSHf1k?t=5450

    }
}
