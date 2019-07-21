package com.github.mschroeder.github.jasgl;

/**
 * A game state can be used together with a game multiplexer.
 * @author Markus Schr&ouml;der
 */
public interface GameState extends Game {
    
    /**
     * Give the game state access to the mutliplexer to switch to another game state.
     * @param gameMultiplexer 
     */
    public void init(GameMultiplexer gameMultiplexer);
    
    /**
     * This is called when the game state is entered.
     * @param predecessor 
     */
    public void enter(String predecessorName, Game predecessor);
    
    /**
     * This is called when the game state is left.
     * @param successor 
     */
    public void leave(String successorName, Game successor);
    
}
