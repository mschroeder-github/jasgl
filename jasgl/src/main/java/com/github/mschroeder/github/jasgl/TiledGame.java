package com.github.mschroeder.github.jasgl;

import org.mapeditor.core.MapObject;
import org.mapeditor.core.ObjectGroup;

/**
 * A tiled game is a game that has one tiled map, a sprite that is controlled by
 * the player, camera movement focusing on the player's sprite, sprite loading from the map and
 * event handling.
 * @author Markus Schr&ouml;der
 */
public abstract class TiledGame implements Game {

    protected GameLoop gameLoop;
    
    //the positioner that moves the player's sprite
    protected InputBasedPositioner playerSpritePositioner;
    //camera to follow the player's sprite
    protected SpriteCamera playerSpriteCamera;
    
    //the map in which the player's sprite moves
    protected TiledOrthogonalLevelMap map;
    //other sprites (NPCs, doors, etc.)
    protected ListOfSprites sprites;

    public TiledGame(InputBasedPositioner playerSpritePositioner, SpriteCamera playerSpriteCamera, TiledOrthogonalLevelMap map) {
        this.playerSpritePositioner = playerSpritePositioner;
        this.playerSpriteCamera = playerSpriteCamera;
        this.map = map;
        
        this.sprites = new ListOfSprites();
    }
    
    /**
     * The sprite the player controls in this game.
     * @param sprite 
     */
    public void setPlayerSprite(Sprite sprite) {
        playerSpritePositioner.sprites.clear();
        playerSpritePositioner.sprites.add(sprite);
        
        playerSpriteCamera.setSprite(sprite);
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
    
}
