package org.ce216.artifactcatalog.historicalartifactcatalogproject;
import java.util.List;

import java.util.ArrayList;


public class ArtifactManager {
    private List<Artifact> artifacts = new ArrayList<>(); // null olmasın

    public ArtifactManager() {
    }

    public List<Artifact> getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(List<Artifact> artifacts) {
        this.artifacts = artifacts;
    }
}
