package one.xis.remote.js;

import lombok.Data;

import java.io.PrintWriter;

@Data
class JSArrayElement implements JSValue {
    private final JSArray array;
    private final JSValue key;

    @Override
    public void writeJS(PrintWriter writer) {
        writeReferenceJS(writer);
    }

    @Override
    public String getName() {
        return new StringBuilder()
                .append(array.getName())
                .append("[")
                .append(array.getName())
                .append("]").toString();
    }

    @Override
    public void writeReferenceJS(PrintWriter writer) {
        writer.append(array.getName());
        writer.append("[");
        writer.append(key.getName());
        writer.append("]");
    }

}
