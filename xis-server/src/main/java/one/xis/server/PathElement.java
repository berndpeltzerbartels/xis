package one.xis.server;

import lombok.Getter;
import lombok.Setter;


abstract class PathElement {
    abstract String normalized();

    @Getter
    @Setter
    private PathElement next;
}
