package com.paragon.client.managers.alt;

import java.util.ArrayList;

public class AltManager {

    private final ArrayList<Alt> alts = new ArrayList<>();

    public AltManager() {

    }

    public void addAlt(Alt alt) {
        this.alts.add(alt);
    }

    public void removeAlt(Alt alt) {
        alts.remove(alt);
    }

    public ArrayList<Alt> getAlts() {
        return alts;
    }

}
