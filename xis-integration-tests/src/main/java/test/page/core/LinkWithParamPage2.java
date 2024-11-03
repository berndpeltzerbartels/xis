package test.page.core;

import one.xis.ModelData;
import one.xis.Page;
import one.xis.URLParameter;

@Page("/linkWithParamPage2.html")
class LinkWithParamPage2 {

    @ModelData("title")
    String title(@URLParameter("title") String title) {
        return "The title is '" + title + "'";
    }
}
