package com.github.mschroeder.github.jasgl;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Markus Schr&ouml;der
 */
public class Utils {
    
    /**
     * All arrow keys: left, right, up, down.
     */
    public static final List<Integer> ARROWS = Arrays.asList(
            KeyEvent.VK_LEFT,
            KeyEvent.VK_RIGHT,
            KeyEvent.VK_UP,
            KeyEvent.VK_DOWN
    );
    
    /**
     * All length 1 directions: left (-1, 0), right ( 1, 0), up ( 0,-1), down ( 0, 1).
     */
    public static final List<Point.Double> DIRECTIONS = Arrays.asList(
            new Point.Double(-1, 0),
            new Point.Double( 1, 0),
            new Point.Double( 0,-1),
            new Point.Double( 0, 1)
    );
    
    /**
     * Enum for the directions: left, right, up, down.
     */
    public enum Direction {
        Left,
        Right,
        Up,
        Down
    }
    
    /**
     * Based on arrow key returns direction vector.
     * E.g. VK_LEFT returns (-1,0).
     * @param keyCode
     * @return 
     */
    public static Point.Double directionVector(int keyCode) {
        return DIRECTIONS.get(ARROWS.indexOf(keyCode));
    }
    
    /**
     * Based on arrow key returns direction enum.
     * E.g. VK_LEFT returns Direction.Left.
     * @param keyCode
     * @return 
     */
    public static Direction direction(int keyCode) {
        return Direction.values()[ARROWS.indexOf(keyCode)];
    }
    
}
