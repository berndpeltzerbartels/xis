package one.xis.test.dom;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InputElementImpl extends ElementImpl {

    private String type;
    private String value;
    private boolean checked;

    public InputElementImpl() {
        super("input");
    }

    protected InputElementImpl(String name) {
        super(name);
    }

    public void click() {
        this.checked = !this.checked;
    }
}
