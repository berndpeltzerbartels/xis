package one.xis.js;

import com.google.javascript.jscomp.*;
import com.google.javascript.jscomp.Compiler;
import one.xis.context.XISComponent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

@XISComponent
class JavascriptCompressor {

    /**
     * Uses Google Closure Compiler to compress JavaScript sources.
     *
     * @param sources List of JavaScript source code strings.
     * @return A JavascriptCompressionResult containing the compressed code and source map (if any).
     */
    JavascriptCompressionResult compress(List<String> sources) {
        Compiler.setLoggingLevel(Level.OFF);
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(options);
        options.setSourceMapOutputPath("./sourcemap.map"); // Dummy-Pfad, wird nicht in eine Datei geschrieben
        options.setSourceMapFormat(SourceMap.Format.V3);

        List<SourceFile> externs = Collections.emptyList();
        List<SourceFile> inputs = new ArrayList<>();

        int i = 0;
        for (String source : sources) {
            inputs.add(SourceFile.fromCode("input" + i++ + ".js", source));
        }

        Result result = compiler.compile(externs, inputs, options);

        if (result.success) {
            String compressedSource = compiler.toSource();
            StringBuilder sourceMapBuilder = new StringBuilder();
            if (result.sourceMap != null) {
                try {
                    result.sourceMap.appendTo(sourceMapBuilder, "sourcemap.map");
                } catch (IOException e) {
                    throw new RuntimeException("Konnte Source Map nicht erstellen", e);
                }
            }
            return new JavascriptCompressionResult(compressedSource, sourceMapBuilder.toString());
        } else {
            // Fehlerbehandlung, falls die Kompilierung fehlschlägt
            throw new RuntimeException("JavaScript-Kompilierung fehlgeschlagen");
        }
    }
}

