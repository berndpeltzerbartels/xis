package one.xis.parameter;

import com.google.gson.stream.JsonReader;
import lombok.RequiredArgsConstructor;
import one.xis.utils.lang.CollectionUtils;
import one.xis.validation.ValidatorResultElement;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;

@RequiredArgsConstructor
class ArrayDeserializer {
    private final FormattedParameterDeserializer parameterDeserializer;

    Collection<Object> deserializeArrayToArray(JsonReader reader, Target target, ValidatorResultElement result) throws IOException {
        var list = new ArrayList<>();
        deserializeArray(reader, target, result, list);
        return list;
    }

    @SuppressWarnings("unchecked")
    Collection<Object> deserializeArrayToCollection(JsonReader reader, Target target, ValidatorResultElement result) throws IOException {
        var collection = CollectionUtils.emptyInstance((Class<? extends Collection<Object>>) target.getType());
        deserializeArray(reader, target, result, collection);
        return collection;
    }

    private void deserializeArray(JsonReader reader, Target target, ValidatorResultElement parentResult, Collection<Object> collection) throws IOException {
        reader.beginArray();
        int index = 0;
        while (reader.hasNext()) {
            var result = parentResult.childElement(target.getName(), index++);
            Target elementTarget;
            if (target.getElementType() instanceof ParameterizedType parameterizedType) {
                elementTarget = new ParameterizedTargetElement(target.getName(), parameterizedType);
            } else if (target.getElementType() instanceof Class<?> clazz) {
                elementTarget = new ClassTargetElement(target.getName(), clazz);
            } else {
                throw new IllegalStateException();
            }
            parameterDeserializer.read(reader, elementTarget, result).ifPresent(collection::add);
        }
        reader.endArray();
    }

}
