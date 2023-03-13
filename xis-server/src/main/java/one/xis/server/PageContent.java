package one.xis.server;

import lombok.Data;

import java.util.Map;

@Data
class PageContent {
    private String headContent;
    private String bodyContent;
    private Map<String, Object> bodyAttributes;
}
