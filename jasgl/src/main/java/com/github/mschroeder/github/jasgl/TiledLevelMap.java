package com.github.mschroeder.github.jasgl;

import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.mapeditor.core.MapLayer;
import org.mapeditor.core.MapObject;
import org.mapeditor.core.ObjectGroup;
import org.mapeditor.core.Tile;
import org.mapeditor.core.TileLayer;

/**
 * Represents an orthogonal map created by <a href="https://www.mapeditor.org/">Tiled</a> map editor.
 * Use the getter methods to access tile and object layer together with the map objects.
 * @author Markus Schr&ouml;der
 */
public class TiledLevelMap extends LevelMap {

    private boolean DEBUG = true;
    
    private CustomTMXMapReader reader;
    private org.mapeditor.core.Map map;
    
    /**
     * Loads one map from file.
     * @param tmxFile a file crated with the <a href="https://www.mapeditor.org/">Tiled</a> map editor.  
     */
    public TiledLevelMap(File tmxFile) {
        long start = System.currentTimeMillis();
        reader = new CustomTMXMapReader();
        try {
            map = reader.readMap(tmxFile.getAbsolutePath());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        long end = System.currentTimeMillis();
        
        if(DEBUG)
            System.out.println("load map " + tmxFile.getName() + " in " + (end - start) + " ms");
    }
    
    /**
     * Loads one map from resources.
     * @param resourcePath path to the tmx file in the resources.
     */
    public TiledLevelMap(String resourcePath) {
        long start = System.currentTimeMillis();
        reader = new CustomTMXMapReader();
        try {
            map = reader.readMapFromResources(resourcePath);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        long end = System.currentTimeMillis();
        
        if(DEBUG)
            System.out.println("load map " + resourcePath + " in " + (end - start) + " ms");
    }
    
    /**
     * Finds tile layer by name.
     * @param name
     * @return null if not found.
     */
    public TileLayer getTileLayerByName(String name) {
        for (MapLayer layer : map.getLayers()) {
            if (layer instanceof TileLayer && layer.getName().equals(name)) {
                return (TileLayer) layer;
            }
        }
        return null;
    }
    
    /**
     * Lists all tile layers in the map.
     * @return 
     */
    public List<TileLayer> getTileLayers() {
        List<TileLayer> result = new ArrayList<>();
        for (MapLayer layer : map.getLayers()) {
            if (layer instanceof TileLayer) {
                result.add((TileLayer) layer);
            }
        }
        return result;
    }
    
    /**
     * Lists all object groups in the map.
     * @return 
     */
    public List<ObjectGroup> getObjectGroups() {
        List<ObjectGroup> result = new ArrayList<>();
        for (MapLayer layer : map.getLayers()) {
            if (layer instanceof ObjectGroup) {
                result.add((ObjectGroup) layer);
            }
        }
        return result;
    }

    /**
     * Finds an object group (layer) by name.
     * @param name
     * @return null if not found.
     */
    public ObjectGroup getObjectGroupByName(String name) {
        for (MapLayer layer : map.getLayers()) {
            if (layer instanceof ObjectGroup && layer.getName().equals(name)) {
                return (ObjectGroup) layer;
            }
        }
        return null;
    }

    /**
     * Finds a map object by name in a given object group (layer).
     * @param og
     * @param name
     * @return null if not found.
     */
    public MapObject getMapObjectByName(ObjectGroup og, String name) {
        for(MapObject mo : og.getObjects()) {
            if (mo.getName().equals(name)) {
                return mo;
            }
        }
        return null;
    }

    /**
     * Lists all map objects from all object groups (layers).
     * @return 
     */
    public List<MapObject> getMapObjects() {
        List<MapObject> l = new ArrayList<>();
        for(ObjectGroup og : getObjectGroups()) {
            Iterator<MapObject> iter = og.iterator();
            while(iter.hasNext()) {
                l.add(iter.next());
            }
        }
        return l;
    }
    
    /**
     * Get tiles from all layers at position x,y.
     * @param x
     * @param y
     * @return 
     */
    public List<Tile> getTiles(int x, int y) {
        List<Tile> result = new ArrayList<>();
        for(TileLayer layer : getTileLayers()) {
            if(layer.contains(x, y)) {
                result.add(layer.getTileAt(x, y));
            }
        }
        return result;
    }

    /**
     * The original map.
     * @return 
     */
    public org.mapeditor.core.Map getTiledMap() {
        return map;
    }

    @Override
    public Dimension getDimension() {
        return new Dimension(
                map.getTileWidth()  * map.getWidth(), 
                map.getTileHeight() * map.getHeight()
        );
    }
    
}
