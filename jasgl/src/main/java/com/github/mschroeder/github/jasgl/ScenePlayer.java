package com.github.mschroeder.github.jasgl;

import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import javax.swing.SwingWorker;

/**
 * Plays a scene.
 * @author Markus Schr&ouml;der
 */
public class ScenePlayer {
    
    protected Game game;
    
    private boolean playing;
    private SwingWorker worker;
    protected CountDownLatch playerLatch;

    public ScenePlayer(Game game) {
        this.game = game;
    }
    
    public void play(Scene scene) {
        final ScenePlayer scenePlayer = this;
        
        worker = new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                playing = true;
                        
                //run all scene acts
                for(SceneAct act : scene) {
                    try {
                        Method m;
                        try {
                            m = scenePlayer.getClass().getDeclaredMethod(act.getMethodName(), act.getParamTypes());
                        } catch(NoSuchMethodException e) {
                            m = ScenePlayer.class.getDeclaredMethod(act.getMethodName(), act.getParamTypes());
                        }
                        m.setAccessible(true);
                        m.invoke(scenePlayer, act.getParams());
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
                
                playing = false;
                return null;
            }
        };
        worker.execute();
    }
 
    public boolean isPlaying() {
        return playing;
    }
    
    //player presses the action button
    public void playerAction() {
        if(playerLatch != null) {
            playerLatch.countDown();
        }
    }
    
    //scene player methods

    private void waiting(Long ms) throws InterruptedException {
        Thread.sleep(ms);
    }
    
    /*
    TODO remove: game specific
    
    private void dialog(String id, String text) throws InterruptedException {
        //MapObjChar c = gameState.getBeingById(id);
        //dialog(c.getName() + ": " + text);
    }
    
    private void dialog(String text) throws InterruptedException {
        //gameState.setMessageBoxText(text);
        //playerLatch = new CountDownLatch(1);
        
        //wait for player to press the action button
        //playerLatch.await();
        
        //gameState.setMessageBoxText(null);
    }
    
    private void emoji(String id, String emoji) {
        //MapObjChar c = gameState.getBeingById(id);
        //c.setEmoji(emoji);
        //gameState.repaint();
    }
    
    private void noemoji(String id) {
        //MapObjChar c = gameState.getBeingById(id);
        //c.resetEmoji();
        //gameState.repaint();
    }
    
    private void look(String id, Direction dir) {
        //MapObjChar c = gameState.getBeingById(id);
        //c.setViewingDirection(dir);
        //gameState.repaint();
    }
    */
}
