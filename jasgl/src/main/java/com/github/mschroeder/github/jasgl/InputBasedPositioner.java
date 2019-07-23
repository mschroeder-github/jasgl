package com.github.mschroeder.github.jasgl;

/**
 * The positioner moves sprites based on input (e.g. key pressed or mouse clicked).
 * @author Markus Schr&ouml;der
 */
public abstract class InputBasedPositioner extends Positioner {
    
    public abstract void input(Keyboard keyboard, Mouse mouse);
    
}
