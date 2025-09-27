package test.reactive.global;

import lombok.Getter;
import one.xis.GlobalVariable;
import one.xis.Page;

@Getter
@Page("/global-variable-test.html")
class GlobalVariablePage {

    @GlobalVariable("globalPageValue")
    public String getSharedValue() {
        return "123";
    }

}