package one.xis.test.dom;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SelectElementImpl extends ElementImpl implements SelectElement {
    public boolean multiple;
    public int selectedIndex;
    public String value;
    public List<OptionElement> selectedOptions = new ArrayList<>();

    public SelectElementImpl() {
        super("select");
    }


    void updateSelectionState(OptionElementImpl optionElement) {
        if (multiple) {
            updateMultipleSelectionState(optionElement);
        } else {
            updateSingleSelectionState(optionElement);
        }
    }

    void updateSingleSelectionState(OptionElementImpl optionElement) {
        var options = this.getElementsByTagName("option");
        for (var i = 0; i < options.length; i++) {
            var option = (OptionElement) options.item(i);
            if (option.isSelected()) {
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
