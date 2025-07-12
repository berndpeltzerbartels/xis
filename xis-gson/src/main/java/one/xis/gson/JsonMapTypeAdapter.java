package one.xis.gson;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Map;

public class JsonMapTypeAdapter extends TypeAdapter<JsonMap> {
    @Override
    public void write(JsonWriter out, JsonMap map) throws IOException {
        // Normales Gson-Verhalten: Map<String, String> serialisieren
        out.beginObject();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            out.name(entry.getKey());
            String value = entry.getValue();
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value);
            }
        }
        out.endObject();
    }

    @Override
    public JsonMap read(JsonReader in) throws IOException {
        // Normales Gson-Verhalten: Map<String, String> auslesen, aber Werte als String
        JsonMap map = new JsonMap();
        in.beginObject();
        while (in.hasNext()) {
            String key = in.nextName();
            String value;
            JsonToken token = in.peek();
            if (token == JsonToken.NULL) {
                in.nextNull();
                value = null;
            } else if (token == JsonToken.STRING) {
                value = in.nextString();
            } else {
                // Für Objekte, Arrays, Zahlen, Booleans: komplettes JSON als String lesen
                JsonElement elem = JsonParser.parseReader(in);
                value = elem.toString();
            }
            map.put(key, value);
        }
        in.endObject();
        return map;
    }
}
