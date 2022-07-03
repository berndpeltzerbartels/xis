package one.xis.jscomponent;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import lombok.Getter;
import one.xis.context.AppContext;

@Singleton
class XISContextHolder {

    @Getter
    private AppContext appContext;

    @PostConstruct
    void init() {
        appContext = AppContext.getInstance("one.xis");
    }

}
