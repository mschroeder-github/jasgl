package com.github.mschroeder.github.jasgl;

import java.awt.Dimension;
import org.mapeditor.core.Tile;

/**
 * A sprite that can be rendered with various tiles but only one tile at a time.
 * @author Markus Schr&ouml;der
 */
public class MultiTileSprite extends TileSprite {
    
    protected Tile[] tiles;
    private int tileIndex;
    
    public MultiTileSprite(Dimension size, Tile... tiles) {
        super(size, tiles[0]);
        this.tiles = tiles;
    }

    public int getTileIndex() {
        return tileIndex;
    }

    public void setTileIndex(int tileIndex) {
        this.tileIndex = tileIndex;
        this.tile = tiles[tileIndex];
    }
    
    public void incTileIndex() {
        int ti = getTileIndex();
        ti++;
        if(ti >= tiles.length)
            ti = 0;
        setTileIndex(ti);
    }
    
    public void decTileIndex() {
        int ti = getTileIndex();
        ti--;
        if(ti < 0)
            ti = tiles.length-1;
        setTileIndex(ti);
    }
    
}
