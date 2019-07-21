package com.github.mschroeder.github.jasgl;

import java.awt.Point;
import java.util.LinkedList;
import com.github.mschroeder.github.jasgl.Utils.Direction;

/**
 * A positioner that works like the one rpg maker implemented.
 * @author Markus Schr&ouml;der
 */
public class RpgMakerCharPositioner extends Positioner {
    
    
    private LinkedList<Integer> keyCodes = new LinkedList<>();

    private int movingKeyCode;
    private boolean moving;
    private double movedDistance;
    private double maxDistance = 32.0;
    
    /**
     * Use the arrow keys to walk the given sprites.
     * @param keyboard 
     */
    public void input(Keyboard keyboard) {
        for(Integer keyCode : Utils.ARROWS) {
            if(keyboard.pressed(keyCode)) {
                keyCodes.addFirst(keyCode);
            }
            if(keyboard.released(keyCode)) {
                keyCodes.remove((Object)keyCode);
            }
        }
        
        //a key is pressed and we are not moving
        if(continueMoving() && !moving) {
            //init the moving
            movedDistance = 0;
            movingKeyCode = keyCodes.get(0);
            Direction dir = Utils.direction(movingKeyCode);
            moving = true;
            
            sprites.forEach(RpgMakerCharSprite.class, s -> { 
                s.changeDirection(dir);
                s.play();
            });
        }
    }
    
    //if no key is pressed stop moving
    private boolean continueMoving() {
         return !keyCodes.isEmpty();
    }
    
    @Override
    public void update(double ms) {
        //System.out.println(keyCodes);
        
        //only if moving
        if(moving) {
            double speed = (1/15.0);
            double distance = ms * speed;
            
            //dir
            Point.Double dirVec = Utils.directionVector(movingKeyCode);
            
            //move the sprites
            for(Sprite sprite : sprites) {
                sprite.pos.x += dirVec.x * distance;
                sprite.pos.y += dirVec.y * distance;
            }
            
            //the moved distance
            movedDistance += distance;
            if(movedDistance > maxDistance) {
                //new init
                movedDistance = 0;
                moving = continueMoving();
                if(moving) {
                    movingKeyCode = keyCodes.get(0);
                    Direction dir = Utils.direction(movingKeyCode);
                    sprites.forEach(RpgMakerCharSprite.class, s -> { 
                        s.changeDirection(dir);
                    });
                } else {
                    sprites.forEach(RpgMakerCharSprite.class, s -> { 
                        s.stop();
                    });
                }
            }
        }
    }
    
}
