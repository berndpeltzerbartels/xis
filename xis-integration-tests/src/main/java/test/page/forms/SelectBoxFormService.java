package test.page.forms;

import java.util.List;

public interface SelectBoxFormService {

    List<SelectBoxFormOption> options();

    SelectBoxFormModel getSelectBoxFormModel();

    void saveSelectBoxFormModel(SelectBoxFormModel formModel);
}
