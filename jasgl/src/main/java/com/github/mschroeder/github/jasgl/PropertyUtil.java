package com.github.mschroeder.github.jasgl;

import java.util.List;

import org.mapeditor.core.MapObject;
import org.mapeditor.core.Properties;
import org.mapeditor.core.Property;
import org.mapeditor.core.Tile;

public class PropertyUtil {

    public static Properties getProperties(Object obj) {
        if (obj == null)
            throw new RuntimeException("obj == null in PropertyUtil.getProperties");

        if (obj instanceof Tile)
            return ((Tile) obj).getProperties();
        else if (obj instanceof MapObject)
            return ((MapObject) obj).getProperties();
        else
            throw new RuntimeException("unknown type in PropertyUtil.getProperties: " + obj.getClass().getName());
    }

    private static Properties newProperties(Object obj) {
        if (obj == null)
            throw new RuntimeException("obj == null in PropertyUtil.setProperties");
        
        Properties emptyProperties = new Properties();
        if (obj instanceof Tile)
            ((Tile) obj).setProperties(emptyProperties);
        else if (obj instanceof MapObject)
            ((MapObject) obj).setProperties(emptyProperties);
        else
            throw new RuntimeException("unknown type in PropertyUtil.setProperties: " + obj.getClass().getName());
        
        return emptyProperties;
    }

    public static String getString(Object obj, String propertyName) {
        return getString(obj, propertyName, null);
    }

    public static String getString(Object obj, String propertyName, String defaultValue) {
        Properties properties = getProperties(obj);
        if (properties == null)
            return defaultValue;
        return properties.getProperty(propertyName, defaultValue);
    }
    
    public static boolean getBoolean(Object obj, String propertyName) {
        return getBoolean(obj, propertyName, false);
    }
    
    public static boolean getBoolean(Object obj, String propertyName, boolean defaultValue) {
        String boolString = getString(obj, propertyName, Boolean.toString(defaultValue));
        return Boolean.valueOf(boolString);
    }
    
    public static void setString(Object obj, String propertyName, String value) {
        Properties properties = getProperties(obj);
        if (properties == null) {
            properties = newProperties(obj);
        }
        List<Property> props = properties.getProperties();
        for (Property prop : props) {
            if (prop.getName().equals(propertyName)) {
                prop.setValue(value);
                return;
            }
        }
        // not found in props
        properties.setProperty(propertyName, value);
    }
    
    public static void setBoolean(Object obj, String propertyName, boolean value) {
        setString(obj, propertyName, Boolean.toString(value));
    }
    
}
