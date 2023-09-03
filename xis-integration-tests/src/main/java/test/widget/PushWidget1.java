package test.widget;

import one.xis.Action;
import one.xis.Widget;

import java.util.List;

@Widget
class PushWidget1 {

    @Action("click")
    void action(PushPageClient client) {
        client.setTitle("widget2");
        client.setItemList(List.of(new PushItem(1, "item1"), new PushItem(2, "item2")));
    }
}
