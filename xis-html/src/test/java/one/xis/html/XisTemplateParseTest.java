package one.xis.html;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class XisTemplateParseTest {
    
    private final HtmlParser parser = new HtmlParser();
    
    @Test
    void parseXisTemplate() {
        String html = "<xis:template xmlns:xis=\"http://xis.one/schema\"><div>test</div></xis:template>";
        
        System.out.println("Input: " + html);
        
        try {
            var document = parser.parse(html);
            var result = document.asString();
            System.out.println("Output: " + result);
            System.out.println("Root element tag name: " + document.getDocumentElement().getLocalName());
            System.out.println("Root element class: " + document.getDocumentElement().getClass());
            
            assertThat(result).isNotNull();
        } catch (Exception e) {
            System.err.println("ERROR parsing: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            System.err.println("Stack trace:");
            e.printStackTrace();
            throw e;
        }
    }
    
    @Test 
    void debugLocalName() {
        // Test how local names are handled for namespace tags
        String[] testInputs = {
            "<template><div>test</div></template>",
            "<xis:template><div>test</div></xis:template>", 
            "<xis:template xmlns:xis=\"http://xis.one/schema\"><div>test</div></xis:template>"
        };
        
        for (String html : testInputs) {
            System.out.println("Testing: " + html);
            try {
                var document = parser.parse(html);
                var root = document.getDocumentElement();
                System.out.println("  -> localName: '" + root.getLocalName() + "'");
                System.out.println("  -> tagName: '" + root.getTagName() + "'");
                System.out.println("  -> attributes: " + root.getAttributes());
            } catch (Exception e) {
                System.err.println("  -> ERROR: " + e.getMessage());
            }
            System.out.println();
        }
    }
}