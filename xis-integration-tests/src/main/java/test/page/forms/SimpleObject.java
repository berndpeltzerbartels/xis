package test.page.forms;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
class SimpleObject {

    private int id;
    private String title;
    private String property1;
    private String property2;
}
