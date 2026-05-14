import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.security.MessageDigest;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ReleaseZip {

    private final boolean skipSigning;

    public ReleaseZip(boolean skipSigning) {
        this.skipSigning = skipSigning;
    }

    private File m2RepositoryDir() {
        var m2Home = System.getProperty("user.home") + "/.m2/repository";
        return new File(m2Home);
    }

    private File homeDir() {
        return new File(System.getProperty("user.home"));
    }

    private File createZipFile(String groupId, String version, Collection<String> coordinates) {
        var artefactDirs = getArtefactDirs(groupId, version, coordinates);
        if (artefactDirs.isEmpty()) {
            throw new RuntimeException("No artefact dirs found for " + groupId + ":" + version);
        }
        var zipFile = new File(homeDir(), groupId + "." + version + ".zip");
        if (zipFile.exists()) {
            if (!zipFile.delete()) {
                throw new RuntimeException("Could not delete existing zip file: " + zipFile.getAbsolutePath());
            }
        }
        try (var zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile))) {
            for (File artefactDir : artefactDirs) {
                for (File file : Objects.requireNonNull(artefactDir.listFiles())) {
                    if (isArtefactFile(file)) {
                        var md5File = addMd5File(file);
                        var sha1File = addSha1File(file);
                        var filePathInZip = pathInZip(file);
                        var md5PathInZip = pathInZip(md5File);
                        var sha1PathInZip = pathInZip(sha1File);
                        
                        zipOutputStream.putNextEntry(new ZipEntry(filePathInZip));
                        zipOutputStream.write(Files.readAllBytes(file.toPath()));
                        zipOutputStream.closeEntry();
                        zipOutputStream.putNextEntry(new ZipEntry(md5PathInZip));
                        zipOutputStream.write(Files.readAllBytes(md5File.toPath()));
                        zipOutputStream.closeEntry();
                        zipOutputStream.putNextEntry(new ZipEntry(sha1PathInZip));
                        zipOutputStream.write(Files.readAllBytes(sha1File.toPath()));
                        zipOutputStream.closeEntry();

                        if (!skipSigning) {
                            var ascFile = addAscFile(file);
                            var ascPathInZip = pathInZip(ascFile);
                            zipOutputStream.putNextEntry(new ZipEntry(ascPathInZip));
                            zipOutputStream.write(Files.readAllBytes(ascFile.toPath()));
                            zipOutputStream.closeEntry();
                        }


                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return zipFile;
    }

    private String pathInZip(File file) {
        var repositoryPath = m2RepositoryDir().getAbsolutePath();
        var path = file.getAbsolutePath().replace(repositoryPath, "");
        while (path.startsWith(File.separator)) {
            path = path.substring(1);
        }
        return path.replace(File.separatorChar, '/');
    }

    private Collection<File> getArtefactDirs(String groupId, String version, Collection<String> coordinates) {
        if (!coordinates.isEmpty()) {
            return getConfiguredArtefactDirs(version, coordinates);
        }
        return scanArtefactDirs(groupId, version);
    }

    private Collection<File> getConfiguredArtefactDirs(String version, Collection<String> coordinates) {
        var artefactDirs = new ArrayList<File>();
        for (String coordinate : coordinates) {
            var parts = coordinate.split(":");
            if (parts.length != 2) {
                throw new RuntimeException("Invalid artifact coordinate: " + coordinate);
            }
            var groupPath = parts[0].replace(".", File.separator);
            var artifactId = parts[1];
            var versionDir = new File(new File(m2RepositoryDir(), groupPath), artifactId + File.separator + version);
            var pomName = artifactId + "-" + version + ".pom";
            if (!new File(versionDir, pomName).exists()) {
                throw new RuntimeException("Missing published artifact: " + coordinate + ":" + version);
            }
            artefactDirs.add(versionDir);
        }
        return artefactDirs;
    }

    private Collection<File> scanArtefactDirs(String groupId, String version) {
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
        return file.isFile() && (file.getName().endsWith(".jar")
                || file.getName().endsWith(".pom")
                || file.getName().endsWith(".module"));
    }

    private File addMd5File(File file) {
        var md5File = new File(file.getAbsolutePath() + ".md5");
        try (var writer = new FileWriter(md5File)) {
            writer.write(checksum(file, "MD5"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return md5File;
    }

    private File addSha1File(File file) {
        var sha1File = new File(file.getAbsolutePath() + ".sha1");
        try (var writer = new FileWriter(sha1File)) {
            writer.write(checksum(file, "SHA-1"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return sha1File;
    }

    private String checksum(File file, String algorithm) {
        try {
            var digest = MessageDigest.getInstance(algorithm);
            var hash = digest.digest(Files.readAllBytes(file.toPath()));
            var result = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                result.append(String.format("%02x", b));
            }
            return result.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
        if (args.length < 2) {
            System.err.println("Usage: java ReleaseZip <groupId> <version> [skipSigning]");
            System.exit(1);
        }
        boolean skipSigning = args.length > 2 && "true".equalsIgnoreCase(args[2]);
        var coordinates = args.length > 3
                ? List.of(args).subList(3, args.length)
                : List.<String>of();
        new ReleaseZip(skipSigning).createZipFile(args[0], args[1], coordinates);
    }
}
