package com.github.mschroeder.github.jasgl;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;
import org.mapeditor.core.MapObject;
import org.mapeditor.core.Tile;
import org.mapeditor.core.TileLayer;

/**
 * Use this class to test if an area intersects with tiles or map objects in a tiled level map.
 * @author Markus Schr&ouml;der
 */
public class LevelMapIntersectioner extends Intersectioner {
    
    protected TiledLevelMap map;

    public LevelMapIntersectioner(TiledLevelMap map) {
        this.map = map;
    }
    
    /**
     * Checks if a given area intersects with map tiles.
     * Does not look at area width and height.
     * @param area
     * @return 
     */
    public List<LevelMapTile> intersectsTiles(Area area) {
        List<LevelMapTile> tiles = new ArrayList<>();
        
        Rectangle bounds = area.getBounds();
        
        int tw = map.getTiledMap().getTileWidth();
        int th = map.getTiledMap().getTileHeight();
        
        //0  32 64
        //[0][1][2]
        //64 / 32 = 2
        int sx = bounds.x / tw;
        int sy = bounds.y / th;
        
        for(TileLayer layer : map.getTileLayers()) {
            Tile tile = layer.getTileAt(sx, sy);
            
            //tile is null if nothing was drawn
            if(tile == null)
                continue;
            
            tiles.add(new LevelMapTile(tile, new Point(sx, sy), layer, map));
        }
        
        /*
        for(TileLayer layer : map.getTileLayers()) {
            for(int y = 0; y < map.getTiledMap().getHeight(); y++) {
                for(int x = 0; x < map.getTiledMap().getWidth(); x++) {
                    Tile tile = layer.getTileAt(x, y);
                    
                    //tile is null if nothing was drawn
                    if(tile == null)
                        continue;
                    
                    int xx = map.getTiledMap().getTileWidth() * x;
                    int yy = map.getTiledMap().getTileHeight() * y;
                    
                    Rectangle rect = new Rectangle(xx, yy, tile.getWidth(), tile.getHeight());
                    
                    if(bounds.intersects(rect)) {
                        tiles.add(tile);
                    }
                }
            }
        }
        */
        
        return tiles;
    }
    
    public List<MapObject> intersectsMapObjects(Area area) {
        throw new RuntimeException("not implemented yet");
    }
    
}
