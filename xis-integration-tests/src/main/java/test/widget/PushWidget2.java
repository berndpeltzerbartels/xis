package test.widget;

import one.xis.ModelData;
import one.xis.Frontlet;

import java.util.List;

@Frontlet
class PushWidget2 {

    @ModelData("items")
    List<PushItem> items() {
        return List.of(new PushItem(1, "item1"), new PushItem(2, "item2"));
    }
}
