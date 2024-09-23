package one.xis.server;


import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;

import java.util.Map;

/**
 * We serialize the data on our own, because the programmer
 * may configure the frameworks serialzation in a way causing problems
 * for our data.
 * <p>
 * Mainly micronaut's default serialization leaves attributes with
 * empty arrays etc.
 */
@XISComponent
@RequiredArgsConstructor
class DataSerializerImpl implements DataSerializer {

    private final Gson gson;

    @Override
    public String serialize(Map<String, Object> data) {
        return gson.toJson(data);
    }
}
