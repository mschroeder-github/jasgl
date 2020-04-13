package com.github.mschroeder.github.jasgl.sprite;

import com.github.mschroeder.github.jasgl.sprite.TileSprite;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import org.mapeditor.core.Animation;
import org.mapeditor.core.Frame;
import org.mapeditor.core.Tile;
import org.mapeditor.core.TileSet;

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
    
    public MultiTileSprite(Dimension size, TileSet tileset, Animation animation) {
        super(size, null);
        List<Tile> tileArrayList = new ArrayList<>();
        for(Frame frame : animation.getFrame()) {
            tileArrayList.add(tileset.getTile(frame.getTileid()));
        }
        tiles = tileArrayList.toArray(new Tile[0]);
        tile = tiles[0];
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
