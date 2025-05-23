package org.ce216.artifactcatalog.historicalartifactcatalogproject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Application;
import javafx.beans.property.*;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class MainWindow extends Application {

    private List<Artifact> artifactList = new ArrayList<>();
    private TableView<Artifact> tableView = new TableView<>();
    private TextArea helpTextArea = new TextArea();
    private TextField dateFilter = new TextField();
    private TextField idFilter = new TextField();
    private TextField nameFilter = new TextField();
    private ListView<String> categoryListView = new ListView<>();
    private ListView<String> civListView = new ListView<>();
    private ListView<String> locListView = new ListView<>();
    private ListView<String> placeListView = new ListView<>();
    private ListView<String> tagListView = new ListView<>();
    private ListView<String> comListView = new ListView<>();
    private VBox categoryCheckBoxVBox = new VBox();
    private VBox civCheckBoxVBox = new VBox();
    private VBox locCheckBoxVBox = new VBox();
    private VBox placeCheckBoxVBox = new VBox();
    private VBox tagCheckBoxVBox = new VBox();
    private VBox comCheckBoxVBox = new VBox();
    private String imageDirectoryPath = null;
    DatePicker startDateFilter = new DatePicker();
    DatePicker endDateFilter = new DatePicker();
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

        mfile.getItems().addAll(addImportFileButton, addExportFileButton);
        mhelp.getItems().add(viewHelpItem);
        mview.getItems().add(showTableItem);
        mfunctions.getItems().addAll(createArtifactItem, editArtifactItem, deleteArtifactItem);
        mFilter.getItems().addAll(filterItem);
        menuBar.getMenus().addAll(mfile, mview, mfunctions, mFilter, mhelp);

        // Import/Export actions
        addImportFileButton.setOnAction(e -> showImportOptions(stage));
        addExportFileButton.setOnAction(e -> showExportOptions(stage));

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
                // Görseli sil
                String imagePath = selected.getImagePath();
                if (imagePath != null && !imagePath.isBlank()) {
                    File imageFile = new File("images", imagePath);
                    if (imageFile.exists()) {
                        imageFile.delete();
                    }
                }
                artifactList.remove(selected);
                updateFilters();
                saveToDefault();
                tableView.getItems().setAll(artifactList);
            } else {
                showAlert("Please select an artifact to delete.");
            }
        });

        // Table Columns
        TableColumn<Artifact, String> idCol = new TableColumn<>("ID");
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
    private void showExportOptions(Stage stage) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Export Options");

        ButtonType exportJsonType = new ButtonType("Export JSON File", ButtonBar.ButtonData.OTHER);
        ButtonType exportImagesType = new ButtonType("Export Pictures", ButtonBar.ButtonData.OTHER);
        ButtonType cancelType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().getButtonTypes().addAll(exportJsonType, exportImagesType, cancelType);

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        content.getChildren().add(new Label("What would you like to export?"));

        dialog.getDialogPane().setContent(content);

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent()) {
            if (result.get() == exportJsonType) {
                exportFile(stage);
            } else if (result.get() == exportImagesType) {
                exportImages(stage);
            }
        }
    }
    private void exportImages(Stage stage) {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Select Folder to Save Pictures");

        File targetDir = dirChooser.showDialog(stage);
        if (targetDir != null) {
            File imageSourceDir = new File("images");
            if (imageSourceDir.exists() && imageSourceDir.isDirectory()) {
                for (Artifact artifact : artifactList) {
                    String imagePath = artifact.getImagePath();
                    if (imagePath != null && !imagePath.isEmpty()) {
                        File sourceFile = new File(imageSourceDir, imagePath);
                        if (sourceFile.exists()) {
                            File destFile = new File(targetDir, sourceFile.getName());
                            try {
                                Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                System.out.println("Copied: " + destFile.getName());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                System.out.println("All images exported to: " + targetDir.getAbsolutePath());
            } else {
                System.out.println("No images to export.");
            }
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

        DatePicker datePicker = new DatePicker();
        if (artifact.getDiscoveryDate() != null) {
            datePicker.setValue(artifact.getDiscoveryDate());
        }

        Label imageLabel = new Label(artifact.getImagePath() != null ? artifact.getImagePath() : "No image selected");
        Button browseImageButton = new Button("Change Image");

        browseImageButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Image");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            File selectedFile = fileChooser.showOpenDialog(dialog.getOwner());

            if (selectedFile != null) {
                try {
                    // Uzantıyı al
                    String originalName = selectedFile.getName();
                    String extension = "";
                    int dotIndex = originalName.lastIndexOf('.');
                    if (dotIndex > 0 && dotIndex < originalName.length() - 1) {
                        extension = originalName.substring(dotIndex); // Örn: ".jpg"
                    }

                    // Yeni dosya adı: artifact ID + uzantı
                    String newFileName = artifact.getArtifactId() + extension;

                    // images klasörüne kaydet
                    File imagesDir = new File("images");
                    if (!imagesDir.exists()) {
                        imagesDir.mkdirs();
                    }

                    File destFile = new File(imagesDir, newFileName);

                    // Kopyala ve üzerine yaz
                    Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                    // Label'a yeni ad
                    imageLabel.setText(newFileName);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    imageLabel.setText("Image copy failed!");
                }
            }
        });

        VBox vbox = new VBox(5);
        vbox.getChildren().addAll(
                new Label("Artifact Name:"), nameField,
                new Label("Category:"), categoryField,
                new Label("Civilization:"), civilizationField,
                new Label("Discovery Location:"), locationField,
                new Label("Discovery Date:"), datePicker,
                new Label("Current Place:"), placeField,
                new Label("Composition:"), compositionField,
                new Label("Image Path:"), imageLabel, browseImageButton
        );

        dialog.getDialogPane().setContent(vbox);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                artifact.setArtifactName(nameField.getText());
                artifact.setCategory(categoryField.getText());
                artifact.setCivilization(civilizationField.getText());
                artifact.setDiscoveryLocation(locationField.getText());
                artifact.setCurrentPlace(placeField.getText());
                artifact.setDiscoveryDate(datePicker.getValue());
                artifact.setComposition(compositionField.getText());
                artifact.setImagePath(imageLabel.getText());

                updateFilters();
                tableView.refresh(); // Tabloyu güncelle
                saveToDefault();
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
        locListView = new ListView<>();
        locListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        locListView.setMaxHeight(100);
        dateFilter.setPromptText("Date");
        startDateFilter.setPromptText("Start Date");
        endDateFilter.setPromptText("End Date");
        placeListView = new ListView<>();
        placeListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        placeListView.setMaxHeight(100);
        comListView = new ListView<>();
        comListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        comListView.setMaxHeight(100);


        categoryListView.getItems().setAll(
                artifactList.stream().map(Artifact::getCategory).distinct().collect(Collectors.toList())
        );
        civListView.getItems().setAll(
                artifactList.stream().map(Artifact::getCivilization).distinct().collect(Collectors.toList())
        );
        locListView.getItems().setAll(
                artifactList.stream().map(Artifact::getDiscoveryLocation).distinct().collect(Collectors.toList())
        );
        placeListView.getItems().setAll(
                artifactList.stream().map(Artifact::getCurrentPlace).distinct().collect(Collectors.toList())
        );
        comListView.getItems().setAll(
                artifactList.stream().map(Artifact::getComposition).distinct().collect(Collectors.toList())
        );

        // For Category Filter
        categoryCheckBoxVBox.setSpacing(5);
        categoryCheckBoxVBox.setMaxHeight(100); // Adjust height as needed

        // Populate category checkboxes
        categoryListView.getItems().stream()
                .map(category -> new CheckBox(category))
                .forEach(categoryCheckBoxVBox.getChildren()::add);

        categoryListView.setMaxWidth(Double.MAX_VALUE);
        // For Civilization Filter
        civCheckBoxVBox.setSpacing(5);
        civCheckBoxVBox.setMaxHeight(100); // Adjust height as needed

        // Populate civilization checkboxes
        civListView.getItems().stream()
                .map(civ -> new CheckBox(civ))
                .forEach(civCheckBoxVBox.getChildren()::add);

        civListView.setMaxWidth(Double.MAX_VALUE);

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


        // For location Filter
        locCheckBoxVBox.setSpacing(5);
        locCheckBoxVBox.setMaxHeight(100); // Adjust height as needed

        // Populate location checkboxes
        locListView.getItems().stream()
                .map(location -> new CheckBox(location))
                .forEach(locCheckBoxVBox.getChildren()::add);
        locCheckBoxVBox.setMaxWidth(Double.MAX_VALUE);

        // For place Filter
        placeCheckBoxVBox.setSpacing(5);
        placeCheckBoxVBox.setMaxHeight(100); // Adjust height as needed

        // Populate place checkboxes
        placeListView.getItems().stream()
                .map(place -> new CheckBox(place))
                .forEach(placeCheckBoxVBox.getChildren()::add);
        placeCheckBoxVBox.setMaxWidth(Double.MAX_VALUE);

        // For composition Filter
        comCheckBoxVBox.setSpacing(5);
        comCheckBoxVBox.setMaxHeight(100); // Adjust height as needed

        // Populate composition checkboxes
        comListView.getItems().stream()
                .map(composition -> new CheckBox(composition))
                .forEach(comCheckBoxVBox.getChildren()::add);
        comCheckBoxVBox.setMaxWidth(Double.MAX_VALUE);

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
        filterScrollPane.setFitToHeight(true);
        filterScrollPane.setFitToWidth(true);

        VBox filterPanel = new VBox(headerPane, selectedFiltersBox, filterScrollPane);

        //filter özelliklerinin istendiğinde listelenmesi
        Accordion accordion = new Accordion();
        accordion.getPanes().addAll(
                new TitledPane("Categories", categoryCheckBoxVBox),
                new TitledPane("Civilizations", civCheckBoxVBox),
                new TitledPane("Compositions", comCheckBoxVBox),
                new TitledPane("Location", locCheckBoxVBox),
                new TitledPane("Place", placeCheckBoxVBox),
                new TitledPane("Tags", tagCheckBoxVBox)
        );

        TextField minWidthField = new TextField();
        minWidthField.setPromptText("Min Width (cm)");
        TextField maxWidthField = new TextField();
        maxWidthField.setPromptText("Max Width (cm)");

        TextField minLengthField = new TextField();
        minLengthField.setPromptText("Min Length (cm)");
        TextField maxLengthField = new TextField();
        maxLengthField.setPromptText("Max Length (cm)");

        TextField minHeightField = new TextField();
        minHeightField.setPromptText("Min Height (cm)");
        TextField maxHeightField = new TextField();
        maxHeightField.setPromptText("Max Height (cm)");

        TextField minWeightField = new TextField();
        minWeightField.setPromptText("Min Weight (kg)");
        TextField maxWeightField = new TextField();
        maxWeightField.setPromptText("Max Weight (kg)");
        VBox sizeFilterBox = new VBox(5,
                new Label("Width (cm)"), minWidthField, maxWidthField,
                new Label("Length (cm)"), minLengthField, maxLengthField,
                new Label("Height (cm)"), minHeightField, maxHeightField,
                new Label("Weight (kg)"), minWeightField, maxWeightField
        );

        VBox staticFilters = new VBox(10,
                idFilter, nameFilter, startDateFilter, endDateFilter, sizeFilterBox
        );
        filters.getChildren().addAll(staticFilters, accordion, buttons);

        applyFiltersButton.setOnAction(e -> {
            Double minWidth = parseDoubleOrNull(minWidthField.getText());
            Double maxWidth = parseDoubleOrNull(maxWidthField.getText());
            Double minLength = parseDoubleOrNull(minLengthField.getText());
            Double maxLength = parseDoubleOrNull(maxLengthField.getText());
            Double minHeight = parseDoubleOrNull(minHeightField.getText());
            Double maxHeight = parseDoubleOrNull(maxHeightField.getText());
            Double minWeight = parseDoubleOrNull(minWeightField.getText());
            Double maxWeight = parseDoubleOrNull(maxWeightField.getText());


            // Get selected categories and civilizations
            List<String> selectedCategories = categoryCheckBoxVBox.getChildren().stream()
                    .filter(node -> node instanceof CheckBox && ((CheckBox) node).isSelected())
                    .map(node -> ((CheckBox) node).getText())
                    .collect(Collectors.toList());

            List<String> selectedCivs = civCheckBoxVBox.getChildren().stream()
                    .filter(node -> node instanceof CheckBox && ((CheckBox) node).isSelected())
                    .map(node -> ((CheckBox) node).getText())
                    .collect(Collectors.toList());

            String selectedId = idFilter.getText();
            String selectedName = nameFilter.getText();
            LocalDate selectedStartDate = startDateFilter.getValue();
            LocalDate selectedEndDate = endDateFilter.getValue();

            List<String> selectedTags = tagCheckBoxVBox.getChildren().stream()
                    .filter(node -> node instanceof CheckBox && ((CheckBox) node).isSelected())
                    .map(node -> ((CheckBox) node).getText())
                    .collect(Collectors.toList());

            List<String> selectedLocation = locCheckBoxVBox.getChildren().stream()
                    .filter(node -> node instanceof CheckBox && ((CheckBox) node).isSelected())
                    .map(node -> ((CheckBox) node).getText())
                    .collect(Collectors.toList());

            List<String> selectedPlace = placeCheckBoxVBox.getChildren().stream()
                    .filter(node -> node instanceof CheckBox && ((CheckBox) node).isSelected())
                    .map(node -> ((CheckBox) node).getText())
                    .collect(Collectors.toList());

            List<String> selectedComposition = comCheckBoxVBox.getChildren().stream()
                    .filter(node -> node instanceof CheckBox && ((CheckBox) node).isSelected())
                    .map(node -> ((CheckBox) node).getText())
                    .collect(Collectors.toList());

            // Pass the selected categories and civilizations to the filter method
            applyFilters(
                    selectedId, selectedName, selectedCategories, selectedCivs, selectedTags,
                    selectedLocation, selectedStartDate, selectedEndDate,
                    selectedPlace, selectedComposition,
                    selectedIdLabel, selectedNameLabel, selectedCategoryLabel,
                    selectedCivLabel, selectedTagLabel, selectedLocationLabel, selectedDateLabel,
                    selectedCurrentPlaceLabel, selectedCompositionLabel,
                    minWidth, maxWidth, minLength, maxLength, minHeight, maxHeight, minWeight, maxWeight
            );

            // Eğer en az bir filtre seçildiyse Selected Filters kutusunu göster
            boolean isFilterApplied = !selectedId.isEmpty() || !selectedName.isEmpty() ||
                    !selectedCategories.isEmpty() || !selectedCivs.isEmpty() || !selectedTags.isEmpty() ||
                    !selectedLocation.isEmpty() || selectedStartDate != null || selectedEndDate != null ||
                    !selectedPlace.isEmpty() || !selectedComposition.isEmpty();

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
            locListView.getSelectionModel().clearSelection();
            startDateFilter.setValue(null);
            endDateFilter.setValue(null);
            placeListView.getSelectionModel().clearSelection();
            comListView.getSelectionModel().clearSelection();
            minWidthField.clear();
            maxWidthField.clear();
            minLengthField.clear();
            maxLengthField.clear();
            minHeightField.clear();
            maxHeightField.clear();
            minWeightField.clear();
            maxWeightField.clear();

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
            locCheckBoxVBox.getChildren().forEach(node -> {
                if (node instanceof CheckBox) {
                    ((CheckBox) node).setSelected(false);
                }
            });
            placeCheckBoxVBox.getChildren().forEach(node -> {
                if (node instanceof CheckBox) {
                    ((CheckBox) node).setSelected(false);
                }
            });
            comCheckBoxVBox.getChildren().forEach(node -> {
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

        filters.setPrefWidth(200);
        return filterPanel;
    }

    private void applyFilters(
            String selectedId, String selectedName, List<String> selectedCategories, List<String> selectedCivs,
            List<String> selectedTags, List<String> selectedLocation, LocalDate selectedStartDate, LocalDate selectedEndDate,
            List<String> selectedCurrentPlace, List<String> selectedComposition,
            Label selectedIdLabel, Label selectedNameLabel, Label selectedCategoryLabel,
            Label selectedCivLabel, Label selectedTagLabel, Label selectedLocationLabel, Label selectedDateLabel,
            Label selectedCurrentPlaceLabel, Label selectedCompositionLabel,
            Double minWidth, Double maxWidth, Double minLength, Double maxLength,
            Double minHeight, Double maxHeight, Double minWeight, Double maxWeight
    ){

        // Tarih aralığı kontrolü
        if (selectedStartDate != null && selectedEndDate != null && selectedEndDate.isBefore(selectedStartDate)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Date Range Error");
            alert.setHeaderText("Invalid Date Range");
            alert.setContentText("End date cannot be before start date.");
            alert.showAndWait();
            return; // Filtreleme işlemini durdur
        }

        List<Artifact> filtered = artifactList.stream()
                .filter(a -> selectedId.isEmpty() || String.valueOf(a.getArtifactId()).equals(selectedId))
                .filter(a -> a.getArtifactName().toLowerCase().contains(nameFilter.getText().toLowerCase())).filter(a -> selectedCategories.isEmpty() || selectedCategories.contains(a.getCategory()))
                .filter(a -> selectedCivs.isEmpty() || selectedCivs.contains(a.getCivilization()))
                .filter(a -> selectedTags.isEmpty() || (a.getTags() != null && a.getTags().stream().anyMatch(selectedTags::contains)))
                .filter(a -> selectedLocation.isEmpty() || selectedLocation.contains(a.getDiscoveryLocation()))
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
                .filter(a -> selectedCurrentPlace.isEmpty() || selectedCurrentPlace.contains(a.getCurrentPlace()))
                .filter(a -> selectedComposition.isEmpty() || selectedComposition.contains(a.getComposition()))
                .filter(a -> minWidth == null || a.getWidth() >= minWidth)
                .filter(a -> maxWidth == null || a.getWidth() <= maxWidth)
                .filter(a -> minLength == null || a.getLength() >= minLength)
                .filter(a -> maxLength == null || a.getLength() <= maxLength)
                .filter(a -> minHeight == null || a.getHeight() >= minHeight)
                .filter(a -> maxHeight == null || a.getHeight() <= maxHeight)
                .filter(a -> minWeight == null || a.getWeight() >= minWeight)
                .filter(a -> maxWeight == null || a.getWeight() <= maxWeight)
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
            selectedLocationLabel.setText("Location: " + String.join(", ", selectedLocation));
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
            selectedCurrentPlaceLabel.setText("Current Place: "+ String.join(", ", selectedCurrentPlace));
            selectedCurrentPlaceLabel.setVisible(true);
            selectedCurrentPlaceLabel.setManaged(true);
        } else {
            selectedCurrentPlaceLabel.setVisible(false);
            selectedCurrentPlaceLabel.setManaged(false);
        }

        // Composition
        if (!selectedComposition.isEmpty()) {
            selectedCompositionLabel.setText("Composition: " + String.join(", ", selectedComposition));
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

            Set<Artifact> existingArtifacts = new HashSet<>();

            for (File file : selectedFiles) {
                try {
                    ArtifactManager wrapper = mapper.readValue(file, ArtifactManager.class);
                    List<Artifact> importedArtifacts = wrapper.getArtifacts();

                    for (Artifact artifact : importedArtifacts) {
                        // Eğer artifactId null, boş veya "Unknown" ise üret
                        if (artifact.getArtifactId() == null || artifact.getArtifactId().equals("Unknown")) {
                            artifact.setArtifactId();
                        }

                        boolean alreadyExists = artifactList.stream()
                                .anyMatch(a -> a.getArtifactId().equals(artifact.getArtifactId()));

                        if (!alreadyExists) {
                            artifactList.add(artifact);
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
            saveToDefault();

        } else {
            System.out.println("No files selected.");
        }
    }

    private File getDefaultJsonFile() {
        String appData = System.getenv("APPDATA"); // Windows için AppData\Roaming
        File programDir = new File(appData, "HistoricalArtifactCatalog"); // Kendi klasörün
        if (!programDir.exists()) {
            programDir.mkdirs(); // Yoksa oluştur
        }
        return new File(programDir, "default.json"); // Oradaki default.json dosyası
    }
    private void saveToDefault() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        ArtifactManager manager = new ArtifactManager();
        manager.setArtifacts(artifactList);

        try {
            File defaultFile = getDefaultJsonFile(); // Değişiklik burada
            mapper.writerWithDefaultPrettyPrinter().writeValue(defaultFile, manager);
            System.out.println("Saved artifacts to default.json");

        } catch (IOException e) {
            System.out.println("Failed to save to default.json");
            e.printStackTrace();
        }
    }


    private void loadDefaultArtifacts() {
        File defaultFile = getDefaultJsonFile(); // Değişiklik burada
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

        Label imageLabel = new Label("No image selected");
        Button browseImageButton = new Button("Select Image");
        final File[] selectedImageFile = new File[1]; // dışarıdan erişim için array

        browseImageButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Image");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                selectedImageFile[0] = file;
                imageLabel.setText(file.getAbsolutePath());
            }
        });

        vbox.getChildren().addAll(
                new Label("Artifact Name:"), nameField,
                new Label("Category:"), categoryField,
                new Label("Civilization:"), civilizationField,
                new Label("Discovery Location:"), locationField,
                new Label("Discovery Date:"), datePicker,
                new Label("Current Place:"), placeField,
                new Label("Composition:"), compositionField,
                new Label("Image Path:"), imageLabel, browseImageButton
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
                newArtifact.setArtifactId();

                // artifactList'e ekleyip ID'sini oluşturduktan sonra kaydedilecek
                boolean alreadyExists = artifactList.stream()
                        .anyMatch(a -> a.getArtifactId().equals(newArtifact.getArtifactId()));

                if (!alreadyExists) {
                    artifactList.add(newArtifact);
                }
                
                saveToDefault(); // ID atanmış olur (eğer ID otomatikse)
                updateFilters();
                tableView.getItems().setAll(artifactList);

                // Eğer bir resim seçildiyse ve Artifact ID varsa kopyala
                if (selectedImageFile[0] != null && newArtifact.getArtifactId() != null) {
                    try {
                        String originalName = selectedImageFile[0].getName();
                        String extension = "";
                        int dotIndex = originalName.lastIndexOf('.');
                        if (dotIndex > 0 && dotIndex < originalName.length() - 1) {
                            extension = originalName.substring(dotIndex);
                        }

                        File imagesDir = new File("images");
                        if (!imagesDir.exists()) {
                            imagesDir.mkdirs();
                        }

                        String newFileName = newArtifact.getArtifactId() + extension;
                        File destFile = new File(imagesDir, newFileName);
                        Files.copy(selectedImageFile[0].toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                        newArtifact.setImagePath(newFileName);
                        saveToDefault(); // imagePath'i de kaydet
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }

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

        comCheckBoxVBox.getChildren().clear();
        List<String> compositions = artifactList.stream()
                .map(Artifact::getComposition)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .collect(Collectors.toList());

        compositions.stream()
                .map(CheckBox::new)
                .forEach(comCheckBoxVBox.getChildren()::add);

        locCheckBoxVBox.getChildren().clear();
        List<String> locations = artifactList.stream()
                .map(Artifact::getDiscoveryLocation)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .collect(Collectors.toList());

        locations.stream()
                .map(CheckBox::new)
                .forEach(locCheckBoxVBox.getChildren()::add);

        placeCheckBoxVBox.getChildren().clear();
        List<String> places = artifactList.stream()
                .map(Artifact::getCurrentPlace)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .collect(Collectors.toList());

        places.stream()
                .map(CheckBox::new)
                .forEach(placeCheckBoxVBox.getChildren()::add);

    }

    private void showArtifactDetailsDialog(Artifact artifact) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Artifact Details");

        // Sabit boyut ayarları
        dialog.getDialogPane().setMinWidth(400);
        dialog.getDialogPane().setMaxWidth(400);
        dialog.getDialogPane().setMinHeight(500);
        dialog.getDialogPane().setMaxHeight(500);
        dialog.setResizable(false); // Kullanıcı boyutlandıramasın

        ButtonType dummyClose = new ButtonType("", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(dummyClose);

       // Butonun görünmemesi için stil uygula
        Node closeButton = dialog.getDialogPane().lookupButton(dummyClose);
        if (closeButton != null) {
            closeButton.setVisible(false);
            closeButton.setManaged(false);
        }

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        Label nameLabel = new Label("Name: " + artifact.getArtifactName());
        Label categoryLabel = new Label("Category: " + artifact.getCategory());
        Label civLabel = new Label("Civilization: " + artifact.getCivilization());
        Label locationLabel = new Label("Discovery Location: " + artifact.getDiscoveryLocation());
        Label dateLabel = new Label("Discovery Date: " +
                (artifact.getDiscoveryDate() != null ? artifact.getDiscoveryDate().toString() : "Unknown"));
        Label placeLabel = new Label("Current Place: " + artifact.getCurrentPlace());
        Label compositionLabel = new Label("Composition: " + artifact.getComposition());
        Label tagsLabel = new Label("Tags: " +
                (artifact.getTags() != null ? String.join(", ", artifact.getTags()) : "None"));
        Label widthLabel = new Label("Width: " +
                (artifact.getWidth() > 0 ? artifact.getWidth() + " cm" : "Unknown"));
        Label lengthLabel = new Label("Length: " +
                (artifact.getLength() > 0 ? artifact.getLength() + " cm" : "Unknown"));
        Label heightLabel = new Label("Height: " +
                (artifact.getHeight() > 0 ? artifact.getHeight() + " cm" : "Unknown"));
        Label weightLabel = new Label("Weight: " +
                (artifact.getWeight() > 0 ? artifact.getWeight() + " kg" : "Unknown"));

        // Görsel (imagePath üzerinden)
        ImageView imageView = new ImageView();
        if (artifact.getImagePath() != null && !artifact.getImagePath().isEmpty() && imageDirectoryPath != null) {
            try {
                File imageFile = new File(imageDirectoryPath, artifact.getImagePath());
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    imageView.setImage(image);
                    imageView.setFitWidth(300); // Maksimum genişlik
                    imageView.setFitHeight(150); // Maksimum yükseklik
                    imageView.setPreserveRatio(true);
                    imageView.setSmooth(true);
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
                placeLabel, compositionLabel, widthLabel, lengthLabel, heightLabel, weightLabel, tagsLabel, imageView
        );

        dialog.getDialogPane().setContent(content);
        dialog.initModality(Modality.APPLICATION_MODAL); // ana pencereyi kilitle
        dialog.showAndWait();
    }

    private Double parseDoubleOrNull(String text) {
        try {
            if (text == null || text.trim().isEmpty()) {
                return null;
            }
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static void main(String[] args) {
        launch();
    }

}