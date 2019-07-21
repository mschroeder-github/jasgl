package com.github.mschroeder.github.jasgl;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Just a sprite that renders a 32x32 rectangle for testing purpose.
 * @author Markus Schr&ouml;der
 */
public class DummySprite extends Sprite {

    @Override
    public void render(Graphics2D g) {
        g.setColor(Color.black);
        g.fillRect(getX(), getY(), 32, 32);
    }

    @Override
    public void update(double ms) {
        
    }
    
}
