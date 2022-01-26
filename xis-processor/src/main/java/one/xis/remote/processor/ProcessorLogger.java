package one.xis.remote.processor;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.tools.Diagnostic;

@RequiredArgsConstructor
class ProcessorLogger {
    private final ProcessingEnvironment environment;
    private final Processor processor;

    void info(@NonNull String message) {
        environment.getMessager().printMessage(Diagnostic.Kind.NOTE, getProcessorPrefix() + message);
    }

    void error(@NonNull Throwable t) {
        environment.getMessager().printMessage(Diagnostic.Kind.ERROR, getProcessorPrefix() + t);
    }

    void error(String message, @NonNull Throwable t) {
        environment.getMessager().printMessage(Diagnostic.Kind.ERROR, getProcessorPrefix() + message + ":" + message);
    }

    void error(String message) {
        environment.getMessager().printMessage(Diagnostic.Kind.ERROR, getProcessorPrefix() + message);
    }

    private String getProcessorPrefix() {
        return processor.getClass().getSimpleName() + " - ";
    }
}
