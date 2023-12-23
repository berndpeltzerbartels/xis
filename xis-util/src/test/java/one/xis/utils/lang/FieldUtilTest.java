package one.xis.utils.lang;

import lombok.Data;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class FieldUtilTest {

    @Data
    static class TestBean {
        private int intField;
    }

    @Test
    @DisplayName("Rying to assign incomptibe field value causes IllegalArgumentException")
    void setFieldValueWithIncompitibleType() {
        assertThrows(IllegalArgumentException.class, () -> FieldUtil.setFieldValue(new TestBean(), "intField", "xyz"));
    }
}