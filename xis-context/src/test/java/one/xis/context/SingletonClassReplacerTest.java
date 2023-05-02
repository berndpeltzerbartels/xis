package one.xis.context;

import one.xis.utils.lang.CollectionUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

class SingletonClassReplacerTest {

    @Test
    void doReplace() {
        var replacer = new SingletonClassReplacer();
        var classes = Set.of(C1.class, C2.class, C3.class, C4.class);
        var result = replacer.doReplacement(classes);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(CollectionUtils.first(result), C4.class);
    }

    @XISComponent
    private class C1 {

    }

    @XISComponent(replacementFor = C1.class)
    private class C2 {
    }

    @XISComponent(replacementFor = C2.class)
    private class C3 {
    }

    @XISComponent(replacementFor = C3.class)
    private class C4 {
    }


}