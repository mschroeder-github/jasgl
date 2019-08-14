package com.github.mschroeder.github.jasgl;

/**
 * Helps in building Scenes.
 * @author Markus Schr&ouml;der
 */
public class SceneBuilder {
    
    protected Scene scene;
    
    public SceneBuilder() {
        scene = new Scene();
    }
    
    public static SceneBuilder begin() {
        return new SceneBuilder();
    }
    
    public Scene end() {
        return scene;
    }
    
    //helper
    
    protected boolean isGerman() {
        String lang = System.getProperty("user.language");
        return lang.toLowerCase().startsWith("de");
    }
    
    protected String i18n(String enText, String deText) {
        return  isGerman() ? deText : enText;
    }
    
    //building methods
    
    public SceneBuilder waiting(long ms) {
        scene.add(new SceneAct("waiting", ms));
        return this;
    }
    
    /*
        TODO remove: game specific
    
    public SceneBuilder dialog(String id, String enText, String deText) {
        scene.add(new SceneAct("dialog", id, i18n(enText, deText)));
        return this;
    }
    
    public SceneBuilder dialog(String enText, String deText) {
        scene.add(new SceneAct("dialog", i18n(enText, deText)));
        return this;
    }
    
    public SceneBuilder emoji(String id, String emoji) {
        scene.add(new SceneAct("emoji", id, emoji));
        return this;
    }
    
    public SceneBuilder noemoji(String id) {
        scene.add(new SceneAct("noemoji", id));
        return this;
    }
    
    public SceneBuilder look(String id, String directions, long delay) {
        for(int i = 0; i < directions.length(); i++) {
            switch(directions.charAt(i)) {
                case 'u': scene.add(new SceneAct("look", id, Direction.Up)); break;
                case 'd': scene.add(new SceneAct("look", id, Direction.Down)); break;
                case 'l': scene.add(new SceneAct("look", id, Direction.Left)); break;
                case 'r': scene.add(new SceneAct("look", id, Direction.Right)); break;
            }
            scene.add(new SceneAct("waiting", delay));
        }
        return this;
    }
    */
    
}
