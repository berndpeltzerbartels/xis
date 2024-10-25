package one.xis.server;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import one.xis.validation.ValidatorMessages;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServerResponse {

    @JsonIgnore
    private int status;
    private Map<String, Object> data = new HashMap<>();
    private Map<String, Object> formData = new HashMap<>();
    private String nextPageURL;
    private String nextWidgetId;
    private ValidatorMessages validatorMessages = new ValidatorMessages();
}
