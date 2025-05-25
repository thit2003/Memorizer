# 🧠 Memorizer (Mind-Map Tool)

## A Java-Based Visual Mind Mapping Tool

**Memorizer** is a Java-based application that enables users to visually create and organize ideas using a dynamic mind-map interface. It provides intuitive tools for node management, shape drawing, line creation, and full canvas customization.

---

## 📁 Project Structure

```
Memorizer/
├── src/                         # 🧩 Java source files
│   ├── main/
│   │   └── java/
│   └── test/
├── mindmap.json                 # 🧠 Example saved mind map in JSON format
├── pom.xml                      # ⚙️ Maven build configuration
├── .gitignore                   # 🚫 Git ignored files and IDE configs
└── README.md                    # 📘 Project description
```

---

## 🧰 Key Features

- ➕ **Add/Edit/Delete Nodes**
- 🧱 **Draw Shapes**: Rectangle, Circle, Square
- 🔗 **Draw and Delete Lines** with mouse movement
- 🎨 **Customize Appearance**:
  - Background color
  - Node font and size
- 🖱️ **Drag and Move** any element with left mouse click
- 💾 **Local Storage**:
  - Save/Load mind maps as `.json`
  - Export canvas as `.png`

---

## 🛠️ Technologies Used

- ☕ **Java 23**
- 🧪 **Maven** for dependency management
- 📦 **Gson** for JSON serialization/deserialization
- 🖼️ **Swing/AWT** (assumed for UI components)

---

## 📦 Installation

### 🔧 Requirements
- Java 17+ (Java 23 preferred)
- Maven 3.x

### 🚀 To Run:
```bash
git clone https://github.com/your-username/Memorizer.git
cd Memorizer
mvn compile
mvn exec:java
```

> ✅ Make sure your `pom.xml` includes the `exec-maven-plugin` if you want to run via `mvn exec:java`.

---

## 📝 Save & Load Format

Mind maps are saved as `.json` files with structure like:

```json
{
  "nodes": [
    { "name": "aa", "position": { "x": 469, "y": 315 }, "connections": [] }
  ],
  "shapes": [],
  "lines": [
    { "x1": 584, "y1": 331, "x2": 1097, "y2": 386 }
  ],
  "backgroundColor": -1118482,
  "nodeColor": -16777216,
  "nodeFont": "Arial"
}
```

---

## 📫 Contact

For issues or contributions, feel free to open a pull request or contact the developer.

---
