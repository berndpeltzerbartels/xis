package one.xis.test.it;

import one.xis.Model;
import one.xis.Page;

@Page("/titleAndHeadline.html")
class TitleAndHeadlinePage {

    @Model("data")
    TitleAndHeadlineData titleAndHeadlineData() {
        return new TitleAndHeadlineData("title", "headline");
    }
}
