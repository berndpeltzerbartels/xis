import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class NodeJS {


    public void execute(String jsFilePath) {
        execute(getResourceDir(), "node", jsFilePath);
    }


    private File getResourceDir() {
        return new File(new File("."), "xis-test/src/main/resources");
    }

    public static void execute(File directory, String... command) {
        System.out.println(directory.getAbsolutePath());
        System.out.println(directory.exists());
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(directory);

        // -- Linux --

        // Run a shell command
        processBuilder.command(command);

        // Run a shell script
        //processBuilder.command("path/to/hello.sh");

        // -- Windows --

        // Run a command
        //processBuilder.command("cmd.exe", "/c", "dir C:\\Users\\mkyong");

        // Run a bat file
        //processBuilder.command("C:\\Users\\mkyong\\hello.bat");

        try {

            Process process = processBuilder.start();

            StringBuilder output = new StringBuilder();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }

            int exitVal = process.waitFor();
            if (exitVal == 0) {
                System.out.println("Success!");
                System.out.println(output);
                System.exit(0);
            } else {
                //abnormal...
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
        }
    }

    public static void main(String[] args) {
        new NodeJS().execute(args[0]);
    }


}
