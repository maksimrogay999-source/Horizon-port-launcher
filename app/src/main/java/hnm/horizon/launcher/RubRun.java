package hnm.horizon.launcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Map;

public class RubRun {

    public static void run(File envDir, String[] command, LogListener listener) {
        run(envDir, envDir, command, listener);
    }

    public static void run(File envDir, File workingDir, String[] command, LogListener listener) {
        new Thread(() -> {
            try {
                Process process = startProcess(envDir, workingDir, command);
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    listener.onLog(line);
                }
                int exitCode = process.waitFor();
                listener.onFinished(exitCode);
            } catch (Exception e) {
                listener.onLog("EXCEPTION: " + e.getMessage());
                listener.onFinished(-1);
            }
        }).start();
    }

    public static String runSync(File envDir, String[] command) {
        return runSync(envDir, envDir, command);
    }

    public static String runSync(File envDir, File workingDir, String[] command) {
        StringBuilder output = new StringBuilder();
        try {
            Process process = startProcess(envDir, workingDir, command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            process.waitFor();
        } catch (Exception e) {
            output.append("EXCEPTION: ").append(e.getMessage());
        }
        return output.toString();
    }

    private static Process startProcess(File envDir, File workingDir, String[] command) throws Exception {
        // Prepare ( sheet-code ) 
        Runtime.getRuntime().exec(new String[]{"sh", "-c", "chmod +x " + new File(envDir, "bin").getAbsolutePath() + "/*"}).waitFor();
        
        String ndkBin = new File(envDir, "toolchains/llvm/prebuilt/linux-x86_64/bin").getAbsolutePath();
        Runtime.getRuntime().exec(new String[]{"sh", "-c", "chmod +x " + ndkBin + "/*"}).waitFor();

        String binName = command[0];
        File binFile = new File(envDir, "bin/" + binName);
        String binPath = binFile.exists() ? binFile.getAbsolutePath() : binName;

        String[] fullCommand = new String[command.length];
        fullCommand[0] = binPath;
        System.arraycopy(command, 1, fullCommand, 1, command.length - 1);

        ProcessBuilder pb = new ProcessBuilder(fullCommand);
        pb.directory(workingDir);
        pb.redirectErrorStream(true);

        Map<String, String> envVars = pb.environment();
        
        File homeDir = new File(envDir, "HOME");
        if (!homeDir.exists()) homeDir.mkdirs();
        envVars.put("HOME", homeDir.getAbsolutePath());

        envVars.put("LD_LIBRARY_PATH", 
                new File(envDir, "lib").getAbsolutePath() + ":" +
                envVars.getOrDefault("LD_LIBRARY_PATH", ""));

        envVars.put("PATH", 
                new File(envDir, "bin").getAbsolutePath() + ":" +
                new File(envDir, "toolchains/llvm/prebuilt/linux-x86_64/bin").getAbsolutePath() + ":" +
                envVars.getOrDefault("PATH", ""));

        envVars.put("RUSTFLAGS", "--sysroot=" + new File(envDir, "rust").getAbsolutePath());
        
        envVars.put("CARGO_TARGET_AARCH64_LINUX_ANDROID_LINKER", 
                new File(envDir, "toolchains/llvm/prebuilt/linux-x86_64/bin/aarch64-linux-android28-clang").getAbsolutePath());

        return pb.start();
    }

    public interface LogListener {
        void onLog(String line);
        void onFinished(int exitCode);
    }
}