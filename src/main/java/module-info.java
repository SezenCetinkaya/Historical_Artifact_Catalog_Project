module org.ce216.artifactcatalog.historicalartifactcatalogproject {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires java.desktop;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;

    opens org.ce216.artifactcatalog.historicalartifactcatalogproject to javafx.fxml, com.fasterxml.jackson.databind;
    exports org.ce216.artifactcatalog.historicalartifactcatalogproject;
}