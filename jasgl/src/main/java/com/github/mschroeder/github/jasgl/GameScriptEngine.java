package com.github.mschroeder.github.jasgl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.mapeditor.core.MapObject;

public class GameScriptEngine {

    private GameMultiplexer gameMultiplexer;
    
    private String jsEngineSync = "DummySyncObject";
    private ScriptEngine jsEngine;
    private Bindings jsBindings;
    private GameScriptConsole jsConsole;


    public GameScriptEngine(GameMultiplexer gameMultiplexer) {
        this.gameMultiplexer = gameMultiplexer;
        this.gameMultiplexer.setScriptEngine(this);

        jsConsole = new GameScriptConsole();

        // init javascript engine
        ScriptEngineManager sem = new ScriptEngineManager();
        jsEngine = sem.getEngineByName("JavaScript");
        jsBindings = jsEngine.createBindings();

        jsBindings.put("console", jsConsole);
        jsBindings.put("gameMultiplexer", gameMultiplexer);
        
        eval("console.log('scripting engine initialized.');", null);
    }
    
    public GameMultiplexer getGameMultiplexer() {
        return gameMultiplexer;
    }
    
    public Game getCurrentGame() {
        return gameMultiplexer.getCurrentGame();
    }
    
    public GameMemory getMemory() {
         return gameMultiplexer.getMemory();
    }
    
    public Object eval(String script, MapObject obj) {
        return eval(script, obj, new HashMap<String, Object>());
    }

    public Object eval(String script, MapObject obj, Map<String, Object> additionalBindings) {
        if (script == null || script.length() <= 0)
            return null;
        synchronized(jsEngineSync) {
            try {
                jsBindings.put("memory", gameMultiplexer.getMemory());
                jsBindings.put("game", gameMultiplexer.getCurrentGame());
                jsBindings.put("script", script);
                jsBindings.put("object", obj);
                if (additionalBindings != null) {
                    for (Entry<String, Object> bind : additionalBindings.entrySet()) {
                        jsBindings.put(bind.getKey(), bind.getValue());
                    }
                }
                Object result = jsEngine.eval(script, jsBindings);
                return result;
            } catch (Exception e) {
                System.err.println("Exception while calling script on object " + obj);
                e.printStackTrace();
                return null;
            }
        }
    }

}
