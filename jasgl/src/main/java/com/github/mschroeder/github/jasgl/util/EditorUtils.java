package com.github.mschroeder.github.jasgl.util;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 *
 * @author Markus Schr&ouml;der
 */
public class EditorUtils {

    public static BufferedImage getImageFromClipboard() {
        Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
            try {
                return (BufferedImage) transferable.getTransferData(DataFlavor.imageFlavor);
            } catch (UnsupportedFlavorException | IOException ex) {
                System.err.println("getImageFromClipboard " + ex.getMessage());
                return null;
            }
        } else {
            return null;
        }
    }
    
    public static List<File> getSortedFiles(File projectFolder, Predicate<File> filter) {
        if (projectFolder == null) {
            return Arrays.asList();
        }

        File[] list = projectFolder.listFiles((f) -> {
            return filter.test(f) && f.isFile();
        });

        List<File> files = new ArrayList<>(Arrays.asList(list));

        files.sort((a, b) -> {
            return a.getName().compareTo(b.getName());
        });

        return files;
    }
    
    /*
    public static void main(String[] args) {
        Image img = getImageFromClipboard();
        
        System.out.println(img);
        if(img != null) {
            System.out.println(img.getClass());
            System.out.println(img.getWidth(null) + "x" + img.getHeight(null));
        }
    }
    */
}
