package com.github.mschroeder.github.jasgl;

import org.mapeditor.core.MapObject;
import org.mapeditor.core.ObjectGroup;

/**
 * A tiled game is a game that has one tiled map, a sprite that is controlled by
 * the player, camera movement focusing on the player's sprite, sprite loading from the map and
 * event handling.
 * @author Markus Schr&ouml;der
 */
public abstract class TiledGame implements Game, GameState {

    protected GameMultiplexer gameMultiplexer;
    protected GameLoop gameLoop;
    
    //the positioner that moves the player's sprite
    protected InputBasedPositioner playerSpritePositioner;
    //camera to follow the player's sprite
    protected SpriteCamera playerSpriteCamera;
    
    //the map in which the player's sprite moves
    protected TiledLevelMap map;
    //other sprites (NPCs, doors, etc.)
    protected ListOfSprites sprites;
    
    protected Sprite playerSprite;

    protected GameScriptEngine scriptEngine;

    public TiledGame(InputBasedPositioner playerSpritePositioner, SpriteCamera playerSpriteCamera, TiledLevelMap map) {
        this.playerSpritePositioner = playerSpritePositioner;
        this.playerSpriteCamera = playerSpriteCamera;
        this.map = map;
        
        this.sprites = new ListOfSprites();
        
        //level map to sprites
        for(ObjectGroup og : map.getObjectGroups()) {
            for(MapObject mo : og.getObjects()) {
                loadSprite(og, mo, sprites);
            }
        }
    }
    
    /**
     * The sprite the player controls in this game.
     * @param sprite null if you want to reset the player sprite
     */
    public void setPlayerSprite(Sprite sprite) {
        
        //reset
        if(sprite == null) {
            if(this.playerSprite != null) {
                
                playerSpritePositioner.sprites.clear();
                playerSpritePositioner.reset();
                playerSpriteCamera.setSprite(null);
                sprites.remove(this.playerSprite);
                
                this.playerSprite = null;
            }
        } else {
            this.playerSprite = sprite;

            playerSpritePositioner.sprites.clear();
            playerSpritePositioner.sprites.add(sprite);

            playerSpriteCamera.setSprite(sprite);

            sprites.addIfAbsent(sprite);
        }
    }
    
    /**
     * Teleports the player to another game and switchs to the game.
     * @param gameName name of the game registered in gameMultiplexer
     * @param x the player's new x position
     * @param y the player's new y position
     */
    public void teleportPlayer(String gameName, double x, double y) {
        if(this.playerSprite == null)
            throw new RuntimeException("playerSprite is null");
        
        //the teleported sprite
        Sprite teleportedSprite = this.playerSprite;
        //new position in the other game
        teleportedSprite.pos.x = x;
        teleportedSprite.pos.y = y;
        
        //reset the player sprite in this game
        this.setPlayerSprite(null);
        
        TiledGame targetGame = (TiledGame) gameMultiplexer.getGame(gameName);
        if(targetGame == null)
            throw new RuntimeException(gameName + " not found for teleport");
        
        //give target game the player
        targetGame.setPlayerSprite(teleportedSprite);
        
        //stop the animation if it is animated
        if(teleportedSprite instanceof SpriteSheetSprite) {
            SpriteSheetSprite sss = (SpriteSheetSprite) teleportedSprite;
            sss.stop();
        }
        
        //update camera
        targetGame.playerSpriteCamera.update(0);
        
        //switch to game
        gameMultiplexer.switchTo(gameName);
    }
    
    /**
     * In your implementation you decide how a map object is loaded as a sprite.
     * @param objectGroup the object layer
     * @param mapObject the object in the layer
     * @param sprites the list where you add a sprite based on the mapObject
     */
    public abstract void loadSprite(ObjectGroup objectGroup, MapObject mapObject, ListOfSprites sprites);
    
    @Override
    public void init(GameLoop gameLoop) {
        this.gameLoop = gameLoop;
        playerSpriteCamera.init(gameLoop);
        playerSpriteCamera.update(0);
    }

    @Override
    public void input(Keyboard keyboard, Mouse mouse) {
        playerSpritePositioner.input(keyboard, mouse);
    }

    @Override
    public void update(double ms) {
        playerSpritePositioner.update(ms);
        sprites.update(ms);
        playerSpriteCamera.update(ms);
    }

    public ListOfSprites getSprites() {
        return sprites;
    }

    @Override
    public void init(GameMultiplexer gameMultiplexer) {
        this.gameMultiplexer = gameMultiplexer;
    }

    @Override
    public void enter(String predecessorName, Game predecessor) {
    }

    @Override
    public void leave(String successorName, Game successor) {
    }

    public TiledLevelMap getMap() {
        return map;
    }
    
    @Override
    public void setScriptEngine(GameScriptEngine scriptEngine) {
        this.scriptEngine = scriptEngine;
    }
    
}
