package test.page.modelcalls;

import one.xis.context.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ModelCallCounter {
    private final AtomicInteger pageModelCalls = new AtomicInteger();
    private final AtomicInteger pageActionCalls = new AtomicInteger();
    private final AtomicInteger frontletModelCalls = new AtomicInteger();

    int pageModelCalls() {
        return pageModelCalls.incrementAndGet();
    }

    void pageActionCalled() {
        pageActionCalls.incrementAndGet();
    }

    int frontletModelCalls() {
        return frontletModelCalls.incrementAndGet();
    }

    public int getPageModelCalls() {
        return pageModelCalls.get();
    }

    public int getPageActionCalls() {
        return pageActionCalls.get();
    }

    public int getFrontletModelCalls() {
        return frontletModelCalls.get();
    }
}
