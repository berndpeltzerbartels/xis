package one.xis.dto;

import lombok.Data;

@Data
public class Response {
    private String controllerClass;
    private ComponentState componentState;// TODO only edited objects ?
    private ClientState clientState; // TODO only edited objects ?
}
