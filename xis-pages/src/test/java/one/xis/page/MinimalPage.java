package one.xis.page;

import one.xis.Page;
import one.xis.context.XISInject;

@Page(value = "/MinimalPage")
class MinimalPage {

    @XISInject
    private PageService pageService;
}
