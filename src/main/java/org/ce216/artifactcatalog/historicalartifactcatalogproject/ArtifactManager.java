package org.ce216.artifactcatalog.historicalartifactcatalogproject;
import java.util.List;

public class ArtifactManager {
    private List<Artifact> artifacts; // artifact list of readed json

    public ArtifactManager() {
    }
    public List<Artifact> getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(List<Artifact> artifacts) {
        this.artifacts = artifacts;
}
}