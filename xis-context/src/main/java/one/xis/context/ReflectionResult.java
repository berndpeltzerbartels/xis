package one.xis.context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.List;

@Getter
@RequiredArgsConstructor
class ReflectionResult {
    private final List<Class<?>> allComponentClasses; // Do not use Set here. There might b more than one candidate
    private final Collection<Class<?>> classesToInstantiate;
}
