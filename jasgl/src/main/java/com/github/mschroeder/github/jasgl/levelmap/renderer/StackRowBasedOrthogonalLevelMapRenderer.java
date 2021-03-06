package com.github.mschroeder.github.jasgl.levelmap.renderer;

import com.github.mschroeder.github.jasgl.sprite.ListOfSprites;
import com.github.mschroeder.github.jasgl.levelmap.TiledLevelMap;
import com.github.mschroeder.github.jasgl.util.PropertyUtils;
import com.github.mschroeder.github.jasgl.sprite.Sprite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.mapeditor.core.MapObject;
import org.mapeditor.core.Tile;

/**
 * Renders tile stacked and row based with map objects (having tiles) and sprites in between based
 * on y-coordinate.
 * @author Markus Schr&ouml;der
 * @author Sven Schwarz
 */
public class StackRowBasedOrthogonalLevelMapRenderer {
    
    private TiledLevelMap map;
    private ListOfSprites sprites;
    
    private Consumer<RenderContext<Tile>> tileRenderer = (ctx) -> {
        ctx.drawDefault();
    };
    private Consumer<RenderContext<MapObject>> objectRenderer = (ctx) -> {
        ctx.drawDefault();
    };
    private Predicate<Tile> tileAlwaysOnTopPredicate = (prop) -> {
        return false;
    };
    
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
        //TODO: problem: what about map objects being 2 tiles width and start at (-1,0): they have to be drawn also
        List<List<List<Object>>> listOfRowsOfStack = map.getListOfRowsOfStack();
        
        //project sprites to grid system to know when we have to draw them
        Map<Integer, List<Sprite>> grid2sprite = new HashMap<>();
        for(Sprite sprite : sprites) {
            Rectangle rect = sprite.getArea().getBounds();
            double rectMaxY = rect.getMaxY();
            
            //-------------------- 0
            //0
            //--------------------- 32  =0
            //1   |      |              =1
            //-----______---------- 64  =1
            
            int gridY = (int) Math.ceil(rectMaxY / map.getTiledMap().getTileHeight()) - 1;
            grid2sprite.computeIfAbsent(gridY, i -> new ArrayList<>()).add(sprite);
        }
        
        for(int y = 0; y < listOfRowsOfStack.size(); y++) {
            
            //tile stacks in a row
            for(int x = 0; x < listOfRowsOfStack.get(y).size(); x++) {
                //bottom-first
                List<Object> stack = listOfRowsOfStack.get(y).get(x);
                for(Object obj : stack) {
                    drawObject(obj, x, y, false, g, stack);
                }
            }
            
            //sprites on top
            if(grid2sprite.containsKey(y)) {
                for(Sprite sprite : grid2sprite.get(y)) {
                    sprite.render(g);
                }
            }
        }
        
        //second draw to overdraw sprites
        for(int y = 0; y < listOfRowsOfStack.size(); y++) {
            for(int x = 0; x < listOfRowsOfStack.get(y).size(); x++) {
                //bottom-first
                List<Object> stack = listOfRowsOfStack.get(y).get(x);
                for(Object obj : stack) {
                    drawObject(obj, x, y, true, g, stack);
                }
            }
        }
    }
    
    private void drawObject(Object obj, int x, int y, boolean secondDraw, Graphics2D g, List<Object> stack) {
        if (!isExisting(obj))
            return;
        
        //if second draw and object is tile (map object overdraw solved with y coordinate)
        if(secondDraw && obj instanceof Tile) {
            //if second draw then moveBehind has to be true
            if(!tileAlwaysOnTopPredicate.test((Tile)obj))
                return;
        }
        
        if(obj instanceof Tile) {
            Tile tile = (Tile) obj;
            if(tile.getImage() != null) {
                RenderContext<Tile> ctx = new RenderContext<>(
                        tile, tile.getImage(), 
                        new Point(x,y), new Point(x*tile.getWidth(),y*tile.getHeight()), 
                        new Dimension(tile.getWidth(), tile.getHeight()), 
                        secondDraw, g, stack);
                tileRenderer.accept(ctx);
            }
        }
        
        //second draw only for tiles
        if(secondDraw)
            return;
        
        if(obj instanceof MapObject) {
            MapObject mapObject = (MapObject) obj;

            if(mapObject.getTile() != null) {
                RenderContext<MapObject> ctx = new RenderContext<>(
                        mapObject, mapObject.getTile().getImage(), 
                        new Point(x,y), new Point((int) mapObject.getX(),(int) mapObject.getY()), 
                        new Dimension(mapObject.getWidth().intValue(), mapObject.getHeight().intValue()), 
                        secondDraw, g, stack);
                objectRenderer.accept(ctx);
            }
        }
    }
    
    private boolean isExisting(Object obj) {
        if (obj == null)
            return false;
        return PropertyUtils.getBoolean(obj, "existing", true);  // NOT EXISTING must be set explicity!
    }
    
    
    
    public static class RenderContext<T> {
        private T object;
        private Image image;
        private Point grid;
        private Point position;
        private Dimension size;
        private boolean secondDraw;
        private Graphics2D g;
        private List<Object> stack;

        public RenderContext(T object, Image image, Point grid, Point position, Dimension size, boolean secondDraw, Graphics2D g, List<Object> stack) {
            this.object = object;
            this.image = image;
            this.grid = grid;
            this.position = position;
            this.size = size;
            this.secondDraw = secondDraw;
            this.g = g;
            this.stack = stack;
        }
        
        public Tile asTile() {
            return (Tile) object;
        }
        
        public MapObject asMapObject() {
            return (MapObject) object;
        }

        public boolean isTile() {
            return object instanceof Tile;
        }
        
        public boolean isMapObject() {
            return object instanceof MapObject;
        }
        
        public T getObject() {
            return object;
        }

        public Image getImage() {
            return image;
        }

        public Point getGrid() {
            return grid;
        }

        public Point getPosition() {
            return position;
        }

        public Dimension getSize() {
            return size;
        }

        public boolean isSecondDraw() {
            return secondDraw;
        }

        public Graphics2D getGraphics() {
            return g;
        }

        public List<Object> getStack() {
            return stack;
        }
        
        public int getLayerIndex() {
            return stack.indexOf(object);
        }
        
        public void drawDefault() {
            if(object instanceof Tile) {
                g.drawImage(
                        image, //tile.getImage(), 
                        grid.x * size.width, //x * map.getTiledMap().getTileWidth(), 
                        grid.y * size.height, //y * map.getTiledMap().getTileHeight(),
                        null
                );
            } else if(object instanceof MapObject) {
                MapObject mapObject = asMapObject();
                
                if(mapObject.isVisible() != null && !mapObject.isVisible())
                    return;
                
                //AffineTransform old = g.getTransform();
                //g.rotate(Math.toRadians(mapObject.getRotation()));
                g.drawImage(image,
                        position.x, //(int) mapObject.getX(), 
                        position.y - size.height,//(int) mapObject.getY() - mapObject.getHeight().intValue(),
                        size.width, //mapObject.getWidth().intValue(),
                        size.height, //mapObject.getHeight().intValue(),
                        null
                );
                //g.setTransform(old);
            }
        }
    }

    public void setTileRenderer(Consumer<RenderContext<Tile>> tileRenderer) {
        this.tileRenderer = tileRenderer;
    }

    public void setObjectRenderer(Consumer<RenderContext<MapObject>> objectRenderer) {
        this.objectRenderer = objectRenderer;
    }

    public void setTileAlwaysOnTopPredicate(Predicate<Tile> tileAlwaysOnTopPredicate) {
        this.tileAlwaysOnTopPredicate = tileAlwaysOnTopPredicate;
    }
    
}
