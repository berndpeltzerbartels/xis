package one.xis.boot.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import one.xis.context.XISBean;
import one.xis.context.XISComponent;


// TODO Check if this is needed
@XISComponent
class ObjectMapperConfig {

    @XISBean
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
