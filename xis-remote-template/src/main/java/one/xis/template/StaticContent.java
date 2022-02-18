package one.xis.template;

import lombok.Data;

@Data
public class StaticContent implements MixedContent {
    private final String content;
}
