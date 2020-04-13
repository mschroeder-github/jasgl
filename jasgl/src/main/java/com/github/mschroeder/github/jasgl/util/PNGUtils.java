package com.github.mschroeder.github.jasgl.util;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import org.w3c.dom.NodeList;

/**
 *
 * @author Markus Schr&ouml;der
 */
public class PNGUtils {
    
    public static final String COMMENT = "Comment";
    
    public static void write(File file, int w, int h, Consumer<Graphics2D> renderer, String comment) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        renderer.accept(g);
        img.flush();
        g.dispose();
        try {
            ImageIO.write(img, "png", file);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        write(file, comment);
    }
    
    public static void write(File file, String comment) {
        Map<String, String> m = new HashMap<>();
        m.put(PNGUtils.COMMENT, comment);
        write(file, m);
    }
    
    public static void write(File file, Map<String, String> metadataMap) {
        File outFile = new File(file.getParentFile(), file.getName() + ".tmp");
        
        try {
            write(new FileInputStream(file), new FileOutputStream(outFile), metadataMap);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        
        file.delete();
        
        outFile.renameTo(file);
        
        outFile.delete();
    }

    public static void write(InputStream in, OutputStream out, Map<String, String> metadataMap) throws IOException {
        
        IIOImage image;
        ImageReader reader;
        
        try (
                ImageInputStream input = ImageIO.createImageInputStream(in)
            ) {

            Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("png");
            reader = readers.next();

            reader.setInput(input);
            image = reader.readAll(0, null);
            
            IIOMetadataNode root = new IIOMetadataNode(IIOMetadataFormatImpl.standardMetadataFormatName);
            for(Entry<String, String> e : metadataMap.entrySet()) {
                IIOMetadataNode text = createText(e.getKey(), e.getValue());
                root.appendChild(text);
            }
            image.getMetadata().setFromTree(IIOMetadataFormatImpl.standardMetadataFormatName, root);
        }
        
        try (
                ImageOutputStream output = ImageIO.createImageOutputStream(out)
            ) {
            ImageWriter writer = ImageIO.getImageWriter(reader);
            writer.setOutput(output);
            writer.write(image);
        }
    }
    
    public static ImageWithMetaData read(File f) {
        try {
            return read(new FileInputStream(f));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public static ImageWithMetaData read(InputStream in) throws IOException {
        try (ImageInputStream input = ImageIO.createImageInputStream(in)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            ImageReader reader = readers.next();

            reader.setInput(input);
            Map<String, String> map = getTextEntry(reader.getImageMetadata(0));
            
            BufferedImage image = reader.read(0);
            
            return new ImageWithMetaData(image, map);
        }
    }
    
    private static Map<String, String> getTextEntry(final IIOMetadata metadata) {
        IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(IIOMetadataFormatImpl.standardMetadataFormatName);
        NodeList entries = root.getElementsByTagName("TextEntry");

        Map<String, String> m = new HashMap<>();
        
        for (int i = 0; i < entries.getLength(); i++) {
            IIOMetadataNode node = (IIOMetadataNode) entries.item(i);
            m.put(node.getAttribute("keyword"), node.getAttribute("value"));
        }

        return m;
    }

    private static void addTextEntry(final IIOMetadata metadata, Map<String, String> metadataMap) throws IIOInvalidTreeException {
        IIOMetadataNode textEntry = new IIOMetadataNode("TextEntry");
        
        for(Entry<String, String> e : metadataMap.entrySet()) {
            textEntry.setAttribute(e.getKey(), e.getValue());
        }

        IIOMetadataNode text = new IIOMetadataNode("Text");
        text.appendChild(textEntry);

        IIOMetadataNode root = new IIOMetadataNode(IIOMetadataFormatImpl.standardMetadataFormatName);
        root.appendChild(text);

        metadata.mergeTree(IIOMetadataFormatImpl.standardMetadataFormatName, root);
    }
    
    private static IIOMetadataNode createText(final String key, final String value) throws IIOInvalidTreeException {
        IIOMetadataNode textEntry = new IIOMetadataNode("TextEntry");
        textEntry.setAttribute("keyword", key);
        textEntry.setAttribute("value", value);

        IIOMetadataNode text = new IIOMetadataNode("Text");
        text.appendChild(textEntry);

        return text;
    }

    public static class ImageWithMetaData {
        private BufferedImage image;
        private Map<String, String> metaData;

        public ImageWithMetaData(BufferedImage image, Map<String, String> metaData) {
            this.image = image;
            this.metaData = metaData;
        }

        public BufferedImage getImage() {
            return image;
        }

        public Map<String, String> getMetaData() {
            return metaData;
        }
    }
    
    /*
    public static void main(String[] args) throws IOException {
        JsonObject obj = new JsonObject();
        obj.addProperty("test", true);
        obj.addProperty("bla", 3);
        
        Map<String, String> m = new HashMap<>();
        m.put("Comment", obj.toString());
        
        write(
                new FileInputStream(new File("/home/otaku/dlBrowser/test.png")), 
                new FileOutputStream(new File("/home/otaku/dlBrowser/test-out.png")), 
                m
        );
    }
    */
}
