package Memorizer;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Node {
    private String name;
    private Point position;
    private List<Node> connections; // Store connected nodes

    public Node(String name, Point position) {
        this.name = name;
        this.position = position;
        this.connections = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public List<Node> getConnections() {
        return connections;
    }

}
