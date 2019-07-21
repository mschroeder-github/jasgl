package com.github.mschroeder.github.jasgl;

/**
 * An animator will be update with the update part of the game loop and calls
 * next whenever a certain time is passed.
 * @author Markus Schr&ouml;der
 */
public abstract class Animator {
    
    private double elapsed;
    private double wait;
    
    private boolean paused;
    
    public Animator() {
        paused = true;
    }
    
    public void update(double ms) {
        if(paused)
            return;
        
        elapsed += ms;
        
        if(elapsed >= wait) {
            wait = next(false);
            elapsed = 0;
        }
    }
    
    
    
    public void play() {
        wait = next(true);
        paused = false;
    }
    
    public void pause() {
        paused = true;
    }
    
    public void stop() {
        paused = true;
    }

    public boolean isPaused() {
        return paused;
    }
    
    /**
     * Implementations have to implement this method to state when the next
     * next() call should happen. The implemention can decide what will happen.
     * @param first true if it is the first call of the animator.
     * @return in millisecond when the next next() call should happen.
     */
    public abstract double next(boolean first);
    
}
