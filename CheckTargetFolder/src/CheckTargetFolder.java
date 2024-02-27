import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CheckTargetFolder {
    public static void main(String[] args) throws IOException, InterruptedException {
        // Get the project directory path as input
        if (args.length != 1) {
            System.out.println("Usage: java CheckTargetFolder <project_directory_path>");
            return;
        }

        String projectDirectoryPath = args[0];

        // Create a File object representing the project directory
        File projectDirectory = new File(projectDirectoryPath);

        // Check if the project directory exists
        if (!projectDirectory.exists() || !projectDirectory.isDirectory()) {
            System.out.println("Invalid project directory path.");
            return;
        }

        // List all directories (modules) within the project directory
        File[] moduleDirectories = projectDirectory.listFiles(File::isDirectory);
        System.out.println("moduleDirectories");
        // Specify the name of the target folder
        String targetFolderName = "target";

        // Flag to track if target folder is found in any module
        boolean targetFound = false;

        // Iterate through each module directory
        assert moduleDirectories != null;
        for (File moduleDirectory : moduleDirectories) {
            // Create a File object representing the target folder in the module directory
            File targetFolder = new File(moduleDirectory, targetFolderName);

            // Check if the target folder exists
            if (targetFolder.exists() && targetFolder.isDirectory()) {
                System.out.println("Target folder exists in module: " + moduleDirectory.getName());
                targetFound = true;
            }
        }
        if(targetFound){
            String parentPomPath="C:\\Users\\a869932\\spring-boot-multimodule"+File.separator+"pom.xml";
            addDependencyToParentPom(parentPomPath);
        }

        // If target folder not found in any module
        else  {
            System.out.println("Target folder does not exist in any module of the project.");
        }

    }
    private static void addDependencyToParentPom(String parentPomPath) throws IOException, InterruptedException {
        String pluginConfig ="<profiles>\n" +
                "<profile>\n" +
                  "   <id>openrewrite</id>\n"+
                "  <build>\n" +
                "    <plugins>\n" +
                "      <plugin>\n" +
                "        <groupId>org.openrewrite.maven</groupId>\n" +
                "        <artifactId>rewrite-maven-plugin</artifactId>\n" +
                "        <version>5.23.1</version>\n" +
                "        <configuration>\n" +
                "          <activeRecipes>\n" +
                "            <recipe> </recipe>\n" +
                "          </activeRecipes>\n" +
                "          <failOnDryRunResults>true</failOnDryRunResults>\n" +
                "        </configuration>\n" +
                "        <dependencies>\n" +
                "          <dependency>\n" +
                "            <groupId>org.openrewrite.recipe</groupId>\n" +
                "            <artifactId>rewrite-static-analysis</artifactId>\n" +
                "            <version>1.3.1</version>\n" +
                "          </dependency>\n" +
                "        </dependencies>\n" +
                "      </plugin>\n" +
                "    </plugins>\n" +
                "  </build>\n" +
                "</profile>\n" +
                "</profiles>";

        Path pomFilePath = Paths.get(parentPomPath);
        String pomContent = new String(Files.readAllBytes(pomFilePath));

        if (!pomContent.contains(pluginConfig)) {
            // Find the position to insert the plugin configuration within the <plugins> tag
            int index = pomContent.indexOf("</project>");
            if (index != -1) {
                // Insert the plugin configuration before the </plugins> tag
                pomContent = pomContent.substring(0, index) + pluginConfig + pomContent.substring(index);
                Files.write(pomFilePath, pomContent.getBytes());
                System.out.println("Plugin configuration added to the parent POM file: " + parentPomPath);
                runMavenCommand();
            } else {
                System.out.println("Failed to add plugin configuration. Could not find </plugins> tag in the parent POM file.");
            }
        } else {
            System.out.println("Plugin configuration already exists in the parent POM file.");
        }
    }
    private static void runMavenCommand() throws IOException, InterruptedException {
        String filePath = "C:\\Users\\a869932\\spring-boot-multimodule";
        String[] command = {"cmd", "/c", "mvn", "-Popenrewrite", "rewrite:run"};

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(new File(filePath)); // Set working directory to the project root
        Process process = processBuilder.start();

        // Read output from the process
        process.getInputStream().transferTo(System.out);
        process.getErrorStream().transferTo(System.err);
        process.waitFor();

    }

}
