package one.xis.json;

import com.google.gson.JsonElement;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Field;

@RequiredArgsConstructor
class JsonField {
    private final Field field;

    void updateField(JsonElement element) {
        
    }

}
