package com.github.mschroeder.github.jasgl.editor;

import com.github.mschroeder.github.jasgl.GameLoopFrame;
import com.github.mschroeder.github.jasgl.levelmap.JasglLevelMap;
import com.github.mschroeder.github.jasgl.util.GraphicsUtils;
import com.github.mschroeder.github.jasgl.util.PNGUtils;
import com.github.mschroeder.github.jasgl.util.PNGUtils.ImageWithMetaData;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.PixelGrabber;
import java.awt.image.RGBImageFilter;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 *
 * @author Markus Schr&ouml;der
 */
public class EditorRenderPanel extends javax.swing.JPanel {

    private final String EMPTY_DATA = "{ \"tiles\": [] }";

    private List<TileListener> listeners;

    public enum Mode {
        Tileset,
        Map
    }
    private Mode mode = Mode.Tileset;

    private File file;

    //only for tileset
    private BufferedImage image;

    private JsonObject data;

    private Point.Double scale;

    private Color bgColor = new Color(137, 137, 137);
    private Color gridColor = new Color(50, 50, 50, 128);
    private Color selectColor = new Color(0, 154, 255, 90);
    private Color zColor = new Color(200, 200, 20, 128);
    private Color blockColor = new Color(210, 26, 26, 128);
    private Color groundColor = new Color(100, 180, 80, 128);
    private double blockThickness = 0.25;

    private Set<JsonObject> selected;

    private boolean showGrid;
    private boolean showZ;
    private boolean showBlock;
    private boolean showLayers;

    //map
    private Stamp mapStamp;
    private Dimension grid;
    private Point gridOffset;
    private Point mousePoint; //map: when moved, tileset: when pressed

    public enum DrawMode {
        Draw,
        Select,
        Erase
    }

    public enum PencilMode {
        Point,
        Line,
        Rect,
        Circle
    }
    private DrawMode drawMode = DrawMode.Draw;
    private PencilMode pencilMode = PencilMode.Point;
    private boolean fillPencil;
    private String eraseLayer;
    private Point lastGridPoint;
    private boolean tilesWereChanged;

    private Map<String, ImageWithMetaData> tilesetCache;
    private Map<String, Map<Integer, Map<Integer, JsonObject>>> img2x2y2tile;

    public EditorRenderPanel() {
        initComponents();

        scale = new Point.Double(2.0, 2.0);

        selected = new HashSet<>();

        grid = new Dimension(16, 16);
        gridOffset = new Point();
        tilesetCache = new HashMap<>();
        img2x2y2tile = new HashMap<>();

        listeners = new ArrayList<>();
    }

    //load
    //loads data from file
    public void setFile(File file) {
        this.file = file;

        if (file == null) {
            image = null;
        } else if (mode == Mode.Tileset) {
            ImageWithMetaData imd = PNGUtils.read(file);
            image = imd.getImage();
            String strdata = imd.getMetaData().getOrDefault(PNGUtils.COMMENT, EMPTY_DATA);
            data = new Gson().fromJson(strdata, JsonObject.class);

            updatePanelSize();
        } else if (mode == Mode.Map) {
            ImageWithMetaData imd = PNGUtils.read(file);
            String strdata = imd.getMetaData().getOrDefault(PNGUtils.COMMENT, EMPTY_DATA);
            data = new Gson().fromJson(strdata, JsonObject.class);
        }

        repaint();
    }

    public boolean isLoaded() {
        return file != null && image != null && data != null;
    }

    //for tileset panel size to scroll
    private void updatePanelSize() {
        if (mode == Mode.Map) {
            return;
        }

        double w = image.getWidth() * scale.x;
        double h = image.getHeight() * scale.y;

        int wint = (int) Math.floor(w);
        int hint = (int) Math.floor(h);

        setSize(wint, hint);
        setPreferredSize(new Dimension(wint, hint));
    }

    //select tiles
    private List<JsonObject> getTiles() {
        if (data == null) {
            return Arrays.asList();
        }

        JsonArray array = data.getAsJsonArray("tiles");

        List<JsonObject> tiles = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            tiles.add(array.get(i).getAsJsonObject());
        }

        return tiles;
    }

    //updates the tiles JsonArray in data
    //called in "map" mode (not tileset)
    private void setTiles(List<JsonObject> tiles) {
        if (data == null) {
            return;
        }

        //from list to array
        JsonArray array = new JsonArray(tiles.size());
        for (JsonObject tile : tiles) {
            array.add(tile);
        }
        data.add("tiles", array);

        //save history in mouse release, not here
        tilesWereChanged = true;
    }

    private Set<JsonObject> getSelectedTiles() {
        return selected;
    }

    //used by mosue pressed dragged
    private List<JsonObject> getTilesBy(int x, int y) {

        double sx = x / scale.x;
        double sy = y / scale.y;

        List<JsonObject> result = new ArrayList<>();
        for (JsonObject tile : getTiles()) {
            Rectangle rect = new Rectangle(
                    tile.getAsJsonPrimitive("x").getAsInt(),
                    tile.getAsJsonPrimitive("y").getAsInt(),
                    tile.getAsJsonPrimitive("w").getAsInt(),
                    tile.getAsJsonPrimitive("h").getAsInt()
            );
            if (rect.contains(sx, sy)) {
                result.add(tile);
            }
        }
        return result;
    }

    private List<JsonObject> getTilesByGrid(int gx, int gy) {
        return getTilesByGrid(gx, gy, getTiles());
    }

    private List<JsonObject> getTilesByGrid(int gx, int gy, List<JsonObject> tiles) {
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

    //rendering
    @Override
    public void paint(Graphics g) {
        render((Graphics2D) g);
    }

    private void render(Graphics2D g) {
        //g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(bgColor);
        g.fillRect(0, 0, getWidth(), getHeight());

        g.scale(scale.x, scale.y);

        if (mode == Mode.Tileset) {
            renderTileset(g);
        } else if (mode == Mode.Map) {
            renderMap(g);
        }
    }

    private void renderTileset(Graphics2D g) {
        if (image == null) {
            return;
        }

        g.drawImage(image, 0, 0, null);

        for (JsonObject tile : getTiles()) {
            int x = tile.getAsJsonPrimitive("x").getAsInt();
            int y = tile.getAsJsonPrimitive("y").getAsInt();
            int w = tile.getAsJsonPrimitive("w").getAsInt();
            int h = tile.getAsJsonPrimitive("h").getAsInt();
            
            renderTileExtras(new Rectangle(x, y, w, h), tile, g);
        }
    }

    private void renderTileExtras(Rectangle dstScreenRect, JsonObject tile, Graphics2D g) {
        int x = dstScreenRect.x;
        int y = dstScreenRect.y;
        int w = dstScreenRect.width;
        int h = dstScreenRect.height;

        if(mode == Mode.Tileset) {
            if (showGrid) {
                g.setColor(gridColor);
                g.drawRect(x, y, w, h);
            }
        }

        //selection
        if (isSelected(tile)) {
            g.setColor(selectColor);
            g.fillRect(x, y, w, h);
        }

        //z
        if (showZ) {
            if (tile.has("z") && tile.getAsJsonPrimitive("z").getAsInt() != 0) {
                int z = tile.getAsJsonPrimitive("z").getAsInt();

                g.setColor(zColor);
                g.fillRect(x, y, w, h);

                int fw = g.getFontMetrics().stringWidth(String.valueOf(z));
                int fh = g.getFontMetrics().getHeight();
                int fx = x + (w / 2) - (fw / 2);
                int fy = y + fh - 4 + (h / 2) - (fh / 2);

                g.setColor(Color.white);
                g.drawString(String.valueOf(z), fx - 1, fy - 1);
                g.setColor(Color.black);
                g.drawString(String.valueOf(z), fx, fy);
            }
        }

        if(showLayers) {
            if (tile.has("layer")) {
                String layer = tile.getAsJsonPrimitive("layer").getAsString();

                if (layer.equals(Stamp.GROUND)) {
                    g.setColor(groundColor);
                }

                g.fillRect(x, y, w, h);
            }
        }

        //block
        if (showBlock) {

            int bw = (int) (tile.getAsJsonPrimitive("w").getAsInt() * blockThickness);
            int bh = (int) (tile.getAsJsonPrimitive("h").getAsInt() * blockThickness);

            //left
            if (tile.has("l") && tile.getAsJsonPrimitive("l").getAsBoolean()) {
                g.setColor(blockColor);
                g.fillRect(x, y, bw, h);
            }

            //right
            if (tile.has("r") && tile.getAsJsonPrimitive("r").getAsBoolean()) {
                g.setColor(blockColor);
                g.fillRect(x + w - bw, y, bw, h);
            }

            //up
            if (tile.has("u") && tile.getAsJsonPrimitive("u").getAsBoolean()) {
                g.setColor(blockColor);
                g.fillRect(x, y, w, bh);
            }

            //down
            if (tile.has("d") && tile.getAsJsonPrimitive("d").getAsBoolean()) {
                g.setColor(blockColor);
                g.fillRect(x, y + h - bh, w, bh);
            }

        }
    }

    private void renderMap(Graphics2D g) {
        if (file == null) {
            return;
        }

        //stamp preview
        if (drawMode == DrawMode.Draw) {
            Stamp previewStamp = getPreviewStamp();
            if (previewStamp != null) {
                //render what will be drawn as preview
                List<JsonObject> preview = draw(previewStamp, getTiles());
                renderMapTiles(preview, g);
            } else {
                //render what is currently drawn
                renderMapTiles(getTiles(), g);
            }
        } else if (drawMode == DrawMode.Erase) {
            List<JsonObject> preview = erase(new Rectangle(getGridPoint(mousePoint), new Dimension(1, 1)), getTiles());
            renderMapTiles(preview, g);

        } else if (drawMode == DrawMode.Select) {
            renderMapTiles(getTiles(), g);
        }

        if (showGrid) {
            g.setColor(gridColor);
            GraphicsUtils.drawGrid(g, grid.width, grid.height, (int) (getWidth() / scale.x), (int) (getHeight() / scale.y));
        }
    }

    private void renderMapTiles(List<JsonObject> mapTiles, Graphics2D g) {

        mapTiles.sort(new TileComparator());

        boolean showTileNumber = false;
        int tileNumber = 0;
        if (showTileNumber) {
            g.setFont(g.getFont().deriveFont(4f));
        }

        for (JsonObject mapTile : mapTiles) {

            JsonObject tileref = mapTile.getAsJsonObject("tileref");
            String img = tileref.get("img").getAsString();

            Map<Integer, Map<Integer, JsonObject>> x2y2tile = ensureTilesetLoaded(img);
            BufferedImage tilesetImage = tilesetCache.get(img).getImage();

            int x = tileref.get("x").getAsInt();
            int y = tileref.get("y").getAsInt();
            JsonObject tile = x2y2tile.get(x).get(y);

            int w = tile.get("w").getAsInt();
            int h = tile.get("h").getAsInt();

            Rectangle src = new Rectangle(x, y, w, h);

            int gx = mapTile.get("gx").getAsInt();
            int gy = mapTile.get("gy").getAsInt();
            int gz = mapTile.get("gz").getAsInt();

            Point dstScreenPoint = getScreenPoint(new Point(gx, gy - gz));

            Rectangle dst = new Rectangle(dstScreenPoint, grid);

            GraphicsUtils.drawImage(g, tilesetImage, src, dst);

            renderTileExtras(dst, tile, g);
            
            if (showTileNumber) {
                String tileNumberStr = String.valueOf(tileNumber);
                int fw = g.getFontMetrics().stringWidth(tileNumberStr);
                int fh = g.getFontMetrics().getHeight();
                int fx = dstScreenPoint.x + (grid.width / 2) - (fw / 2);
                int fy = dstScreenPoint.y + fh - 4 + (grid.height / 2) - (fh / 2);

                g.setColor(Color.white);
                g.drawString(tileNumberStr, fx - 1, fy - 1);
                g.setColor(Color.black);
                g.drawString(tileNumberStr, fx, fy);

                tileNumber++;
            }
        }
    }

    //in JasglLevelMap is the current implementation
    @Deprecated
    private class TileComparator implements Comparator<JsonObject> {

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

            int yComp = Integer.compare(agy, bgy);

            if (yComp == 0) {
                //same line
                int xComp = Integer.compare(agx, bgx);
                if (xComp == 0) {

                }
                return xComp;
            }

            return yComp;
        }

    }

    //map
    private List<JsonObject> draw(Stamp stamp, List<JsonObject> mapTiles) {

        List<JsonObject> result = new ArrayList<>();

        Set<JsonObject> stampTiles = new HashSet<>(stamp.getTiles());

        //overwrite
        for (JsonObject srcTile : mapTiles) {
            JsonObject trgTile = srcTile.deepCopy();

            for (JsonObject stampTile : stampTiles.toArray(new JsonObject[0])) {
                if (tilesOverlapping(trgTile, stampTile)) {

                    //stamp tile was exchanged
                    stampTiles.remove(stampTile);

                    //overwrite tileref
                    trgTile.add("tileref", stampTile.getAsJsonObject("tileref"));
                }
            }

            result.add(trgTile);
        }

        //rest of not exchanged tiles (just add)
        result.addAll(stampTiles);

        return result;
    }

    private List<JsonObject> erase(Rectangle gridRect, List<JsonObject> mapTiles) {

        List<JsonObject> result = new ArrayList<>(mapTiles);

        for (int gy = gridRect.y; gy < gridRect.y + gridRect.height; gy++) {
            for (int gx = gridRect.x; gx < gridRect.x + gridRect.width; gx++) {

                List<JsonObject> tiles = getTilesByGrid(gx, gy, result);

                if (eraseLayer == null || eraseLayer.equals(Stamp.ALL)) {
                    result.removeAll(tiles);
                } else {
                    for (JsonObject mapTile : tiles) {
                        JsonObject tilesetTile = resolveTileRef(mapTile);
                        if (getLayer(tilesetTile).equals(eraseLayer)) {
                            result.remove(mapTile);
                        }
                    }
                }
            }
        }

        return result;
    }

    //img2x2y2tile is ready
    private Map<Integer, Map<Integer, JsonObject>> ensureTilesetLoaded(String img) {

        if (!img2x2y2tile.containsKey(img)) {
            ImageWithMetaData imageWithMetaData = PNGUtils.read(new File(file.getParentFile(), img));
            tilesetCache.put(img, imageWithMetaData);

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

                img2x2y2tile.put(img, x2y2tile);
            }
        }

        return img2x2y2tile.get(img);
    }

    public void resetTilesetCache() {
        img2x2y2tile.clear();
        tilesetCache.clear();
    }
    
    //settings
    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
        repaint();
    }

    public void setShowZ(boolean showZ) {
        this.showZ = showZ;
        repaint();
    }

    public void setShowBlock(boolean showBlock) {
        this.showBlock = showBlock;
        repaint();
    }

    public void setShowLayers(boolean layers) {
        this.showLayers = layers;
        repaint();
    }

    public void setPencilMode(PencilMode drawMode) {
        this.pencilMode = drawMode;
    }

    public void setDrawMode(DrawMode drawMode) {
        this.drawMode = drawMode;

        if (drawMode == DrawMode.Draw) {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        } else if (drawMode == DrawMode.Select) {
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        } else if (drawMode == DrawMode.Erase) {
            setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        }
    }

    public void setFillPencil(boolean fillPencil) {
        this.fillPencil = fillPencil;
    }

    public void setEraseLayer(String eraseLayer) {
        this.eraseLayer = eraseLayer;
    }

    //tileset setting and selection change
    public void setZforSelected(int z) {
        if (!isLoaded()) {
            return;
        }

        for (JsonObject tile : getSelectedTiles()) {
            tile.addProperty("z", z);
        }

        PNGUtils.write(file, data.toString());
        repaint();
    }

    public void setBlockforSelected(boolean l, boolean r, boolean u, boolean d) {
        if (!isLoaded()) {
            return;
        }

        for (JsonObject tile : getSelectedTiles()) {
            tile.addProperty("l", l);
            tile.addProperty("r", r);
            tile.addProperty("u", u);
            tile.addProperty("d", d);
        }

        PNGUtils.write(file, data.toString());
        repaint();
    }

    public void setBlockforSelected(boolean block) {
        setBlockforSelected(block, block, block, block);
    }

    public void setLayerForSelected(String layer) {
        if (!isLoaded()) {
            return;
        }

        for (JsonObject tile : getSelectedTiles()) {
            if (layer == null) {
                tile.remove("layer");
            } else {
                tile.addProperty("layer", layer);
            }
        }

        PNGUtils.write(file, data.toString());
        repaint();
    }

    private void select(List<JsonObject> tiles, boolean append) {
        if (!append) {
            unselect();
        }
        for (JsonObject tile : tiles) {
            selected.add(tile);
        }
    }

    private void select(JsonObject tile) {
        select(Arrays.asList(tile), false);
    }

    private void unselect(JsonObject tile) {
        selected.remove(tile);
    }

    private void unselect() {
        selected.clear();
    }

    private boolean isSelected(JsonObject tile) {
        return selected.contains(tile);
    }

    public void colorToAlpha(MouseEvent evt) {
        if (!isLoaded()) {
            return;
        }

        Color c;
        try {
            int[] colorArray = image.getData().getPixel((int) (evt.getX() / scale.x), (int) (evt.getY() / scale.y), (int[]) null);
            c = new Color(colorArray[0], colorArray[1], colorArray[2]);
        } catch (ArrayIndexOutOfBoundsException e) {
            //ignore
            return;
        }

        BufferedImage prev = image;
        BufferedImage newImage = makeColorTransparent(image, c);

        //preview
        this.image = newImage;
        repaint();

        if (JOptionPane.showConfirmDialog(this, "Color to Alpha correct?") != JOptionPane.YES_OPTION) {
            this.image = prev;
            repaint();
        } else {
            try {
                //write new file
                ImageIO.write(newImage, "png", file);
                //write meta data
                PNGUtils.write(file, data.toString());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private BufferedImage makeColorTransparent(BufferedImage im, final Color color) {
        ImageFilter filter = new RGBImageFilter() {

            // the color we are looking for... Alpha bits are set to opaque
            public int markerRGB = color.getRGB() | 0xFF000000;

            @Override
            public final int filterRGB(int x, int y, int rgb) {
                if ((rgb | 0xFF000000) == markerRGB) {
                    // Mark the alpha bits as zero - transparent
                    return 0x00FFFFFF & rgb;
                } else {
                    // nothing to do
                    return rgb;
                }
            }
        };

        ImageProducer ip = new FilteredImageSource(im.getSource(), filter);
        return createBufferedImage(ip);
    }

    private BufferedImage createBufferedImage(ImageProducer producer) {
        PixelGrabber pg = new PixelGrabber(producer, 0, 0, -1, -1, null, 0, 0);
        try {
            pg.grabPixels();
        } catch (InterruptedException e) {
            throw new RuntimeException("Image fetch interrupted");
        }
        if ((pg.status() & ImageObserver.ABORT) != 0) {
            throw new RuntimeException("Image fetch aborted");
        }
        if ((pg.status() & ImageObserver.ERROR) != 0) {
            throw new RuntimeException("Image fetch error");
        }
        BufferedImage p = new BufferedImage(pg.getWidth(), pg.getHeight(), BufferedImage.TYPE_INT_ARGB);
        p.setRGB(0, 0, pg.getWidth(), pg.getHeight(), (int[]) pg.getPixels(), 0, pg.getWidth());
        return p;
    }

    public static void setRGB(BufferedImage image, int x, int y, int width, int height, int[] pixels) {
        int type = image.getType();
        if (type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB) {
            image.getRaster().setDataElements(x, y, width, height, pixels);
        } else {
            image.setRGB(x, y, width, height, pixels, 0, width);
        }
    }

    //stamps
    //from tileset
    public Stamp getTilesetStamp() {
        Stamp stamp = new Stamp();

        if (!isLoaded() || getSelectedTiles().isEmpty()) {
            //empty
            return stamp;
        }

        int maxY = Integer.MIN_VALUE;
        int minX = Integer.MAX_VALUE;
        //int w = 0;
        //int h = 0;
        for (JsonObject tile : getSelectedTiles()) {
            maxY = Math.max(maxY, tile.getAsJsonPrimitive("y").getAsInt());
            minX = Math.min(minX, tile.getAsJsonPrimitive("x").getAsInt());

            //assumes same w and h for all tiles
            //w = tile.getAsJsonPrimitive("w").getAsInt();
            //h = tile.getAsJsonPrimitive("h").getAsInt();
        }

        for (JsonObject tile : getSelectedTiles()) {

            JsonObject maptile = new JsonObject();

            int x = tile.getAsJsonPrimitive("x").getAsInt();
            int y = tile.getAsJsonPrimitive("y").getAsInt();
            int z = tile.getAsJsonPrimitive("z").getAsInt();
            int w = tile.getAsJsonPrimitive("w").getAsInt();
            int h = tile.getAsJsonPrimitive("h").getAsInt();

            int gx = (x - minX) / w;
            int gy = ((y - maxY) / h) + z;
            int gz = z;

            maptile.addProperty("gx", gx);
            maptile.addProperty("gy", gy);
            maptile.addProperty("gz", gz);

            JsonObject tileref = new JsonObject();
            tileref.addProperty("img", file.getName());
            tileref.addProperty("x", x);
            tileref.addProperty("y", y);

            maptile.add("tileref", tileref);

            //System.out.println(maptile);
            stamp.getTiles().add(maptile);
        }
        //System.out.println("---------------------------------------");

        return stamp;
    }

    //to map
    public void setMapStamp(Stamp mapStamp) {
        this.mapStamp = mapStamp;
        repaint();
    }

    //create preview based on current mouse position
    public Stamp getPreviewStamp() {
        if (mapStamp == null || mousePoint == null) {
            return null;
        }

        Stamp stamp = new Stamp();

        Point gridPoint = getGridPoint(mousePoint);

        for (JsonObject tile : mapStamp.getTiles()) {
            JsonObject stampTile = tile.deepCopy();

            stampTile.addProperty("gx", stampTile.getAsJsonPrimitive("gx").getAsInt() + gridPoint.x);
            stampTile.addProperty("gy", stampTile.getAsJsonPrimitive("gy").getAsInt() + gridPoint.y);

            //System.out.println(stampTile);
            stamp.getTiles().add(stampTile);
        }
        //System.out.println("==========================================");

        return stamp;
    }

    //helper: coord convert
    public Point getGridPoint(Point screenPoint) {
        return new Point(
                ((int) (screenPoint.x / scale.x) / grid.width) + gridOffset.x,
                ((int) (screenPoint.y / scale.y) / grid.height) + gridOffset.y
        );
    }

    public Point getScreenPoint(Point gridPoint) {
        int x = ((gridPoint.x - gridOffset.x) * grid.width);
        int y = ((gridPoint.y - gridOffset.y) * grid.height);
        return new Point(x, y);
    }

    private boolean tilesOverlapping(JsonObject a, JsonObject b) {
        int agx = a.getAsJsonPrimitive("gx").getAsInt();
        int agy = a.getAsJsonPrimitive("gy").getAsInt();
        int agz = a.getAsJsonPrimitive("gz").getAsInt();

        int bgx = b.getAsJsonPrimitive("gx").getAsInt();
        int bgy = b.getAsJsonPrimitive("gy").getAsInt();
        int bgz = b.getAsJsonPrimitive("gz").getAsInt();

        List<JsonObject> ab = Arrays.asList(a, b);
        String[] layers = new String[2];
        for (int i = 0; i < 2; i++) {
            JsonObject tile = resolveTileRef(ab.get(i));
            layers[i] = getLayer(tile);
        }

        return agx == bgx && agy == bgy && agz == bgz && layers[0].equals(layers[1]);
    }

    private String getLayer(JsonObject tile) {
        if (tile.has("layer")) {
            return tile.getAsJsonPrimitive("layer").getAsString();
        }
        return Stamp.OBJECT;
    }

    private BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    private void areaSelect(Point from, Point to, int delta, boolean append) {
        int deltaY = (to.y < from.y ? -1 : 1) * delta;
        int deltaX = (to.x < from.x ? -1 : 1) * delta;

        Function<Integer, Boolean> yfunc = (y) -> (to.y < from.y ? y > to.y : y < to.y);
        Function<Integer, Boolean> xfunc = (x) -> (to.x < from.x ? x > to.x : x < to.x);

        for (int i = from.y; Math.abs(i - to.y) < deltaY || yfunc.apply(i); i += deltaY) {
            for (int j = from.x; Math.abs(j - to.x) < deltaX || xfunc.apply(j); j += deltaX) {
                select(getTilesBy(j, i), append);
            }
        }
    }

    private Rectangle getMapGridRectangle() {

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

    private void saveMapIfChanged() {
        //write if tiles were changed in setTiles
        if (tilesWereChanged) {
            long begin = System.currentTimeMillis();

            Rectangle rect = getMapGridRectangle();

            int w = rect.width * grid.width;
            int h = rect.height * grid.height;

            PNGUtils.write(file, w, h, g -> {
                g.translate(-1 * rect.x * grid.width, -1 * rect.y * grid.height);
                renderMapTiles(getTiles(), g);
            }, data.toString());

            tilesWereChanged = false;

            long end = System.currentTimeMillis();
            System.out.println((end - begin) + " ms map saved " + rect.width + "x" + rect.height + " tiles, " + w + "x" + h + " px");
        }
    }

    public void addListener(TileListener listener) {
        listeners.add(listener);
    }

    public JsonObject resolveTileRef(JsonObject mapTile) {
        JsonObject tileref = mapTile.getAsJsonObject("tileref");
        Map<Integer, Map<Integer, JsonObject>> x2y2tile = ensureTilesetLoaded(tileref.get("img").getAsString());
        return x2y2tile.get(tileref.get("x").getAsInt()).get(tileref.get("y").getAsInt());
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                formMouseMoved(evt);
            }
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                formMouseDragged(evt);
            }
        });
        addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                formMouseWheelMoved(evt);
            }
        });
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                formMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                formMouseReleased(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_formMouseWheelMoved
        if (evt.isControlDown()) {
            double delta;
            if (evt.getUnitsToScroll() < 0) {
                delta = 0.25;
            } else {
                delta = -0.25;
            }

            scale.x += delta;
            if (scale.x < 0.1) {
                scale.x = 0.1;
            } else if (scale.x > 4) {
                scale.x = 4;
            }

            scale.x = Math.round(scale.x * 100.0) / 100.0;
            scale.y = scale.x;

            updatePanelSize();
            repaint();

        } else {
            //int val = jScrollPaneExploration.getVerticalScrollBar().getModel().getValue();
            //int unit = jScrollPaneExploration.getVerticalScrollBar().getUnitIncrement();
            //val += unit * evt.getUnitsToScroll();
            //jScrollPaneExploration.getVerticalScrollBar().getModel().setValue(val);
        }
    }//GEN-LAST:event_formMouseWheelMoved

    private void formMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMousePressed

        if (mode == Mode.Tileset) {
            if (SwingUtilities.isLeftMouseButton(evt)) {
                if (evt.isAltDown()) {
                    colorToAlpha(evt);
                } else if (evt.isShiftDown()) {

                    selected.clear();
                    areaSelect(mousePoint, evt.getPoint(), 4, true);
                    repaint();

                } else {
                    mousePoint = evt.getPoint();
                    select(getTilesBy(evt.getX(), evt.getY()), evt.isControlDown());
                    repaint();
                }
            }
        } else if (mode == Mode.Map) {
            if (SwingUtilities.isLeftMouseButton(evt)) {

                lastGridPoint = getGridPoint(evt.getPoint());

                if (drawMode == DrawMode.Draw) {
                    List<JsonObject> newTiles = draw(getPreviewStamp(), getTiles());
                    setTiles(newTiles);
                } else if (drawMode == DrawMode.Select) {
                    List<JsonObject> selected = getTilesByGrid(lastGridPoint.x, lastGridPoint.y);
                    listeners.forEach(l -> l.selected(selected));
                } else if (drawMode == DrawMode.Erase) {
                    List<JsonObject> newTiles = erase(new Rectangle(lastGridPoint, new Dimension(1, 1)), getTiles());
                    setTiles(newTiles);
                }

                repaint();

            } else if (SwingUtilities.isRightMouseButton(evt)) {
                if(file != null && evt.getClickCount() >= 2) {
                    JasglLevelMap levelMap = new JasglLevelMap(file);
                    PreviewGame game = new PreviewGame(levelMap);
                    game.setGridStartPoint(getGridPoint(evt.getPoint()));
                    GameLoopFrame.loop("Preview (press Esc to close)", 500, 500, Color.black, game);
                }
            }
        }
    }//GEN-LAST:event_formMousePressed

    private void formMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseDragged
        if (mode == Mode.Tileset) {
            if (SwingUtilities.isLeftMouseButton(evt) && mousePoint != null) {
                areaSelect(mousePoint, evt.getPoint(), 4, true);
                repaint();
            }
        } else if (mode == Mode.Map) {
            if (SwingUtilities.isLeftMouseButton(evt)) {

                //do nothing if grid was not changed
                Point nowGridPoint = getGridPoint(evt.getPoint());
                if (nowGridPoint.equals(lastGridPoint)) {
                    return;
                }

                if (drawMode == DrawMode.Draw) {
                    if (pencilMode == PencilMode.Point) {
                        //getPreviewStamp uses mousePoint
                        mousePoint = evt.getPoint();

                        //draw and save it
                        List<JsonObject> newTiles = draw(getPreviewStamp(), getTiles());
                        setTiles(newTiles);
                    }
                } else if (drawMode == DrawMode.Erase) {
                    if (pencilMode == PencilMode.Point) {
                        List<JsonObject> newTiles = erase(new Rectangle(nowGridPoint, new Dimension(1, 1)), getTiles());
                        setTiles(newTiles);
                    }
                }

                repaint();

            } else if (SwingUtilities.isRightMouseButton(evt)) {

            }
        }
    }//GEN-LAST:event_formMouseDragged

    private void formMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseReleased
        if (mode == Mode.Tileset) {
            if (SwingUtilities.isLeftMouseButton(evt)) {

            }
        } else if (mode == Mode.Map) {
            if (SwingUtilities.isLeftMouseButton(evt)) {

                if (pencilMode == PencilMode.Point) {
                    //TODO save in history here

                    saveMapIfChanged();
                }

            } else if (SwingUtilities.isRightMouseButton(evt)) {

            }
        }
    }//GEN-LAST:event_formMouseReleased

    private void formMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseMoved
        if (mode == Mode.Tileset) {
            return;
        }

        mousePoint = evt.getPoint();
        repaint();
    }//GEN-LAST:event_formMouseMoved


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
