package one.xis.remote.processor;

import lombok.SneakyThrows;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;


class ProcessorUtils {
    private final ProcessingEnvironment environment;
    private static File sourceDir;

    ProcessorUtils(ProcessingEnvironment environment) {
        this.environment = environment;
        if (sourceDir == null) {
            sourceDir = getSourceLocation();
        }
    }

    @SneakyThrows
    PrintWriter writer(String path, Element... originatingElements) {
        return new PrintWriter(fileObject(path, originatingElements).openOutputStream(), false, StandardCharsets.UTF_8);
    }

    PrintWriter writer(String path, Collection<Element> originatingElements) {
        return writer(path, originatingElements.toArray(new Element[originatingElements.size()]));
    }

    @SneakyThrows
    FileObject fileObject(String path, Element... originatingElements) {
        return environment.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", path, originatingElements);
    }

    // Hack for gradle problem when using StandardLocation.SOURCE_PATH
    File getFileInSourceFolder(String packageName, String fileName) {
        List<String> names = new ArrayList<>(Arrays.asList(packageName.split("\\.")));
        if (names.isEmpty()) {
            throw new IllegalStateException();
        }
        names.add(fileName);
        Iterator<String> namesIterator = names.iterator();
        File file = sourceDir;
        while (namesIterator.hasNext()) {
            file = new File(file, namesIterator.next());
        }
        return file;
    }

    private File getSourceLocation() {
        File buildDir = getBuildDir();
        File projectDir = buildDir.getParentFile();
        if (!projectDir.exists()) {
            throw new IllegalStateException(projectDir + " does not exist");
        }
        File srcDir = new File(new File(new File(projectDir, "src"), "main"), "java");
        if (!srcDir.exists()) {
            throw new IllegalStateException(projectDir + " does not exist");
        }
        return srcDir;
    }

    private File getBuildDir() {
        try {
            FileObject dummy = environment.getFiler().createSourceFile("Dummy");
            File dummyFile = new File(dummy.toUri().toURL().getFile());
            File file = dummyFile;
            while (file != null && !file.getName().equals("build")) {
                file = file.getParentFile();
            }
            if (file == null) {
                throw new FileNotFoundException("can not find build-directory");
            }
            writeDummyClass(dummy);
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void writeDummyClass(FileObject dummyFileObject) {
        try (PrintWriter writer = new PrintWriter(dummyFileObject.openWriter())) {
            writer.write("class Dummy {}");
        } catch (IOException e) {
            throw new RuntimeException("Dummy hack failed");
        }

    }
}
