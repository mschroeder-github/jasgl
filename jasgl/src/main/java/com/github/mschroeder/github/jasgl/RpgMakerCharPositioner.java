package com.github.mschroeder.github.jasgl;

import com.github.mschroeder.github.jasgl.Utils.Direction;
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
    
    /**
     * The positioner can check if the sprites that will be moved will intersect
     * something. If this is the case, the positioner is not allowed to move them.
     */
    //protected SpritesTiledMapIntersectioner intersectioner;
    
    private List<CollisionDecisionMaker> collisionDecisionMakers;
    
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
        for (Integer keyCode : Utils.ARROWS) {
            if (keyboard.released(keyCode)) {
                keyCodes.removeIf(kc -> kc == keyCode);
            }
        }
        for (Integer keyCode : Utils.ARROWS) {
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
            sprites.forEach(RpgMakerCharSprite.class, s -> {
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
           
            //if still key down prepare again for moving
            if (!keyCodes.isEmpty()) {
                prepare();
            }
            
            //if allowed, moving is still true
            moving = continueMoving();
            
            //if not stop animation
            if (!moving) {
                sprites.forEach(RpgMakerCharSprite.class, s -> {
                    s.stop();
                });
            }
        }
    }
    
    private void prepare() {
        movedDistance = 0;
        movingKeyCode = keyCodes.get(0);
        direction = Utils.direction(movingKeyCode);
        dirVec = Utils.directionVector(movingKeyCode);
        sprites.forEach(RpgMakerCharSprite.class, s -> {
            s.setDirection(direction);
            sprite2movement.put(s, new Movement(s.pos, dirVec));
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
                RpgMakerCharSprite sprite = (RpgMakerCharSprite) e.getKey();
                
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

}
