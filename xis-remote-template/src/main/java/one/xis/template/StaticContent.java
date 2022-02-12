package one.xis.template;

import lombok.Data;

@Data
class StaticContent implements MixedContent {
    private final String content;
}
