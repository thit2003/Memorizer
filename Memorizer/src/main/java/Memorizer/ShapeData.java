package Memorizer;

class ShapeData {
    String type;  // "Rectangle", "Circle"
    int x, y, width, height;  // Position & size

    // Constructor
    public ShapeData(String type, int x, int y, int width, int height) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
}
