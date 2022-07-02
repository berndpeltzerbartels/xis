package one.xis.context;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

class SingletonInstatiationErrorMessage {

    void check(Collection<SingtelonInstantiator> unexecutedInstantitors) {
        Collection<SingtelonInstantiator> problemInstantiators = findRootProblemInstantiators(unexecutedInstantitors);
    }


    private Collection<SingtelonInstantiator> findRootProblemInstantiators(Collection<SingtelonInstantiator> unexecutedInstantitors) {
        Map<Class<?>, SingtelonInstantiator> instantiatorMap = new HashMap<>();
        unexecutedInstantitors.forEach(instantiator -> instantiatorMap.put(instantiator.getType(), instantiator));
        unexecutedInstantitors.stream().map(SingtelonInstantiator::getUnsatisfiedConstructorParameters).flatMap(Collection::stream).map(ConstructorParameter::getElementType).forEach(instantiatorMap::remove);
        return instantiatorMap.values();
    }
}
