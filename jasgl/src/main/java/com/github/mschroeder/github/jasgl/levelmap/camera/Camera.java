package com.github.mschroeder.github.jasgl.levelmap.camera;

import com.github.mschroeder.github.jasgl.GameLoop;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;

/**
 * The camera controls the view port.
 * @author Markus Schr&ouml;der
 */
public abstract class Camera {
    
    protected GameLoop gameLoop;
    
    /**
     * The offset to move the view port.
     */
    protected Point.Double offset;
    
    public Camera() {
        offset = new Point2D.Double();
    }
    
    /**
     * Update the camera.
     * @param ms 
     */
    public abstract void update(double ms);
    
    /**
     * Get the game loop to access for example screen size. 
     * @param gameLoop 
     */
    public void init(GameLoop gameLoop) {
        this.gameLoop = gameLoop;
    }
    
    /**
     * Adjusts the camera in the graphics.
     * @param g 
     */
    public void render(Graphics2D g) {
        g.translate(-offset.x, -offset.y);
    }
    
    /**
     * Resets the camera adjustment.
     * @param g 
     */
    public void reset(Graphics2D g) {
        g.translate(offset.x, offset.y);
    }
    
}
