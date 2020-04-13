package com.github.mschroeder.github.jasgl.levelmap.camera;

import com.github.mschroeder.github.jasgl.sprite.Sprite;
import com.github.mschroeder.github.jasgl.levelmap.LevelMap;
import com.github.mschroeder.github.jasgl.levelmap.camera.Camera;
import com.github.mschroeder.github.jasgl.sprite.Sprite;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Area;

/**
 * A camera that follows a sprite.
 * @author Markus Schr&ouml;der
 */
public class SpriteCamera extends Camera {

    protected Sprite sprite;
    
    private LevelMap levelMap;
    
    public Sprite getSprite() {
        return sprite;
    }

    public void setSprite(Sprite sprite) {
        this.sprite = sprite;
    }
    
    @Override
    public void update(double ms) {
        if(sprite == null) {
            offset.x = 0;
            offset.y = 0;
            return;
        }
        
        Dimension screen = gameLoop.getScreenSize();
        
        Area a = sprite.getArea();
        Rectangle bounds = a.getBounds();
        
        offset.x = sprite.pos.x - (screen.width / 2.0) + (bounds.getWidth()/2.0);
        offset.y = sprite.pos.y - (screen.height / 2.0) + (bounds.getHeight()/2.0);
        
        if(levelMap != null) {
            if(offset.x < 0) {
                offset.x = 0;
            }
            if(offset.y < 0) {
                offset.y = 0;
            }
            
            Dimension map = levelMap.getDimension();
            
            if(offset.x + screen.width > map.width) {
                offset.x = map.width - screen.width;
            }
            if(offset.y + screen.height > map.height) {
                offset.y = map.height - screen.height;
            }
        }
    }

    public LevelMap getLevelMap() {
        return levelMap;
    }

    /**
     * If you set a map the camera knows the dimension of the map and stops
     * at the border.
     * @param levelMap 
     */
    public void setLevelMap(LevelMap levelMap) {
        this.levelMap = levelMap;
    }

    
    
    
    
}
