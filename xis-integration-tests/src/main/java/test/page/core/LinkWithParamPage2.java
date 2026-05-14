package test.page.core;

import one.xis.ModelData;
import one.xis.Page;
import one.xis.QueryParameter;

@Page("/linkWithParamPage2.html")
class LinkWithParamPage2 {

    @ModelData("title")
    String title(@QueryParameter("title") String title) {
        return "The title is '" + title + "'";
    }
}
