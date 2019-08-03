package com.github.mschroeder.github.jasgl;

import java.awt.Graphics2D;

/**
 * The main game logic containing the three major parts: input, update and render.
 * @author Markus Schr&ouml;der
 */
public interface Game {
    
    /**
     * To access the game loop and change it in-game.
     * @param gameLoop 
     */
    public void init(GameLoop gameLoop);
    
    /**
     * Decide in a frame how input changes game state. 
     * @param keyboard
     * @param mouse 
     */
    public void input(Keyboard keyboard, Mouse mouse);
    
    /**
     * Update the game state as if given milliseconds are elapsed.
     * @param ms 
     */
    public void update(double ms);
    
    /**
     * Render the frame of the current game state.
     * @param g 
     */
    public void render(Graphics2D g);
    
    /**
     * Set a script engine.
     * @param scriptEngine
     */
    public void setScriptEngine(GameScriptEngine scriptEngine);
    

}
