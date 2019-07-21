package com.github.mschroeder.github.jasgl;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;

/**
 * A sprite is a moveable object in the game.
 * @author Markus Schr&ouml;der
 */
public abstract class Sprite {
    
    /**
     * Position of the sprite.
     */
    public Point.Double pos;
    
    public Sprite() {
        pos = new Point2D.Double();
    }
    
    /**
     * Updates the inner state of this sprite without knowing the external
     * game state.
     * @param ms 
     */
    public abstract void update(double ms);
    
    /**
     * Render the sprite.
     * @param g 
     */
    public abstract void render(Graphics2D g);
    
    public int getX() {
        return (int) Math.round(pos.x);
    }
    
    public int getY() {
        return (int) Math.round(pos.y);
    }
    
}
