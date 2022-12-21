package one.xis.context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.utils.lang.CollectorUtils;

import java.util.Collection;

@RequiredArgsConstructor
public class AppContextImpl implements AppContext {

    @Getter
    private final Collection<Object> singletons;

    @Override
    public <T> T getSingleton(Class<T> type) {
        return singletons.stream().filter(type::isInstance).map(type::cast).collect(CollectorUtils.toOnlyElement());
    }


}
