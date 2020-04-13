package com.github.mschroeder.github.jasgl.script;

import com.github.mschroeder.github.jasgl.script.GameScriptConsole;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import org.mapeditor.core.MapObject;

/**
 * A useful wrapper around Java's ScriptEngine to enable scripting in games.
 * @author Markus Schr&ouml;der
 */
public class GameScriptEngine {

    //to use synchronized in eval() method
    private Object evalLock = new Object();
    
    private ScriptEngine jsEngine;
    private Bindings jsBindings;
    private GameScriptConsole jsConsole;
    
    public GameScriptEngine() {
        jsConsole = new GameScriptConsole();

        // init javascript engine
        ScriptEngineManager sem = new ScriptEngineManager();
        jsEngine = sem.getEngineByName("JavaScript");
        jsBindings = jsEngine.createBindings();

        jsBindings.put("console", jsConsole);
        
        //TODO maybe comment
        eval("console.log('scripting engine initialized.');", null);
    }
    
    public void putBinding(String name, Object value) {
        jsBindings.put(name, value);
    }
    
    public void removeBinding(String name) {
        jsBindings.remove(name);
    }
    
    public Object eval(String script, MapObject obj) {
        return eval(script, obj, new HashMap<String, Object>());
    }

    public Object eval(String script, MapObject obj, Map<String, Object> additionalBindings) {
        if (script == null || script.length() <= 0)
            return null;
        synchronized(evalLock) {
            try {
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
                throw new RuntimeException(e);
            }
        }
    }

}
