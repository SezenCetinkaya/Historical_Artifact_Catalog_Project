package org.ce216.artifactcatalog.historicalartifactcatalogproject;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
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

        MenuBar menuBar = createMenuBar(stage);
        VBox filterPane = createFilterPane();

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

        helpTextArea.setEditable(false);
        helpTextArea.setWrapText(true);
        helpTextArea.setVisible(false);

        StackPane stackPane = new StackPane(tableView, helpTextArea);

        HBox mainContent = new HBox();
        mainContent.getChildren().addAll(stackPane, filterPane);
        HBox.setHgrow(stackPane, Priority.ALWAYS);

        vbox.getChildren().addAll(menuBar, mainContent);

        Scene scene = new Scene(vbox, 1200, 600);
        stage.setTitle("Historical Artifact Catalog");
        stage.setScene(scene);
        stage.show();
    }

    private MenuBar createMenuBar(Stage stage) {
        MenuBar menuBar = new MenuBar();
        Menu mfile = new Menu("File");
        Menu mhelp = new Menu("Help");
        Menu mview = new Menu("View");
        Menu mfunctions = new Menu("Operations");

        MenuItem importItem = new MenuItem("Import File");
        MenuItem exportItem = new MenuItem("Export File");
        MenuItem helpItem = new MenuItem("View Help");
        MenuItem showTableItem = new MenuItem("Show Table");

        mfile.getItems().addAll(exportItem, importItem);
        mhelp.getItems().add(helpItem);
        mview.getItems().add(showTableItem);
        menuBar.getMenus().addAll(mfile, mhelp, mview, mfunctions);

        importItem.setOnAction(e -> importFile(stage));
        exportItem.setOnAction(e -> exportFile(stage));

        helpItem.setOnAction(e -> {
            helpTextArea.setText(loadHelpText());
            helpTextArea.setVisible(true);
            tableView.setVisible(false);
        });

        showTableItem.setOnAction(e -> {
            tableView.setVisible(true);
            helpTextArea.setVisible(false);
        });

        return menuBar;
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
            } catch (IOException e) {
                e.printStackTrace();
            }
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
            } catch (IOException e) {
                e.printStackTrace();
            }
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