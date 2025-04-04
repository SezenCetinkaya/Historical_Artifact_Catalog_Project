package org.ce216.artifactcatalog.historicalartifactcatalogproject;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MainWindow extends Application {

    private List<Artifact> artifactList = new ArrayList<>();
    private TableView<Artifact> tableView = new TableView<>();
    private TextArea helpTextArea = new TextArea();

    @Override
    public void start(Stage stage) {
        VBox vbox = new VBox();
        HBox hbox = new HBox();

        // Menü Bar
        MenuBar menuBar = new MenuBar();
        Menu mfile = new Menu("File");
        Menu mhelp = new Menu("Help");
        Menu mview = new Menu("View");
        MenuItem addImportFileButton = new MenuItem("Import File");
        MenuItem addExportFileButton = new MenuItem("Export File");
        MenuItem viewHelpItem = new MenuItem("View Help");
        MenuItem showTableItem = new MenuItem("Show Table");

        mfile.getItems().addAll(addExportFileButton, addImportFileButton);
        mhelp.getItems().add(viewHelpItem);  // Help menüsünde View Help butonu
        mview.getItems().add(showTableItem);  // View menüsünde Show Table butonu
        menuBar.getMenus().addAll(mfile, mhelp, mview);

        addImportFileButton.setOnAction(e -> importFile(stage));
        addExportFileButton.setOnAction(e -> exportFile(stage));

        // Table Columns
        TableColumn<Artifact, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getArtifactId()));

        TableColumn<Artifact, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getArtifactName()));

        TableColumn<Artifact, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategory()));

        TableColumn<Artifact, String> civCol = new TableColumn<>("Civilization");
        civCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCivilization()));

        TableColumn<Artifact, String> locationCol = new TableColumn<>("Discovery Location");
        locationCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDiscoveryLocation()));

        TableColumn<Artifact, String> dateCol = new TableColumn<>("Discovery Date");
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDiscoveryDate()));

        TableColumn<Artifact, String> placeCol = new TableColumn<>("Current Place");
        placeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCurrentPlace()));

        tableView.getColumns().addAll(idCol, nameCol, categoryCol, civCol, locationCol, dateCol, placeCol);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Help TextArea
        helpTextArea.setEditable(false);
        helpTextArea.setWrapText(true);
        helpTextArea.setVisible(false);

        // StackPane to hold both the Table and Help TextArea
        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(tableView, helpTextArea); // Both TableView and Help TextArea added here.

        // View Help butonuna tıklanınca Yardım metnini göster
        viewHelpItem.setOnAction(e -> {
            helpTextArea.setText(loadHelpText()); // Yardım metnini yükle
            helpTextArea.setVisible(true);  // Yardım metnini göster
            tableView.setVisible(false);    // Tabloyu gizle
        });

        // Show Table butonuna tıklanınca tabloyu göster
        showTableItem.setOnAction(e -> {
            tableView.setVisible(true);    // Tabloyu göster
            helpTextArea.setVisible(false); // Yardım metnini gizle
        });

        vbox.getChildren().addAll(menuBar, stackPane); // StackPane'i VBox'a ekle

        Scene scene = new Scene(vbox, 1000, 500);
        stage.setTitle("Historical Artifact Catalog");
        stage.setScene(scene);
        stage.show();
    }



    private void importFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                ArtifactManager wrapper = mapper.readValue(selectedFile, ArtifactManager.class);
                artifactList = wrapper.getArtifacts();
                tableView.getItems().setAll(artifactList);
                System.out.println("Imported " + artifactList.size() + " artifacts.");
            } catch (IOException e) {
                System.out.println("Failed to read JSON file.");
                e.printStackTrace();
            }
        } else {
            System.out.println("No file selected.");
        }
    }

    private void exportFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File fileToSave = fileChooser.showSaveDialog(stage);

        if (fileToSave != null) {
            ObjectMapper mapper = new ObjectMapper();
            ArtifactManager wrapper = new ArtifactManager();
            wrapper.setArtifacts(artifactList);

            try {
                mapper.writerWithDefaultPrettyPrinter().writeValue(fileToSave, wrapper);
                System.out.println("Exported " + artifactList.size() + " artifacts to: " + fileToSave.getAbsolutePath());
            } catch (IOException e) {
                System.out.println("Error while saving the file.");
                e.printStackTrace();
            }
        } else {
            System.out.println("No file selected for export.");
        }
    }

    private String loadHelpText() {
        try (InputStream inputStream = getClass().getResourceAsStream("/Help.txt")) {
            if (inputStream == null) {
                return "Help.txt could not be found in resources.";
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return "Help content could not be loaded.";
        }
    }


    public static void main(String[] args) {
        launch();
    }
}
