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
        Menu mfunctions=new Menu("Operations");
        MenuItem addImportFileButton = new MenuItem("Import File");
        MenuItem addExportFileButton = new MenuItem("Export File");
        MenuItem viewHelpItem = new MenuItem("View Help");
        MenuItem showTableItem = new MenuItem("Show Table");
        MenuItem createArtifactItem=new MenuItem("Create Artifact");

        mfile.getItems().addAll(addExportFileButton, addImportFileButton);
        mhelp.getItems().add(viewHelpItem);  // Help menüsünde View Help butonu
        mview.getItems().add(showTableItem);  // View menüsünde Show Table butonu
        mfunctions.getItems().addAll(createArtifactItem); //Operations menüsünde Create butonları
        menuBar.getMenus().addAll(mfile, mhelp, mview,mfunctions);

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


        createArtifactItem.setOnAction(e -> showCreateArtifactDialog(stage)); //yeni oluşturma işlemi için açılan diyalog paneli

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

    //Yeni artifact eklemek için açılan bilgileri yazma diyalog düzeni
    private void showCreateArtifactDialog(Stage stage){
        Dialog<Artifact> dialog = new Dialog<>();
        dialog.setTitle("Create New Artifact");
        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);//işlemi bitirmek için CREATE butonu
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);//işlemi iptal etmek için CANCEL butonu

        VBox vbox=new VBox(5);
        TextField nameField = new TextField();
        TextField categoryField = new TextField();
        TextField civilizationField = new TextField();
        TextField locationField = new TextField();
        TextField compositionField = new TextField();
        TextField dateField = new TextField();
        TextField placeField = new TextField();

        vbox.getChildren().addAll(
                new Label("Artifact Name:"),nameField,
                new Label("Category:"),categoryField,
                new Label("Civilization:"),civilizationField,
                new Label("Discovery Location:"),locationField,
                new Label("Discovery Date:"),dateField,
                new Label("Current Place:"),placeField,
                new Label("Composition:"),compositionField
        );
        dialog.getDialogPane().setContent(vbox);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton==createButtonType){
                Artifact newArtifact=new Artifact();
                newArtifact.setArtifactName(nameField.getText().isEmpty() ? newArtifact.getArtifactName() : nameField.getText());
                newArtifact.setCategory(categoryField.getText().isEmpty() ? newArtifact.getCategory() : categoryField.getText());
                newArtifact.setCivilization(civilizationField.getText().isEmpty() ? newArtifact.getCivilization() : civilizationField.getText());
                newArtifact.setDiscoveryLocation(locationField.getText().isEmpty() ? newArtifact.getDiscoveryLocation() : locationField.getText());
                newArtifact.setDiscoveryDate(dateField.getText().isEmpty() ? newArtifact.getDiscoveryDate() : dateField.getText());
                newArtifact.setCurrentPlace(placeField.getText().isEmpty() ? newArtifact.getCurrentPlace() : placeField.getText());
                newArtifact.setComposition(compositionField.getText().isEmpty() ? newArtifact.getComposition() : compositionField.getText());

                artifactList.add(newArtifact);
                tableView.getItems().setAll(artifactList);
                return newArtifact;
            }
            return null;
        });
        dialog.showAndWait();
    }

    public static void main(String[] args) {
        launch();
    }
}
