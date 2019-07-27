package com.github.mschroeder.github.jasgl;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Point2D;

/**
 * A sprite is a moveable object in the game.
 * @author Markus Schr&ouml;der
 */
public abstract class Sprite implements Areaable {
    
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

    /**
     * The position point as small rectangle.
     * @return 
     */
    @Override
    public Area getArea() {
        return new Area(new Rectangle.Double(pos.x, pos.y, 0.0001, 0.0001));
    }
    
    
}
