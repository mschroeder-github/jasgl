package com.github.mschroeder.github.jasgl;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Sprite sheet for rpg maker charsets for various versions.
 * Because rpg maker puts usually 8 character in one character sprite set
 * we cut the animations in the following way: <pre>[charIndex]_[down/left/right/up]</pre>.
 * @author Markus Schr&ouml;der
 */
public class RpgMakerCharSpriteSheet extends SpriteSheet {

    public enum Version {
        V2000,
        V2003,
        XP,
        VX,
        VXAce,
        MV //4x2 sprite wtih walk animation
    }

    //standard in rpg maker
    private static final List<String> walks = Arrays.asList(
            "down",
            "left",
            "right",
            "up"
    );
    
    //TODO maybe change image if background is a color and not transparent
    
    public RpgMakerCharSpriteSheet(Version version, Image image) {
        super(image);
        cut(version);
    }
    
    private void cut(Version version) {
        if(version == Version.MV) {
            
            //subsheet size
            Dimension subsheet = getSubsheetSize(version);
            Dimension character = getCharacterSize(version);
            
            int charIndex = 0;
            int walkIndex = 0;
            
            //subsheet
            for(int i = 0; i < 2; i++) {
                for(int j = 0; j < 4; j++) {
                
                    //walk
                    for(int y = 0; y < 4; y++) {

                        List<Rectangle> frames = 
                        animatedFrames.computeIfAbsent(
                                charIndex + "_" + walks.get(walkIndex), 
                                s -> new ArrayList<>()
                        );
                        
                        for(int x = 0; x < 3; x++) {
                            
                            int xx = j*subsheet.width + x*character.width;
                            int yy = i*subsheet.height + y*character.height;
                            
                            Rectangle rect = new Rectangle(
                                    xx, yy, 
                                    character.width, character.height
                            );
                            frames.add(rect);
                        }
                        
                        walkIndex++;
                    }
                    
                    charIndex++;
                    walkIndex = 0;
                }
            }
            
        } else {
            throw new RuntimeException(version + " not supported yet");
        }
    }
    
    /**
     * Based on rpg maker version the default image size.
     * @param version
     * @return 
     */
    public static Dimension getImageSize(Version version) {
        if(version == Version.MV) {
            return new Dimension(576, 384);
        }
        throw new RuntimeException(version + " not supported yet");
    }
    
    /**
     * Based on rpg maker version the subimage size because they put usually
     * 8 characters in one image.
     * @param version
     * @return 
     */
    public static Dimension getSubsheetSize(Version version) {
        if(version == Version.MV) {
            Dimension imageSize = getImageSize(version);
            return new Dimension(imageSize.width / 4, imageSize.height / 2);
        }
        throw new RuntimeException(version + " not supported yet");
    }
    
    /**
     * Based on rpg maker version the default character size.
     * @param version
     * @return 
     */
    public static Dimension getCharacterSize(Version version) {
        if(version == Version.MV) {
            Dimension imageSize = getImageSize(version);
            return new Dimension(imageSize.width / 12, imageSize.height / 8);
        }
        throw new RuntimeException(version + " not supported yet");
    }
}
