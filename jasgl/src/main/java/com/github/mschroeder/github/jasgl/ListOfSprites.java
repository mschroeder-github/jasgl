package com.github.mschroeder.github.jasgl;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
    
    /**
     * Adds the sprite only if it is absent.
     * @param sprite 
     */
    public void addIfAbsent(Sprite sprite) {
        if(!contains(sprite)) {
            add(sprite);
        }
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
    
    /**
     * Renders the sprites.
     * @param g
     * @param sortByY if true, sorts by y-coordinate to have correct draw order
     */
    public void render(Graphics2D g, boolean sortByY) {
        if(sortByY) {
            Collections.sort(this, (a, b) -> {
                return Double.compare(a.pos.y, b.pos.y);
            });
        }
        for(Sprite s : this) {
            s.render(g);
        }
    }
    
    public void render(Graphics2D g) {
        render(g, false);
    }
}
