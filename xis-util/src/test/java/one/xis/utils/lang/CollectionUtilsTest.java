package one.xis.utils.lang;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CollectionUtilsTest {

    @Test
    void getTypeParameter() {
        var list = new ArrayList<String>();
        assertEquals(String.class, CollectionUtils.getTypeParameter(list.getClass()));
    }
}