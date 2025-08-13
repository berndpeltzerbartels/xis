package one.xis.test.dom;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SelectElement extends Element {
    public boolean multiple;
    public int selectedIndex;
    public String value;
    public List<OptionElement> selectedOptions = new ArrayList<>();

    public SelectElement() {
        super("select");
    }


    void updateSelectionState(OptionElement option) {
        if (multiple) {
            updateMultipleSelectionState(option);
        } else {
            updateSingleSelectionState();
        }
    }

    void updateSingleSelectionState() {
        var options = this.findDescendants(e -> e.localName.equals("option"));
        for (var i = 0; i < options.size(); i++) {
            var option = (OptionElement) options.get(i);
            if (option.selected) {
                selectedOptions = List.of(option);
                this.setAttribute("value", option.getAttribute("value"));
                this.value = option.getAttribute("value");
                this.selectedIndex = i;
                return;
            }
        }
    }

    void updateMultipleSelectionState(OptionElement option) {
        selectedOptions.add(option);
        this.setAttribute("value", selectedOptions.stream().map(o -> o.getAttribute("value")).collect(Collectors.joining(",")));
    }
}
