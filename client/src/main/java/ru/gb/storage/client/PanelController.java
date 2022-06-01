package ru.gb.storage.client;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class PanelController implements Initializable {


    @FXML
        TableView<FileInfo> filesTable;

        @FXML
        ComboBox <String> diskBox;

        @FXML
        TextField pathField;


    @Override
        public void initialize (URL location, ResourceBundle resources){
                TableColumn<FileInfo, String> fileTypeColumn = new TableColumn<>();
                fileTypeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getType().getName()));
                fileTypeColumn.setPrefWidth(20);

                TableColumn<FileInfo, String> fileNameColumn = new TableColumn<>(" Name ");
                fileNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileName()));
                fileNameColumn.setPrefWidth(280);

                TableColumn<FileInfo, Long> fileSizeColumn = new TableColumn<>(" Size ");
                fileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSize()));
                fileSizeColumn.setCellFactory(column -> {
                    return new TableCell<FileInfo, Long>() {
                        @Override
                        protected void updateItem(Long item, boolean empty) {
                            super.updateItem(item, empty);
                            if (item == null || empty) {
                                setText(null);
                                setStyle("");
                            } else {
                                String text = String.format("%,d bytes", item);
                                if (item == -1L) {
                                    text = "[DIR]";
                                }
                                setText(text);
                            }
                        }
                    };

                });
                fileSizeColumn.setPrefWidth(80);

                DateTimeFormatter datf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                TableColumn<FileInfo, String> fileDateColumn = new TableColumn<>(" Date mod. ");
                fileDateColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLastModified().format(datf)));
                fileDateColumn.setPrefWidth(120);

                filesTable.getColumns().addAll(fileTypeColumn, fileNameColumn, fileSizeColumn, fileDateColumn);
                filesTable.getSortOrder().add(fileTypeColumn);

                diskBox.getItems().clear();
                for (Path p : FileSystems.getDefault().getRootDirectories()) {
                    diskBox.getItems().add(p.toString());
                }
                diskBox.getSelectionModel().select(0);

                filesTable.setOnMouseClicked(new EventHandler<javafx.scene.input.MouseEvent>() {
                    @Override
                    public void handle(javafx.scene.input.MouseEvent event) {
                        if (event.getClickCount() == 2) {
                            Path path = Paths.get(pathField.getText()).resolve(filesTable.getSelectionModel().getSelectedItem().getFileName());
                            if (Files.isDirectory(path)) {
                                updateList(path);
                            }
                        }
                    }
                });
        diskBox.setVisible(false);
//            updateList((Path) Paths.get("."));
//            updateList((Path) Paths.get(myNetwork.getPathSrv()));
        }

        public void updateList(Path path){
            try {
                pathField.setText(path.normalize().toAbsolutePath().toString());
                filesTable.getItems().clear();
                filesTable.getItems().addAll(Files.list(path).map(FileInfo::new).collect(Collectors.toList()));
                filesTable.sort();
            } catch (IOException e){
                System.out.println("Not update File List");
//                Alert alert = new Alert(Alert.AlertType.WARNING, "Not update File List",ButtonType.OK);
//                alert.showAndWait();
            }
        }

        public void btnPathUpAction(ActionEvent actionEvent){
            Path upperPath = Paths.get(pathField.getText()).getParent();
            if(upperPath != null){
                updateList(upperPath);
            }
        }

        public String getSelectedFileName(){
            if (!filesTable.isFocused()){
                return null;
            }
            return filesTable.getSelectionModel().getSelectedItem().getFileName();
        }

        public String getCurrentPath(){
            return pathField.getText();
        }

        public void selectDiskAction(ActionEvent actionEvent) {
                ComboBox<String> element = (ComboBox<String>) actionEvent.getSource();
                updateList(Paths.get(element.getSelectionModel().getSelectedItem()));
        }
}
