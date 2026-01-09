package one.xis.context.defaultcomponent.beanmethod;

import lombok.Getter;

@Getter
public class Config {
    private final String source;

    public Config(String source) {
        this.source = source;
    }
}
