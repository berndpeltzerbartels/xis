package one.xis.parameter;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import one.xis.Format;
import one.xis.context.AppContext;
import one.xis.context.XISComponent;
import one.xis.utils.lang.FieldUtil;

import java.util.Collection;
import java.util.HashSet;

@XISComponent
@RequiredArgsConstructor
class FormattedTypeAdapterFactory implements TypeAdapterFactory {

    private final Collection<Class<?>> classesHavingFormattedFields = new HashSet<>();
    private final Collection<Class<?>> classesHavinNoFormattedFields = new HashSet<>();

    private final AppContext appContext;

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        var rawType = (Class<T>) type.getRawType();
        var delegate = gson.getDelegateAdapter(this, type);
        if (hasAnnotatedFields(rawType)) {
            return new FormattedTypeAdapter(delegate, appContext);
        }
        return delegate;
    }

    private <T> boolean hasAnnotatedFields(Class<T> type) {
        if (classesHavingFormattedFields.contains(type)) {
            return true;
        }
        if (classesHavinNoFormattedFields.contains(type)) {
            return false;
        }
        for (var field : FieldUtil.getAllFields(type)) {
            if (field.isAnnotationPresent(Format.class)) {
                classesHavingFormattedFields.add(type);
                return true;
            }
        }
        classesHavinNoFormattedFields.add(type);
        return false;
    }
}
