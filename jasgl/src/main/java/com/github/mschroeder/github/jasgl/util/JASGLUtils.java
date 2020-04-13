package com.github.mschroeder.github.jasgl.util;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Markus Schr&ouml;der
 */
public class JASGLUtils {
    
    /**
     * All arrow keys: left, right, up, down.
     */
    public static final List<Integer> ARROWS = Arrays.asList(
            KeyEvent.VK_LEFT,
            KeyEvent.VK_RIGHT,
            KeyEvent.VK_UP,
            KeyEvent.VK_DOWN
    );
    
    public static final List<Integer> WASD = Arrays.asList(
            KeyEvent.VK_A,
            KeyEvent.VK_D,
            KeyEvent.VK_W,
            KeyEvent.VK_S
    );
    
    public static final List<Integer> WASD_NEO = Arrays.asList(
            KeyEvent.VK_U,
            KeyEvent.VK_A,
            KeyEvent.VK_V,
            KeyEvent.VK_I
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
    
    public static Point.Double directionVector(List<Integer> positionKeyCodes, int keyCode) {
        return DIRECTIONS.get(positionKeyCodes.indexOf(keyCode));
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
    
    public static Direction direction(List<Integer> positionKeyCodes, int keyCode) {
        return Direction.values()[positionKeyCodes.indexOf(keyCode)];
    }
    
    /**
     * Based on x, y derives direction.
     * @param x
     * @param y
     * @return 
     */
    public static Direction direction(int x, int y) {
        if(x < 0 && y == 0) {
            return Direction.Left;
        } else if(x > 0 && y == 0) {
            return Direction.Right;
        } else if(x == 0 && y > 0) {
            return Direction.Down;
        } else if(x == 0 && y < 0) {
            return Direction.Up;
        }
        return null;
    }
    
    /**
     * Returns logically a implies b.
     * @param a
     * @param b
     * @return 
     */
    public static boolean implies(boolean a, boolean b) {
        return !a || b;
    }
}
