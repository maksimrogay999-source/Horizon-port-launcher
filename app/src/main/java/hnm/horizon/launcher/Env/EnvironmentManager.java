package hnm.horizon.launcher.Env;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import hnm.horizon.launcher.Files.AppFileManager;
import hnm.horizon.launcher.Files.FileManager;
import hnm.horizon.launcher.Launcher.InstanceManager;

public class EnvironmentManager {
    private final AppFileManager fileManager;

    public EnvironmentManager(AppFileManager fileManager) {
        this.fileManager = fileManager;
    }

    public String testEnvironment() throws Exception {
        File envDir = fileManager.getEnvDir();
        if (!envDir.exists()) throw new Exception("Папка env не найдена!");

        File testDir = new File(envDir, "hello_test");
        if (testDir.exists()) FileManager.deleteDirectory(testDir);
        testDir.mkdirs();

        File cargoToml = new File(testDir, "Cargo.toml");
        File srcDir = new File(testDir, "src");
        srcDir.mkdirs();
        File mainRs = new File(srcDir, "main.rs");

        writeToFile(cargoToml, "[package]\nname = \"hello_test\"\nversion = \"0.1.0\"\nedition = \"2021\"\n");
        writeToFile(mainRs, "fn main() { println!(\"HELLO FROM ANDROID ENV!\"); }\n");

        String[] buildCmd = {"cargo", "build", "--target", "aarch64-linux-android"};
        String buildLog = RubRun.runSync(envDir, testDir, buildCmd);

        File binPath = new File(testDir, "target/aarch64-linux-android/debug/hello_test");
        if (!binPath.exists()) throw new Exception("Сборка упала:\n" + buildLog);

        Runtime.getRuntime().exec(new String[]{"chmod", "+x", binPath.getAbsolutePath()}).waitFor();
        String runLog = RubRun.runSync(envDir, testDir, new String[]{binPath.getAbsolutePath()});

        return "УСПЕХ!\nВывод:\n" + runLog + "\n\nЛог сборки:\n" + buildLog;
    }

    public void prepareEnv() throws Exception {
        File envDir = fileManager.getEnvDir();
        if (!envDir.exists()) throw new Exception("Папка env не найдена. Сначала импортируйте её.");
        InstanceManager.prepareEnvironment(envDir);
    }

    private void writeToFile(File file, String content) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(content.getBytes());
        }
    }
}