package one.xis.parameter;

import com.google.gson.stream.JsonWriter;
import lombok.Getter;

import java.io.IOException;
import java.io.Writer;

class GsonWriter extends JsonWriter {

    @Getter
    private String currentFieldName;

    public GsonWriter(Writer out) {
        super(out);
    }

    @Override
    public JsonWriter name(String name) throws IOException {
        currentFieldName = name;
        return super.name(name);
    }
}
