package com.github.mschroeder.github.jasgl.levelmap.collision;

import com.github.mschroeder.github.jasgl.sprite.Sprite;
import java.awt.geom.Area;

/**
 * This interface can be used to specify when a sprite collides with something.
 * Just return true or false. This can be used by Positioners to know when to 
 * stop the movement.
 * @author Markus Schr&ouml;der
 */
public interface CollisionDecisionMaker {
    
    /**
     * Decides if sprite with this area collides on something.
     * @param sprite
     * @param area
     * @return 
     */
    public boolean collides(Sprite sprite, Area area);
    
}
