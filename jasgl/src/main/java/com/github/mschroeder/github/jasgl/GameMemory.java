package com.github.mschroeder.github.jasgl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.FileUtils;

/**
 * The save game you save for the user to persist the progress of the game.
 * @author Markus Schr&ouml;der
 */
public abstract class GameMemory {
    
    public static final String DEFAULT_EXT = ".sav";
    public static final File DEFAULT_SAVE_FOLDER = new File("./save");
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private static Gson gson;
    static {
        DEFAULT_SAVE_FOLDER.mkdirs();
        
        //how to (de)serialize
        GsonBuilder b = new GsonBuilder();
        b.serializeNulls();
        b.setPrettyPrinting();
        gson = b.create();
    }
    
    public GameMemory() {
        
    }
    
    /**
     * Saves in the default save folder a save file.
     */
    public void save() {
        String now = LocalDateTime.now().format(dtf);
        now = now.replaceAll("\\-", "").replaceAll("\\:", "");
        String filename = now + ".sav";
        
        String json = gson.toJson(this);
        try {
            FileUtils.writeStringToFile(new File(DEFAULT_SAVE_FOLDER, filename), json, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * Loads the last saved save game from the default save folder.
     * @param <T> extends GameMemory
     * @param c the actual type
     * @return a loaded instance or fresh instance if no save game available in default folder
     */
    public static <T extends GameMemory> T load(Class<T> c) {
        File[] files = DEFAULT_SAVE_FOLDER.listFiles();
        if(files == null) {
            try {
                return c.newInstance();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        
        List<File> fileList = new ArrayList<>(Arrays.asList(files));
        fileList.removeIf(f -> !f.getName().endsWith(DEFAULT_EXT));
        //sort by name (2019 > 2018)
        fileList.sort((o1, o2) -> {
            return o2.getName().compareTo(o1.getName());
        });
        
        if(fileList.isEmpty()) {
            try {
                return c.newInstance();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        
        String jsonContent;
        try {
            //fileList.get(0) is latest
            jsonContent = FileUtils.readFileToString(fileList.get(0), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        
        return (T) gson.fromJson(jsonContent, c);
    }

    @Override
    public String toString() {
        return gson.toJson(this);
    }
    
}
