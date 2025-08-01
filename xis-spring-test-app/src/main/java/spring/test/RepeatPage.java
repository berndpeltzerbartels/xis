package spring.test;

import one.xis.ModelData;
import one.xis.Page;
import one.xis.Roles;

import java.util.List;

@Roles("admin")
@Page("/repeat.html")
class RepeatPage {

    @ModelData("items")
    List<RepeatPageItem> items() {
        return List.of(new RepeatPageItem(1, "title1"), new RepeatPageItem(2, "title2"), new RepeatPageItem(3, "title3"));
    }
}
