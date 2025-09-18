package one.xis.plugin;

import org.gradle.api.Project;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.bundling.Jar;

import java.util.stream.Collectors;

public abstract class XISBootRunnableJarTask extends Jar {
    public XISBootRunnableJarTask() {
        setGroup("xis");
        setDescription("Creates an executable JAR with all dependencies.");
        getArchiveClassifier().set("all");
        setDuplicatesStrategy(DuplicatesStrategy.EXCLUDE);

        // Konfiguration erfolgt im Plugin beim Registrieren
    }

    // XISBootRunnableJarTask.java
    public void configure(Project project) {
        getManifest().getAttributes().put("Main-Class", "one.xis.boot.Runner");

        SourceSetContainer sets = project.getExtensions().getByType(SourceSetContainer.class);
        SourceSet main = sets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);

        // Kompilierte Klassen und Ressourcen einbinden
        ///from(main.getOutput());
        from(project.getBuildDir() + "/classes/java/main");
        from(project.getBuildDir() + "/resources/main");


        // Fat Jar: Alle Abhängigkeiten einbinden
        from(
                project.getConfigurations().getByName("runtimeClasspath")
                        .filter(file -> file.getName().endsWith(".jar"))
                        .getFiles()
                        .stream()
                        .map(project::zipTree)
                        .collect(Collectors.toList())
        );
    }

}
