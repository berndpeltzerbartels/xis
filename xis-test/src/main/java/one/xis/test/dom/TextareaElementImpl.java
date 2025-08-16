package one.xis.test.dom;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TextareaElementImpl extends InputElementImpl {

    private int cols;
    private int rows;

    public TextareaElementImpl() {
        super("textarea");
    }
}
