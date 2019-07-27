package com.github.mschroeder.github.jasgl;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Area;

/**
 * A camera that follows a sprite.
 * @author Markus Schr&ouml;der
 */
public class SpriteCamera extends Camera {

    protected Sprite sprite;

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
    }
    
}
