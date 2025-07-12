package one.xis.gson;

import com.google.gson.*;
import io.goodforgod.gson.configuration.GsonConfiguration;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISBean;
import one.xis.context.XISComponent;

import java.time.Duration;

@XISComponent
@RequiredArgsConstructor
public class GsonFactory {

    @XISBean
    public Gson gson() {
        return new GsonConfiguration().builder()
                .registerTypeAdapter(Duration.class, new JsonSerializer<Duration>() {
                    @Override
                    public JsonElement serialize(Duration src, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
                        return new JsonPrimitive(src.toString());
                    }
                })
                .registerTypeAdapter(Duration.class, new JsonDeserializer<Duration>() {
                    @Override
                    public Duration deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        return Duration.parse(json.getAsString());
                    }
                })
                .registerTypeAdapter(JsonMap.class, new JsonMapTypeAdapter())
                .create();
    }
}