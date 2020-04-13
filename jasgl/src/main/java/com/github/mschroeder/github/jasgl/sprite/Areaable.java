package com.github.mschroeder.github.jasgl.sprite;

import java.awt.geom.Area;

/**
 * The sprite is not only a point: it has an arbitrary area.
 * @author Markus Schr&ouml;der
 */
public interface Areaable {
    
    /**
     * Returns an area of the sprite.
     * @return 
     */
    public Area getArea();
    
}
