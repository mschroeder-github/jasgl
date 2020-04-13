package com.github.mschroeder.github.jasgl.sprite;

import com.github.mschroeder.github.jasgl.animator.FrameAnimator;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;

/**
 * A sprite that has a sprite sheet.
 * @author Markus Schr&ouml;der
 */
public class SpriteSheetSprite extends Sprite implements Playable {

    protected SpriteSheet sheet;
    protected FrameAnimator animator;
    
    protected String animation;
    
    public SpriteSheetSprite(SpriteSheet sheet, FrameAnimator animator) {
        this.sheet = sheet;
        this.animator = animator;
    }
    
    /**
     * Sets the animation named in the {@link SpriteSheet}.
     * @param animation 
     */
    @Override
    public void setAnimation(String animation) {
        this.animation = animation;
    }
    
    /**
     * Plays the current animation.
     */
    @Override
    public void play() {
        animator.play();
    }
    
    /**
     * Stops playing the current animation.
     */
    @Override
    public void stop() {
        animator.stop();
    }
    
    /**
     * Sets and plays the animation named in the {@link SpriteSheet}.
     * @param animation 
     */
    @Override
    public void play(String animation) {
        setAnimation(animation);
        play();
    }
    
    /**
     * Hides the sprite by resetting the animation.
     */
    @Override
    public void hide() {
        stop();
        this.animation = null;
    }
    
    @Override
    public void update(double ms) {
        animator.update(ms);
    }

    @Override
    public void render(Graphics2D g) {
        if(animation == null)
            return;
        
        List<Rectangle> frames = sheet.getAnimatedFrames().get(animation);
        
        if(animator.getFrame() >= frames.size()) {
            return;
        }
        
        Rectangle frame = frames.get(animator.getFrame());
        
        g.drawImage(sheet.getImage(), 
            getX(), getY(), getX() + frame.width, getY() + frame.height,
            frame.x, frame.y, (int) frame.getMaxX(), (int) frame.getMaxY(),
            null);
    }
    
}
