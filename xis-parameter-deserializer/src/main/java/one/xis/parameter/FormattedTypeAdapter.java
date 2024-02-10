package one.xis.parameter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.RequiredArgsConstructor;
import one.xis.FieldFormat;
import one.xis.Format;
import one.xis.UserContext;
import one.xis.context.AppContext;
import one.xis.utils.lang.FieldUtil;

import java.io.IOException;

@RequiredArgsConstructor
class FormattedTypeAdapter<T> extends TypeAdapter<T> {
    private final TypeAdapter<T> delegate;
    private final AppContext appContext;

    @Override
    @SuppressWarnings("unchecked")
    public void write(JsonWriter out, T value) throws IOException {
        var clazz = value.getClass();
        for (var field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Format.class)) {
                var annotation = field.getAnnotation(Format.class);
                var fieldValue = FieldUtil.getFieldValue(value, field);
                var formatter = (FieldFormat<Object>) appContext.getSingleton(annotation.value());
                var context = UserContext.getInstance();
                out.value(formatter.format(fieldValue, context.getLocale(), context.getZoneId()));
            } else {
                delegate.write(out, value);
            }
        }
    }

    @Override
    public T read(JsonReader in) throws IOException {
        return delegate.read(in);
    }
}
