package one.xis.test.dom;


import lombok.Getter;
import lombok.Setter;

import java.util.stream.Collectors;

@Getter
@Setter
public class SelectElementImpl extends ElementImpl implements SelectElement {

    public NodeList selectedOptions = new NodeList();
    private boolean multiple;
    private String value;
    private int selectedIndex = -1;

    public SelectElementImpl() {
        super("select");
    }


    @SuppressWarnings("unused")
    public void setValue(String value) {
        this.value = value;
        var options = this.getElementsByTagName("option");
        for (var i = 0; i < options.length; i++) {
            var option = (OptionElementImpl) options.item(i);
            if (value.equals(option.getAttribute("value"))) {
                option.setSelected(true);
                return;
            }
        }
    }


    void updateSelectionState(OptionElementImpl optionElement) {
        if (isMultiple()) {
            updateMultipleSelectionState(optionElement);
        } else {
            updateSingleSelectionState(optionElement);
        }
    }

    void updateSingleSelectionState(OptionElementImpl optionElement) {
        selectedOptions.clear();
        selectedIndex = -1; // Reset selected index
        var options = this.getElementsByTagName("option");
        for (var i = 0; i < options.length; i++) {
            var option = (OptionElement) options.item(i);
            if (option.isSelected()) {
                selectedOptions.addNode(option);
                this.value = option.getAttribute("value");
                this.selectedIndex = i;
                return;
            }
        }
    }

    void updateMultipleSelectionState(OptionElement option) {
        var selectedList = this.selectedOptions.list();
        selectedOptions.clear();
        for (var i = 0; i < getOptions().length; i++) {
            var opt = (OptionElementImpl) getOptions().item(i);
            if (opt.isSelected()) {
                selectedOptions.addNode(opt);
                // Update the value attribute by concatenating selected option values

                if (opt == option) {
                    this.selectedIndex = i;
                }
            } else {
                // If the option is not selected, we remove it from the selected options
                selectedList.remove(opt);
            }
        }
        if (selectedList.isEmpty()) {
            this.selectedIndex = -1;
        }
        value = selectedList.stream().filter(OptionElementImpl.class::isInstance).map(OptionElementImpl.class::cast).
                map(OptionElementImpl::getValue).collect(Collectors.joining(","));
    }

    // called by graalvm
    @SuppressWarnings("unused")
    NodeList getOptions() {
        return this.getElementsByTagName("option");
    }

    private boolean isMultiple() {
        return "multiple".equals(getAttribute("multiple"));
    }
}
