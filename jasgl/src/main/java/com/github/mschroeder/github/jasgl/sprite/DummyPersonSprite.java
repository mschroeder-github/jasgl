package com.github.mschroeder.github.jasgl.sprite;

import com.github.mschroeder.github.jasgl.util.JASGLUtils.Direction;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Area;

/**
 *
 * @author Markus Schr&ouml;der
 */
public class DummyPersonSprite extends Sprite implements Areaable, Pivotable {

    private Color groundColor = Color.blue;
    private Color directionColor = Color.green;
    private Color personColor = Color.orange;
    private Direction direction = Direction.Down;
    private Rectangle area = new Rectangle(0,0,32,32);
    private double directionThickness = 0.25;
    
    
    private boolean animateMove = true;
    private int moveOffset = 0;
    private double passedTime;
    private double moveEvery = 300;
    
    @Override
    public void update(double ms) {
        if(animateMove) {
            passedTime += ms;
            if(passedTime > moveEvery) {
                passedTime = (passedTime - moveEvery);
                moveOffset = moveOffset == 0 ? -2 : 0;
            }
        }
    }

    @Override
    public void render(Graphics2D g) {
        int x = (int) pos.x;
        int y = (int) pos.y;
        int w = area.width;
        int h = area.height;
        
        g.setColor(groundColor);
        g.fillRect((int) pos.x, (int) pos.y, 32, 32);
        
        int bw = (int) (area.width * directionThickness);
        int bh = (int) (area.height * directionThickness);

        g.setColor(directionColor);
        //left
        if (direction == Direction.Left) {
            g.fillRect(x, y, bw, h);
        }
        //right
        if (direction == Direction.Right) {
            g.fillRect(x + w - bw, y, bw, h);
        }
        //up
        if (direction == Direction.Up) {
            g.fillRect(x, y, w, bh);
        }
        //down
        if (direction == Direction.Down) {
            g.fillRect(x, y + h - bh, w, bh);
        }
        
        int personX = x + (area.width / 4);
        int personY = moveOffset + y - (area.height/ 2) - (int) (area.height * 0.33f);
        int personWidth = area.width / 2;
        int personHeight = h + (int) (area.height * 0.3f);
        
        g.setColor(personColor);
        g.fillRect(personX, personY, personWidth, personHeight);
    }

    @Override
    public void setDirection(Direction dir) {
        this.direction = dir;
    }
    
    @Override
    public Area getArea() {
        return new Area(area);
    }
    
}
