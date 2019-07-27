package com.github.mschroeder.github.jasgl;

import java.awt.Point;
import org.mapeditor.core.Tile;
import org.mapeditor.core.TileLayer;

/**
 * A data class to hold more information about a tile.
 * @author Markus Schr&ouml;der
 */
public class LevelMapTile {
    private Tile tile;
    private Point gridPosition;
    private TileLayer layer;
    private LevelMap levelMap;

    public LevelMapTile(Tile tile, Point gridPosition, TileLayer layer, LevelMap levelMap) {
        this.tile = tile;
        this.gridPosition = gridPosition;
        this.layer = layer;
        this.levelMap = levelMap;
    }

    public Tile getTile() {
        return tile;
    }

    public Point getGridPosition() {
        return gridPosition;
    }

    public TileLayer getLayer() {
        return layer;
    }

    public LevelMap getLevelMap() {
        return levelMap;
    }

    @Override
    public String toString() {
        return "LevelMapTile{" + "tile=" + tile + ", gridPosition=" + gridPosition + ", layer=" + layer.getName() + '}';
    }

 
    
}
