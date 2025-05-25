package Memorizer;

import java.awt.*;
import java.util.List;

class MindMapData {
    List<Node> nodes;
    List<ShapeData> shapes;
    List<LineData> lines;
    int backgroundColor;  // Store as RGB int
    int nodeColor;  // Store as RGB int
    String nodeFont; // Store font as string

    public MindMapData(List<Node> nodes, List<ShapeData> shapes, List<LineData> lines,
                       Color backgroundColor, Color nodeColor, Font nodeFont) {
        this.nodes = nodes;
        this.shapes = shapes;
        this.lines = lines;
        this.backgroundColor = backgroundColor.getRGB();  // Convert Color to int
        this.nodeColor = nodeColor.getRGB();  // Convert Color to int
        this.nodeFont = nodeFont.getFontName();  // Convert Font to String
    }
}