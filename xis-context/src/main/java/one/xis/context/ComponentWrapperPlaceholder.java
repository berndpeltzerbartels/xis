package one.xis.context;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Avoid  iteration when component was created.
 */
@NoArgsConstructor
@AllArgsConstructor
class ComponentWrapperPlaceholder {

    @Setter
    private ComponentWrapper componentWrapper;

    void fieldValueFound(FieldWrapper fieldWrapper) {
        if (componentWrapper != null) {
            componentWrapper.fieldValueFound(fieldWrapper);
        }
    }

    void initMethodParamtersSet(InitMethodWrapper initMethodWrapper) {
        if (componentWrapper != null) {
            componentWrapper.initMethodParamtersSet(initMethodWrapper);
        }
    }

    void beanMethodParameterSet(BeanMethodWrapper beanMethodWrapper) {
        if (componentWrapper != null) {
            componentWrapper.beanMethodParameterSet(beanMethodWrapper);
        }
    }
}
