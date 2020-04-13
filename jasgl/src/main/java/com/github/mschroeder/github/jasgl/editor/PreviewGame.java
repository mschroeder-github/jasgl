package com.github.mschroeder.github.jasgl.editor;

import com.github.mschroeder.github.jasgl.Game;
import com.github.mschroeder.github.jasgl.GameLoop;
import com.github.mschroeder.github.jasgl.Keyboard;
import com.github.mschroeder.github.jasgl.Mouse;
import com.github.mschroeder.github.jasgl.levelmap.JasglLevelMap;
import com.github.mschroeder.github.jasgl.levelmap.camera.SpriteCamera;
import com.github.mschroeder.github.jasgl.levelmap.renderer.JasglLevelMapRenderer;
import com.github.mschroeder.github.jasgl.positioner.RpgMakerCharPositioner;
import com.github.mschroeder.github.jasgl.sprite.DummyPersonSprite;
import com.github.mschroeder.github.jasgl.sprite.ListOfSprites;
import com.github.mschroeder.github.jasgl.util.JASGLUtils;
import com.github.mschroeder.github.jasgl.util.JASGLUtils.Direction;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;

/**
 *
 * @author Markus Schr&ouml;der
 */
public class PreviewGame implements Game {

    private GameLoop gameLoop;
    private JasglLevelMap map;
    private JasglLevelMapRenderer mapRenderer;
    private ListOfSprites listOfSprites;
    private SpriteCamera spriteCamera;
    private RpgMakerCharPositioner positioner;
    private Point gridStartPoint = new Point(0,0);
    private DummyPersonSprite sprite;
    
    
    public PreviewGame(JasglLevelMap map) {
        this.map = map;
        mapRenderer = new JasglLevelMapRenderer(map);
        spriteCamera = new SpriteCamera();
        positioner = new RpgMakerCharPositioner();
        
        sprite = new DummyPersonSprite();
        
        //own list
        listOfSprites = new ListOfSprites();
        listOfSprites.add(sprite);
        
        //positioner list
        positioner.setPositionKeyCodes(JASGLUtils.WASD_NEO);
        positioner.getSprites().add(sprite);
        
        positioner.getCollisionDecisionMakers().add((sprite, area) -> {
            Rectangle rect = area.getBounds();
            Direction dir = JASGLUtils.direction(rect.x, rect.y);
            
            int x = sprite.getX() + rect.x;
            int y = sprite.getY() + rect.y;
            
            int gx = x / map.getGrid().width;
            int gy = y / map.getGrid().height;
            
            return map.isBlock(gx, gy, dir);
        });
    }
    
    @Override
    public void init(GameLoop gameLoop) {
        this.gameLoop = gameLoop;
        gameLoop.enableDebug();
        
        sprite.pos.x = gridStartPoint.x * map.getGrid().width;
        sprite.pos.y = gridStartPoint.y * map.getGrid().height;
    }

    @Override
    public void input(Keyboard keyboard, Mouse mouse) {
        if(keyboard.pressed(KeyEvent.VK_ESCAPE)) {
            gameLoop.close();
        }
        positioner.input(keyboard, mouse);
    }

    @Override
    public void update(double ms) {
        positioner.update(ms);
        spriteCamera.update(ms);
        listOfSprites.update(ms);
    }

    @Override
    public void render(Graphics2D g) {
        spriteCamera.render(g);
        
        mapRenderer.render(listOfSprites, g);
        
        spriteCamera.reset(g);
    }

    public Point getGridStartPoint() {
        return gridStartPoint;
    }

    public void setGridStartPoint(Point gridStartPoint) {
        this.gridStartPoint = gridStartPoint;
    }
    
}
