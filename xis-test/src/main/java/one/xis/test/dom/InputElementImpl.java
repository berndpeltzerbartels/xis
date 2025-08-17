package one.xis.test.dom;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InputElementImpl extends ElementImpl implements InputElement {


    public InputElementImpl() {
        super("input");


    }


    protected InputElementImpl(String name) {
        super(name);
    }

    private void onClick() {
        setChecked(!isChecked());
    }

    @Override
    public String getValue() {
        return getAttribute("value");
    }

    @Override
    public boolean isChecked() {
        return Boolean.parseBoolean(getAttribute("checked"));
    }

    @Override
    public void setValue(String v1) {
        setAttribute("value", v1);
    }

    @Override
    public void setAttribute(String name, String value) {
        if (name.equals("type")) {
            if ("checkbox".equals(value) || "radio".equals(value)) {
                super.setAttribute("checked", "false");
                addEventListener("click", event -> {
                    if ("checkbox".equals(getType()) || "radio".equals(getType())) {
                        onClick();
                    }
                });
            }

        }
        super.setAttribute(name, value);
    }

    public String getType() {
        return getAttribute("type");
    }

    void setChecked(boolean checked) {
        setAttribute("checked", String.valueOf(checked));
    }
}
