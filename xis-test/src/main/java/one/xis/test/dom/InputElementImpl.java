package one.xis.test.dom;

import lombok.Getter;
import lombok.Setter;
import org.graalvm.polyglot.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class InputElementImpl extends ElementImpl implements InputElement {

    private static final Set<String> INPUT_MEMBERS = Set.of("value", "checked");

    InputElementImpl() {
        super("input");
        addEventListener("click", this::handleClick);
    }

    private void handleClick(Object event) {
        setAttribute("checked", String.valueOf(!isChecked()));
    }


    @Override
    public String getValue() {
        var value = getAttribute("value");
        return value != null ? value : "";
    }

    @Override
    public boolean isChecked() {
        return Boolean.parseBoolean(getAttribute("checked"));
    }

    @Override
    public void setValue(String v1) {
        setAttribute("value", v1);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object getMemberKeys() {
        var list = new ArrayList<>((List<String>) super.getMemberKeys());
        list.addAll(INPUT_MEMBERS);
        return list;
    }

    @Override
    public void putMember(String key, Value value) {
        // Behandelt Zuweisungen für spezifische Members.
        switch (key) {
            case "value" -> setValue(value.asString());
            case "checked" -> setAttribute("checked", value.asBoolean());
            // Für alles andere, die Superklasse fragen.
            default -> super.putMember(key, value);
        }
    }

    @Override
    public Object getMember(String key) {
        // Behandelt zuerst die spezifischen Members von InputElement.
        return switch (key) {
            case "value" -> getValue();
            case "checked" -> isChecked();
            // Wenn nicht hier behandelt, an die Superklasse weiterleiten.
            default -> super.getMember(key);
        };
    }


    @Override
    public boolean hasMember(String key) {
        return INPUT_MEMBERS.contains(key) || ElementImpl.MEMEBERS.contains(key);
    }
}