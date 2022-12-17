package one.xis.page;

import one.xis.Page;
import one.xis.context.XISInject;

@Page(path = "/MinimalPage")
class MinimalPage {

    @XISInject
    private PageService pageService;
}
