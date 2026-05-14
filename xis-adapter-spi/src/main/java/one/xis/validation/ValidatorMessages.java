package one.xis.validation;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ValidatorMessages {
    private final List<String> globalMessages = new ArrayList<>();
    private final Map<String, String> messages = new HashMap<>();

    void addGlobalMessages(String message) {
        globalMessages.add(message);
    }

    void addMessages(String field, String message) {
        messages.put(field, message);
    }

}
