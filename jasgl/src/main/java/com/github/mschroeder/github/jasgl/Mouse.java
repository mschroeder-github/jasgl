package com.github.mschroeder.github.jasgl;

/**
 * Game input method is used to access the mouse device state.
 * @author Markus Schr&ouml;der
 */
public interface Mouse {
    
    /**
     * Is left mouse button in the current frame pressed.
     * @return 
     */
    public boolean left();
    
    /**
     * Is middle mouse button in the current frame pressed.
     * @return 
     */
    public boolean middle();
    
    
    /**
     * Is right mouse button in the current frame pressed.
     * @return 
     */
    public boolean right();
    
}
