package org.ce216.artifactcatalog.historicalartifactcatalogproject;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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
import java.util.stream.Collectors;

public class MainWindow extends Application {

    private List<Artifact> artifactList = new ArrayList<>();
    private TableView<Artifact> tableView = new TableView<>();
    private TextArea helpTextArea = new TextArea();

    private TextField nameFilter = new TextField();
    private TextField categoryFilter = new TextField();
    private TextField civilizationFilter = new TextField();
    private TextField locationFilter = new TextField();
    private TextField dateFilter = new TextField();
    private TextField placeFilter = new TextField();
    private TextField compositionFilter = new TextField();

    @Override
    public void start(Stage stage) {
        VBox vbox = new VBox();

        // Menu
        MenuBar menuBar = new MenuBar();
        Menu mfile = new Menu("File");
        Menu mhelp = new Menu("Help");
        Menu mview = new Menu("View");
        Menu mfunctions = new Menu("Operations");

        MenuItem addImportFileButton = new MenuItem("Import File");
        MenuItem addExportFileButton = new MenuItem("Export File");
        MenuItem viewHelpItem = new MenuItem("View Help");
        MenuItem showTableItem = new MenuItem("Show Table");
        MenuItem createArtifactItem = new MenuItem("Create Artifact");
        MenuItem editArtifactItem = new MenuItem("Edit Artifact");
        MenuItem deleteArtifactItem = new MenuItem("Delete Artifact");

        mfile.getItems().addAll(addExportFileButton, addImportFileButton);
        mhelp.getItems().add(viewHelpItem);
        mview.getItems().add(showTableItem);
        mfunctions.getItems().addAll(createArtifactItem, editArtifactItem, deleteArtifactItem);
        menuBar.getMenus().addAll(mfile, mhelp, mview, mfunctions);



        // Import/Export actions
        addImportFileButton.setOnAction(e -> importFile(stage));
        addExportFileButton.setOnAction(e -> exportFile(stage));


        VBox filterPane = createFilterPane();

        editArtifactItem.setOnAction(e -> {
            Artifact selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showEditArtifactDialog(selected);
            } else {
                showAlert("Please select an artifact to edit.");
            }
        });

        deleteArtifactItem.setOnAction(e -> {
            Artifact selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                artifactList.remove(selected);
                reassignArtifactIds();
                tableView.getItems().setAll(artifactList);
            } else {
                showAlert("Please select an artifact to delete.");
            }
        });

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

        // Help area
        helpTextArea.setEditable(false);
        helpTextArea.setWrapText(true);
        helpTextArea.setVisible(false);

        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(tableView, helpTextArea);

        viewHelpItem.setOnAction(e -> {
            helpTextArea.setText(loadHelpText());
            helpTextArea.setVisible(true);
            tableView.setVisible(false);
        });

        showTableItem.setOnAction(e -> {
            tableView.setVisible(true);
            helpTextArea.setVisible(false);
        });



        HBox mainContent = new HBox();
        mainContent.getChildren().addAll(stackPane, filterPane);
        HBox.setHgrow(stackPane, Priority.ALWAYS);

        createArtifactItem.setOnAction(e -> showCreateArtifactDialog(stage));

        vbox.getChildren().addAll(menuBar, mainContent, stackPane);


        Scene scene = new Scene(vbox, 1200, 600);
        stage.setTitle("Historical Artifact Catalog");
        stage.setScene(scene);
        stage.show();
    }

    private void reassignArtifactIds() {
        for (int i = 0; i < artifactList.size(); i++) {
            artifactList.get(i).setArtifactId(i + 1);
        }
    }



    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showEditArtifactDialog(Artifact artifact) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Edit Artifact");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        TextField nameField = new TextField(artifact.getArtifactName());
        TextField categoryField = new TextField(artifact.getCategory());
        TextField civilizationField = new TextField(artifact.getCivilization());
        TextField locationField = new TextField(artifact.getDiscoveryLocation());
        TextField dateField = new TextField(artifact.getDiscoveryDate());
        TextField placeField = new TextField(artifact.getCurrentPlace());
        TextField compositionField = new TextField(artifact.getComposition());

        VBox vbox = new VBox(5);
        vbox.getChildren().addAll(
                new Label("Artifact Name:"), nameField,
                new Label("Category:"), categoryField,
                new Label("Civilization:"), civilizationField,
                new Label("Discovery Location:"), locationField,
                new Label("Discovery Date:"), dateField,
                new Label("Current Place:"), placeField,
                new Label("Composition:"), compositionField
        );

        dialog.getDialogPane().setContent(vbox);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                artifact.setArtifactName(nameField.getText());
                artifact.setCategory(categoryField.getText());
                artifact.setCivilization(civilizationField.getText());
                artifact.setDiscoveryLocation(locationField.getText());
                artifact.setDiscoveryDate(dateField.getText());
                artifact.setCurrentPlace(placeField.getText());
                artifact.setComposition(compositionField.getText());
                tableView.refresh();
            }
            return null;
        });

        dialog.showAndWait();
    }

    private VBox createFilterPane() {
        VBox filters = new VBox(5);
        nameFilter.setPromptText("Name");
        categoryFilter.setPromptText("Category");
        civilizationFilter.setPromptText("Civilization");
        locationFilter.setPromptText("Location");
        dateFilter.setPromptText("Date");
        placeFilter.setPromptText("Place");
        compositionFilter.setPromptText("Composition");

        Button applyFiltersButton = new Button("Apply Filters");
        applyFiltersButton.setOnAction(e -> applyFilters());

        filters.getChildren().addAll(
                new Label("Filters:"),
                nameFilter, categoryFilter, civilizationFilter,
                locationFilter, dateFilter, placeFilter,
                compositionFilter, applyFiltersButton
        );
        filters.setPrefWidth(200);
        return filters;
    }

    private void applyFilters() {
        List<Artifact> filtered = artifactList.stream()
                .filter(a -> a.getArtifactName().toLowerCase().contains(nameFilter.getText().toLowerCase()))
                .filter(a -> a.getCategory().toLowerCase().contains(categoryFilter.getText().toLowerCase()))
                .filter(a -> a.getCivilization().toLowerCase().contains(civilizationFilter.getText().toLowerCase()))
                .filter(a -> a.getDiscoveryLocation().toLowerCase().contains(locationFilter.getText().toLowerCase()))
                .filter(a -> a.getDiscoveryDate().toLowerCase().contains(dateFilter.getText().toLowerCase()))
                .filter(a -> a.getCurrentPlace().toLowerCase().contains(placeFilter.getText().toLowerCase()))
                .filter(a -> a.getComposition().toLowerCase().contains(compositionFilter.getText().toLowerCase()))
                .collect(Collectors.toList());
        tableView.getItems().setAll(filtered);
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

    private void showCreateArtifactDialog(Stage stage) {
        Dialog<Artifact> dialog = new Dialog<>();
        dialog.setTitle("Create New Artifact");
        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        VBox vbox = new VBox(5);
        TextField nameField = new TextField();
        TextField categoryField = new TextField();
        TextField civilizationField = new TextField();
        TextField locationField = new TextField();
        TextField compositionField = new TextField();
        TextField dateField = new TextField();
        TextField placeField = new TextField();

        vbox.getChildren().addAll(
                new Label("Artifact Name:"), nameField,
                new Label("Category:"), categoryField,
                new Label("Civilization:"), civilizationField,
                new Label("Discovery Location:"), locationField,
                new Label("Discovery Date:"), dateField,
                new Label("Current Place:"), placeField,
                new Label("Composition:"), compositionField
        );

        dialog.getDialogPane().setContent(vbox);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                Artifact newArtifact = new Artifact();
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

    // 🔍 SEARCH METHOD — En sonda duruyor!
    private List<Artifact> searchArtifacts(String query) {
        List<Artifact> result = new ArrayList<>();
        for (Artifact artifact : artifactList) {
            if ((artifact.getArtifactName() != null && artifact.getArtifactName().toLowerCase().contains(query.toLowerCase())) ||
                    (artifact.getCategory() != null && artifact.getCategory().toLowerCase().contains(query.toLowerCase())) ||
                    (artifact.getCivilization() != null && artifact.getCivilization().toLowerCase().contains(query.toLowerCase())) ||
                    (artifact.getDiscoveryLocation() != null && artifact.getDiscoveryLocation().toLowerCase().contains(query.toLowerCase())) ||
                    (artifact.getDiscoveryDate() != null && artifact.getDiscoveryDate().toLowerCase().contains(query.toLowerCase())) ||
                    (artifact.getCurrentPlace() != null && artifact.getCurrentPlace().toLowerCase().contains(query.toLowerCase())) ||
                    (artifact.getComposition() != null && artifact.getComposition().toLowerCase().contains(query.toLowerCase()))) {
                result.add(artifact);
            }
        }
        return result;
    }
}
