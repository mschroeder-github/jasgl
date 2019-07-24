package com.github.mschroeder.github.jasgl;

import com.github.mschroeder.github.jasgl.Utils.Direction;
import java.awt.Point;
import java.util.LinkedList;

/**
 * A positioner that works like the one rpg maker implemented.
 * @author Markus Schr&ouml;der
 */
public class RpgMakerCharPositioner extends InputBasedPositioner {
    
    private LinkedList<Integer> keyCodes = new LinkedList<>();

    private int movingKeyCode;
    private boolean moving;
    private double movedDistance;
    
    //settings
    private double stepDistance;
    private double pixelPerSecondSpeed;

    public RpgMakerCharPositioner() {
        this.stepDistance = 32.0;
        this.pixelPerSecondSpeed = 96.0;
    }
    
    public RpgMakerCharPositioner(double stepDistance, double pixelPerSecondSpeed) {
        this.stepDistance = stepDistance;
        this.pixelPerSecondSpeed = pixelPerSecondSpeed;
    }
    
    /**
     * Use the arrow keys to walk the given sprites.
     * @param keyboard 
     * @param mouse 
     */
    @Override
    public void input(Keyboard keyboard, Mouse mouse) {
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
            double pixelPerMilliSpeed = pixelPerSecondSpeed / 1000;
            double distance = ms * pixelPerMilliSpeed;
            
            //dir
            Point.Double dirVec = Utils.directionVector(movingKeyCode);
            
            //move the sprites
            for(Sprite sprite : sprites) {
                sprite.pos.x += dirVec.x * distance;
                sprite.pos.y += dirVec.y * distance;
            }
            
            //the moved distance
            movedDistance += distance;
            if(movedDistance > stepDistance) {
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
