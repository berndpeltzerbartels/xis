package test.widget;

import one.xis.Push;
import one.xis.PushData;
import one.xis.RefreshClient;

import java.util.List;

@Push(PushWidget2.class) // TODO noch in gebrauch ?
interface PushPageClient extends RefreshClient {

    void setTitle(@PushData("title") String title);

    void setItemList(@PushData("items") List<PushItem> items);

}
