package com.github.mschroeder.github.jasgl;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.util.List;
import org.mapeditor.core.MapObject;
import org.mapeditor.core.Properties;
import org.mapeditor.core.Tile;

/**
 * Renders tile stacked and row based with map objects (having tiles) and sprites in between based
 * on y-coordinate.
 * @author Markus Schr&ouml;der
 */
public class StackRowBasedOrthogonalLevelMapRenderer {
    
    private TiledLevelMap map;
    private ListOfSprites sprites;
    
    //TODO add attributes to adjust behavior
    
    public StackRowBasedOrthogonalLevelMapRenderer(TiledLevelMap map, ListOfSprites sprites) {
        this.map = map;
        this.sprites = sprites;
    }
    
    /**
     * Renders row based and tile stacked with map objects (having tiles) and sprites in between based
     * on y-coordinate. Because of map objects and sprites we draw row (y-coordinate) based.
     * @param g
     */
    public void render(Graphics2D g) {
        List<List<List<Object>>> listOfRowsOfStack = map.getListOfRowsOfStack();
        
        for(int y = 0; y < listOfRowsOfStack.size(); y++) {
            for(int x = 0; x < listOfRowsOfStack.get(y).size(); x++) {
                //bottom-first
                for(Object obj : listOfRowsOfStack.get(y).get(x)) {
                    drawObject(obj, x, y, false, g);
                }
            }
            
            //draw all sprites standing in row y
            //TODO could be improved: not all sprites are needed
            for(Sprite sprite : sprites) {
                Rectangle rect = sprite.getArea().getBounds();
                double rectMaxY = rect.getMaxY();
                
                //double yMin = y * map.getTiledMap().getTileHeight() - map.getTiledMap().getTileHeight();
                //double yMax = y * map.getTiledMap().getTileHeight();
                
                //0:[][][]----o---- y - 0.99
                //1:[][][]____#____ y
                //2:[][][]
                if(rectMaxY >= ((y * map.getTiledMap().getTileHeight() - (map.getTiledMap().getTileHeight()*0.99))) 
                && rectMaxY <= (y * map.getTiledMap().getTileHeight())) {
                    sprite.render(g);
                }
            }
        }
        
        //second draw to overdraw sprites
        for(int y = 0; y < listOfRowsOfStack.size(); y++) {
            for(int x = 0; x < listOfRowsOfStack.get(y).size(); x++) {
                //bottom-first
                for(Object obj : listOfRowsOfStack.get(y).get(x)) {
                    drawObject(obj, x, y, true, g);
                }
            }
        }
    }
    
    private void drawObject(Object obj, int x, int y, boolean secondDraw, Graphics2D g) {
        
        //if second draw and object is tile (map object overdraw solved with y coordinate)
        if(secondDraw && obj instanceof Tile) {
            Properties properties = getProperties(obj);

            //TODO special impl
            boolean moveBehind = Boolean.parseBoolean(properties.getProperty("moveBehind", "false"));
            //if second draw then moveBehind has to be true
            if(!moveBehind)
                return;
        }
        
        if(obj instanceof Tile) {
            Tile tile = (Tile) obj;
            if(tile.getImage() != null) {
                g.drawImage(
                        tile.getImage(), 
                        x * map.getTiledMap().getTileWidth(), 
                        y * map.getTiledMap().getTileHeight(),
                        null
                );
            }
        }
        
        //second draw only for tiles
        if(secondDraw)
            return;
        
        
        if(obj instanceof MapObject) {
            MapObject mapObject = (MapObject) obj;

            if(mapObject.isVisible() != null && !mapObject.isVisible())
                return;

            if(mapObject.getTile() != null) {
                Image objectImage = mapObject.getTile().getImage();
                //AffineTransform old = g.getTransform();
                //g.rotate(Math.toRadians(mapObject.getRotation()));
                g.drawImage(objectImage,
                        (int) mapObject.getX(), 
                        (int) mapObject.getY() - mapObject.getHeight().intValue(),
                        mapObject.getWidth().intValue(),
                        mapObject.getHeight().intValue(),
                        null
                );
                //g.setTransform(old);
            }
        }
    }
    
    private Properties getProperties(Object obj) {
        if(obj instanceof Tile) {
            return ((Tile) obj).getProperties();
        }
        return ((MapObject) obj).getProperties();
    }
    
}
