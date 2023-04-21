package one.xis.js.parse;

import lombok.Getter;
import one.xis.utils.io.IOUtils;

class ParserScriptsFiles {

    @Getter
    private static String content;

    static {
        content = IOUtils.getResourceAsString("js/parse/ExpressionParser.js");
        content += IOUtils.getResourceAsString("js/parse/TreeParser.js");
        content += IOUtils.getResourceAsString("js/parse/Tokenizer.js");
        content += IOUtils.getResourceAsString("js/parse/TextContentParser.js");
        content += IOUtils.getResourceAsString("js/parse/TokenLinker.js");
        content += IOUtils.getResourceAsString("js/parse/TextContent.js");
        content += IOUtils.getResourceAsString("js/parse/CharIterator.js");
        content += IOUtils.getResourceAsString("js/Functions.js");
    }


}
