package com.github.mschroeder.github.jasgl;

import java.awt.Dimension;
import java.awt.Graphics2D;

/**
 * Represents one Level as known as a map.
 * @author Markus Schr&ouml;der
 */
public abstract class LevelMap {
    
    /**
     * A map has to render a certain layer itself.
     * This way you can render layers in a specific order to visualize overlaps.
     * @param layerName name of the layer that should be rendered. If null,
     * all layers should be rendered.
     * @param g 
     */
    public abstract void render(String layerName, Graphics2D g);
    
    /**
     * Returns the maximum dimension in pixel.
     * @return 
     */
    public abstract Dimension getDimension();
    
}
