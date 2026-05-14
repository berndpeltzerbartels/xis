package test.page.core;

import lombok.RequiredArgsConstructor;
import one.xis.Action;
import one.xis.Parameter;
import one.xis.ClientId;
import one.xis.ModelData;
import one.xis.Page;

@Page("/dragDrop.html")
@RequiredArgsConstructor
class DragDropPage {

    private final DragDropService service;

    @ModelData("source")
    String source() {
        return "a2";
    }

    @ModelData("target")
    String target() {
        return "a4";
    }

    @Action
    void move(@ClientId String clientId, @Parameter("from") String from, @Parameter("to") String to) {
        service.move(from, to);
    }

    @Action
    void moveExplicitIndex(@Parameter(index = 2) String to, @Parameter(index = 1) String from) {
        service.move(from, to);
    }
}
