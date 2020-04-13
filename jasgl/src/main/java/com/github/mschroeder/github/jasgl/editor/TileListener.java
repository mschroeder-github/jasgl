package com.github.mschroeder.github.jasgl.editor;

import com.google.gson.JsonObject;
import java.util.List;

/**
 * Used to communicate from EditorRenderPanel to Frame.
 * @author Markus Schr&ouml;der
 */
public interface TileListener {
 
    public void selected(List<JsonObject> tiles);
    
}
