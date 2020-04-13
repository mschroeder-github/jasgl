package com.github.mschroeder.github.jasgl.sprite;

import com.github.mschroeder.github.jasgl.sprite.Sprite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Area;
import org.mapeditor.core.Tile;

/**
 * A sprite that is rendered using one tile.
 * @author Markus Schr&ouml;der
 */
public class TileSprite extends Sprite {
    
    protected Tile tile;
    protected Dimension size;

    public TileSprite(Dimension size, Tile tile) {
        this.tile = tile;
        this.size = size;
    }

    @Override
    public void update(double ms) {
        
    }

    @Override
    public void render(Graphics2D g) {
        //g.setColor(Color.black);
        //g.fillRect(0, 0, 1000, 1000);
        g.drawImage(tile.getImage(), (int) pos.x, (int) pos.y, size.width, size.height, null);
    }

    public Tile getTile() {
        return tile;
    }

    public Dimension getSize() {
        return size;
    }

    @Override
    public Area getArea() {
        return new Area(new Rectangle.Double(pos.x, pos.y, size.width, size.height));
    }
    
    
    
}
