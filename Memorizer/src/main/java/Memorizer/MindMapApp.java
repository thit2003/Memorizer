package Memorizer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.print.Printable;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class MindMapApp {
    private JFrame frame;
    private JPanel panel;
    private List<Node> nodes;
    private List<Shape> shapes;
    private Node draggedNode;
    private Shape draggedShape;
    private Point mouseOffset;
    private boolean isDrawingLine = false;  // Flag to track drawing mode
    private Line2D.Float tempLine = null;   // Temporary line before finalizing
    private List<Line2D.Float> lines = new ArrayList<>();
    private Line2D.Float selectedLine = null; // Track selected line for deletion
    private Color nodeColor = Color.BLACK;  // Default node color
    private Font nodeFont = new Font("Arial", Font.PLAIN, 14);  // Default font
    private int nodeSize = 16;  // Default size
    private Shape selectedShape = null; // Store the selected shape


    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                MindMapApp window = new MindMapApp();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public MindMapApp() {
        nodes = new ArrayList<>();
        shapes = new ArrayList<>();
        initialize();
    }

    private void initialize() {
        frame = new JFrame();
        frame.setBounds(100, 100, 900, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setStroke(new BasicStroke(2));

                // 🎯 Draw all completed lines first (so they stay in the background)
                g2d.setColor(Color.BLACK);
                for (Line2D.Float line : lines) {
                    g2d.draw(line);
                }

                // 🎯 Draw temporary line while dragging
                if (tempLine != null) {
                    g2d.setColor(Color.RED);
                    g2d.draw(tempLine);
                }

                // 🎯 Draw all shapes **before** drawing nodes
                g2d.setColor(Color.RED);
                for (Shape shape : shapes) {
                    g2d.fill(shape);
                }

                // 🎯 Draw all nodes **after** shapes (so they appear on top)
                g2d.setColor(nodeColor); // 🎯 Use dynamic node color
                g2d.setFont(nodeFont); // 🎯 Use selected font
                for (Node node : nodes) {
                    Point position = node.getPosition();
                    g2d.drawString(node.getName(), position.x, position.y);
                }

            }
        };

        panel.setLayout(null);
        frame.getContentPane().add(panel, BorderLayout.CENTER);

        // Add Node Button
        JButton addNodeButton = new JButton("Add Node");
        addNodeButton.setBounds(10, 10, 120, 30);
        panel.add(addNodeButton);
        addNodeButton.addActionListener(e -> addNode());

        // Edit Node Button
        JButton editNodeButton = new JButton("Edit Node");
        editNodeButton.setBounds(140, 10, 120, 30);
        panel.add(editNodeButton);
        editNodeButton.addActionListener(e -> editNode());

        // Delete Node Button
        JButton deleteNodeButton = new JButton("Delete Node");
        deleteNodeButton.setBounds(270, 10, 120, 30);
        panel.add(deleteNodeButton);
        deleteNodeButton.addActionListener(e -> deleteNode());

        // Add Shape Button
        JButton addShapeButton = new JButton("Add Shape");
        addShapeButton.setBounds(400, 10, 120, 30);
        panel.add(addShapeButton);
        addShapeButton.addActionListener(e -> addShape());

        // Delete Shape Button
        JButton deleteShapeButton = new JButton("Delete Shape");
        deleteShapeButton.setBounds(530, 10, 120, 30);
        panel.add(deleteShapeButton);
        deleteShapeButton.addActionListener(e -> deleteSelectedShape());

        // Draw Line Button
        JButton drawLineButton = new JButton("Draw Line");
        drawLineButton.setBounds(660, 10, 120, 30);
        panel.add(drawLineButton);
        drawLineButton.addActionListener(e -> startLineDrawing());

        // Delete Line Button
        JButton deleteLineButton = new JButton("Delete Line");
        deleteLineButton.setBounds(790, 10, 120, 30);
        panel.add(deleteLineButton);
        deleteLineButton.addActionListener(e -> deleteLine());

        // Settings Button
        JButton settingsButton = new JButton("Settings");
        settingsButton.setBounds(920, 10, 120, 30);
        panel.add(settingsButton);
        settingsButton.addActionListener(e -> openSettingsMenu());

        JButton saveButton = new JButton("Save");
        saveButton.setBounds(1050, 10, 120, 30);
        panel.add(saveButton);
        saveButton.addActionListener(e -> saveMindMap());

        JButton loadButton = new JButton("Load");
        loadButton.setBounds(1180, 10, 120, 30);
        panel.add(loadButton);
        loadButton.addActionListener(e -> loadMindMap());

        // Export Image
        JButton exportImageButton = new JButton("Export as Image");
        exportImageButton.setBounds(1310, 10, 150, 30);
        panel.add(exportImageButton);
        exportImageButton.addActionListener(e -> exportAsImage());

        // Mouse Listeners for Dragging Nodes, Shapes, and Drawing Lines
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (isDrawingLine) {
                    // 🎯 Start a new temporary line from the starting point
                    tempLine = new Line2D.Float(e.getPoint(), e.getPoint());
                    panel.repaint();
                } else {
                    selectedLine = null;
                    selectedShape = null;
                    draggedNode = null;
                    draggedShape = null;

                    // 🎯 Check if clicking on an existing line
                    for (Line2D.Float line : lines) {
                        if (line.ptSegDist(e.getPoint()) < 5) {
                            selectedLine = line;
                            panel.repaint();
                            return;
                        }
                    }

                    // 🎯 Check if clicking on a shape
                    for (Shape shape : shapes) {
                        if (shape.contains(e.getPoint())) {
                            selectedShape = shape;
                            draggedShape = shape;

                            // ✅ Store correct mouse offset **relative to shape's position**
                            Rectangle shapeBounds = shape.getBounds();
                            mouseOffset = new Point(e.getX() - shapeBounds.x, e.getY() - shapeBounds.y);
                            return;
                        }
                    }

                    // 🎯 Check if clicking on a node
                    for (Node node : nodes) {
                        Rectangle nodeBounds = new Rectangle(node.getPosition().x - 5, node.getPosition().y - 15, 100, 30);
                        if (nodeBounds.contains(e.getPoint())) {
                            draggedNode = node;
                            mouseOffset = new Point(e.getX() - node.getPosition().x, e.getY() - node.getPosition().y);
                            return;
                        }
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isDrawingLine && tempLine != null) {
                    // 🎯 Finalize the drawn line
                    tempLine.setLine(tempLine.getP1(), e.getPoint());
                    lines.add(tempLine); // ✅ Save the line
                    tempLine = null; // ✅ Reset temp line
                    isDrawingLine = false; // ✅ Stop drawing mode
                    panel.repaint();
                } else {
                    draggedNode = null;
                    draggedShape = null;
                }
            }
        });

        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isDrawingLine && tempLine != null) {
                    // 🎯 Update the temporary line while dragging
                    tempLine.setLine(tempLine.getP1(), e.getPoint());
                    panel.repaint();
                } else if (draggedNode != null) {
                    // 🎯 Move node correctly
                    Point newPosition = new Point(e.getX() - mouseOffset.x, e.getY() - mouseOffset.y);
                    draggedNode.setPosition(newPosition);

                    JLabel label = findLabelForNode(draggedNode);
                    if (label != null) {
                        label.setLocation(newPosition);
                    }

                    panel.repaint();
                } else if (draggedShape != null) {
                    // 🎯 Move shape correctly using stored offset
                    Rectangle shapeBounds = draggedShape.getBounds();
                    int newX = e.getX() - mouseOffset.x;
                    int newY = e.getY() - mouseOffset.y;

                    // 🎯 Preserve the shape type when dragging
                    Shape newShape;
                    if (draggedShape instanceof Rectangle) {
                        newShape = new Rectangle(newX, newY, shapeBounds.width, shapeBounds.height);
                    } else if (draggedShape instanceof Ellipse2D.Float) {
                        newShape = new Ellipse2D.Float(newX, newY, shapeBounds.width, shapeBounds.height);
                    } else {
                        newShape = draggedShape; // Keep original shape type
                    }

                    // 🎯 Replace the old shape with the new moved shape
                    shapes.remove(draggedShape);
                    shapes.add(newShape);
                    draggedShape = newShape; // Update reference

                    panel.repaint();
                }
            }
        });

    }

    private void addNode() {
        String nodeName = JOptionPane.showInputDialog("Enter node name:");
        if (nodeName == null || nodeName.trim().isEmpty()) {
            return; // Cancel if no input
        }

        Point position = new Point(50, 50); // Default position for new node

        // ✅ Check if a node already exists at the same position
        for (Node existingNode : nodes) {
            if (existingNode.getPosition().equals(position)) {
                JOptionPane.showMessageDialog(frame, "A node already exists at this position.", "Duplicate Node", JOptionPane.WARNING_MESSAGE);
                return; // Prevent duplicate
            }
        }

        // ✅ Add node only if it's unique
        Node newNode = new Node(nodeName, position);
        nodes.add(newNode);

        // ✅ Repaint the panel to update the UI
        panel.repaint();
    }

    private void editNode() {
        String nodeName = JOptionPane.showInputDialog("Enter the name of the node to edit:");
        for (Node node : nodes) {
            if (node.getName().equals(nodeName)) {
                String newName = JOptionPane.showInputDialog("Edit node name:", node.getName());
                node.setName(newName);

                for (Component component : panel.getComponents()) {
                    if (component instanceof JLabel && ((JLabel) component).getText().equals(nodeName)) {
                        ((JLabel) component).setText(newName);
                        break;
                    }
                }
                break;
            }
        }
    }

    private void deleteNode() {
        String nodeName = JOptionPane.showInputDialog("Enter the name of the node to delete:");
        Node toRemove = null;

        for (Node node : nodes) {
            if (node.getName().equals(nodeName)) {
                toRemove = node;
                break;
            }
        }

        if (toRemove != null) {
            nodes.remove(toRemove);
            for (Component component : panel.getComponents()) {
                if (component instanceof JLabel && ((JLabel) component).getText().equals(toRemove.getName())) {
                    panel.remove(component);
                    break;
                }
            }
        }

        panel.revalidate();
        panel.repaint();
    }

    private void addShape() {
        String[] shapeOptions = {"Rectangle", "Circle", "Square"};
        String selectedShape = (String) JOptionPane.showInputDialog(frame, "Choose a shape", "Shape", JOptionPane.QUESTION_MESSAGE, null, shapeOptions, shapeOptions[0]);

        if (selectedShape != null) {
            Shape newShape = null;
            int x = 200, y = 200;

            switch (selectedShape) {
                case "Rectangle":
                    newShape = new Rectangle(x, y, 100, 50);
                    break;
                case "Circle":
                    newShape = new Ellipse2D.Float(x, y, 100, 100);
                    break;
                case "Square":
                    newShape = new Rectangle(x, y, 100, 100);
                    break;
            }

            if (newShape != null) {
                shapes.add(newShape);
                panel.repaint();
            }
        }
    }

    private void deleteShape() {
        if (draggedShape != null) {
            shapes.remove(draggedShape);  // Remove the selected shape
            draggedShape = null;  // Reset selection
            panel.repaint();  // Refresh UI
        } else {
            JOptionPane.showMessageDialog(frame, "Please select a shape to delete.", "No Shape Selected", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void deleteSelectedShape() {
        if (selectedShape != null) {
            shapes.remove(selectedShape); // Remove shape from list
            selectedShape = null; // Reset selection
            panel.repaint(); // Refresh UI
        } else {
            JOptionPane.showMessageDialog(frame, "No shape selected!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void moveShape(Shape shape, int newX, int newY) {
        if (shape instanceof Rectangle) {
            Rectangle rect = (Rectangle) shape;
            rect.setLocation(newX, newY); // Move without distortion
        } else if (shape instanceof Ellipse2D.Float) {
            Ellipse2D.Float circle = (Ellipse2D.Float) shape;
            float width = circle.width;
            float height = circle.height;

            // Remove old shape and replace with a new one at the updated position
            shapes.remove(circle);
            Ellipse2D.Float newCircle = new Ellipse2D.Float(newX, newY, width, height);
            shapes.add(newCircle);

            // Update draggedShape reference to the new shape
            draggedShape = newCircle;
        }
        panel.repaint();
    }

    private JLabel findLabelForNode(Node node) {
        for (Component component : panel.getComponents()) {
            if (component instanceof JLabel label && label.getText().equals(node.getName())) {
                return label;
            }
        }
        return null;
    }

    private void startLineDrawing() {
        isDrawingLine = true; // Enable line drawing mode
        selectedLine = null;  // Reset line selection for deletion
        tempLine = null;      // Reset temporary line
    }

    private void deleteLine() {
        if (selectedLine != null) {
            lines.remove(selectedLine);  // Remove the selected line
            selectedLine = null;  // Reset selection
            panel.repaint();
        } else {
            JOptionPane.showMessageDialog(frame, "Please select a line to delete.", "No Line Selected", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void openSettingsMenu() {
        JDialog settingsDialog = new JDialog(frame, "Customize Mind Map", true);
        settingsDialog.setSize(400, 300);
        settingsDialog.setLayout(new GridLayout(4, 1));

        // Background Color Picker
        JButton bgColorButton = new JButton("Change Background Color");
        bgColorButton.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(frame, "Choose Background Color", panel.getBackground());
            if (newColor != null) {
                panel.setBackground(newColor);
                panel.repaint();
            }
        });

        // Node Color Picker
        JButton nodeColorButton = new JButton("Change Node Color");
        nodeColorButton.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(frame, "Choose Node Color", nodeColor);
            if (newColor != null) {
                nodeColor = newColor; // ✅ Update global node color
                panel.repaint(); // ✅ Refresh UI
            }
        });

        // Font Chooser
        JButton fontButton = new JButton("Change Node Font");
        fontButton.addActionListener(e -> {
            JDialog fontDialog = new JDialog(frame, "Select Font", true);
            fontDialog.setSize(400, 250);
            fontDialog.setLayout(new BorderLayout());

            JPanel fontPanel = new JPanel(new GridLayout(4, 1, 5, 5)); // 4 Rows, Proper Spacing

            // Font Families
            String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
            JComboBox<String> fontDropdown = new JComboBox<>(fonts);

            // Font Styles
            String[] styles = {"Plain", "Bold", "Italic"};
            JComboBox<String> styleDropdown = new JComboBox<>(styles);

            // Font Sizes
            Integer[] sizes = {12, 14, 16, 18, 20, 24, 30};
            JComboBox<Integer> sizeDropdown = new JComboBox<>(sizes);

            // Apply Button
            JButton applyButton = new JButton("Apply");
            applyButton.addActionListener(event -> {
                String selectedFont = (String) fontDropdown.getSelectedItem();
                int selectedStyle = styleDropdown.getSelectedIndex();
                int selectedSize = (int) sizeDropdown.getSelectedItem();

                nodeFont = new Font(selectedFont, selectedStyle, selectedSize);
                panel.repaint();
                fontDialog.dispose();
            });

            // Add components with spacing
            fontPanel.add(new JLabel("Select Font:"));
            fontPanel.add(fontDropdown);
            fontPanel.add(new JLabel("Select Style:"));
            fontPanel.add(styleDropdown);
            fontPanel.add(new JLabel("Select Size:"));
            fontPanel.add(sizeDropdown);

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(applyButton); // Center the apply button

            // Add panels to dialog
            fontDialog.add(fontPanel, BorderLayout.CENTER);
            fontDialog.add(buttonPanel, BorderLayout.SOUTH);

            fontDialog.setLocationRelativeTo(frame); // Center the dialog
            fontDialog.setVisible(true);
        });


        // Node Size Picker
        String[] sizes = {"Small", "Medium", "Large"};
        JComboBox<String> sizeDropdown = new JComboBox<>(sizes);
        sizeDropdown.addActionListener(e -> {
            String selectedSize = (String) sizeDropdown.getSelectedItem();
            switch (selectedSize) {
                case "Small": nodeSize = 12; break;
                case "Medium": nodeSize = 16; break;
                case "Large": nodeSize = 20; break;
            }
            panel.repaint();
        });

        settingsDialog.add(bgColorButton);
        settingsDialog.add(nodeColorButton);
        settingsDialog.add(fontButton);
        settingsDialog.add(sizeDropdown);

        settingsDialog.setVisible(true);
    }

    private void exportAsImage() {
        BufferedImage image = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        panel.paint(g2d); // Capture panel contents
        g2d.dispose();

        // Show Save Dialog (PNG Only)
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save as PNG Image");
        fileChooser.setFileFilter(new FileNameExtensionFilter("PNG Image", "png"));

        int userSelection = fileChooser.showSaveDialog(frame);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File file = new File(fileChooser.getSelectedFile().getAbsolutePath() + ".png");
            try {
                ImageIO.write(image, "png", file);
                JOptionPane.showMessageDialog(frame, "Mind map exported successfully as PNG!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame, "Error saving PNG image.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveMindMap() {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            // 🎯 Open a File Save Dialog
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Mind Map");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JSON Files", "json"));

            int userSelection = fileChooser.showSaveDialog(frame);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if (!file.getAbsolutePath().endsWith(".json")) {
                    file = new File(file.getAbsolutePath() + ".json");
                }

                FileWriter writer = new FileWriter(file);

                // Convert and save data
                List<ShapeData> shapeDataList = new ArrayList<>();
                for (Shape shape : shapes) {
                    Rectangle bounds = shape.getBounds();
                    String type = (shape instanceof Ellipse2D) ? "Circle" : "Rectangle";
                    shapeDataList.add(new ShapeData(type, bounds.x, bounds.y, bounds.width, bounds.height));
                }

                List<LineData> lineDataList = new ArrayList<>();
                for (Line2D.Float line : lines) {
                    lineDataList.add(new LineData((int) line.x1, (int) line.y1, (int) line.x2, (int) line.y2));
                }

                MindMapData mindMapData = new MindMapData(nodes, shapeDataList, lineDataList,
                        panel.getBackground(), nodeColor, nodeFont);

                gson.toJson(mindMapData, writer);
                writer.close();
                JOptionPane.showMessageDialog(frame, "Mind Map Saved Successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadMindMap() {
        try {
            Gson gson = new Gson();

            // 🎯 Open a File Open Dialog
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Load Mind Map");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JSON Files", "json"));

            int userSelection = fileChooser.showOpenDialog(frame);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                FileReader reader = new FileReader(file);

                // Read JSON
                MindMapData mindMapData = gson.fromJson(reader, MindMapData.class);
                reader.close();

                // Restore nodes
                nodes.clear();
                nodes.addAll(mindMapData.nodes);

                // Restore shapes
                shapes.clear();
                for (ShapeData shapeData : mindMapData.shapes) {
                    Shape shape = shapeData.type.equals("Circle") ?
                            new Ellipse2D.Float(shapeData.x, shapeData.y, shapeData.width, shapeData.height) :
                            new Rectangle(shapeData.x, shapeData.y, shapeData.width, shapeData.height);
                    shapes.add(shape);
                }

                // Restore lines
                lines.clear();
                for (LineData lineData : mindMapData.lines) {
                    lines.add(new Line2D.Float(lineData.x1, lineData.y1, lineData.x2, lineData.y2));
                }

                // Restore settings
                panel.setBackground(new Color(mindMapData.backgroundColor));
                nodeColor = new Color(mindMapData.nodeColor);
                nodeFont = new Font(mindMapData.nodeFont, Font.PLAIN, 16);

                panel.repaint();
                JOptionPane.showMessageDialog(frame, "Mind Map Loaded Successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
