package one.xis.remote.processor;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.utils.lang.ExceptionUtils;

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
        environment.getMessager().printMessage(Diagnostic.Kind.ERROR, getProcessorPrefix() + t + "\n" + ExceptionUtils.getStackTrace(t));
    }

    void error(String message, @NonNull Throwable t) {
        environment.getMessager().printMessage(Diagnostic.Kind.ERROR, getProcessorPrefix() + message + ":" + "\n" + ExceptionUtils.getStackTrace(t));
    }

    void error(String message) {
        environment.getMessager().printMessage(Diagnostic.Kind.ERROR, getProcessorPrefix() + message);
    }

    private String getProcessorPrefix() {
        return processor.getClass().getSimpleName() + " - ";
    }
}
