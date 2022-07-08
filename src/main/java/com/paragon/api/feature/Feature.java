package com.paragon.api.feature;

/**
 * A client feature
 *
 * @author Wolfsurge
 */
public class Feature {

    // The name of the feature.
    private String name;

    // An optional description
    private String description;

    public Feature(String name) {
        this.name = name;
    }

    public Feature(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
