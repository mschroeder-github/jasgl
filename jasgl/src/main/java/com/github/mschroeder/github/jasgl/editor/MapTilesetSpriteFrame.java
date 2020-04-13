package com.github.mschroeder.github.jasgl.editor;

import com.github.mschroeder.github.jasgl.util.EditorUtils;
import com.github.mschroeder.github.jasgl.util.PNGUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Markus Schr&ouml;der
 */
public class MapTilesetSpriteFrame extends javax.swing.JFrame implements TileListener {

    private final String TILESET_EXT = ".tileset.png";
    private final String MAP_EXT = ".map.png";
    
    //where maps and tilesets and sprites are stored
    private File projectFolder;
    
    public MapTilesetSpriteFrame(File projectFolder) {
        this.projectFolder = projectFolder;
        initComponents();
        
        //size
        Dimension size = new Dimension(1700, 900);
        setSize(size);
        setPreferredSize(size);
        setLocationRelativeTo(null);
        
        //different types
        getTilesetRenderPanel().setMode(EditorRenderPanel.Mode.Tileset);
        getMapRenderPanel().setMode(EditorRenderPanel.Mode.Map);
        
        //list of files
        jTableTileset.setModel(new TilesetTableModel());
        jTableMap.setModel(new MapTableModel());
        
        //tileset z change
        jSpinnerTilesetZ.addChangeListener((e) -> {
            jSpinnerTilesetZValueChanged(e);
        });
        ((JSpinner.DefaultEditor)jSpinnerTilesetZ.getEditor()).getTextField().addKeyListener(new KeyListener(){
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                    getTilesetRenderPanel().setZforSelected((int)jSpinnerTilesetZ.getValue());
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
            
        });
        
        //init render based on GUI (tileset)
        getTilesetRenderPanel().setShowBlock(jCheckBoxTilesetShowBlock.isSelected());
        getTilesetRenderPanel().setShowGrid(jCheckBoxTilesetShowGrid.isSelected());
        getTilesetRenderPanel().setShowZ(jCheckBoxTilesetShowZ.isSelected());
        getTilesetRenderPanel().setShowLayers(jCheckBoxTilesetShowLayers.isSelected());
        
        //init render based on GUI (map)
        getMapRenderPanel().setShowBlock(jCheckBoxMapShowBlock.isSelected());
        getMapRenderPanel().setShowGrid(jCheckBoxMapShowGrid.isSelected());
        getMapRenderPanel().setShowZ(jCheckBoxMapShowZ.isSelected());
        getMapRenderPanel().setFillPencil(jCheckBoxPencilFill.isSelected());
        
        getMapRenderPanel().addListener(this);
        
        DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) jComboBoxEraseLayer.getModel();
        model.removeAllElements();
        model.addElement(Stamp.ALL);
        model.addElement(Stamp.GROUND);
        model.addElement(Stamp.OBJECT);
        model.setSelectedItem(Stamp.ALL);
        getMapRenderPanel().setEraseLayer((String) jComboBoxEraseLayer.getSelectedItem());
    }
    
    private void updateTilesetTable() {
        jTableTileset.clearSelection();
        jTableTileset.updateUI();
    }
    
    private void updateMapTable() {
        jTableMap.clearSelection();
        jTableMap.updateUI();
    }
    
    private List<File> getTilesetFilesSorted() {
        return EditorUtils.getSortedFiles(projectFolder, f -> f.getName().endsWith(TILESET_EXT));
    }
    
    private List<File> getMapFilesSorted() {
        return EditorUtils.getSortedFiles(projectFolder, f -> f.getName().endsWith(MAP_EXT));
    }

    @Override
    public void selected(List<JsonObject> tiles) {
        jTabbedPaneTile.removeAll();
        
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        
        for(JsonObject tile : tiles) {
            int gx = tile.get("gx").getAsInt();
            int gy = tile.get("gy").getAsInt();
            int gz = tile.get("gz").getAsInt();
            
            JTextArea area = new JTextArea(gson.toJson(tile));
            JScrollPane scroll = new JScrollPane(area);
            jTabbedPaneTile.addTab(String.format("(%d,%d,%d)", gx, gy, gz), scroll);
        }
    }
    
    private class TilesetTableModel extends AbstractTableModel {

        @Override
        public int getRowCount() {
            return getTilesetFilesSorted().size();
        }

        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public String getColumnName(int column) {
            return "Tileset";
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            File f = getTilesetFilesSorted().get(rowIndex);
            return f.getName();
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            File f = getTilesetFilesSorted().get(rowIndex);
            String name = (String) aValue;
            rename(f, name);
        }
    }
    
    private class MapTableModel extends AbstractTableModel {

        @Override
        public int getRowCount() {
            return getMapFilesSorted().size();
        }

        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public String getColumnName(int column) {
            return "Map";
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            File f = getMapFilesSorted().get(rowIndex);
            return f.getName();
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            File f = getMapFilesSorted().get(rowIndex);
            String name = (String) aValue;
            rename(f, name);
        }
    }
    
    private void rename(File f, String name) {
        File newFile = new File(f.getParentFile(), name);
        if (!(newFile.getName().endsWith(TILESET_EXT) || newFile.getName().endsWith(MAP_EXT))) {
            return;
        }

        if (!newFile.exists()) {
            f.renameTo(newFile);
            updateTilesetTable();
        }
    }
    
    private void openTilesetFile(File f) {
        getTilesetRenderPanel().setFile(f);
    }
    
    private void openMapFile(File f) {
        getMapRenderPanel().setFile(f);
    }
    
    private EditorRenderPanel getTilesetRenderPanel() {
        return (EditorRenderPanel) jPanelTilesetRender;
    }
    
    private EditorRenderPanel getMapRenderPanel() {
        return (EditorRenderPanel) jPanelMapRender;
    }
    
    private JsonObject createTile(int x, int y, int w, int h) {
        JsonObject tile = new JsonObject();
        tile.addProperty("x", x);
        tile.addProperty("y", y);
        tile.addProperty("w", w);
        tile.addProperty("h", h);
        
        tile.addProperty("z", 0);
        
        tile.addProperty("l", false);
        tile.addProperty("r", false);
        tile.addProperty("u", false);
        tile.addProperty("d", false);
        return tile;
    }
    
    private void cutTileset() {
        if(jTableTileset.getSelectedRow() == -1)
            return;
        
        File file = getTilesetFilesSorted().get(jTableTileset.getSelectedRow());
        
        if(JOptionPane.showConfirmDialog(this, "Cut " + file.getName() + "?") != JOptionPane.OK_OPTION) {
            return;
        } 
        
        String cut = JOptionPane.showInputDialog(this, "Cut", "16x16");
        String[] split = cut.split("x");
        
        int w = Integer.parseInt(split[0]);
        int h = Integer.parseInt(split[1]);
        
        BufferedImage image;
        try {
            image = ImageIO.read(file);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        
        JsonArray array = new JsonArray();
        for(int y = 0; y < image.getHeight(); y += h) {
            for(int x = 0; x < image.getWidth(); x += w) {
                array.add(createTile(x, y, w, h));
            }
        }
        
        JsonObject data = new JsonObject();
        data.add("tiles", array);
        
        PNGUtils.write(file, data.toString());
        
        openTilesetFile(file);
    }
    
    private void removeTileset() {
        List<File> files = new ArrayList<>();
        List<File> all = getTilesetFilesSorted();
        for(int i : jTableTileset.getSelectedRows()) {
            files.add(all.get(i));
        }
        if(JOptionPane.showConfirmDialog(this, "Remove " + files.size() + "?") != JOptionPane.OK_OPTION) {
            return;
        } 
        for(File f : all) {
            f.delete();
        }
        updateTilesetTable();
    }
    
    private void removeMap() {
        List<File> files = new ArrayList<>();
        List<File> all = getMapFilesSorted();
        for(int i : jTableMap.getSelectedRows()) {
            files.add(all.get(i));
        }
        if(JOptionPane.showConfirmDialog(this, "Remove " + files.size() + "?") != JOptionPane.OK_OPTION) {
            return;
        } 
        for(File f : all) {
            f.delete();
        }
        updateMapTable();
    }
    
    private void jSpinnerTilesetZValueChanged(ChangeEvent e) {
        getTilesetRenderPanel().setZforSelected((int)jSpinnerTilesetZ.getValue());
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroupMapMode = new javax.swing.ButtonGroup();
        buttonGroupDrawMode = new javax.swing.ButtonGroup();
        jSplitPaneMain = new javax.swing.JSplitPane();
        jTabbedPaneTilesetSprite = new javax.swing.JTabbedPane();
        jPanelTileset = new javax.swing.JPanel();
        jSplitPaneTileset = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        jSpinnerTilesetZ = new javax.swing.JSpinner();
        jCheckBoxTilesetShowLayers = new javax.swing.JCheckBox();
        jCheckBoxTilesetShowGrid = new javax.swing.JCheckBox();
        jCheckBoxTilesetShowZ = new javax.swing.JCheckBox();
        jCheckBoxTilesetShowBlock = new javax.swing.JCheckBox();
        jScrollPaneTileset = new javax.swing.JScrollPane();
        jPanelTilesetRender = new com.github.mschroeder.github.jasgl.editor.EditorRenderPanel();
        jPanelTilesetList = new javax.swing.JPanel();
        jToolBarTileset = new javax.swing.JToolBar();
        jButtonTilesetCut = new javax.swing.JButton();
        jButtonTilesetRemove = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTableTileset = new javax.swing.JTable();
        jPanelSprite = new javax.swing.JPanel();
        jTabbedPaneMap = new javax.swing.JTabbedPane();
        jPanelMap = new javax.swing.JPanel();
        jSplitPaneMap = new javax.swing.JSplitPane();
        jPanel2 = new javax.swing.JPanel();
        jToolBar2 = new javax.swing.JToolBar();
        jButtonMapNew = new javax.swing.JButton();
        jButtonDuplicate = new javax.swing.JButton();
        jButtonRemove = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTableMap = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        jToolBar3 = new javax.swing.JToolBar();
        jRadioButtonMapDraw = new javax.swing.JRadioButton();
        jRadioButtonMapSelect = new javax.swing.JRadioButton();
        jRadioButtonMapErase = new javax.swing.JRadioButton();
        jCheckBoxMapShowGrid = new javax.swing.JCheckBox();
        jCheckBoxMapShowZ = new javax.swing.JCheckBox();
        jCheckBoxMapShowBlock = new javax.swing.JCheckBox();
        jScrollPaneMapRender = new javax.swing.JScrollPane();
        jPanelMapRender = new EditorRenderPanel();
        jToolBar4 = new javax.swing.JToolBar();
        jRadioButtonPoint = new javax.swing.JRadioButton();
        jRadioButtonLine = new javax.swing.JRadioButton();
        jRadioButtonRect = new javax.swing.JRadioButton();
        jRadioButtonCircle = new javax.swing.JRadioButton();
        jCheckBoxPencilFill = new javax.swing.JCheckBox();
        jComboBoxEraseLayer = new javax.swing.JComboBox<>();
        jTabbedPaneTile = new javax.swing.JTabbedPane();
        jMenuBarMain = new javax.swing.JMenuBar();
        jMenuMap = new javax.swing.JMenu();
        jMenuItemResetTilesetCache = new javax.swing.JMenuItem();
        jMenuTileset = new javax.swing.JMenu();
        jMenuItemTilesetPasteImage = new javax.swing.JMenuItem();
        jMenuItemBlock = new javax.swing.JMenuItem();
        jMenuItemPass = new javax.swing.JMenuItem();
        jMenuItemGround = new javax.swing.JMenuItem();
        jMenuItemResetLayer = new javax.swing.JMenuItem();
        jMenuSprite = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Editor");
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                formComponentShown(evt);
            }
        });

        jSplitPaneMain.setDividerLocation(800);
        jSplitPaneMain.setResizeWeight(1.0);

        jSplitPaneTileset.setDividerLocation(400);
        jSplitPaneTileset.setResizeWeight(1.0);

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        jSpinnerTilesetZ.setModel(new javax.swing.SpinnerNumberModel(0, null, 10, 1));
        jToolBar1.add(jSpinnerTilesetZ);

        jCheckBoxTilesetShowLayers.setSelected(true);
        jCheckBoxTilesetShowLayers.setText("Show Layers");
        jCheckBoxTilesetShowLayers.setFocusable(false);
        jCheckBoxTilesetShowLayers.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jCheckBoxTilesetShowLayers.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jCheckBoxTilesetShowLayers.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jCheckBoxTilesetShowLayers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxTilesetShowLayersActionPerformed(evt);
            }
        });
        jToolBar1.add(jCheckBoxTilesetShowLayers);

        jCheckBoxTilesetShowGrid.setSelected(true);
        jCheckBoxTilesetShowGrid.setText("Show Grid");
        jCheckBoxTilesetShowGrid.setFocusable(false);
        jCheckBoxTilesetShowGrid.setHideActionText(true);
        jCheckBoxTilesetShowGrid.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jCheckBoxTilesetShowGrid.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jCheckBoxTilesetShowGrid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxTilesetShowGridActionPerformed(evt);
            }
        });
        jToolBar1.add(jCheckBoxTilesetShowGrid);

        jCheckBoxTilesetShowZ.setSelected(true);
        jCheckBoxTilesetShowZ.setText("Show Z");
        jCheckBoxTilesetShowZ.setToolTipText("");
        jCheckBoxTilesetShowZ.setFocusable(false);
        jCheckBoxTilesetShowZ.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jCheckBoxTilesetShowZ.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jCheckBoxTilesetShowZ.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxTilesetShowZActionPerformed(evt);
            }
        });
        jToolBar1.add(jCheckBoxTilesetShowZ);

        jCheckBoxTilesetShowBlock.setSelected(true);
        jCheckBoxTilesetShowBlock.setText("Show Block");
        jCheckBoxTilesetShowBlock.setFocusable(false);
        jCheckBoxTilesetShowBlock.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jCheckBoxTilesetShowBlock.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jCheckBoxTilesetShowBlock.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxTilesetShowBlockActionPerformed(evt);
            }
        });
        jToolBar1.add(jCheckBoxTilesetShowBlock);

        jPanelTilesetRender.setLayout(null);
        jScrollPaneTileset.setViewportView(jPanelTilesetRender);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPaneTileset, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPaneTileset, javax.swing.GroupLayout.DEFAULT_SIZE, 547, Short.MAX_VALUE))
        );

        jSplitPaneTileset.setLeftComponent(jPanel1);

        jToolBarTileset.setFloatable(false);
        jToolBarTileset.setRollover(true);

        jButtonTilesetCut.setText("Cut");
        jButtonTilesetCut.setFocusable(false);
        jButtonTilesetCut.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonTilesetCut.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonTilesetCut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonTilesetCutActionPerformed(evt);
            }
        });
        jToolBarTileset.add(jButtonTilesetCut);

        jButtonTilesetRemove.setText("Remove");
        jButtonTilesetRemove.setToolTipText("");
        jButtonTilesetRemove.setFocusable(false);
        jButtonTilesetRemove.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonTilesetRemove.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonTilesetRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonTilesetRemoveActionPerformed(evt);
            }
        });
        jToolBarTileset.add(jButtonTilesetRemove);

        jTableTileset.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jTableTileset.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTableTilesetMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jTableTileset);

        javax.swing.GroupLayout jPanelTilesetListLayout = new javax.swing.GroupLayout(jPanelTilesetList);
        jPanelTilesetList.setLayout(jPanelTilesetListLayout);
        jPanelTilesetListLayout.setHorizontalGroup(
            jPanelTilesetListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBarTileset, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );
        jPanelTilesetListLayout.setVerticalGroup(
            jPanelTilesetListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelTilesetListLayout.createSequentialGroup()
                .addComponent(jToolBarTileset, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 547, Short.MAX_VALUE))
        );

        jSplitPaneTileset.setRightComponent(jPanelTilesetList);

        javax.swing.GroupLayout jPanelTilesetLayout = new javax.swing.GroupLayout(jPanelTileset);
        jPanelTileset.setLayout(jPanelTilesetLayout);
        jPanelTilesetLayout.setHorizontalGroup(
            jPanelTilesetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPaneTileset)
        );
        jPanelTilesetLayout.setVerticalGroup(
            jPanelTilesetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPaneTileset)
        );

        jTabbedPaneTilesetSprite.addTab("Tileset", jPanelTileset);

        javax.swing.GroupLayout jPanelSpriteLayout = new javax.swing.GroupLayout(jPanelSprite);
        jPanelSprite.setLayout(jPanelSpriteLayout);
        jPanelSpriteLayout.setHorizontalGroup(
            jPanelSpriteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 579, Short.MAX_VALUE)
        );
        jPanelSpriteLayout.setVerticalGroup(
            jPanelSpriteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 586, Short.MAX_VALUE)
        );

        jTabbedPaneTilesetSprite.addTab("Sprite", jPanelSprite);

        jSplitPaneMain.setRightComponent(jTabbedPaneTilesetSprite);

        jSplitPaneMap.setDividerLocation(170);

        jToolBar2.setFloatable(false);
        jToolBar2.setRollover(true);

        jButtonMapNew.setText("New");
        jButtonMapNew.setFocusable(false);
        jButtonMapNew.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonMapNew.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonMapNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonMapNewActionPerformed(evt);
            }
        });
        jToolBar2.add(jButtonMapNew);

        jButtonDuplicate.setText("Dupl.");
        jButtonDuplicate.setFocusable(false);
        jButtonDuplicate.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonDuplicate.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonDuplicate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDuplicateActionPerformed(evt);
            }
        });
        jToolBar2.add(jButtonDuplicate);

        jButtonRemove.setText("Remove");
        jButtonRemove.setFocusable(false);
        jButtonRemove.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonRemove.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemoveActionPerformed(evt);
            }
        });
        jToolBar2.add(jButtonRemove);

        jTableMap.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jTableMap.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTableMapMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(jTableMap);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jToolBar2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jToolBar2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 553, Short.MAX_VALUE))
        );

        jSplitPaneMap.setLeftComponent(jPanel2);

        jToolBar3.setFloatable(false);
        jToolBar3.setRollover(true);

        buttonGroupMapMode.add(jRadioButtonMapDraw);
        jRadioButtonMapDraw.setSelected(true);
        jRadioButtonMapDraw.setText("Draw");
        jRadioButtonMapDraw.setFocusable(false);
        jRadioButtonMapDraw.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jRadioButtonMapDraw.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jRadioButtonMapDraw.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRadioButtonMapDrawItemStateChanged(evt);
            }
        });
        jToolBar3.add(jRadioButtonMapDraw);

        buttonGroupMapMode.add(jRadioButtonMapSelect);
        jRadioButtonMapSelect.setText("Select");
        jRadioButtonMapSelect.setFocusable(false);
        jRadioButtonMapSelect.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jRadioButtonMapSelect.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jRadioButtonMapSelect.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRadioButtonMapSelectItemStateChanged(evt);
            }
        });
        jToolBar3.add(jRadioButtonMapSelect);

        buttonGroupMapMode.add(jRadioButtonMapErase);
        jRadioButtonMapErase.setText("Erase");
        jRadioButtonMapErase.setFocusable(false);
        jRadioButtonMapErase.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jRadioButtonMapErase.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jRadioButtonMapErase.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRadioButtonMapEraseItemStateChanged(evt);
            }
        });
        jToolBar3.add(jRadioButtonMapErase);

        jCheckBoxMapShowGrid.setSelected(true);
        jCheckBoxMapShowGrid.setText("Show Grid");
        jCheckBoxMapShowGrid.setFocusable(false);
        jCheckBoxMapShowGrid.setHideActionText(true);
        jCheckBoxMapShowGrid.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jCheckBoxMapShowGrid.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jCheckBoxMapShowGrid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxMapShowGridActionPerformed(evt);
            }
        });
        jToolBar3.add(jCheckBoxMapShowGrid);

        jCheckBoxMapShowZ.setSelected(true);
        jCheckBoxMapShowZ.setText("Show Z");
        jCheckBoxMapShowZ.setToolTipText("");
        jCheckBoxMapShowZ.setFocusable(false);
        jCheckBoxMapShowZ.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jCheckBoxMapShowZ.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jCheckBoxMapShowZ.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxMapShowZActionPerformed(evt);
            }
        });
        jToolBar3.add(jCheckBoxMapShowZ);

        jCheckBoxMapShowBlock.setSelected(true);
        jCheckBoxMapShowBlock.setText("Show Block");
        jCheckBoxMapShowBlock.setFocusable(false);
        jCheckBoxMapShowBlock.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jCheckBoxMapShowBlock.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jCheckBoxMapShowBlock.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxMapShowBlockActionPerformed(evt);
            }
        });
        jToolBar3.add(jCheckBoxMapShowBlock);

        jPanelMapRender.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jPanelMapRenderMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jPanelMapRenderMouseEntered(evt);
            }
        });
        jPanelMapRender.setLayout(null);
        jScrollPaneMapRender.setViewportView(jPanelMapRender);

        jToolBar4.setFloatable(false);
        jToolBar4.setRollover(true);

        buttonGroupDrawMode.add(jRadioButtonPoint);
        jRadioButtonPoint.setSelected(true);
        jRadioButtonPoint.setText("·");
        jRadioButtonPoint.setToolTipText("Dot");
        jRadioButtonPoint.setFocusable(false);
        jRadioButtonPoint.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jRadioButtonPoint.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jRadioButtonPoint.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRadioButtonPointItemStateChanged(evt);
            }
        });
        jToolBar4.add(jRadioButtonPoint);

        buttonGroupDrawMode.add(jRadioButtonLine);
        jRadioButtonLine.setText("/");
        jRadioButtonLine.setToolTipText("Line");
        jRadioButtonLine.setFocusable(false);
        jRadioButtonLine.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jRadioButtonLine.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jRadioButtonLine.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRadioButtonLineItemStateChanged(evt);
            }
        });
        jToolBar4.add(jRadioButtonLine);

        buttonGroupDrawMode.add(jRadioButtonRect);
        jRadioButtonRect.setText("☐");
        jRadioButtonRect.setToolTipText("Rectangle");
        jRadioButtonRect.setFocusable(false);
        jRadioButtonRect.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jRadioButtonRect.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jRadioButtonRect.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRadioButtonRectItemStateChanged(evt);
            }
        });
        jToolBar4.add(jRadioButtonRect);

        buttonGroupDrawMode.add(jRadioButtonCircle);
        jRadioButtonCircle.setText("○");
        jRadioButtonCircle.setToolTipText("Circle");
        jRadioButtonCircle.setFocusable(false);
        jRadioButtonCircle.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jRadioButtonCircle.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jRadioButtonCircle.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRadioButtonCircleItemStateChanged(evt);
            }
        });
        jToolBar4.add(jRadioButtonCircle);

        jCheckBoxPencilFill.setSelected(true);
        jCheckBoxPencilFill.setText("FIlled");
        jCheckBoxPencilFill.setFocusable(false);
        jCheckBoxPencilFill.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jCheckBoxPencilFill.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jCheckBoxPencilFill.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxPencilFillActionPerformed(evt);
            }
        });
        jToolBar4.add(jCheckBoxPencilFill);

        jComboBoxEraseLayer.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBoxEraseLayer.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxEraseLayerItemStateChanged(evt);
            }
        });
        jToolBar4.add(jComboBoxEraseLayer);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPaneMapRender)
            .addComponent(jToolBar4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jTabbedPaneTile)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jToolBar3, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToolBar4, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPaneMapRender, javax.swing.GroupLayout.DEFAULT_SIZE, 401, Short.MAX_VALUE)
                .addGap(9, 9, 9)
                .addComponent(jTabbedPaneTile, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jSplitPaneMap.setRightComponent(jPanel3);

        javax.swing.GroupLayout jPanelMapLayout = new javax.swing.GroupLayout(jPanelMap);
        jPanelMap.setLayout(jPanelMapLayout);
        jPanelMapLayout.setHorizontalGroup(
            jPanelMapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPaneMap)
        );
        jPanelMapLayout.setVerticalGroup(
            jPanelMapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPaneMap)
        );

        jTabbedPaneMap.addTab("Map", jPanelMap);

        jSplitPaneMain.setLeftComponent(jTabbedPaneMap);

        jMenuMap.setText("Map");

        jMenuItemResetTilesetCache.setText("Reset Tileset Cache");
        jMenuItemResetTilesetCache.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemResetTilesetCacheActionPerformed(evt);
            }
        });
        jMenuMap.add(jMenuItemResetTilesetCache);

        jMenuBarMain.add(jMenuMap);

        jMenuTileset.setText("Tileset");

        jMenuItemTilesetPasteImage.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemTilesetPasteImage.setText("Paste Image");
        jMenuItemTilesetPasteImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemTilesetPasteImageActionPerformed(evt);
            }
        });
        jMenuTileset.add(jMenuItemTilesetPasteImage);

        jMenuItemBlock.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_B, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemBlock.setText("Set Block");
        jMenuItemBlock.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemBlockActionPerformed(evt);
            }
        });
        jMenuTileset.add(jMenuItemBlock);

        jMenuItemPass.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemPass.setText("Set Pass");
        jMenuItemPass.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemPassActionPerformed(evt);
            }
        });
        jMenuTileset.add(jMenuItemPass);

        jMenuItemGround.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_G, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemGround.setText("Set Ground Layer");
        jMenuItemGround.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemGroundActionPerformed(evt);
            }
        });
        jMenuTileset.add(jMenuItemGround);

        jMenuItemResetLayer.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemResetLayer.setText("Reset Layer");
        jMenuItemResetLayer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemResetLayerActionPerformed(evt);
            }
        });
        jMenuTileset.add(jMenuItemResetLayer);

        jMenuBarMain.add(jMenuTileset);

        jMenuSprite.setText("Sprite");
        jMenuBarMain.add(jMenuSprite);

        setJMenuBar(jMenuBarMain);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPaneMain)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPaneMain)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItemTilesetPasteImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemTilesetPasteImageActionPerformed
        BufferedImage img = EditorUtils.getImageFromClipboard();
        if(img == null)
            return;
        
        String name = JOptionPane.showInputDialog("Name (without extension)");
        if(name == null)
            return;
        
        File imgFile = new File(projectFolder, name + TILESET_EXT);
        
        try {
            ImageIO.write(img, "png", imgFile);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        
        updateTilesetTable();
    }//GEN-LAST:event_jMenuItemTilesetPasteImageActionPerformed

    private void jTableTilesetMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTableTilesetMouseClicked
        if(SwingUtilities.isLeftMouseButton(evt) && jTableTileset.getSelectedRow() != -1) {
            File f = getTilesetFilesSorted().get(jTableTileset.getSelectedRow());
            openTilesetFile(f);
        }
    }//GEN-LAST:event_jTableTilesetMouseClicked

    private void jButtonTilesetCutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonTilesetCutActionPerformed
        cutTileset();
    }//GEN-LAST:event_jButtonTilesetCutActionPerformed

    private void jButtonTilesetRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonTilesetRemoveActionPerformed
        removeTileset();
    }//GEN-LAST:event_jButtonTilesetRemoveActionPerformed

    private void jCheckBoxTilesetShowGridActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxTilesetShowGridActionPerformed
        getTilesetRenderPanel().setShowGrid(jCheckBoxTilesetShowGrid.isSelected());
    }//GEN-LAST:event_jCheckBoxTilesetShowGridActionPerformed

    private void jCheckBoxTilesetShowZActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxTilesetShowZActionPerformed
        getTilesetRenderPanel().setShowZ(jCheckBoxTilesetShowZ.isSelected());
    }//GEN-LAST:event_jCheckBoxTilesetShowZActionPerformed

    private void jCheckBoxTilesetShowBlockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxTilesetShowBlockActionPerformed
        getTilesetRenderPanel().setShowBlock(jCheckBoxTilesetShowBlock.isSelected());
    }//GEN-LAST:event_jCheckBoxTilesetShowBlockActionPerformed

    private void jMenuItemPassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPassActionPerformed
        getTilesetRenderPanel().setBlockforSelected(false);
    }//GEN-LAST:event_jMenuItemPassActionPerformed

    private void jMenuItemBlockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemBlockActionPerformed
        getTilesetRenderPanel().setBlockforSelected(true);
    }//GEN-LAST:event_jMenuItemBlockActionPerformed

    private void jCheckBoxMapShowGridActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxMapShowGridActionPerformed
        getMapRenderPanel().setShowGrid(jCheckBoxMapShowGrid.isSelected());
    }//GEN-LAST:event_jCheckBoxMapShowGridActionPerformed

    private void jCheckBoxMapShowZActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxMapShowZActionPerformed
        getMapRenderPanel().setShowZ(jCheckBoxMapShowZ.isSelected());
    }//GEN-LAST:event_jCheckBoxMapShowZActionPerformed

    private void jCheckBoxMapShowBlockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxMapShowBlockActionPerformed
        getMapRenderPanel().setShowBlock(jCheckBoxMapShowBlock.isSelected());
    }//GEN-LAST:event_jCheckBoxMapShowBlockActionPerformed

    private void jButtonMapNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonMapNewActionPerformed
        String name = JOptionPane.showInputDialog("Name (without extension)");
        if(name == null)
            return;
        
        File imgFile = new File(projectFolder, name + MAP_EXT);
        
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        try {
            ImageIO.write(img, "png", imgFile);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        
        updateMapTable();
    }//GEN-LAST:event_jButtonMapNewActionPerformed

    private void jButtonDuplicateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDuplicateActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButtonDuplicateActionPerformed

    private void jButtonRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveActionPerformed
        removeMap();
    }//GEN-LAST:event_jButtonRemoveActionPerformed

    private void formComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
        jSplitPaneMain.setDividerLocation(0.5);
        
        SwingUtilities.invokeLater(() -> {
            jSplitPaneMap.setDividerLocation(0.25);
            jSplitPaneTileset.setDividerLocation(0.75);
        });
    }//GEN-LAST:event_formComponentShown

    private void jTableMapMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTableMapMouseClicked
        if(SwingUtilities.isLeftMouseButton(evt) && jTableMap.getSelectedRow() != -1) {
            File f = getMapFilesSorted().get(jTableMap.getSelectedRow());
            openMapFile(f);
        }
    }//GEN-LAST:event_jTableMapMouseClicked

    private void jPanelMapRenderMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanelMapRenderMouseEntered
        Stamp stamp = getTilesetRenderPanel().getTilesetStamp();
        getMapRenderPanel().setMapStamp(stamp);
    }//GEN-LAST:event_jPanelMapRenderMouseEntered

    private void jPanelMapRenderMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanelMapRenderMouseExited
        getMapRenderPanel().setMapStamp(null);
    }//GEN-LAST:event_jPanelMapRenderMouseExited

    private void jMenuItemGroundActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemGroundActionPerformed
        getTilesetRenderPanel().setLayerForSelected(Stamp.GROUND);
    }//GEN-LAST:event_jMenuItemGroundActionPerformed

    private void jMenuItemResetLayerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemResetLayerActionPerformed
        getTilesetRenderPanel().setLayerForSelected(null);
    }//GEN-LAST:event_jMenuItemResetLayerActionPerformed

    private void jCheckBoxTilesetShowLayersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxTilesetShowLayersActionPerformed
        getTilesetRenderPanel().setShowLayers(jCheckBoxTilesetShowLayers.isSelected());
    }//GEN-LAST:event_jCheckBoxTilesetShowLayersActionPerformed

    private void jRadioButtonMapDrawItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButtonMapDrawItemStateChanged
        if(evt.getStateChange() == ItemEvent.SELECTED)
            getMapRenderPanel().setDrawMode(EditorRenderPanel.DrawMode.Draw);
    }//GEN-LAST:event_jRadioButtonMapDrawItemStateChanged

    private void jRadioButtonMapSelectItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButtonMapSelectItemStateChanged
        if(evt.getStateChange() == ItemEvent.SELECTED)
            getMapRenderPanel().setDrawMode(EditorRenderPanel.DrawMode.Select);
    }//GEN-LAST:event_jRadioButtonMapSelectItemStateChanged

    private void jRadioButtonMapEraseItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButtonMapEraseItemStateChanged
        if(evt.getStateChange() == ItemEvent.SELECTED)
            getMapRenderPanel().setDrawMode(EditorRenderPanel.DrawMode.Erase);
    }//GEN-LAST:event_jRadioButtonMapEraseItemStateChanged

    private void jRadioButtonPointItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButtonPointItemStateChanged
        if(evt.getStateChange() == ItemEvent.SELECTED)
            getMapRenderPanel().setPencilMode(EditorRenderPanel.PencilMode.Point);
    }//GEN-LAST:event_jRadioButtonPointItemStateChanged

    private void jRadioButtonLineItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButtonLineItemStateChanged
        if(evt.getStateChange() == ItemEvent.SELECTED)
            getMapRenderPanel().setPencilMode(EditorRenderPanel.PencilMode.Line);
    }//GEN-LAST:event_jRadioButtonLineItemStateChanged

    private void jRadioButtonRectItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButtonRectItemStateChanged
        if(evt.getStateChange() == ItemEvent.SELECTED)
            getMapRenderPanel().setPencilMode(EditorRenderPanel.PencilMode.Rect);
    }//GEN-LAST:event_jRadioButtonRectItemStateChanged

    private void jRadioButtonCircleItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButtonCircleItemStateChanged
        if(evt.getStateChange() == ItemEvent.SELECTED)
            getMapRenderPanel().setPencilMode(EditorRenderPanel.PencilMode.Circle);
    }//GEN-LAST:event_jRadioButtonCircleItemStateChanged

    private void jCheckBoxPencilFillActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxPencilFillActionPerformed
        getMapRenderPanel().setFillPencil(jCheckBoxPencilFill.isSelected());
    }//GEN-LAST:event_jCheckBoxPencilFillActionPerformed

    private void jComboBoxEraseLayerItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxEraseLayerItemStateChanged
        getMapRenderPanel().setEraseLayer((String) jComboBoxEraseLayer.getSelectedItem());
    }//GEN-LAST:event_jComboBoxEraseLayerItemStateChanged

    private void jMenuItemResetTilesetCacheActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemResetTilesetCacheActionPerformed
        getMapRenderPanel().resetTilesetCache();
    }//GEN-LAST:event_jMenuItemResetTilesetCacheActionPerformed

    public static void showGUI(File projectFolder) {
        java.awt.EventQueue.invokeLater(() -> {
            new MapTilesetSpriteFrame(projectFolder).setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroupDrawMode;
    private javax.swing.ButtonGroup buttonGroupMapMode;
    private javax.swing.JButton jButtonDuplicate;
    private javax.swing.JButton jButtonMapNew;
    private javax.swing.JButton jButtonRemove;
    private javax.swing.JButton jButtonTilesetCut;
    private javax.swing.JButton jButtonTilesetRemove;
    private javax.swing.JCheckBox jCheckBoxMapShowBlock;
    private javax.swing.JCheckBox jCheckBoxMapShowGrid;
    private javax.swing.JCheckBox jCheckBoxMapShowZ;
    private javax.swing.JCheckBox jCheckBoxPencilFill;
    private javax.swing.JCheckBox jCheckBoxTilesetShowBlock;
    private javax.swing.JCheckBox jCheckBoxTilesetShowGrid;
    private javax.swing.JCheckBox jCheckBoxTilesetShowLayers;
    private javax.swing.JCheckBox jCheckBoxTilesetShowZ;
    private javax.swing.JComboBox<String> jComboBoxEraseLayer;
    private javax.swing.JMenuBar jMenuBarMain;
    private javax.swing.JMenuItem jMenuItemBlock;
    private javax.swing.JMenuItem jMenuItemGround;
    private javax.swing.JMenuItem jMenuItemPass;
    private javax.swing.JMenuItem jMenuItemResetLayer;
    private javax.swing.JMenuItem jMenuItemResetTilesetCache;
    private javax.swing.JMenuItem jMenuItemTilesetPasteImage;
    private javax.swing.JMenu jMenuMap;
    private javax.swing.JMenu jMenuSprite;
    private javax.swing.JMenu jMenuTileset;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanelMap;
    private javax.swing.JPanel jPanelMapRender;
    private javax.swing.JPanel jPanelSprite;
    private javax.swing.JPanel jPanelTileset;
    private javax.swing.JPanel jPanelTilesetList;
    private javax.swing.JPanel jPanelTilesetRender;
    private javax.swing.JRadioButton jRadioButtonCircle;
    private javax.swing.JRadioButton jRadioButtonLine;
    private javax.swing.JRadioButton jRadioButtonMapDraw;
    private javax.swing.JRadioButton jRadioButtonMapErase;
    private javax.swing.JRadioButton jRadioButtonMapSelect;
    private javax.swing.JRadioButton jRadioButtonPoint;
    private javax.swing.JRadioButton jRadioButtonRect;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPaneMapRender;
    private javax.swing.JScrollPane jScrollPaneTileset;
    private javax.swing.JSpinner jSpinnerTilesetZ;
    private javax.swing.JSplitPane jSplitPaneMain;
    private javax.swing.JSplitPane jSplitPaneMap;
    private javax.swing.JSplitPane jSplitPaneTileset;
    private javax.swing.JTabbedPane jTabbedPaneMap;
    private javax.swing.JTabbedPane jTabbedPaneTile;
    private javax.swing.JTabbedPane jTabbedPaneTilesetSprite;
    private javax.swing.JTable jTableMap;
    private javax.swing.JTable jTableTileset;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToolBar jToolBar2;
    private javax.swing.JToolBar jToolBar3;
    private javax.swing.JToolBar jToolBar4;
    private javax.swing.JToolBar jToolBarTileset;
    // End of variables declaration//GEN-END:variables
}
