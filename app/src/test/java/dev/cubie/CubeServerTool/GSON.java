package dev.cubie.CubeServerTool;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JSONTest {

    @Test
    void testJsonSerialization() {
        // Erstelle ein JSONObject
        JSONObject obj = new JSONObject();
        obj.put("name", "John");
        obj.put("age", 30);

        // Konvertiere das JSONObject in einen String
        String jsonString = obj.toString();
        System.out.println("JSON: " + jsonString);

        // Parse den JSON-String zurück in ein JSONObject
        JSONObject jsonObject = new JSONObject(jsonString);

        // Überprüfe, ob die Originaldaten korrekt wiederhergestellt wurden
        assertEquals("John", jsonObject.getString("name"));
        assertEquals(30, jsonObject.getInt("age"));
    }
}
