package one.xis.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class ActionRequest extends Request {
    private String action;
    private Map<String, Object> componentModel;
}
