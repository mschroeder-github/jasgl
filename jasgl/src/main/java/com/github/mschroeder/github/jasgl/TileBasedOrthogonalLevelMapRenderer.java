package com.github.mschroeder.github.jasgl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.function.Consumer;
import org.mapeditor.core.Map;
import org.mapeditor.core.MapObject;
import org.mapeditor.core.ObjectGroup;
import org.mapeditor.core.Tile;
import org.mapeditor.core.TileLayer;
import org.mapeditor.view.MapRenderer;

/**
 * With a custom tile consumer you can render each tile individually.
 * There is also the possibility to pass parameters.
 * @author Markus Schr&ouml;der
 */
public class TileBasedOrthogonalLevelMapRenderer {

    private OrthogonalRenderer orthogonalRenderer;
    /**
     * Can decide if and how tile should be drawn.
     * Can also use drawDefault() to draw the default.
     */
    private Consumer<DrawContext> tileRenderer;
    
    private TiledLevelMap map;

    public TileBasedOrthogonalLevelMapRenderer(TiledLevelMap map) {
        this.map = map;
        this.orthogonalRenderer = new OrthogonalRenderer(map.getTiledMap());
    }
    
    public class DrawContext {
        private Point point;
        private Tile tile;
        private TileLayer tileLayer;
        private Graphics2D graphics;
        private Image image;
        private java.util.Map<String, Object> params;

        public DrawContext(Point point, Tile tile, TileLayer tileLayer, Graphics2D graphics, Image image, java.util.Map<String, Object> params) {
            this.point = point;
            this.tile = tile;
            this.tileLayer = tileLayer;
            this.graphics = graphics;
            this.image = image;
            this.params = params;
        }
    
        public void drawDefault() {
            graphics.drawImage(image, point.x, point.y, null);
        }

        public Point getPoint() {
            return point;
        }

        public Tile getTile() {
            return tile;
        }

        public Graphics2D getGraphics() {
            return graphics;
        }

        public Image getImage() {
            return image;
        }

        public java.util.Map<String, Object> getParams() {
            return params;
        }

        public TileLayer getTileLayer() {
            return tileLayer;
        }
        
    }
    
    public class OrthogonalRenderer implements MapRenderer {

        private final Map map;

        /**
         * <p>
         * Constructor for OrthogonalRenderer.</p>
         *
         * @param map a {@link org.mapeditor.core.Map} object.
         */
        public OrthogonalRenderer(Map map) {
            this.map = map;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Dimension getMapSize() {
            return new Dimension(
                    map.getWidth() * map.getTileWidth(),
                    map.getHeight() * map.getTileHeight());
        }

        /**
         * {@inheritDoc}
         */
        public void paintTileLayer(Graphics2D g, TileLayer layer, java.util.Map<String, Object> params) {
            final Rectangle clip = g.getClipBounds();
            final int tileWidth = map.getTileWidth();
            final int tileHeight = map.getTileHeight();
            final Rectangle bounds = layer.getBounds();

            g.translate(bounds.x * tileWidth, bounds.y * tileHeight);
            clip.translate(-bounds.x * tileWidth, -bounds.y * tileHeight);

            clip.height += map.getTileHeightMax();

            final int startX = Math.max(0, clip.x / tileWidth);
            final int startY = Math.max(0, clip.y / tileHeight);
            final int endX = Math.min(layer.getWidth(),
                    (int) Math.ceil(clip.getMaxX() / tileWidth));
            final int endY = Math.min(layer.getHeight(),
                    (int) Math.ceil(clip.getMaxY() / tileHeight));

            for (int x = startX; x < endX; ++x) {
                for (int y = startY; y < endY; ++y) {
                    final Tile tile = layer.getTileAt(x, y);
                    if (tile == null) {
                        continue;
                    }
                    final Image image = tile.getImage();
                    if (image == null) {
                        continue;
                    }

                    Point drawLoc = new Point(x * tileWidth, (y + 1) * tileHeight - image.getHeight(null));

                    // Add offset from tile layer property
                    drawLoc.x += layer.getOffsetX() != null ? layer.getOffsetX() : 0;
                    drawLoc.y += layer.getOffsetY() != null ? layer.getOffsetY() : 0;

                    // Add offset from tileset property
                    drawLoc.x += tile.getTileSet().getTileoffset() != null ? tile.getTileSet().getTileoffset().getX() : 0;
                    drawLoc.y += tile.getTileSet().getTileoffset() != null ? tile.getTileSet().getTileoffset().getY() : 0;

                    if(tileRenderer != null) {
                        tileRenderer.accept(new DrawContext(drawLoc, tile, layer, g, image, params));
                    } else {
                        g.drawImage(image, drawLoc.x, drawLoc.y, null);
                    }
                }
            }

            g.translate(-bounds.x * tileWidth, -bounds.y * tileHeight);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void paintObjectGroup(Graphics2D g, ObjectGroup group) {
            final Dimension tsize = new Dimension(map.getTileWidth(), map.getTileHeight());
            assert tsize.width != 0 && tsize.height != 0;
            final Rectangle bounds = map.getBounds();

            g.translate(
                    bounds.x * tsize.width,
                    bounds.y * tsize.height);

            for (MapObject mo : group) {
                final double ox = mo.getX();
                final double oy = mo.getY();
                final Double objectWidth = mo.getWidth();
                final Double objectHeight = mo.getHeight();
                final double rotation = mo.getRotation();
                final Tile tile = mo.getTile();

                if (tile != null) {
                    Image objectImage = tile.getImage();
                    AffineTransform old = g.getTransform();
                    g.rotate(Math.toRadians(rotation));
                    g.drawImage(objectImage, (int) ox, (int) oy, null);
                    g.setTransform(old);
                } else if (objectWidth == null || objectWidth == 0
                        || objectHeight == null || objectHeight == 0) {
                    g.setRenderingHint(
                            RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setColor(Color.black);
                    g.fillOval((int) ox + 1, (int) oy + 1, 10, 10);
                    g.setColor(Color.orange);
                    g.fillOval((int) ox, (int) oy, 10, 10);
                    g.setRenderingHint(
                            RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_OFF);
                } else {
                    g.setColor(Color.black);
                    g.drawRect((int) ox + 1, (int) oy + 1,
                            mo.getWidth().intValue(),
                            mo.getHeight().intValue());
                    g.setColor(Color.orange);
                    g.drawRect((int) ox, (int) oy,
                            mo.getWidth().intValue(),
                            mo.getHeight().intValue());
                }
                final String s = mo.getName() != null ? mo.getName() : "(null)";
                g.setColor(Color.black);
                g.drawString(s, (int) (ox - 5) + 1, (int) (oy - 5) + 1);
                g.setColor(Color.white);
                g.drawString(s, (int) (ox - 5), (int) (oy - 5));
            }

            g.translate(
                    -bounds.x * tsize.width,
                    -bounds.y * tsize.height);
        }

        @Override
        public void paintTileLayer(Graphics2D g, TileLayer layer) {
            paintTileLayer(g, layer, new HashMap<>());
        }
    }

    public void render(String layerName, Graphics2D g) {
        render(layerName, g, new HashMap<>());
    }
    
    public void render(String layerName, Graphics2D g, java.util.Map<String, Object> params) {
        if(layerName == null) {
            for(TileLayer tl : map.getTileLayers()) {
                orthogonalRenderer.paintTileLayer(g, tl, params);
            }
        } else {
            TileLayer tl = map.getTileLayerByName(layerName);
            if(tl != null) {
                orthogonalRenderer.paintTileLayer(g, tl, params);
            }
        }
    }
    
    public Consumer<DrawContext> getTileRenderer() {
        return tileRenderer;
    }

    public void setTileRenderer(Consumer<DrawContext> tileRenderer) {
        this.tileRenderer = tileRenderer;
    }

}
