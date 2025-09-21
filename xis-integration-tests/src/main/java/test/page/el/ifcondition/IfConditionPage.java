package test.page.el.ifcondition;


import one.xis.ModelData;
import one.xis.Page;

@Page("/ifCondition.html")
class IfConditionPage {

    @ModelData
    String data() {
        return "data";
    }
}
