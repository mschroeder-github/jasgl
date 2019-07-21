package com.github.mschroeder.github.jasgl;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.MouseInputListener;

/**
 * A frame with a canvas running the game loop and using a Game impl for the
 * game logic.
 * @author Markus Schr&ouml;der
 */
public class GameLoopFrame extends javax.swing.JFrame implements GameLoop {

    private static final int FPS30MS = (int) ((1 / 30.0) * 1000);
    private static final long NANO_TO_MILLI = 1000000;
    
    private Color background;
    private Game game;
    
    private boolean debugFrameRate;
    private int frame = 0;
    
    private boolean running = true;

    public GameLoopFrame(String title, int w, int h, Color background, Game game) {
        initComponents();

        this.background = background;
        this.game = game;
        
        //TODO resizeable
        //TODO fullscreen?
        //TODO no decoration
        
        this.setIgnoreRepaint(true);
        canvas.setIgnoreRepaint(true);
        
        //mouse
        canvas.addMouseListener(new InternalMouseInputListener());
        
        //keyboard
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher((KeyEvent evt) -> {
            globalKeyboardEventProcessing(evt);
            return false;
        });
        
        
        setTitle(title);
        setSize(w, h);
        setLocationRelativeTo(null);
        
        
        
        canvas.createBufferStrategy(2);
    }
    
    private Queue<KeyEvent> keyEventQueue = new ConcurrentLinkedQueue<>();
    private Queue<MouseEvent> mouseEventQueue = new ConcurrentLinkedQueue<>();
    
    //====================================================================

    private InternalMouse internalMouse = new InternalMouse();
    private class InternalMouseInputListener implements MouseInputListener {

        @Override
        public void mouseClicked(MouseEvent e) {
            mouseEventQueue.add(e);
        }

        
        @Override
        public void mousePressed(MouseEvent e) {
            mouseEventQueue.add(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            mouseEventQueue.add(e);
        }

        
        @Override
        public void mouseEntered(MouseEvent e) {
            mouseEventQueue.add(e);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            mouseEventQueue.add(e);
        }

        
        
        @Override
        public void mouseDragged(MouseEvent e) {
            mouseEventQueue.add(e);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            
        }
    }
    
    private class InternalMouse implements Mouse {
        
        private List<MouseEvent> mouseEvents = new ArrayList<>();

        private Map<Integer, MouseEvent> toButtonMap() {
            Map<Integer, MouseEvent> m = new HashMap<>();
            for(MouseEvent e : mouseEvents) {
                m.put(e.getButton(), e);
            }
            return m;
        }
        
        private Map<Map.Entry<Integer, Integer>, MouseEvent> toButtonIDMap() {
            Map<Map.Entry<Integer, Integer>, MouseEvent> m = new HashMap<>();
            for(MouseEvent e : mouseEvents) {
                m.put(new AbstractMap.SimpleEntry<>(e.getButton(), e.getID()), e);
            }
            return m;
        }
        
        @Override
        public boolean left() {
            Entry e = new AbstractMap.SimpleEntry<>(MouseEvent.BUTTON1, MouseEvent.MOUSE_PRESSED);
            return toButtonIDMap().containsKey(e);
        }

        @Override
        public boolean middle() {
            Entry e = new AbstractMap.SimpleEntry<>(MouseEvent.BUTTON2, MouseEvent.MOUSE_PRESSED);
            return toButtonIDMap().containsKey(e);
        }

        @Override
        public boolean right() {
            Entry e = new AbstractMap.SimpleEntry<>(MouseEvent.BUTTON3, MouseEvent.MOUSE_PRESSED);
            return toButtonIDMap().containsKey(e);
        }
        
    }
    
    //====================================================================
    
    private InternalKeyboard internalKeyboard = new InternalKeyboard();
    private void globalKeyboardEventProcessing(KeyEvent evt) {
        switch(evt.getID()) {
            case KeyEvent.KEY_TYPED: return;
            case KeyEvent.KEY_PRESSED:
                if(!internalKeyboard.pressed[evt.getKeyCode()]) {
                    internalKeyboard.pressed[evt.getKeyCode()] = true;
                    keyEventQueue.add(evt);
                }
                break;
            case KeyEvent.KEY_RELEASED: 
                if(internalKeyboard.pressed[evt.getKeyCode()]) {
                    internalKeyboard.pressed[evt.getKeyCode()] = false;
                    keyEventQueue.add(evt);
                }
                break;
        }
    }
    private class InternalKeyboard implements Keyboard {

        private List<KeyEvent> keyEvents = new ArrayList<>();
        
        //know what is pressed (now)
        private boolean[] pressed = new boolean[9999];
        
        @Override
        public boolean hold(int keyCode) {
            return pressed[keyCode];
        }
        
        public int[] pressed() {
            return keyEvents.stream().filter(ke -> ke.getID() == KeyEvent.KEY_PRESSED).mapToInt(ke -> ke.getKeyCode()).toArray();
        }

        @Override
        public boolean pressed(int keyCode) {
            return keyEvents.stream().filter(ke -> ke.getID() == KeyEvent.KEY_PRESSED).anyMatch(ke -> ke.getKeyCode() == keyCode);
        }

        @Override
        public boolean released(int keyCode) {
            return keyEvents.stream().filter(ke -> ke.getID() == KeyEvent.KEY_RELEASED).anyMatch(ke -> ke.getKeyCode() == keyCode);
        }
        
    }
    
    //swing worker runs game loop
    private void gameLoopAsSwingWorker() {
        SwingWorker sw = new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                while (running) {

                    long sleep = FPS30MS;
                    long begin;
                    long end;
                    long elapsed;
                    long elapsedMs;
                    
                    //---------------------------------------------
                    //input
                    
                    begin = System.nanoTime();

                    //all key events in this frame
                    internalKeyboard.keyEvents.clear();
                    internalKeyboard.keyEvents.addAll(keyEventQueue);
                    
                    //all mouse events in this frame
                    internalMouse.mouseEvents.clear();
                    internalMouse.mouseEvents.addAll(mouseEventQueue);

                    //game changes state based on input
                    try {
                        game.input(internalKeyboard, internalMouse);
                    } catch(Exception e) {
                        e.printStackTrace();
                        running = false;
                        continue;
                    }

                    //clear queue to have 
                    keyEventQueue.clear();
                    mouseEventQueue.clear();
                    
                    end = System.nanoTime();
                    elapsed = end - begin;
                    elapsedMs = elapsed / NANO_TO_MILLI;

                    sleep -= elapsedMs;
                    if (sleep < 0) {
                        sleep = 0;
                    }

                    //-------------------------------------------
                    //update
                    
                    begin = System.nanoTime();
                    try {
                        game.update(sleep);
                    } catch(Exception e) {
                        e.printStackTrace();
                        running = false;
                        continue;
                    }
                    end = System.nanoTime();
                    elapsed = end - begin;
                    elapsedMs = elapsed / NANO_TO_MILLI;

                    sleep -= elapsedMs;
                    if (sleep < 0) {
                        sleep = 0;
                    }

                    //---------------------------------------------
                    //render
                    
                    begin = System.nanoTime();
                    //Do in EDT
                    SwingUtilities.invokeLater(() -> {
                        activeRendering();
                    });
                    end = System.nanoTime();
                    elapsed = end - begin;
                    elapsedMs = elapsed / NANO_TO_MILLI;

                    sleep -= elapsedMs;
                    if (sleep < 0) {
                        sleep = 0;
                    }
                    
                    
                    //-------------------------------------------
                    
                    //if(debugFrameRate) {
                    //    System.out.println("sleep: " + sleep);
                    //}
                    
                    try {
                        Thread.sleep(sleep);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                }
                dispose();
                return null;
            }
        };
        sw.execute();
    }
    
    //uses double buffer
    private void activeRendering() {
        Graphics g = canvas.getBufferStrategy().getDrawGraphics();
        if (g != null) {
            render((Graphics2D) g);
            g.dispose();
            canvas.getBufferStrategy().show();
            Toolkit.getDefaultToolkit().sync();
        }
    }

    //render with canvas and double buffer
    private void render(Graphics2D g) {
        //background
        if(background != null) {
            g.setColor(background);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        
        //draw map
        try {
            game.render(g);
        } catch(Exception e) {
            e.printStackTrace();
            running = false;
            return;
        }                        

        //debug frame stuff
        if(debugFrameRate) {
            g.setColor(Color.black);
            g.drawString("frame: " + frame, 10, 20);
        }
        
        //frame counter
        frame++;
        if (frame % 30 == 0) {
            frame = 0;
        }
    }
    
    //when game wants to close
    @Override
    public void close() {
        running = false;
    }
    
    //when game needs debug infos
    @Override
    public void enableDebug() {
        debugFrameRate = true;
    }
    
    //current frame
    @Override
    public int frame() {
        return frame;
    }
    
    //run a game in the loop
    public static void loop(String title, int w, int h, Color background, Game game) {
        java.awt.EventQueue.invokeLater(() -> {
            new GameLoopFrame(title, w, h, background, game).setVisible(true);
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        canvas = new java.awt.Canvas();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                formComponentShown(evt);
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(canvas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(canvas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        
    }//GEN-LAST:event_formWindowClosing

    private void formComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
        //before game loop init it
        game.init(this);
        gameLoopAsSwingWorker();
    }//GEN-LAST:event_formComponentShown


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private java.awt.Canvas canvas;
    // End of variables declaration//GEN-END:variables

   
}
