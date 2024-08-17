package one.xis.gson;

import com.google.gson.Gson;
import io.goodforgod.gson.configuration.GsonConfiguration;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISBean;
import one.xis.context.XISComponent;

@XISComponent
@RequiredArgsConstructor
public class GsonFactory {

    @XISBean
    public Gson gson() {
        return new GsonConfiguration().builder().create();
    }
}