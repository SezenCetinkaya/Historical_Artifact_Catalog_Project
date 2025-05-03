package org.ce216.artifactcatalog.historicalartifactcatalogproject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javafx.scene.image.ImageView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import javafx.scene.image.Image;



public class MainWindow extends Application {

    private List<Artifact> artifactList = new ArrayList<>();
    private TableView<Artifact> tableView = new TableView<>();
    private TextArea helpTextArea = new TextArea();
    private TextField idFilter = new TextField();
    private TextField nameFilter = new TextField();
    private TextField categoryFilter = new TextField();
    private TextField civilizationFilter = new TextField();
    private TextField locationFilter = new TextField();
    private TextField dateFilter = new TextField();
    private TextField placeFilter = new TextField();
    private TextField compositionFilter = new TextField();
    private ListView<String> categoryListView = new ListView<>();
    private ListView<String> civListView = new ListView<>();
    private ListView<String> tagListView = new ListView<>();
    private VBox categoryCheckBoxVBox = new VBox();
    private VBox civCheckBoxVBox = new VBox();
    private VBox tagCheckBoxVBox = new VBox();
    private String imageDirectoryPath = null;
    DatePicker startDateFilter = new DatePicker();
    DatePicker endDateFilter = new DatePicker();
    DatePicker datePicker =new DatePicker();
    Label selectedIdLabel = new Label("ID: All");
    Label selectedNameLabel = new Label("Name: All");
    Label selectedCategoryLabel = new Label("Category: All");
    Label selectedCivLabel = new Label("Civilization: All");
    Label selectedTagLabel = new Label("Tags: All");
    Label selectedLocationLabel = new Label("Location: All");
    Label selectedDateLabel = new Label("Date: All");
    Label selectedCurrentPlaceLabel = new Label("Current Place: All");
    Label selectedCompositionLabel = new Label("Composition: All");

    @Override
    public void start(Stage stage) {
        VBox vbox = new VBox();

        // Menu
        MenuBar menuBar = new MenuBar();
        Menu mfile = new Menu("File");
        Menu mhelp = new Menu("Help");
        Menu mview = new Menu("View");
        Menu mfunctions = new Menu("Operations");
        Menu mFilter = new Menu("Filter");

        MenuItem addImportFileButton = new MenuItem("Import File");
        MenuItem addExportFileButton = new MenuItem("Export File");
        MenuItem viewHelpItem = new MenuItem("View Help");
        MenuItem showTableItem = new MenuItem("Show Table");
        MenuItem createArtifactItem = new MenuItem("Create Artifact");
        MenuItem editArtifactItem = new MenuItem("Edit Artifact");
        MenuItem deleteArtifactItem = new MenuItem("Delete Artifact");
        MenuItem filterItem = new MenuItem("Filter");

        mfile.getItems().addAll(addExportFileButton, addImportFileButton);
        mhelp.getItems().add(viewHelpItem);
        mview.getItems().add(showTableItem);
        mfunctions.getItems().addAll(createArtifactItem, editArtifactItem, deleteArtifactItem);
        mFilter.getItems().addAll(filterItem);
        menuBar.getMenus().addAll(mfile, mhelp, mview, mfunctions, mFilter);

        // Import/Export actions
        addImportFileButton.setOnAction(e -> showImportOptions(stage));
        addExportFileButton.setOnAction(e -> exportFile(stage));

        //Filter area
        VBox filterPane = createFilterPane();

        filterPane.setVisible(false);
        filterPane.setManaged(false);

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
                reassignArtifactIDs();
                updateFilters();
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
        dateCol.setCellValueFactory(data -> {
            LocalDate discoveryDate = data.getValue().getDiscoveryDate();
            if (discoveryDate == null) {
                return new SimpleStringProperty("unknown"); // Tarih yoksa "unknown" göster
            }
            return new SimpleStringProperty(discoveryDate.toString()); // Tarih varsa, "yyyy-MM-dd" formatında göster
        });

        TableColumn<Artifact, String> placeCol = new TableColumn<>("Current Place");
        placeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCurrentPlace()));

        TableColumn<Artifact, String> compositionCol = new TableColumn<>("Composition");
        compositionCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getComposition()));

        tableView.getColumns().addAll(idCol, nameCol, categoryCol, civCol, locationCol, dateCol, placeCol, compositionCol);
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

        filterItem.setOnAction(e -> {
            if (filterPane.isVisible()) {
                filterPane.setVisible(false); // Hide the filter box
                filterPane.setManaged(false);
            } else {
                filterPane.setVisible(true);  // Görünür hale getir
                filterPane.setManaged(true);  // Alan kaplamasına izin ver
            }
        });

        HBox mainContent = new HBox();
        mainContent.getChildren().addAll(vbox, filterPane);
        HBox.setHgrow(stackPane, Priority.ALWAYS);

        createArtifactItem.setOnAction(e -> showCreateArtifactDialog(stage));

        vbox.getChildren().addAll(menuBar, stackPane);

        VBox.setVgrow(stackPane, Priority.ALWAYS);
        vbox.setMaxWidth(Double.MAX_VALUE);
        vbox.setMaxHeight(Double.MAX_VALUE);

        tableView.setMaxWidth(Double.MAX_VALUE);
        tableView.setMaxHeight(Double.MAX_VALUE);

        // StackPane ayarları
        stackPane.setMaxWidth(Double.MAX_VALUE);
        stackPane.setMaxHeight(Double.MAX_VALUE);

        // Main layout da genişleyebilsin
        mainContent.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(vbox, Priority.ALWAYS);

        Scene scene = new Scene(mainContent, 1200, 600);
        stage.setTitle("Historical Artifact Catalog");
        stage.setScene(scene);
        stage.show();

        //double click
        tableView.setRowFactory(tv -> {
            TableRow<Artifact> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Artifact rowData = row.getItem();
                    showArtifactDetailsDialog(rowData);
                }
            });
            return row;
        });

        loadDefaultArtifacts();
        File imageDir = new File("images");
        if (imageDir.exists() && imageDir.isDirectory()) {
            imageDirectoryPath = imageDir.getAbsolutePath();
        }

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
        TextField placeField = new TextField(artifact.getCurrentPlace());
        TextField compositionField = new TextField(artifact.getComposition());

        // Tarih alanı artık DatePicker
        DatePicker datePicker = new DatePicker();
        if (artifact.getDiscoveryDate() != null) {
            datePicker.setValue(artifact.getDiscoveryDate());
        }

        VBox vbox = new VBox(5);
        vbox.getChildren().addAll(
                new Label("Artifact Name:"), nameField,
                new Label("Category:"), categoryField,
                new Label("Civilization:"), civilizationField,
                new Label("Discovery Location:"), locationField,
                new Label("Discovery Date:"), datePicker,
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
                artifact.setCurrentPlace(placeField.getText());

                // DatePicker'dan alınan tarihi doğrudan kullanabiliriz
                LocalDate selectedDate = datePicker.getValue();
                artifact.setDiscoveryDate(selectedDate);
                artifact.setComposition(compositionField.getText());
                updateFilters();
                tableView.refresh(); // Tabloyu güncelle
            }
            return null;
        });

        dialog.showAndWait();
    }

    private VBox createFilterPane() {
        VBox filters = new VBox(5);
        idFilter.setPromptText("ID");
        nameFilter.setPromptText("Name");
        categoryListView = new ListView<>();
        categoryListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        categoryListView.setMaxHeight(100);
        civListView = new ListView<>();
        civListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        civListView.setMaxHeight(100);
        locationFilter.setPromptText("Location");
        dateFilter.setPromptText("Date");
        startDateFilter.setPromptText("Start Date");
        endDateFilter.setPromptText("End Date");
        placeFilter.setPromptText("Place");
        compositionFilter.setPromptText("Composition");

        categoryListView.getItems().setAll(
                artifactList.stream().map(Artifact::getCategory).distinct().collect(Collectors.toList())
        );
        civListView.getItems().setAll(
                artifactList.stream().map(Artifact::getCivilization).distinct().collect(Collectors.toList())
        );
        // For Category Filter
        categoryCheckBoxVBox.setSpacing(5);
        categoryCheckBoxVBox.setMaxHeight(100); // Adjust height as needed

        // Populate category checkboxes
        categoryListView.getItems().stream()
                .map(category -> new CheckBox(category))
                .forEach(categoryCheckBoxVBox.getChildren()::add);

        categoryListView.setMaxWidth(Double.MAX_VALUE);
        VBox categoryFilterBox = new VBox(10, new Label("Categories"), categoryCheckBoxVBox);

        // For Civilization Filter
        civCheckBoxVBox.setSpacing(5);
        civCheckBoxVBox.setMaxHeight(100); // Adjust height as needed

        // Populate civilization checkboxes
        civListView.getItems().stream()
                .map(civ -> new CheckBox(civ))
                .forEach(civCheckBoxVBox.getChildren()::add);

        civListView.setMaxWidth(Double.MAX_VALUE);
        VBox civFilterBox = new VBox(10, new Label("Civilizations"), civCheckBoxVBox);

        tagCheckBoxVBox.setSpacing(5);
        tagCheckBoxVBox.setMaxHeight(100);

        List<String> allTags=artifactList.stream()
                .flatMap(a -> a.getTags() != null ? a.getTags().stream() : java.util.stream.Stream.empty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        tagListView.getItems().setAll(allTags);
        allTags.stream()
                .map(tag -> new CheckBox(tag))
                .forEach(tagCheckBoxVBox.getChildren()::add);

        VBox tagFilterBox = new VBox(10, new Label("Tags"), tagCheckBoxVBox);


        Button applyFiltersButton = new Button("Apply Filters");
        Button clearFiltersButton = new Button("Clear Filters");

        HBox buttons = new HBox(8);
        buttons.getChildren().addAll(applyFiltersButton,clearFiltersButton);

        //Selected Filters Box
        VBox selectedFiltersBox = new VBox(8);
        selectedFiltersBox.setPadding(new Insets(10));
        selectedFiltersBox.setStyle("""
                -fx-background-color: #f9f9f9;
                -fx-border-color: #cccccc;
                -fx-background-radius: 10;
            """);

        Label selectedFiltersLabel = new Label("Selected Filters");
        selectedFiltersLabel.setStyle("""
        -fx-font-size: 16px;
        -fx-font-weight: bold;
        -fx-font-style: italic;
    """);

        selectedIdLabel.setText("ID: All");
        selectedNameLabel.setText("Name: All");
        selectedCategoryLabel.setText("Category: All");
        selectedCivLabel.setText("Civilization: All");
        selectedTagLabel.setText("Tag: All");
        selectedLocationLabel.setText("Location: All");
        selectedDateLabel.setText("Date: All");
        selectedCurrentPlaceLabel.setText("Current Place: All");
        selectedCompositionLabel.setText("Composition: All");

        selectedFiltersBox.getChildren().addAll(
                selectedFiltersLabel,
                selectedIdLabel,
                selectedNameLabel,
                selectedCategoryLabel,
                selectedCivLabel,
                selectedTagLabel,
                selectedLocationLabel,
                selectedDateLabel,
                selectedCurrentPlaceLabel,
                selectedCompositionLabel
        );

        // Hide Selected Filters Box
        selectedFiltersBox.setVisible(false);
        selectedFiltersBox.setManaged(false);

        //Close Button
        Button closeButton = new Button("X");
        closeButton.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-color: transparent;" +
                        "-fx-border-color: transparent;" +
                        "-fx-text-fill: black;"
        );

        Label filtersLabel = new Label("Filters: ");
        filtersLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        BorderPane headerPane = new BorderPane();
        headerPane.setLeft(filtersLabel);
        headerPane.setRight(closeButton);
        BorderPane.setAlignment(filtersLabel, Pos.CENTER_LEFT);
        BorderPane.setAlignment(closeButton, Pos.CENTER_RIGHT);

        ScrollPane filterScrollPane = new ScrollPane(filters);

        VBox filterPanel = new VBox(headerPane,selectedFiltersBox, filterScrollPane );
        filterScrollPane.setFitToHeight(true);
        filterScrollPane.setFitToWidth(true);

        applyFiltersButton.setOnAction(e -> {
            String selectedId = idFilter.getText();
            String selectedName = nameFilter.getText();

            // Get selected categories and civilizations
            List<String> selectedCategories = categoryCheckBoxVBox.getChildren().stream()
                    .filter(node -> node instanceof CheckBox && ((CheckBox) node).isSelected())
                    .map(node -> ((CheckBox) node).getText())
                    .collect(Collectors.toList());

            List<String> selectedCivs = civCheckBoxVBox.getChildren().stream()
                    .filter(node -> node instanceof CheckBox && ((CheckBox) node).isSelected())
                    .map(node -> ((CheckBox) node).getText())
                    .collect(Collectors.toList());

            String selectedLocation = locationFilter.getText();
            LocalDate selectedStartDate = startDateFilter.getValue();
            LocalDate selectedEndDate = endDateFilter.getValue();
            String selectedCurrentPlace = placeFilter.getText();
            String selectedComposition = compositionFilter.getText();

            List<String> selectedTags = tagCheckBoxVBox.getChildren().stream()
                    .filter(node -> node instanceof CheckBox && ((CheckBox) node).isSelected())
                    .map(node -> ((CheckBox) node).getText())
                    .collect(Collectors.toList());

            // Pass the selected categories and civilizations to the filter method
            applyFilters(
                    selectedId, selectedName, selectedCategories, selectedCivs, selectedTags,
                    selectedLocation, selectedStartDate, selectedEndDate,
                    selectedCurrentPlace, selectedComposition,
                    selectedIdLabel, selectedNameLabel, selectedCategoryLabel,
                    selectedCivLabel, selectedTagLabel, selectedLocationLabel, selectedDateLabel,
                    selectedCurrentPlaceLabel, selectedCompositionLabel
            );

            // Eğer en az bir filtre seçildiyse Selected Filters kutusunu göster
            boolean isFilterApplied = !selectedId.isEmpty() || !selectedName.isEmpty() ||
                    !selectedCategories.isEmpty() || !selectedCivs.isEmpty() || !selectedTags.isEmpty() ||
                    !selectedLocation.isEmpty() || selectedStartDate != null || selectedEndDate != null ||
                    !selectedCurrentPlace.isEmpty() || !selectedComposition.isEmpty();

            // Eğer filtreler uygulanmışsa kutuyu göster, yoksa gizle
            if (isFilterApplied) {
                selectedFiltersBox.setVisible(true);
                selectedFiltersBox.setManaged(true);
            } else {
                selectedFiltersBox.setVisible(false);
                selectedFiltersBox.setManaged(false);
            }
        });

        closeButton.setOnAction(e -> {
            filterPanel.setVisible(false);
            filterPanel.setManaged(false);
        });

        clearFiltersButton.setOnAction(e -> {
            idFilter.clear();
            nameFilter.clear();
            categoryListView.getSelectionModel().clearSelection();
            civListView.getSelectionModel().clearSelection();
            locationFilter.clear();
            startDateFilter.setValue(null);
            endDateFilter.setValue(null);
            placeFilter.clear();
            compositionFilter.clear();

            // CheckBox'ların seçimini temizle
            categoryCheckBoxVBox.getChildren().forEach(node -> {
                if (node instanceof CheckBox) {
                    ((CheckBox) node).setSelected(false);
                }
            });
            civCheckBoxVBox.getChildren().forEach(node -> {
                if (node instanceof CheckBox) {
                    ((CheckBox) node).setSelected(false);
                }
            });
            tagCheckBoxVBox.getChildren().forEach(node -> {
                if (node instanceof CheckBox) {
                    ((CheckBox) node).setSelected(false);
                }
            });

            // Tüm artifactleri geri yükle
            tableView.getItems().setAll(artifactList);
            tableView.refresh();

            // Selected Filters kutusunu gizle
            selectedFiltersBox.setVisible(false);
            selectedFiltersBox.setManaged(false);

            // Selected Filter Label'larını "All" yap gereksiz ama lazım olabilir
            selectedIdLabel.setText("ID: All");
            selectedNameLabel.setText("Name: All");
            selectedCategoryLabel.setText("Category: All");
            selectedCivLabel.setText("Civilization: All");
            selectedTagLabel.setText("Tags: All");
            selectedLocationLabel.setText("Location: All");
            selectedDateLabel.setText("Date: All");
            selectedCurrentPlaceLabel.setText("Current Place: All");
            selectedCompositionLabel.setText("Composition: All");
        });
        filters.getChildren().addAll(
                idFilter,nameFilter, categoryFilterBox, civFilterBox, tagFilterBox,
                locationFilter,  startDateFilter, endDateFilter, placeFilter,
                compositionFilter, buttons
        );

        filters.setPrefWidth(200);
        return filterPanel;
    }

    private void applyFilters(
            String selectedId, String selectedName, List<String> selectedCategories, List<String> selectedCivs,
            List<String> selectedTags, String selectedLocation, LocalDate selectedStartDate, LocalDate selectedEndDate,
            String selectedCurrentPlace, String selectedComposition,
            Label selectedIdLabel, Label selectedNameLabel, Label selectedCategoryLabel,
            Label selectedCivLabel, Label selectedTagLabel, Label selectedLocationLabel, Label selectedDateLabel,
            Label selectedCurrentPlaceLabel, Label selectedCompositionLabel
    ){
        List<Artifact> filtered = artifactList.stream()
                .filter(a -> selectedId.isEmpty() || String.valueOf(a.getArtifactId()).equals(selectedId))
                .filter(a -> a.getArtifactName().toLowerCase().contains(nameFilter.getText().toLowerCase()))
                .filter(a -> selectedCategories.isEmpty() || selectedCategories.contains(a.getCategory()))
                .filter(a -> selectedCivs.isEmpty() || selectedCivs.contains(a.getCivilization()))
                .filter(a -> selectedTags.isEmpty() || (a.getTags() != null && a.getTags().stream().anyMatch(selectedTags::contains)))
                .filter(a -> a.getDiscoveryLocation().toLowerCase().contains(locationFilter.getText().toLowerCase()))
                .filter(a -> {
                    try {
                        LocalDate discoveryDate = a.getDiscoveryDate();
                        if (selectedStartDate != null && selectedEndDate != null) {
                            return !discoveryDate.isBefore(selectedStartDate) && !discoveryDate.isAfter(selectedEndDate);
                        } else if (selectedStartDate != null) {
                            return !discoveryDate.isBefore(selectedStartDate);
                        } else if (selectedEndDate != null) {
                            return !discoveryDate.isAfter(selectedEndDate);
                        } else {
                            return true;
                        }
                    } catch (Exception ex) {
                        return false;
                    }
                })
                .filter(a -> a.getCurrentPlace().toLowerCase().contains(placeFilter.getText().toLowerCase()))
                .filter(a -> a.getComposition().toLowerCase().contains(compositionFilter.getText().toLowerCase()))
                .collect(Collectors.toList());

        tableView.getItems().setAll(filtered);
        tableView.refresh();
        // ID
        if (!selectedId.isEmpty()) {
            selectedIdLabel.setText("ID: " + selectedId);
            selectedIdLabel.setVisible(true);
            selectedIdLabel.setManaged(true);
        } else {
            selectedIdLabel.setVisible(false);
            selectedIdLabel.setManaged(false);
        }

        // Name
        if (!selectedName.isEmpty()) {
            selectedNameLabel.setText("Name: " + selectedName);
            selectedNameLabel.setVisible(true);
            selectedNameLabel.setManaged(true);
        } else {
            selectedNameLabel.setVisible(false);
            selectedNameLabel.setManaged(false);
        }

        // Category
        if (!selectedCategories.isEmpty()) {
            selectedCategoryLabel.setText("Category: " + String.join(", ", selectedCategories));
            selectedCategoryLabel.setVisible(true);
            selectedCategoryLabel.setManaged(true);
        } else {
            selectedCategoryLabel.setVisible(false);
            selectedCategoryLabel.setManaged(false);
        }

        // Civ
        if (!selectedCivs.isEmpty()) {
            selectedCivLabel.setText("Civilization: " + String.join(", ", selectedCivs));
            selectedCivLabel.setVisible(true);
            selectedCivLabel.setManaged(true);
        } else {
            selectedCivLabel.setVisible(false);
            selectedCivLabel.setManaged(false);
        }
        //Tag
        if (!selectedTags.isEmpty()) {
            selectedTagLabel.setText("Tags: " + String.join(", ", selectedTags));
            selectedTagLabel.setVisible(true);
            selectedTagLabel.setManaged(true);
        } else {
            selectedTagLabel.setVisible(false);
            selectedTagLabel.setManaged(false);
        }

        // Location
        if (!selectedLocation.isEmpty()) {
            selectedLocationLabel.setText("Location: " + selectedLocation);
            selectedLocationLabel.setVisible(true);
            selectedLocationLabel.setManaged(true);
        } else {
            selectedLocationLabel.setVisible(false);
            selectedLocationLabel.setManaged(false);
        }

        // Date
        if (selectedStartDate != null || selectedEndDate != null) {
            String dateRange = "";
            if (selectedStartDate != null && selectedEndDate != null) {
                dateRange = selectedStartDate + " - " + selectedEndDate;
            } else if (selectedStartDate != null) {
                dateRange = "From: " + selectedStartDate;
            } else {
                dateRange = "Until: " + selectedEndDate;
            }
            selectedDateLabel.setText("Date: " + dateRange);
            selectedDateLabel.setVisible(true);
            selectedDateLabel.setManaged(true);
        } else {
            selectedDateLabel.setVisible(false);
            selectedDateLabel.setManaged(false);
        }

        // Current Place
        if (!selectedCurrentPlace.isEmpty()) {
            selectedCurrentPlaceLabel.setText("Current Place: " + selectedCurrentPlace);
            selectedCurrentPlaceLabel.setVisible(true);
            selectedCurrentPlaceLabel.setManaged(true);
        } else {
            selectedCurrentPlaceLabel.setVisible(false);
            selectedCurrentPlaceLabel.setManaged(false);
        }

        // Composition
        if (!selectedComposition.isEmpty()) {
            selectedCompositionLabel.setText("Composition: " + selectedComposition);
            selectedCompositionLabel.setVisible(true);
            selectedCompositionLabel.setManaged(true);
        } else {
            selectedCompositionLabel.setVisible(false);
            selectedCompositionLabel.setManaged(false);
        }
    }
    private void showImportOptions(Stage stage) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Import Options");

        // Buton tiplerini ayarla
        ButtonType imageButtonType = new ButtonType("Import Image Folder", ButtonBar.ButtonData.OTHER);
        ButtonType jsonButtonType = new ButtonType("Import JSON File", ButtonBar.ButtonData.OTHER); // mavi olmasın
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().getButtonTypes().addAll(jsonButtonType, imageButtonType, cancelButtonType);

        // İçerik
        VBox content = new VBox(15); // Butonlar arası boşluk için artırıldı
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER_LEFT);
        content.getChildren().add(new Label("What would you like to import?"));

        dialog.getDialogPane().setContent(content);

        // CANCEL butonunu küçültmek için stil ver (CSS ile)
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(cancelButtonType);
        cancelButton.setStyle("-fx-font-size: 10px; -fx-padding: 3 7 3 7;");

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent()) {
            if (result.get() == jsonButtonType) {
                importFile(stage);
            } else if (result.get() == imageButtonType) {
                importImageFolder(stage);
            }
        }
    }

    private void importImageFolder(Stage stage) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Image Folder");

        File selectedDir = directoryChooser.showDialog(stage);
        if (selectedDir != null && selectedDir.isDirectory()) {
            File[] imageFiles = selectedDir.listFiles(file ->
                    file.getName().endsWith(".jpg") || file.getName().endsWith(".png"));

            if (imageFiles != null) {
                File outputDir = new File("images");
                if (!outputDir.exists()) {
                    outputDir.mkdir(); // Proje kökünde "images" klasörü yoksa oluştur
                }

                for (File imageFile : imageFiles) {
                    try {
                        File destinationFile = new File(outputDir, imageFile.getName());
                        Files.copy(imageFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("Image copied: " + destinationFile.getName());
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("Error copying image: " + imageFile.getName());
                    }
                }

                // Artık images dizini kalıcı oldu
                imageDirectoryPath = outputDir.getAbsolutePath(); // Artık program yeniden başladığında burayı kullan
            }
        }
    }

    private void importFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Files");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));

        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(stage);

        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            Set<Artifact> existingArtifacts = new HashSet<>(artifactList);

            for (File file : selectedFiles) {
                try {
                    ArtifactManager wrapper = mapper.readValue(file, ArtifactManager.class);
                    List<Artifact> importedArtifacts = wrapper.getArtifacts();

                    for (Artifact artifact : importedArtifacts) {
                        if (!existingArtifacts.contains(artifact)) {
                            artifactList.add(artifact);
                            existingArtifacts.add(artifact); // Güncel Set'e de ekle
                        }
                    }

                    System.out.println("Imported from: " + file.getName());

                } catch (IOException e) {
                    System.out.println("Failed to read JSON file: " + file.getName());
                    e.printStackTrace();
                }
            }


            tableView.getItems().setAll(artifactList);
            updateFilters();
            saveToDefault(); // 👈 Yeni verileri default.json'a kaydet

        } else {
            System.out.println("No files selected.");
        }
    }

    private void saveToDefault() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        ArtifactManager manager = new ArtifactManager();
        manager.setArtifacts(artifactList);

        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File("default.json"), manager);
            System.out.println("Saved artifacts to default.json");

        } catch (IOException e) {
            System.out.println("Failed to save to default.json");
            e.printStackTrace();
        }
    }


    private void loadDefaultArtifacts() {
        File defaultFile = new File("default.json");
        if (defaultFile.exists()) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            try {
                ArtifactManager wrapper = mapper.readValue(defaultFile, ArtifactManager.class);
                artifactList = wrapper.getArtifacts();
                tableView.getItems().setAll(artifactList);
                updateFilters();
                System.out.println("Loaded default artifacts: " + artifactList.size());

            } catch (IOException e) {
                System.out.println("Failed to load default.json");
                e.printStackTrace();
            }
        } else {
            System.out.println("No default.json found. Starting with empty list.");
            artifactList = new ArrayList<>();
        }
    }


    private void exportFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File fileToSave = fileChooser.showSaveDialog(stage);

        if (fileToSave != null) {
            ObjectMapper mapper = new ObjectMapper();

            // Java'nın local datei okuması için:
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            mapper.enable(SerializationFeature.INDENT_OUTPUT);

            ArtifactManager wrapper = new ArtifactManager();
            wrapper.setArtifacts(artifactList); // artifactList alınıyor

            try {
                mapper.writeValue(fileToSave, wrapper);
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
        DatePicker datePicker = new DatePicker();
        TextField placeField = new TextField();

        vbox.getChildren().addAll(
                new Label("Artifact Name:"), nameField,
                new Label("Category:"), categoryField,
                new Label("Civilization:"), civilizationField,
                new Label("Discovery Location:"), locationField,
                new Label("Discovery Date:"), datePicker,
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
                newArtifact.setDiscoveryDate(datePicker.getValue() != null ? datePicker.getValue() : newArtifact.getDiscoveryDate());
                newArtifact.setCurrentPlace(placeField.getText().isEmpty() ? newArtifact.getCurrentPlace() : placeField.getText());
                newArtifact.setComposition(compositionField.getText().isEmpty() ? newArtifact.getComposition() : compositionField.getText());
                artifactList.add(newArtifact);
                updateFilters();
                tableView.getItems().setAll(artifactList);
                return newArtifact;
            }
            return null;
        });

        dialog.showAndWait();
    }
    private void updateFilters() {
        // Update the category list in ListView
        categoryListView.getItems().setAll(
                artifactList.stream().map(Artifact::getCategory).distinct().collect(Collectors.toList())
        );

        // Update the civilization list in ListView
        civListView.getItems().setAll(
                artifactList.stream().map(Artifact::getCivilization).distinct().collect(Collectors.toList())
        );

        // Clear and repopulate the CheckBoxes in categoryCheckBoxVBox
        categoryCheckBoxVBox.getChildren().clear();
        categoryListView.getItems().stream()
                .map(category -> new CheckBox(category))
                .forEach(categoryCheckBoxVBox.getChildren()::add);

        // Clear and repopulate the CheckBoxes in civCheckBoxVBox
        civCheckBoxVBox.getChildren().clear();
        civListView.getItems().stream()
                .map(civ -> new CheckBox(civ))
                .forEach(civCheckBoxVBox.getChildren()::add);

        tagCheckBoxVBox.getChildren().clear();
        List<String> allTags = artifactList.stream()
                .flatMap(a -> a.getTags() != null ? a.getTags().stream() : java.util.stream.Stream.empty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        allTags.stream()
                .map(tag -> new CheckBox(tag))
                .forEach(tagCheckBoxVBox.getChildren()::add);
    }

    private void reassignArtifactIDs() {
        for (int i = 0; i < artifactList.size(); i++) {
            artifactList.get(i).setArtifactId(i + 1); // ID’ler 1’den başlasın
        }
    }

    private void showArtifactDetailsDialog(Artifact artifact) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Artifact Details");

        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType);

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        Label nameLabel = new Label("Name: " + artifact.getArtifactName());
        Label categoryLabel = new Label("Category: " + artifact.getCategory());
        Label civLabel = new Label("Civilization: " + artifact.getCivilization());
        Label locationLabel = new Label("Discovery Location: " + artifact.getDiscoveryLocation());
        Label dateLabel = new Label("Discovery Date: " + (artifact.getDiscoveryDate() != null ? artifact.getDiscoveryDate().toString() : "Unknown"));
        Label placeLabel = new Label("Current Place: " + artifact.getCurrentPlace());
        Label compositionLabel = new Label("Composition: " + artifact.getComposition());
        Label tagsLabel = new Label("Tags: " + (artifact.getTags() != null ? String.join(", ", artifact.getTags()) : "None"));

        // Görsel (imagePath üzerinden)
        ImageView imageView = new ImageView();
        if (artifact.getImagePath() != null && !artifact.getImagePath().isEmpty() && imageDirectoryPath != null) {
            try {
                File imageFile = new File(imageDirectoryPath, artifact.getImagePath()); // yeni yol
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString()); // toURI ile dış dosyadan yükle
                    imageView.setImage(image);
                    imageView.setFitWidth(300);
                    imageView.setPreserveRatio(true);
                } else {
                    content.getChildren().add(new Label("Image file not found: " + imageFile.getName()));
                }
            } catch (Exception e) {
                content.getChildren().add(new Label("Image could not be loaded."));
            }
        } else {
            content.getChildren().add(new Label("No image available."));
        }


        content.getChildren().addAll(
                nameLabel, categoryLabel, civLabel, locationLabel, dateLabel,
                placeLabel, compositionLabel, tagsLabel, imageView
        );

        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }
    
    public static void main(String[] args) {
        launch();
    }

}