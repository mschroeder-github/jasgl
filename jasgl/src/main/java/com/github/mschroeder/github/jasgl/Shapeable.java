package com.github.mschroeder.github.jasgl;

import java.awt.Shape;

/**
 * A Sprite is shapeable when it returns a shape that represents its body in the world
 * that occupies space.
 * @author Markus Schr&ouml;der
 */
public interface Shapeable {
    
    /**
     * Returns a shape: can be many forms like rectangle or ellipse.
     * @return 
     */
    public Shape getShape();
    
}
