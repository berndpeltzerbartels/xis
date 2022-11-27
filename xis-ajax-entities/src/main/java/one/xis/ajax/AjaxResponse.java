package one.xis.ajax;

import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
public class AjaxResponse {
    private List<AjaxResponseMessage> messages = new ArrayList<>();

    void addResponseMessages(Collection<AjaxResponseMessage> messages) {
        this.messages.addAll(messages);
    }

}
