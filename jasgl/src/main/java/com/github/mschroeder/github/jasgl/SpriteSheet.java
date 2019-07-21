package com.github.mschroeder.github.jasgl;

import java.awt.Image;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A sprite sheet cuts sprite images from a bigger image and defines the animation.
 * @author Markus Schr&ouml;der
 */
public abstract class SpriteSheet {
    
    //a sprite sheet has always exactly one image it refers to
    protected Image image;
    
    //each animation has a name and the corresponding subimages (frame) we have to cut out of the image
    //here we will not define how fast the animation will be
    protected Map<String, List<Rectangle>> animatedFrames;

    public SpriteSheet(Image image) {
        this.image = image;
        this.animatedFrames = new HashMap<>();
    }

    public Map<String, List<Rectangle>> getAnimatedFrames() {
        return animatedFrames;
    }

    public Image getImage() {
        return image;
    }
    
}
