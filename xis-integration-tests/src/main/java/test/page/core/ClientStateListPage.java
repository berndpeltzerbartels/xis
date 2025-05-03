package test.page.core;

import one.xis.ClientState;
import one.xis.Page;

import java.util.List;

@Page("/client-state-list.html")
class ClientStateListPage {

    private final List<ClientStateListItem> clientStateListItems = List.of(
            new ClientStateListItem(1, "test1"),
            new ClientStateListItem(2, "test2"),
            new ClientStateListItem(3, "test3"),
            new ClientStateListItem(4, "test4"),
            new ClientStateListItem(5, "test5")
    );


    @ClientState("data")
    List<ClientStateListItem> data() {
        return clientStateListItems;
    }


}
