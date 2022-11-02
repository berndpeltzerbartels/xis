package one.xis.dto;

import lombok.Data;

import java.util.Map;

@Data
public class Response {
    private String componentId;
    private Object componentModel;// TODO only edited objects
    private Map<String, Object> clientState; // TODO only edited objects
}
