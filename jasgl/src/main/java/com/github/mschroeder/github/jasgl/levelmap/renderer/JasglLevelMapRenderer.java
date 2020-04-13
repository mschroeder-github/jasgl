package com.github.mschroeder.github.jasgl.levelmap.renderer;

import com.github.mschroeder.github.jasgl.editor.Stamp;
import com.github.mschroeder.github.jasgl.levelmap.JasglLevelMap;
import com.github.mschroeder.github.jasgl.sprite.ListOfSprites;
import com.github.mschroeder.github.jasgl.sprite.Sprite;
import com.github.mschroeder.github.jasgl.util.GraphicsUtils;
import com.google.gson.JsonObject;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 *
 * @author Markus Schr&ouml;der
 */
public class JasglLevelMapRenderer {
    
    private JasglLevelMap map;

    public JasglLevelMapRenderer() {
    }

    public JasglLevelMapRenderer(JasglLevelMap map) {
        this.map = map;
    }
    
    public void render(ListOfSprites sprites, Graphics2D g) {
        if(map == null)
            return;
        
        g.setComposite(AlphaComposite.SrcOver);
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
        
        
        ListOfSprites spritesToRender = new ListOfSprites(sprites);
        spritesToRender.sortByY();
        
        for (JsonObject mapTile : map.getTiles()) {
            
            BufferedImage tilesetImage = map.resolveTileset(mapTile);
            JsonObject tile = map.resolveTileRef(mapTile);
            
            int x = tile.get("x").getAsInt();
            int y = tile.get("y").getAsInt();
            int w = tile.get("w").getAsInt();
            int h = tile.get("h").getAsInt();

            Rectangle src = new Rectangle(x, y, w, h);

            int gx = mapTile.get("gx").getAsInt();
            int gy = mapTile.get("gy").getAsInt();
            int gz = mapTile.get("gz").getAsInt();

            Point dstScreenPoint = map.getScreenPoint(new Point(gx, gy - gz));
            //if(gx == 5 && gy == 6 && gz == 0 && JasglLevelMap.getLayer(tile).equals(Stamp.OBJECT)) {
            //    dstScreenPoint = map.getScreenPoint(new Point(gx + 1, gy - gz));
            //    dstScreenPoint.x -= 16;
            //}
            Rectangle dst = new Rectangle(dstScreenPoint, map.getGrid());
            
            while(!spritesToRender.isEmpty() && JasglLevelMap.getLayer(tile).equals(Stamp.OBJECT) && dst.y >= spritesToRender.get(0).getY()) {
                Sprite sprite = spritesToRender.remove(0);
                sprite.render(g);
            }
            
            //problem: if the image is drawn fast over another and if resized then fragments occur
            //sleep tried, reduces a bit but frame rate drops
            GraphicsUtils.drawImage(g, tilesetImage, src, dst);
            
            //System.out.println(drawImage + " (" + gx + "," + gy + "," + gz + ") " + tile.toString());
            
            //if(gx == 5 && gy == 6 && gz == 0 && JasglLevelMap.getLayer(tile).equals(Stamp.OBJECT)) {
            //    break;
            //}
        }
        //System.out.println("===============================");
        
        //g.drawImage(map.getTilesetCache().get("basic.tileset.png").getImage(), 0, 0, null);
        
        //render rest
        spritesToRender.render(g);
        
        
        
        
    }

    public JasglLevelMap getMap() {
        return map;
    }

    public void setMap(JasglLevelMap map) {
        this.map = map;
    }

}
