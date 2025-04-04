module org.ce216.artifactcatalog.historicalartifactcatalogproject {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires java.desktop;

    opens org.ce216.artifactcatalog.historicalartifactcatalogproject to javafx.fxml;
    exports org.ce216.artifactcatalog.historicalartifactcatalogproject;
}