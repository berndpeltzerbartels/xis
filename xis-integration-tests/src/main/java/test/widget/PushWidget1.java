package test.widget;

import lombok.RequiredArgsConstructor;
import one.xis.Action;
import one.xis.Widget;

import java.util.List;

@Widget
@RequiredArgsConstructor
class PushWidget1 {

    private final PushPageClient client;

    @Action("click")
    void action() {
        client.setTitle("widget2");
        client.setItemList(List.of(new PushItem(1, "item1"), new PushItem(2, "item2")));
    }
}
