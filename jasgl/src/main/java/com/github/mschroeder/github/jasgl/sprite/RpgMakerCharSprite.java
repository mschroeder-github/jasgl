package com.github.mschroeder.github.jasgl.sprite;

import com.github.mschroeder.github.jasgl.util.GraphicsUtils;
import com.github.mschroeder.github.jasgl.util.JASGLUtils.Direction;
import com.github.mschroeder.github.jasgl.animator.SequentialFixedIntervalFrameAnimator;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.util.List;

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
    public Area getArea() {
        return new Area(new Rectangle.Double(pos.x, pos.y, 32, 32));
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
        
        //area
        /*
        Area area = getArea();
        Rectangle rect = area.getBounds();
        g.setColor(Color.black);
        g.drawRect(rect.x, rect.y, rect.width, rect.height);
        
        g.setColor(Color.blue);
        g.drawRect(getX() - ((frame.width - 32)/2), getY() - (frame.height - 32), frame.width, frame.height);
        */
        
        GraphicsUtils.drawImage(g, sheet.getImage(),
                frame,
                new Rectangle(
                        getX() - ((frame.width - 32)/2), 
                        getY() - (frame.height - 32), //-8 or not?
                        frame.width, 
                        frame.height
                )
        );
        
        /*
        g.drawImage(sheet.getImage(), 
            (getX() - frame.width)/2, getY(), getX() + frame.width, getY() + frame.height,
            frame.x, frame.y, (int) frame.getMaxX(), (int) frame.getMaxY(),
            null);*/
    }
    
}

