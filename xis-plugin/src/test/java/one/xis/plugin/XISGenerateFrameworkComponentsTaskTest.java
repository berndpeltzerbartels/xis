package one.xis.plugin;

import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XISGenerateFrameworkComponentsTaskTest {

    private static final String XIS_VERSION = System.getProperty("xis.version");

    @TempDir
    Path projectDir;

    @Test
    void generatedNativeComponentRegistryIncludesServices() throws IOException {
        write("settings.gradle", "rootProject.name = 'native-service-catalog-test'\n");
        write("build.gradle", """
                plugins {
                    id 'java'
                    id 'one.xis.plugin'
                }

                repositories {
                    mavenLocal()
                    mavenCentral()
                }

                dependencies {
                    implementation 'one.xis:xis-boot-native:%s'
                }
                """.formatted(XIS_VERSION));
        write("src/main/java/example/DemoPage.java", """
                package example;

                import one.xis.Page;

                @Page("/demo")
                class DemoPage {
                    DemoPage(DemoService service) {
                    }
                }
                """);
        write("src/main/java/example/DemoService.java", """
                package example;

                import one.xis.context.Service;

                @Service
                class DemoService {
                }
                """);

        var result = GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withPluginClasspath()
                .withArguments("xisGenerateFrameworkComponents")
                .build();

        assertEquals(SUCCESS, result.task(":xisGenerateFrameworkComponents").getOutcome());
        var registry = Files.readString(projectDir.resolve(
                "build/generated/sources/xisFrameworkComponents/java/main/example/XisGeneratedNativeServiceCatalogTestComponents.java"));
        assertTrue(registry.contains("DemoPage.class"));
        assertTrue(registry.contains("DemoService.class"));
    }

    @Test
    void generatedNativeClassCatalogIncludesPackagePrivateAndNestedTypes() throws IOException {
        write("settings.gradle", "rootProject.name = 'native-nested-class-catalog-test'\n");
        write("build.gradle", """
                plugins {
                    id 'java'
                    id 'one.xis.plugin'
                }

                repositories {
                    mavenLocal()
                    mavenCentral()
                }

                dependencies {
                    implementation 'one.xis:xis-boot-native:%s'
                }
                """.formatted(XIS_VERSION));
        write("src/main/java/example/Types.java", """
                package example;

                class First {
                    record Inner(String id) {
                        record Deeper(String value) {
                        }
                    }
                }

                record Second(String name) {
                }
                """);

        var result = GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withPluginClasspath()
                .withArguments("xisGenerateNativeClassCatalog")
                .build();

        assertEquals(SUCCESS, result.task(":xisGenerateNativeClassCatalog").getOutcome());
        var catalog = Files.readAllLines(projectDir.resolve(
                "build/generated/resources/xisNativeClassCatalog/main/META-INF/xis/native/classes/native-nested-class-catalog-test.txt"));
        assertEquals(List.of(
                "example.First",
                "example.First$Inner",
                "example.First$Inner$Deeper",
                "example.Second"
        ), catalog);
    }

    private void write(String relativePath, String content) throws IOException {
        var path = projectDir.resolve(relativePath);
        Files.createDirectories(path.getParent() == null ? projectDir : path.getParent());
        Files.writeString(path, content);
    }
}
