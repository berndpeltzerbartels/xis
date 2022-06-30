package one.xis;

import one.xis.context.AppContext;

public class XISContext {

    public static AppContext getInstance() {
        return AppContext.getExistingInstance("one.xis");
    }

}
