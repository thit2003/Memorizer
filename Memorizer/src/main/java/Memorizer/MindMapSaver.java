package Memorizer;

import com.google.gson.Gson;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class MindMapSaver {
    private static final Gson gson = new Gson();

    public static void saveMindMap(List<Node> nodes, String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            gson.toJson(nodes, writer);
        }
    }
}
