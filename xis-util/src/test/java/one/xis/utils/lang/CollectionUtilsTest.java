package one.xis.utils.lang;

import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

class CollectionUtilsTest {

    @Test
    void emptyInstance() {
        CollectionUtils.emptyInstance(Set.class);
        CollectionUtils.emptyInstance(List.class);
        CollectionUtils.emptyInstance(LinkedList.class);
    }
}