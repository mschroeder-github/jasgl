package com.github.mschroeder.github.jasgl;

import java.util.Map;

import org.mapeditor.core.MapObject;

public interface GameScriptEngine {

    Object eval(String script, MapObject obj);

    Object eval(String script, MapObject obj, Map<String, Object> additionalBindings);

}
