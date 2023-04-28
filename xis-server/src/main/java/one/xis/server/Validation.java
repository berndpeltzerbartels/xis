package one.xis.server;

import one.xis.context.XISComponent;
import one.xis.context.XISInject;

import java.util.Collection;

@XISComponent
class Validation {

    @XISInject
    private Collection<Validator<?>> validators;
}
