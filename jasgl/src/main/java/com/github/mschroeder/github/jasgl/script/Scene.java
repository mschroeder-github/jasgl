package com.github.mschroeder.github.jasgl.script;

import java.util.Collection;
import java.util.LinkedList;

/**
 * A scene is just a list of scene acts.
 * @author Markus Schr&ouml;der
 */
public class Scene extends LinkedList<SceneAct> {

    public Scene() {
    }

    public Scene(Collection<? extends SceneAct> c) {
        super(c);
    }
    
}
