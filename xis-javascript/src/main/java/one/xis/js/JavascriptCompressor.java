package one.xis.js;

import com.google.javascript.jscomp.*;
import com.google.javascript.jscomp.Compiler;
import one.xis.context.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

@Component
class JavascriptCompressor {

    /**
     * Uses Google Closure Compiler to compress JavaScript sources.
     *
     * @param sources Map of JavaScript source code strings by name.
     * @return A JavascriptCompressionResult containing the compressed code and source map (if any).
     */
    JavascriptCompressionResult compress(Map<String, String> sources) {
        final String outputJsName = "bundle.min.js";     // muss zum ausgelieferten JS passen!
        final String outputMapName = "bundle.min.js.map"; // darf beliebig sein; steuert den Kommentar

        Compiler.setLoggingLevel(Level.OFF);
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();

        // SIMPLE oder ADVANCED – wie du magst
        CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(options);

        // Map ERZEUGEN (Schalter) + Kommentar-Ziel
        options.setSourceMapOutputPath(outputMapName);
        options.setSourceMapFormat(SourceMap.Format.V3);

        // sehr wichtig bei fromCode(...):
        options.setSourceMapIncludeSourcesContent(true);
        options.setApplyInputSourceMaps(true);

        // keine goog.exportSymbol-Generierung
        options.setGenerateExports(false);
        
        // Besseres Debugging: Klassennamen und Property-Namen beibehalten
        options.setRenamingPolicy(VariableRenamingPolicy.OFF, PropertyRenamingPolicy.OFF);
        
        // Bessere Fehlerberichte
        options.setPrettyPrint(false);  // false = kompakt, true = lesbarer aber größer
        
        List<SourceFile> inputs = new ArrayList<>();
        sources.forEach((name, code) -> inputs.add(SourceFile.fromCode(name, code)));
        Result result = compiler.compile(List.of(), inputs, options);
        if (!result.success) {
            StringBuilder sb = new StringBuilder();
            for (JSError e : result.errors) sb.append(e).append('\n');
            throw new RuntimeException("JavaScript-Komprimierung fehlgeschlagen:\n" + sb);
        }

        // Minified JS holen …
        String js = compiler.toSource();

        // … optional den automatisch angehängten Kommentar entfernen,
        // weil du den HTTP-Header 'SourceMap' nutzt:
        js = js.replaceFirst("(?m)\\R?//#\\s*sourceMappingURL=.*$", "");

        // Map-JSON erzeugen: 'file' MUSS dem JS-Namen entsprechen
        String mapJson;
        try (var sw = new java.io.StringWriter()) {
            compiler.getSourceMap().appendTo(sw, outputJsName);
            mapJson = sw.toString();
        } catch (IOException e) {
            throw new RuntimeException("Konnte Source Map nicht erstellen", e);
        }

        return new JavascriptCompressionResult(js, mapJson);
    }

}
