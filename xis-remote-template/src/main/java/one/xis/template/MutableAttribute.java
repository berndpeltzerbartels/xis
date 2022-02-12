package one.xis.template;

import lombok.Data;

import java.util.List;

@Data
public class MutableAttribute {
    private final List<MixedContent> contents;
}
