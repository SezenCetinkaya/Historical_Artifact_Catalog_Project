package org.ce216.artifactcatalog.historicalartifactcatalogproject;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class Artifact {
    private static int idCounter=1;
    private String artifactId;
    private String artifactName;
    private String category;
    private String civilization;
    private String discoveryLocation;
    private String composition;
    private LocalDate discoveryDate;
    private String currentPlace;
    private double width, length, height, weight;
    private List<String> tags;
    private String imagePath;

    public Artifact() {
        this.artifactId="Unknown";
        this.artifactName = "Unknown";
        this.category = "Unknown";
        this.civilization = "Unknown";
        this.discoveryLocation = "Unknown";
        this.composition = "Unknown";
        this.discoveryDate = null;
        this.currentPlace = "Unknown";
    }


    public Artifact(String artifactName, String category, String civilization,
                    String discoveryLocation, String composition, LocalDate discoveryDate, String currentPlace,
                    double width, double length, double height, double weight, List<String> tags) {
        this.artifactName = artifactName;
        this.category = category;
        this.civilization = civilization;
        this.discoveryLocation = discoveryLocation;
        this.composition = composition;
        this.discoveryDate = discoveryDate;
        this.currentPlace = currentPlace;
        this.width = width;
        this.length = length;
        this.height = height;
        this.weight = weight;
        this.tags = tags;
    }
    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId() {
        if (discoveryDate == null || discoveryLocation == null || artifactName == null) {
            this.artifactId = "Unknown-" + idCounter++;
            return;
        }

        String shortName = artifactName.length() <= 3 ? artifactName : artifactName.substring(0, 3);
        this.artifactId = discoveryDate.toString() + discoveryLocation + shortName;
    }


    public String getArtifactName() {
        return artifactName;
    }

    public void setArtifactName(String artifactName) {
        this.artifactName = artifactName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCivilization() {
        return civilization;
    }

    public void setCivilization(String civilization) {
        this.civilization = civilization;
    }

    public String getDiscoveryLocation() {
        return discoveryLocation;
    }

    public void setDiscoveryLocation(String discoveryLocation) {
        this.discoveryLocation = discoveryLocation;
    }

    public String getComposition() {
        return composition;
    }

    public void setComposition(String composition) {
        this.composition = composition;
    }

    public LocalDate getDiscoveryDate() {
        return discoveryDate;
    }

    public void setDiscoveryDate(LocalDate discoveryDate) {
        this.discoveryDate = discoveryDate;
    }

    public String getCurrentPlace() {
        return currentPlace;
    }

    public void setCurrentPlace(String currentPlace) {
        this.currentPlace = currentPlace;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Artifact artifact = (Artifact) o;
        return Objects.equals(artifactId, artifact.artifactId); // ID varsa onu kullan, yoksa başka benzersiz alan
    }

    @Override
    public int hashCode() {
        return Objects.hash(artifactId); // ID varsa onunla hashle
    }


}