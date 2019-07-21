package com.github.mschroeder.github.jasgl;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * A list of sprites with additional functionality.
 * @author Markus Schr&ouml;der
 */
public class ListOfSprites extends ArrayList<Sprite> {

    public ListOfSprites(int initialCapacity) {
        super(initialCapacity);
    }

    public ListOfSprites() {
    }

    public ListOfSprites(Collection<? extends Sprite> c) {
        super(c);
    }
    
    public <T> void forEach(Class<T> t, Consumer<T> c) {
        for(Sprite s : this) {
            if(t.isAssignableFrom(s.getClass())) {
                c.accept((T)s);
            }
        }
    }
    
    public void update(double ms) {
        for(Sprite s : this) {
            s.update(ms);
        }
    }
    
    public void render(Graphics2D g, boolean sortByY) {
        for(Sprite s : this) {
            s.render(g);
        }
    }
    
    public void render(Graphics2D g) {
        render(g, false);
    }
}
