package test.page.core;

import one.xis.ModelData;
import one.xis.Page;

@Page("/titleAndHeadline.html")
class TitleAndHeadlinePage {

    @ModelData("data")
    TitleAndHeadlineData titleAndHeadlineData() {
        return new TitleAndHeadlineData("title", "headline");
    }
}
