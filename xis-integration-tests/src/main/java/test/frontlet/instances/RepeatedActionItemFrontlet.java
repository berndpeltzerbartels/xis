package test.frontlet.instances;

import one.xis.Frontlet;
import one.xis.FrontletParameter;
import one.xis.ModelData;
import one.xis.RefreshOnUpdateEvents;

import java.util.HashMap;
import java.util.Map;

@Frontlet
@RefreshOnUpdateEvents("action-item-updated")
class RepeatedActionItemFrontlet {

    private final Map<Integer, Integer> loadCounts = new HashMap<>();

    @ModelData
    String itemText(@FrontletParameter("itemId") Integer itemId) {
        var loadCount = loadCounts.merge(itemId, 1, Integer::sum);
        return itemId + ":" + loadCount;
    }
}
