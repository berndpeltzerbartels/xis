package test;

import lombok.RequiredArgsConstructor;
import one.xis.Page;

@Page("/model.html")
@RequiredArgsConstructor
class ModelPage {
    private final ModelService modelService;

    @one.xis.Model("model")
    Model model(@one.xis.Model("model") Model model) {
        modelService.updateModel(model); // This is not reality-like, but good enough for testing
        return model;
    }

}
