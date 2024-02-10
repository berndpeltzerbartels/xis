package one.xis.parameter;

import com.google.gson.Gson;
import one.xis.context.XISBean;
import one.xis.context.XISComponent;

@XISComponent
class GsonConfig {

    @XISBean
    Gson gson() {
        return new Gson();
    }
    
}
