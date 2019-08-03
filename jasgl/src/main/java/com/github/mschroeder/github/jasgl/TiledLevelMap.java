package com.github.mschroeder.github.jasgl;

import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import org.mapeditor.core.MapLayer;
import org.mapeditor.core.MapObject;
import org.mapeditor.core.ObjectGroup;
import org.mapeditor.core.Tile;
import org.mapeditor.core.TileLayer;

/**
 * Represents an orthogonal map created by
 * <a href="https://www.mapeditor.org/">Tiled</a> map editor. Use the getter
 * methods to access tile and object layer together with the map objects.
 *
 * @author Markus Schr&ouml;der
 */
public class TiledLevelMap extends LevelMap {

    private boolean DEBUG = true;

    private CustomTMXMapReader reader;
    private org.mapeditor.core.Map map;

    /**
     * Loads one map from file.
     *
     * @param tmxFile a file crated with the
     * <a href="https://www.mapeditor.org/">Tiled</a> map editor.
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

        if (DEBUG) {
            System.out.println("load map " + tmxFile.getName() + " in " + (end - start) + " ms");
        }
    }

    /**
     * Loads one map from resources.
     *
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

        if (DEBUG) {
            System.out.println("load map " + resourcePath + " in " + (end - start) + " ms");
        }
    }

    /**
     * Finds tile layer by name.
     *
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
     *
     * @return It is sorted bottom-layer first.
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
     *
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
     *
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
     *
     * @param og
     * @param name
     * @return null if not found.
     */
    public MapObject getMapObjectByName(ObjectGroup og, String name) {
        for (MapObject mo : og.getObjects()) {
            if (mo.getName().equals(name)) {
                return mo;
            }
        }
        return null;
    }

    /**
     * Lists all map objects from all object groups (layers).
     *
     * @return
     */
    public List<MapObject> getMapObjects() {
        List<MapObject> l = new ArrayList<>();
        for (ObjectGroup og : getObjectGroups()) {
            Iterator<MapObject> iter = og.iterator();
            while (iter.hasNext()) {
                l.add(iter.next());
            }
        }
        return l;
    }

    /**
     * Lists all map objects with their grid coordinate. If a map object is
     * larger than a grid tile it will be multiple times in the list for each
     * grid coordinate.
     *
     * @return e.g. (mo1, (0,0)), (mo1, (0,1)), (mo2, (42,13))
     */
    public List<Entry<MapObject, Point>> getMapObjectsWithGridCoord() {
        List<Entry<MapObject, Point>> l = new ArrayList<>();

        for (MapObject mo : getMapObjects()) {

            int moxStart = (int) (mo.getX() / map.getTileWidth());
            int moyStart = (int) (mo.getY() / map.getTileHeight());

            //fix y coord
            if (mo.getTile() != null) {
                moyStart = (int) ((mo.getY() - mo.getHeight()) / map.getTileHeight());
            }

            int w = (int) (mo.getWidth() / map.getTileWidth());
            int h = (int) (mo.getHeight() / map.getTileHeight());

            for (int moy = moyStart; moy < moyStart + h; moy++) {
                for (int mox = moxStart; mox < moxStart + w; mox++) {
                    l.add(new AbstractMap.SimpleEntry<>(mo, new Point(mox, moy)));
                }
            }
        }
        return l;
    }

    /**
     * Get tiles from all layers at position x,y.
     *
     * @param x
     * @param y
     * @return
     */
    public List<Tile> getTiles(int x, int y) {
        List<Tile> result = new ArrayList<>();
        for (TileLayer layer : getTileLayers()) {
            if (layer.contains(x, y)) {
                result.add(layer.getTileAt(x, y));
            }
        }
        return result;
    }

    /**
     * Creates a per row per column list while on a field a stack of Tile or MapObject is formed (bottom-first).
     * @return list of (1) [y] rows of (2) [x] stack of (3) [z] Tile or MapObject.
     */
    public List<List<List<Object>>> getListOfRowsOfStack() {
        List<List<List<Object>>> listOfRowsOfStack = new ArrayList<>();

        for(int y = 0; y < map.getHeight(); y++) {
            
            List<List<Object>> rowOfStack = new ArrayList<>();
            for(int x = 0; x < map.getWidth(); x++) {
                
                //stack for (x,y)
                List<Object> stack = new ArrayList<>();
                for(MapLayer layer : map.getLayers()) {
                    Object obj = null;
                    if(layer instanceof TileLayer) {
                        obj = ((TileLayer) layer).getTileAt(x, y);
                        stack.add(obj);
                    } else if(layer instanceof ObjectGroup) {
                        for(MapObject mo : getObjectsAt(x, y, ((ObjectGroup) layer))) {
                            //System.out.println(mo.getName() + " (" + x + "," + y + ")");
                            stack.add(mo);
                        }
                    }
                }
                
                rowOfStack.add(stack);
            }
            
            listOfRowsOfStack.add(rowOfStack);
        }
        
        return listOfRowsOfStack;
    }
    
    /**
     * Given a grid position returns all mapobjects that are on the field.
     * The map object can be anywhere on a certain field and will still be selected.
     * @param gridX
     * @param gridY
     * @param layer
     * @return 
     */
    public List<MapObject> getObjectsAt(int gridX, int gridY, ObjectGroup layer) {
        List<MapObject> objects = new ArrayList<>();
        for(MapObject mo : layer.getObjects()) {
            double mox = mo.getX();
            double moy;
            if(mo.getTile() != null) {
                //correct y coord
                moy = mo.getY();
            } else {
                //have to add heigh
                moy = mo.getY() + mo.getHeight();
            }
            
            int moGridX = (int)(mox / map.getTileWidth());
            int moGridY = (int)(moy / map.getTileHeight());

            if(moGridX == gridX && moGridY == gridY) {
                objects.add(mo);
            }
        }
        return objects;
    }

    /**
     * The original map.
     *
     * @return
     */
    public org.mapeditor.core.Map getTiledMap() {
        return map;
    }

    @Override
    public Dimension getDimension() {
        return new Dimension(
                map.getTileWidth() * map.getWidth(),
                map.getTileHeight() * map.getHeight()
        );
    }

}
