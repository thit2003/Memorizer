package Memorizer;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class MindMapLoader {
    private static final Gson gson = new Gson();

    public static List<Node> loadMindMap(String filePath) throws IOException {
        try (FileReader reader = new FileReader(filePath)) {
            return gson.fromJson(reader, new TypeToken<List<Node>>() {}.getType());
        }
    }
}
