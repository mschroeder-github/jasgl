package com.github.mschroeder.github.jasgl.editor;

import com.google.gson.JsonObject;
import java.util.HashSet;
import java.util.Set;

/**
 * A stamp is what you insert into the map from a tileset.
 * @author Markus Schr&ouml;der
 */
public class Stamp {
    
    public static String ALL = "all";
    public static String GROUND = "ground";
    public static String OBJECT = "object";
    
    private Set<JsonObject> tiles;

    public Stamp() {
        this.tiles = new HashSet<>();
    }
    
    public Set<JsonObject> getTiles() {
        return tiles;
    }

    public void setTiles(Set<JsonObject> tiles) {
        this.tiles = tiles;
    }

    @Override
    public String toString() {
        return "Stamp{" + "tiles=" + tiles.size() + '}';
    }
    
}
