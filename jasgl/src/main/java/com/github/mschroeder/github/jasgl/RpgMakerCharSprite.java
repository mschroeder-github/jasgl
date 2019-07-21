package com.github.mschroeder.github.jasgl;

import com.github.mschroeder.github.jasgl.Utils.Direction;

/**
 *
 * @author Markus Schr&ouml;der
 */
public class RpgMakerCharSprite extends SpriteSheetSprite implements Pivotable {
    
    private int charIndex;
    
    /**
     * 
     * @param charIndex Because they put 8 characters in one sheet you have to
     * state which character you want: [0, 7].
     * @param sheet 
     */
    public RpgMakerCharSprite(int charIndex, RpgMakerCharSpriteSheet sheet) {
        super(sheet, new SequentialFixedIntervalFrameAnimator(200, 1, 0,1,2,1));
        this.charIndex = charIndex;
        set(charIndex + "_" + Direction.Down.name().toLowerCase());
    }

    /**
     * Because it is Pivotable it can change direction thus change the walk
     * animation.
     * @param dir 
     */
    @Override
    public void changeDirection(Direction dir) {
        set(charIndex + "_" + dir.name().toLowerCase());
    }
    
}

