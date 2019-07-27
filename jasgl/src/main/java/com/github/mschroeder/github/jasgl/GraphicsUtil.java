package com.github.mschroeder.github.jasgl;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;

/**
 * Helper methods when rendering with Graphics2D.
 * @author Markus Schr&ouml;der
 */
public class GraphicsUtil {
    
    /**
     * Draws an image.
     * @param g
     * @param img 
     * @param src source rectangle create a subimage from img
     * @param dst where to render it
     */
    public static void drawImage(Graphics2D g, Image img, Rectangle src, Rectangle dst) {
        g.drawImage(img, 
            dst.x, dst.y, dst.x + dst.width, dst.y + dst.height,
            src.x, src.y, src.x + src.width, src.y + src.height,
            null);
    }
    
    /**
     * Draws a cross with bounds of rectangle.
     * @param g
     * @param src 
     */
    public static void drawCross(Graphics2D g, Rectangle src) {
        g.drawLine(
                (int)(src.getX() + (src.getWidth()/2.0)),
                src.y, 
                (int)(src.getX() + (src.getWidth()/2.0)), 
                src.y + src.height
        );
        g.drawLine(
                src.x,
                (int)(src.getY() + (src.getHeight()/2.0)), 
                src.x + src.width, 
                (int)(src.getY() + (src.getHeight()/2.0))
        );
    }
    
}
