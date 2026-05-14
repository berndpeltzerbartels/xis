package one.xis.context.all;

import one.xis.context.Advice;
import one.xis.context.AdviceInvocation;

public class OverallTimingAdvice implements Advice {
    static int invocations;

    @Override
    public Object around(AdviceInvocation invocation) throws Throwable {
        invocations++;
        return "advised:" + invocation.proceed();
    }
}
