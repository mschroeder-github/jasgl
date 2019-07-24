package com.github.mschroeder.github.jasgl;

import com.github.mschroeder.github.jasgl.Utils.Direction;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.List;

/**
 *
 * @author Markus Schr&ouml;der
 */
public class RpgMakerCharSprite extends SpriteSheetSprite implements Pivotable, Shapeable {
    
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
        setAnimation(charIndex + "_" + Direction.Down.name().toLowerCase());
    }

    /**
     * Because it is Pivotable it can change direction thus change the walk
     * animation.
     * @param dir 
     */
    @Override
    public void setDirection(Direction dir) {
        setAnimation(charIndex + "_" + dir.name().toLowerCase());
    }

    @Override
    public Shape getShape() {
        return new Rectangle.Double(pos.x, pos.y, 32, 32);
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
        
        //test
        //g.setColor(Color.black);
        //g.drawRect(getX(), getY(), 32, 32);
        //g.setColor(Color.blue);
        //g.drawRect(getX(), getY(), frame.width, frame.height);
        
        GraphicsUtil.drawImage(g, sheet.getImage(),
                frame,
                new Rectangle(getX() - ((frame.width - 32)/2), getY() - (frame.height - 32), frame.width, frame.height)
        );
        
        /*
        g.drawImage(sheet.getImage(), 
            (getX() - frame.width)/2, getY(), getX() + frame.width, getY() + frame.height,
            frame.x, frame.y, (int) frame.getMaxX(), (int) frame.getMaxY(),
            null);*/
    }
    
}

