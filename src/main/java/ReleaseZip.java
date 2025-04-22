import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ReleaseZip {

    private File m2RepositoryDir() {
        var m2Home = System.getProperty("user.home") + "/.m2/repository";
        return new File(m2Home);
    }

    private File homeDir() {
        return new File(System.getProperty("user.home"));
    }

    private File createZipFile(String groupId, String version) {
        var artefactDirs = getArtefactDirs(groupId, version);
        if (artefactDirs.isEmpty()) {
            throw new RuntimeException("No artefact dirs found for " + groupId + ":" + version);
        }
        var zipFile = new File(homeDir(), groupId + "." + version + ".zip");
        try (var zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile))) {
            for (File artefactDir : artefactDirs) {
                for (File file : Objects.requireNonNull(artefactDir.listFiles())) {
                    if (isArtefactFile(file)) {
                        var md5File = addMd5File(file);
                        var sha1File = addSha1File(file);
                        var ascFile = addAscFile(file);
                        var filePathInZip = file.getAbsolutePath().replace(m2RepositoryDir().getAbsolutePath(), "");
                        var md5PathInZip = md5File.getAbsolutePath().replace(m2RepositoryDir().getAbsolutePath(), "");
                        var sha1PathInZip = sha1File.getAbsolutePath().replace(m2RepositoryDir().getAbsolutePath(), "");
                        var ascPathInZip = ascFile.getAbsolutePath().replace(m2RepositoryDir().getAbsolutePath(), "");
                        zipOutputStream.putNextEntry(new ZipEntry(filePathInZip));
                        zipOutputStream.write(Files.readAllBytes(file.toPath()));
                        zipOutputStream.closeEntry();
                        zipOutputStream.putNextEntry(new ZipEntry(md5PathInZip));
                        zipOutputStream.write(Files.readAllBytes(md5File.toPath()));
                        zipOutputStream.closeEntry();
                        zipOutputStream.putNextEntry(new ZipEntry(sha1PathInZip));
                        zipOutputStream.write(Files.readAllBytes(sha1File.toPath()));
                        zipOutputStream.closeEntry();

                        zipOutputStream.putNextEntry(new ZipEntry(ascPathInZip));
                        zipOutputStream.write(Files.readAllBytes(ascFile.toPath()));
                        zipOutputStream.closeEntry();


                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return zipFile;
    }

    private Collection<File> getArtefactDirs(String groupId, String version) {
        var artefactDirs = new ArrayList<File>();
        var groupIdPath = groupId.replace(".", "/");
        var artefactDir = new File(m2RepositoryDir(), groupIdPath);
        if (artefactDir.exists()) {
            for (File moduleFile : Objects.requireNonNull(artefactDir.listFiles())) {
                if (moduleFile.isDirectory()) {
                    var moduleName = moduleFile.getName();
                    var versionDir = new File(moduleFile, version);
                    if (versionDir.exists() && versionDir.isDirectory()) {
                        var pomName = moduleName + "-" + version + ".pom";
                        if (new File(versionDir, pomName).exists()) {
                            artefactDirs.add(versionDir);
                        }
                    }
                }
            }
        }
        return artefactDirs;
    }

    private boolean isArtefactFile(File file) {
        return file.isFile() && (file.getName().endsWith(".jar") || file.getName().endsWith(".pom"));
    }

    private File addMd5File(File file) {
        var md5File = new File(file.getAbsolutePath() + ".md5");
        if (!md5File.exists()) {
            // create md5 file
            try {
                var process = Runtime.getRuntime().exec("md5 -q " + file.getAbsolutePath());
                process.waitFor();
                var md5 = new String(process.getInputStream().readAllBytes());
                try (var writer = new FileWriter(md5File)) {
                    writer.write(md5);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return md5File;
    }

    private File addSha1File(File file) {
        var sha1File = new File(file.getAbsolutePath() + ".sha1");
        if (!sha1File.exists()) {
            // create sha1 file
            try {
                var process = Runtime.getRuntime().exec("sha1 -q " + file.getAbsolutePath());
                process.waitFor();
                var sha1 = new String(process.getInputStream().readAllBytes());
                try (var writer = new FileWriter(sha1File)) {
                    writer.write(sha1);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return sha1File;
    }

    private File addAscFile(File file) {
        File ascFile = new File(file.getAbsolutePath() + ".asc");

        // Signatur nur erzeugen, wenn sie noch nicht existiert
        //   if (!ascFile.exists()) {
        try {
            var process = new ProcessBuilder(
                    "gpg",
                    "--batch",
                    "--yes",
                    "--pinentry-mode", "loopback",
                    "--armor",
                    "--detach-sign",
                    file.getAbsolutePath()
            )
                    .redirectError(ProcessBuilder.Redirect.INHERIT)
                    .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                    .start();

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("GPG signing failed with exit code " + exitCode);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error while creating .asc file for: " + file.getName(), e);
        }
        //     }

        return ascFile;
    }


    public static void main(String[] args) {
        new ReleaseZip().createZipFile("one.xis", "0.1.1");
    }
}
