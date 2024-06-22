package one.xis.validation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ObjectPathTest {

    @Test
    void simplePath() {
        var path = new RootPathElement().addChild("field1").addChild("field2").getPath();

        assertThat(path).isEqualTo("/field1/field2");
    }

    @Test
    void collectionPath1() {
        var rootElement = new RootPathElement();
        var array = rootElement.addArrayChild();
        array.addChild();
        array.addChild();
        var path = array.addChild().getPath();

        assertThat(path).isEqualTo("/[2]");
    }

    @Test
    void collectionPath2() {
        var rootElement = new RootPathElement();
        var objectPath = rootElement.addChild("field1");
        var array = objectPath.addArrayChild();
        array.addChild();
        array.addChild();
        var path = array.addChild().getPath();

        assertThat(path).isEqualTo("/field1[2]");
    }
}