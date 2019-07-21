package com.github.mschroeder.github.jasgl;

/**
 * A positioner helps you to move a sprite in a certain way maybe based on input.
 * @author Markus Schr&ouml;der
 */
public abstract class Positioner {
    
    /**
     * The sprites that will be moved by this positioner.
     */
    protected ListOfSprites sprites;
    
    public Positioner() {
        sprites = new ListOfSprites();
    }
    
    /**
     * Update the positioner.
     * @param ms 
     */
    public abstract void update(double ms);

    public ListOfSprites getSprites() {
        return sprites;
    }
    
}
