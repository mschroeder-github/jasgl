package com.github.mschroeder.github.jasgl;

import com.github.mschroeder.github.jasgl.JASGLUtils.Direction;

/**
 * A sprite is pivotable when you can set a direction (left, up, right, down).
 * @author Markus Schr&ouml;der
 */
public interface Pivotable {
    
    /**
     * Sets the direction of the pivotable sprite.
     * @param dir 
     */
    public void setDirection(Direction dir);
    
}
