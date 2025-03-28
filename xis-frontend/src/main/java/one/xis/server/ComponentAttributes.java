package one.xis.server;

import lombok.Data;

import java.util.Collection;
import java.util.HashSet;

@Data
class ComponentAttributes {
    private final Collection<String> pageScope = new HashSet<>();
    private final Collection<String> localStorage = new HashSet<>();
    private final Collection<String> localDatabase = new HashSet<>();
}
