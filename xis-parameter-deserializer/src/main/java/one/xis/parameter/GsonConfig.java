package one.xis.parameter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISBean;
import one.xis.context.XISComponent;

@XISComponent
@RequiredArgsConstructor
class GsonConfig {

    @XISBean
    Gson gson() {
        return new GsonBuilder()
                .setLenient()
                .create();
    }
}
