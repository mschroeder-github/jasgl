package com.github.mschroeder.github.jasgl;

/**
 * Interface for accessing GameLoop from Game.
 * GameLoopFrame impls this.
 * Game has a init method.
 * @author Markus Schr&ouml;der
 */
public interface GameLoop {
    
    /**
     * Enables debug options.
     */
    public void enableDebug();
    
    /**
     * Closes the game loop because game should exit.
     */
    public void close();
    
    /**
     * The current frame.
     * @return 
     */
    public int frame();
    
}
