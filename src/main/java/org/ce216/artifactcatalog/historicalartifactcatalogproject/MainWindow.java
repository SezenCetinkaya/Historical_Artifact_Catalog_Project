package org.ce216.artifactcatalog.historicalartifactcatalogproject;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainWindow extends Application {

    private List<Artifact> artifactList = new ArrayList<>(); // Class-level list

    @Override
    public void start(Stage stage) throws IOException {
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        MenuBar menuBar = new MenuBar();
        Menu mfile = new Menu("File");
        MenuItem addImportFileButton = new MenuItem("Import File");
        MenuItem addExportFileButton = new MenuItem("Export File");

        mfile.getItems().addAll(addExportFileButton, addImportFileButton);
        menuBar.getMenus().addAll(mfile);

        addImportFileButton.setOnAction(e -> importFile(stage));
        addExportFileButton.setOnAction(e -> exportFile(stage));

        vbox.getChildren().addAll(menuBar, hbox);

        Scene scene = new Scene(vbox, 400, 300);
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
                artifactList = wrapper.getArtifacts(); // Save to class-level list
                System.out.println("Imported " + artifactList.size() + " artifacts.");
                for (Artifact artifact : artifactList) {
                    System.out.println(artifact);
                }
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
            wrapper.setArtifacts(artifactList); // Put list into wrapper

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

    public static void main(String[] args) {
        launch();
    }
}