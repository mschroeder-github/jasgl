package com.github.mschroeder.github.jasgl;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * With the game multiplexer you can switch in your game between many game states.
 * @author Markus Schr&ouml;der
 */
public class GameMultiplexer implements Game {

    private GameLoop gameLoop;
    
    //to hold the save game
    private GameMemory memory;
    
    //loaded games with names in order to refer to them
    private Map<String, Game> name2game;
    
    //the current game running
    private String currentGame;
    
    public GameMultiplexer() {
        name2game = new HashMap<>();
    }
    
    public GameMultiplexer addGame(String name, Game game) {
        name2game.put(name, game);
        if(gameLoop != null) {
            game.init(gameLoop);
        }
        if(game instanceof GameState) {
            ((GameState)game).init(this);
        }
        return this;
    }
    
    public GameMultiplexer removeGame(String name) {
        name2game.remove(name);
        return this;
    }
    
    public Game getGame(String name) {
        return name2game.get(name);
    }
    
    public String getName(Game game) {
        for(Entry<String, Game> e : name2game.entrySet()) {
            if(e.getValue().equals(game)) {
                return e.getKey();
            }
        }
        return null;
    }
    
    /**
     * Switch to another registered game.
     * @param nameOfGame 
     * @return this instance
     */
    public GameMultiplexer switchTo(String nameOfGame) {
        Game oldGame = null;
        Game nextGame = null;
        
        //old one deinit
        if(validGameName(currentGame)) {
            oldGame = name2game.get(currentGame);
        } 
        
        //new one init
        if(validGameName(nameOfGame)) {
            nextGame = name2game.get(nameOfGame);
        } 
        
        //transition
        if(oldGame != null && nextGame != null) {
            
            //leave old game
            if(oldGame instanceof GameState) {
                ((GameState)oldGame).leave(nameOfGame, nextGame);
            }
            
            //enter new game
            if(nextGame instanceof GameState) {
                ((GameState)nextGame).enter(currentGame, oldGame);
            }
        }
        
        //switch
        this.currentGame = nameOfGame;
        
        return this;
    }
    
    @Override
    public void init(GameLoop gameLoop) {
        this.gameLoop = gameLoop;
        for(Game game : name2game.values()) {
            game.init(gameLoop);
        }
    }

    @Override
    public void input(Keyboard keyboard, Mouse mouse) {
        if(validGameName(currentGame)) {
            name2game.get(currentGame).input(keyboard, mouse);
        }
    }

    @Override
    public void update(double ms) {
        if(validGameName(currentGame)) {
            name2game.get(currentGame).update(ms);
        }
    }

    @Override
    public void render(Graphics2D g) {
        if(validGameName(currentGame)) {
            name2game.get(currentGame).render(g);
        }
    }
    
    private boolean validGameName(String name) {
        return name != null && name2game.containsKey(name);
    }
    
    public List<Game> getGames() {
        return new ArrayList<>(name2game.values());
    }
    
    public Set<Entry<String, Game>> getNamedGames() {
        return name2game.entrySet();
    }

    public GameMemory getMemory() {
        return memory;
    }

    public void setMemory(GameMemory memory) {
        this.memory = memory;
    }
    
    public boolean hasMemory() {
        return this.memory != null;
    }
    
}
