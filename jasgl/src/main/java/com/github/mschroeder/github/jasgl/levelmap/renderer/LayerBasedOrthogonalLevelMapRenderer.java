package com.github.mschroeder.github.jasgl.levelmap.renderer;

import com.github.mschroeder.github.jasgl.levelmap.TiledLevelMap;
import com.github.mschroeder.github.jasgl.levelmap.TiledLevelMap;
import java.awt.Graphics2D;
import org.mapeditor.core.TileLayer;
import org.mapeditor.view.OrthogonalRenderer;

/**
 * Each layer is rendered separately.
 * @author Markus Schr&ouml;der
 */
public class LayerBasedOrthogonalLevelMapRenderer {
    
    private OrthogonalRenderer orthogonalRenderer;
    private TiledLevelMap map;

    public LayerBasedOrthogonalLevelMapRenderer(TiledLevelMap map) {
        this.map = map;
        this.orthogonalRenderer = new OrthogonalRenderer(map.getTiledMap());
    }
    
    /**
     * A map has to render a certain layer itself.
     * This way you can render layers in a specific order to visualize overlaps.
     * @param layerName name of the layer that should be rendered. If null,
     * all layers should be rendered.
     * @param g 
     */
    public void render(String layerName, Graphics2D g) {
        if(layerName == null) {
            for(TileLayer tl : map.getTileLayers()) {
                orthogonalRenderer.paintTileLayer(g, tl);
            }
        } else {
            TileLayer tl = map.getTileLayerByName(layerName);
            if(tl != null) {
                orthogonalRenderer.paintTileLayer(g, tl);
            }
        }
    }
    
}
