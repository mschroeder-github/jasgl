package com.github.mschroeder.github.jasgl.sprite;

/**
 * A sprite is playable if it has animations to play.
 * @author Markus Schr&ouml;der
 */
public interface Playable {
    
    /**
     * Sets the animation named in the {@link SpriteSheet}.
     * @param animation 
     */
    public void setAnimation(String animation);
    
    /**
     * Plays the current animation.
     */
    public void play();
    
    /**
     * Stops playing the current animation.
     */
    public void stop();
    
    /**
     * Sets and plays the animation named in the {@link SpriteSheet}.
     * @param animation 
     */
    public void play(String animation);
    
    /**
     * Hides the sprite by resetting the animation.
     */
    public void hide();
    
}
