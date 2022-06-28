package one.xis;

import one.xis.context.AppContext;

public class XISContext {

    private static final AppContext INSTANCE = AppContext.getInstance("one.xis");

    public static AppContext getInstance() { // TODO als Future ?
        return INSTANCE;
    }

}
