package one.xis.page;

import one.xis.context.XISComponent;

@XISComponent
class PageControllerWrapperFactory {

    PageControllerWrapper createWrapper(Object controller) {
        return PageControllerWrapper.builder().build();
    }
}
