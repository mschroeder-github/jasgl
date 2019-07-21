package com.github.mschroeder.github.jasgl;

/**
 * Game input method is used to access the keyboard device state.
 * @author Markus Schr&ouml;der
 */
public interface Keyboard {
    
    /**
     * Checks if for a current frame a keyCode is hold.
     * If you hold the key this will be true for preceding frames.
     * @param keyCode the key code to check
     * @return true if key code is hold in current frame.
     */
    public boolean hold(int keyCode);
    
    /**
     * Checks if for current frame the keycode is pressed.
     * If you hold the key the preceding frames will return false because
     * it is only true in the frame where the key was pressed.
     * @param keyCode
     * @return 
     */
    public boolean pressed(int keyCode);
    
    /**
     * 
     * @param keyCode
     * @return 
     */
    public boolean released(int keyCode);
    
    /**
     * Returns all pressed key codes in current frame.
     * @return 
     */
    public int[] pressed();
    
}
