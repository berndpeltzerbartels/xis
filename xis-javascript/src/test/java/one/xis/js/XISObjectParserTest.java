package one.xis.js;

import one.xis.template.ModelElement;
import one.xis.template.TemplateModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class XISObjectParserTest {

    @Nested
    class ParserTest1 {
        @BeforeEach
        void setUp() {
            TemplateModel model = new TemplateModel(new ModelElement("div"));
            //model.getRoot().setIfCondition(new IfCondition(new Expression("")));
        }

        @Test
        void parse() {
        }
    }
}