package on.xis.data;

import one.xis.context.XISComponent;

import java.lang.reflect.Field;

import static on.xis.data.DataModelUtils.isLeafField;
import static one.xis.utils.lang.FieldUtil.isArrayField;
import static one.xis.utils.lang.FieldUtil.isIterableField;

@XISComponent
public class DataModelConverter {

    // TODO inner classes for different basic types ?
    public DataModel toDataModel(Object o) {
        DataModel dataModel = new DataModel();
        //FieldUtil.getAllFields(o.getClass()).forEach();
        return dataModel;
    }

    public <T> T fromDataModel(DataModel model, Class<?> target) {

        return null;
    }


    private void evaluateField(Field field, Object o, DataModelNode parent) {
        if (isIterableField(field)) {
            evaluateIterableField(field, o, parent);
        } else if (isArrayField(field)) {
            evaluateArrayField(field, o, parent);
        } else if (isLeafField(field)) {

        }
    }

    private void evaluateObjectField(Field field, Object o, DataModelNode parent) {

    }


    private void evaluateIterableField(Field field, Object o, DataModelNode parent) {

    }


    private void evaluateArrayField(Field field, Object o, DataModelNode parent) {

    }

    private void evaluateLeafField(Field field, Object o, DataModelNode parent) {

    }


}