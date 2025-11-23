package test.page.global;

import one.xis.GlobalVariable;
import one.xis.Page;

import java.util.List;

@Page("/global-var-list.html")
class GlobalVarListPage {

    private final List<GlobalVarListItem> globalVarListItems = List.of(
            new GlobalVarListItem(1, "test1"),
            new GlobalVarListItem(2, "test2"),
            new GlobalVarListItem(3, "test3"),
            new GlobalVarListItem(4, "test4"),
            new GlobalVarListItem(5, "test5")
    );


    @GlobalVariable("data")
    List<GlobalVarListItem> data() {
        return globalVarListItems;
    }


}
