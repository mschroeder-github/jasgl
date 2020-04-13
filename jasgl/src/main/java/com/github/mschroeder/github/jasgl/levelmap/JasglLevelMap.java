package com.github.mschroeder.github.jasgl.levelmap;

import com.github.mschroeder.github.jasgl.editor.MapTilesetSpriteFrame;
import com.github.mschroeder.github.jasgl.editor.Stamp;
import com.github.mschroeder.github.jasgl.util.JASGLUtils.Direction;
import com.github.mschroeder.github.jasgl.util.PNGUtils;
import com.github.mschroeder.github.jasgl.util.PNGUtils.ImageWithMetaData;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Own implementation of a level map.
 * Use the provided editor {@link MapTilesetSpriteFrame} to create a JASGL map based
 * on tilesets.
 * @author Markus Schr&ouml;der
 */
public class JasglLevelMap extends LevelMap {

    private JsonObject data;
    private Map<String, ImageWithMetaData> tilesetCache;
    private Map<String, Map<Integer, Map<Integer, JsonObject>>> img2x2y2tile;
    private List<JsonObject> tiles;
    
    private Dimension grid;
    
    public JasglLevelMap(File f) {
        load(f.getParentFile().getAbsolutePath(), f.getName(), false);
    }
    
    public JasglLevelMap(String resourceFilename) {
        int i = resourceFilename.lastIndexOf("/");
        load(resourceFilename.substring(0, i), resourceFilename.substring(i+1), true);
    }
    
    private final void load(String baseFolder, String filename, boolean fromResource) {
        tilesetCache = new HashMap<>();
        img2x2y2tile = new HashMap<>();
        grid = new Dimension(32, 32);
        
        //System.out.println(baseFolder);
        //System.out.println(filename);
        
        ImageWithMetaData map = toImageWithMetaData(baseFolder, filename, fromResource);
        //System.out.println(map);
        
        
        
        String mapDataStr = map.getMetaData().get(PNGUtils.COMMENT);
        if(mapDataStr == null) {
            throw new RuntimeException("no map data found");
        }
        
        
        data = new Gson().fromJson(mapDataStr, JsonObject.class);
        JsonArray array = data.getAsJsonArray("tiles");
        
        
        System.out.println(filename + " with " + array.size() + " map tiles");
        
        for(int i = 0; i < array.size(); i++) {
            JsonObject mapTile = array.get(i).getAsJsonObject();
            
            JsonObject tileref = mapTile.getAsJsonObject("tileref");
            String img = tileref.get("img").getAsString();
            
            Map<Integer, Map<Integer, JsonObject>> cache = ensureTilesetLoaded(baseFolder, img, fromResource);
            
            int x = tileref.get("x").getAsInt();
            int y = tileref.get("y").getAsInt();
            JsonObject tile = cache.get(x).get(y);
            
            if(tile == null) {
                throw new RuntimeException("tile (" + x + "," + y + ") in " + img + " not found");
            }
        }
        
        //sort the tiles once for rendering
        tiles = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            tiles.add(array.get(i).getAsJsonObject());
        }
        
        tiles.sort(new TileComparator());
    }
    
    private Map<Integer, Map<Integer, JsonObject>> ensureTilesetLoaded(String baseFolder, String filename, boolean fromResource) {

        if (!img2x2y2tile.containsKey(filename)) {
            ImageWithMetaData imageWithMetaData = toImageWithMetaData(baseFolder, filename, fromResource);
            tilesetCache.put(filename, imageWithMetaData);

            String comment = imageWithMetaData.getMetaData().get(PNGUtils.COMMENT);
            if (comment != null) {

                Map<Integer, Map<Integer, JsonObject>> x2y2tile = new HashMap<>();

                JsonObject commentData = new Gson().fromJson(comment, JsonObject.class);

                JsonArray array = commentData.getAsJsonArray("tiles");
                for (int i = 0; i < array.size(); i++) {
                    JsonObject tile = array.get(i).getAsJsonObject();

                    int x = tile.get("x").getAsInt();
                    int y = tile.get("y").getAsInt();

                    Map<Integer, JsonObject> y2tile = x2y2tile.computeIfAbsent(x, xx -> new HashMap<>());
                    y2tile.put(y, tile);
                }

                img2x2y2tile.put(filename, x2y2tile);
            }
        }

        return img2x2y2tile.get(filename);
    }
    
    private ImageWithMetaData toImageWithMetaData(String baseFolder, String filename, boolean fromResource) {
        InputStream mapStream = toInputStream(baseFolder, filename, fromResource);
        ImageWithMetaData iwmd;
        try {
            iwmd = PNGUtils.read(mapStream);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            try {
                mapStream.close();
            } catch (IOException ex1) {
                throw new RuntimeException(ex1);
            }
        }
        return iwmd;
    }
    
    private InputStream toInputStream(String baseFolder, String filename, boolean fromResource) {
        if(fromResource) {
            return JasglLevelMap.class.getResourceAsStream(baseFolder + "/" + filename);
        }
        try {
            return new FileInputStream(new File(baseFolder, filename));
        } catch (IOException ex) {
            return null;
        }
    }

    public Map<String, ImageWithMetaData> getTilesetCache() {
        return tilesetCache;
    }
    
    @Override
    public Dimension getDimension() {
        Rectangle rect = getMapGridRectangle();
        return new Dimension(grid.width * rect.width, grid.height * rect.height);
    }
    
    public List<JsonObject> getTiles() {
        return tiles;
    }
    
    public List<JsonObject> getTilesAt(int gx, int gy) {
        return JasglLevelMap.this.getTilesAt(gx, gy, getTiles());
    }

    private List<JsonObject> getTilesAt(int gx, int gy, List<JsonObject> tiles) {
        List<JsonObject> result = new ArrayList<>();
        for (JsonObject tile : tiles) {
            int tgx = tile.get("gx").getAsInt();
            int tgy = tile.get("gy").getAsInt();
            if (tgx == gx && tgy == gy) {
                result.add(tile);
            }
        }
        return result;
    }
    
    public JsonObject resolveTileRef(JsonObject mapTile) {
        JsonObject tileref = mapTile.getAsJsonObject("tileref");
        Map<Integer, Map<Integer, JsonObject>> x2y2tile = img2x2y2tile.get(tileref.get("img").getAsString());
        return x2y2tile.get(tileref.get("x").getAsInt()).get(tileref.get("y").getAsInt());
    }
    
    public BufferedImage resolveTileset(JsonObject mapTile) {
        return tilesetCache.get(mapTile.get("tileref").getAsJsonObject().get("img").getAsString()).getImage();
    }
    
    public List<JsonObject> resolveTileRefsAt(int gx, int gy) {
        List<JsonObject> result = new ArrayList<>();
        List<JsonObject> tilesAt = getTilesAt(gx, gy);
        for(JsonObject mapTile : tilesAt) {
            result.add(resolveTileRef(mapTile));
        }
        return result;
    }
    
    public boolean isBlock(int gx, int gy, Direction go) {
        boolean block = false;
        List<JsonObject> tileRefsAt = resolveTileRefsAt(gx, gy);
        for(JsonObject tile : tileRefsAt) {
            if(tile.has("r") && go == Direction.Left) {
                block |= tile.get("r").getAsBoolean();
            }
            if(tile.has("l") && go == Direction.Right) {
                block |= tile.get("l").getAsBoolean();
            }
            if(tile.has("u") && go == Direction.Down) {
                block |= tile.get("u").getAsBoolean();
            }
            if(tile.has("d") && go == Direction.Up) {
                block |= tile.get("d").getAsBoolean();
            }
        }
        return block;
    }
    
    public Rectangle getMapGridRectangle() {

        int minGridX = Integer.MAX_VALUE;
        int minGridY = Integer.MAX_VALUE;

        int maxGridX = Integer.MIN_VALUE;
        int maxGridY = Integer.MIN_VALUE;

        for (JsonObject tile : getTiles()) {
            int tgx = tile.get("gx").getAsInt();
            int tgy = tile.get("gy").getAsInt();
            int tgz = tile.get("gz").getAsInt();

            minGridX = Math.min(minGridX, tgx);
            minGridY = Math.min(minGridY, tgy - tgz);

            maxGridX = Math.max(maxGridX, tgx);
            maxGridY = Math.max(maxGridY, tgy);
        }

        return new Rectangle(new Point(minGridX, minGridY), new Dimension(maxGridX - minGridX + 1, maxGridY - minGridY + 1));
    }
    
    public Point getScreenPoint(Point gridPoint) {
        int x = (gridPoint.x * grid.width);
        int y = (gridPoint.y * grid.height);
        return new Point(x, y);
    }
    
    public static String getLayer(JsonObject tile) {
        if (tile.has("layer")) {
            return tile.getAsJsonPrimitive("layer").getAsString();
        }
        return Stamp.OBJECT;
    }
    
    public class TileComparator implements Comparator<JsonObject> {

        @Override
        public int compare(JsonObject a, JsonObject b) {
            int agx = a.get("gx").getAsInt();
            int agy = a.get("gy").getAsInt();
            int agz = a.get("gz").getAsInt();
            JsonObject tileA = resolveTileRef(a);
            String layerA = getLayer(tileA);

            int bgx = b.get("gx").getAsInt();
            int bgy = b.get("gy").getAsInt();
            int bgz = b.get("gz").getAsInt();
            JsonObject tileB = resolveTileRef(b);
            String layerB = getLayer(tileB);

            
            //same layer
            int layerComp = Integer.compare(getLayerIndex(layerA), getLayerIndex(layerB));
            if(layerComp == 0) {
                
                //same line
                int yComp = Integer.compare(agy, bgy);
                if (yComp == 0) {
                    
                    int xComp = Integer.compare(agx, bgx);
                    //if (xComp == 0) {

                    //}
                    return xComp;
                }

                return yComp;
            }
            
            return layerComp;
        }
    }
    
    public static int getLayerIndex(String layer) {
        if(layer.equals(Stamp.GROUND)) {
            return 0;
        } else if(layer.equals(Stamp.OBJECT)) {
            return 1;
        }
        return 0;
    }
    

    public Dimension getGrid() {
        return grid;
    }
}
