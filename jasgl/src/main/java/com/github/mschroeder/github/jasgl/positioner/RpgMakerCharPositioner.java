package com.github.mschroeder.github.jasgl.positioner;

import com.github.mschroeder.github.jasgl.Keyboard;
import com.github.mschroeder.github.jasgl.Mouse;
import com.github.mschroeder.github.jasgl.levelmap.collision.CollisionDecisionMaker;
import com.github.mschroeder.github.jasgl.sprite.Pivotable;
import com.github.mschroeder.github.jasgl.sprite.Playable;
import com.github.mschroeder.github.jasgl.sprite.Sprite;
import com.github.mschroeder.github.jasgl.sprite.SpriteListener;
import com.github.mschroeder.github.jasgl.util.JASGLUtils;
import com.github.mschroeder.github.jasgl.util.JASGLUtils.Direction;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A positioner that works like the one rpg maker implemented.
 *
 * @author Markus Schr&ouml;der
 */
public class RpgMakerCharPositioner extends InputBasedPositioner {
    
    private List<CollisionDecisionMaker> collisionDecisionMakers;
    private List<SpriteListener> spriteListeners;
    
    private LinkedList<Integer> keyCodes = new LinkedList<>();

    private int movingKeyCode;
    private boolean moving;
    private double movedDistance;
    private Direction direction;
    private Point.Double dirVec;
    private Map<Sprite, Movement> sprite2movement;

    //settings
    private double stepDistance;
    private double pixelPerSecondSpeed;
    
    //left right up down
    private List<Integer> positionKeyCodes = JASGLUtils.ARROWS;

    public RpgMakerCharPositioner() {
        this(32.0, 96.0);
    }

    public RpgMakerCharPositioner(double stepDistance, double pixelPerSecondSpeed) {
        this.sprite2movement = new HashMap<>();
        this.stepDistance = stepDistance;
        this.pixelPerSecondSpeed = pixelPerSecondSpeed;
        double pixelPerMilliSpeed = pixelPerSecondSpeed / 1000;
        double durationMillis = stepDistance / pixelPerMilliSpeed;
        
        collisionDecisionMakers = new ArrayList<>();
        spriteListeners = new ArrayList<>();
    }

    /**
     * Use the arrow keys to walk the given sprites.
     *
     * @param keyboard
     * @param mouse
     */
    @Override
    public void input(Keyboard keyboard, Mouse mouse) {
        //check arrows
        for (Integer keyCode : positionKeyCodes) {
            if (keyboard.released(keyCode)) {
                keyCodes.removeIf(kc -> kc == keyCode);
            }
        }
        for (Integer keyCode : positionKeyCodes) {
            if (keyboard.pressed(keyCode)) {
                keyCodes.addFirst(keyCode);
            }
        }

        //if nothing pressed, do not move
        if (keyCodes.isEmpty()) {
            return;
        }

        //if pressed and not moving: start prepare
        if (!moving) {
            prepare();
        }

        //a key is pressed and we are not moving and map allows it
        if (continueMoving() && !moving) {
            //init the moving
            movedDistance = 0;
            //now update() method will do something
            moving = true;
            sprites.forEach(Playable.class, s -> {
                s.play();
            });
        }
    }

    @Override
    public void update(double ms) {
        //do something if moving is true
        if (!moving) {
            return;
        }

        double pixelPerMilliSpeed = pixelPerSecondSpeed / 1000;
        double pixelDistance = ms * pixelPerMilliSpeed;
        movedDistance += pixelDistance;

        double prop = movedDistance / stepDistance;
        if(prop > 1.0) {
            prop = 1.0;
        }
        
        for(Entry<Sprite, Movement> e : sprite2movement.entrySet()) {
            e.getKey().pos.x = e.getValue().end.x - ((e.getValue().end.x - e.getValue().start.x) * (1.0 - prop));
            e.getKey().pos.y = e.getValue().end.y - ((e.getValue().end.y - e.getValue().start.y) * (1.0 - prop));
        }

        //when the distance is moved
        if (prop >= 1.0) {
            
            //invoke pos changed
            for(Sprite sprite : sprites.toArray(new Sprite[0])) {
                for(SpriteListener l : spriteListeners) {
                    l.spriteChanged(sprite);
                }
            }
            
            //if still key down prepare again for moving
            if (!keyCodes.isEmpty()) {
                prepare();
            }
            
            //if allowed, moving is still true
            moving = continueMoving();
            
            //if not stop animation
            if (!moving) {
                sprites.forEach(Playable.class, s -> {
                    s.stop();
                });
            }
        }
    }
    
    private void prepare() {
        movedDistance = 0;
        movingKeyCode = keyCodes.get(0);
        direction = JASGLUtils.direction(positionKeyCodes, movingKeyCode);
        dirVec = JASGLUtils.directionVector(positionKeyCodes, movingKeyCode);
        sprites.forEach(Pivotable.class, s -> {
            s.setDirection(direction);
            sprite2movement.put((Sprite) s, new Movement(((Sprite)s).pos, dirVec));
        });
    }

    //if no key is pressed stop moving
    private boolean continueMoving() {
        
        //no key pressed: no moving
        if(keyCodes.isEmpty()) {
            return false;
        }
        
        //collision detected: no moving
        if(!collisionDecisionMakers.isEmpty()) {
            for(Entry<Sprite, Movement> e : sprite2movement.entrySet()) {
                Sprite sprite = e.getKey();
                
                Point.Double delta = e.getValue().getDelta();
                
                //where the sprite will be
                Area translated = sprite.getArea()
                .createTransformedArea(
                        AffineTransform.getTranslateInstance(delta.x, delta.y)
                );
                
                //if one sprite collides once: whole moving is stopped
                for(CollisionDecisionMaker cdm : collisionDecisionMakers) {
                    if(cdm.collides(sprite, translated)) {
                        return false;
                    }
                }
            }
        }
        
        //else continue moving
        return true;
    }
    
    /**
     * Resets key press memory of this positioner.
     */
    @Override
    public void reset() {
        keyCodes.clear();
        moving = false;
    }
    
    private class Movement {
        Point.Double start;
        Point.Double end;
        
        public Movement(Point.Double start, Point.Double dir) {
            this.start = (Point.Double) start.clone();
            this.end = (Point.Double) start.clone();
            this.end.x += (dir.x * stepDistance);
            this.end.y += (dir.y * stepDistance);
        }

        @Override
        public String toString() {
            return start + "->" + end;
        }
        
        public Point.Double getDelta() {
            return new Point.Double(end.x - start.x, end.y - start.y);
        }
        
    }

    public List<CollisionDecisionMaker> getCollisionDecisionMakers() {
        return collisionDecisionMakers;
    }

    public List<SpriteListener> getSpriteListeners() {
        return spriteListeners;
    }

    public boolean isMoving() {
        return moving;
    }

    public List<Integer> getPositionKeyCodes() {
        return positionKeyCodes;
    }

    /**
     * Left, Right, Up, Down.
     * @param positionKeyCodes 
     */
    public void setPositionKeyCodes(List<Integer> positionKeyCodes) {
        this.positionKeyCodes = positionKeyCodes;
    }
    
    
    
}
