package test.widget;

import one.xis.ModelData;
import one.xis.Push;
import one.xis.RefreshClient;

import java.util.List;

@Push(PushWidget2.class)
interface PushPageClient extends RefreshClient {

    void setTitle(@ModelData("title") String title);

    void setItemList(@ModelData("items") List<PushItem> items);

}
