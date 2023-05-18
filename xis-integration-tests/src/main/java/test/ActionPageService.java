package test;

import java.util.List;

interface ActionPageService {

    List<ActionPageData> getDataList();

    void update(ActionPageData data);
}
