package com.github.mschroeder.github.jasgl.positioner;

import com.github.mschroeder.github.jasgl.Keyboard;
import com.github.mschroeder.github.jasgl.Mouse;
import com.github.mschroeder.github.jasgl.positioner.Positioner;

/**
 * The positioner moves sprites based on input (e.g. key pressed or mouse clicked).
 * @author Markus Schr&ouml;der
 */
public abstract class InputBasedPositioner extends Positioner {
    
    public abstract void input(Keyboard keyboard, Mouse mouse);
    
    /**
     * Use this method to reset the input-memory of the positioner.
     */
    public abstract void reset();
    
}
