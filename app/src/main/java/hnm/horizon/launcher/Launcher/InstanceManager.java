package hnm.horizon.launcher.Launcher;

import java.io.File;
import java.io.IOException;

public class InstanceManager {
    
    public static final String[] SHARED_LINKS = {".local"};
    public static final String[] INSTANCE_LINKS = {
            ".horizon-cache", "horizon-mods", "mod-browser", "performance",
            "resourcepacks", "saves", "shaderpacks",
    };
    public static final String[] DIRS_TO_CREATE = {
            ".horizon-cache", "horizon-mods", "mod-browser", "performance",
            "resourcepacks", "saves", "shaderpacks"
    };

    // Symlink create
    public static void createSymlink(File source, File target) {
        try {
            if (target.exists()) target.delete();
            Process p = Runtime.getRuntime().exec(new String[]{
                "ln", "-s", source.getAbsolutePath(), target.getAbsolutePath()
            });
            p.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Symlink prepare
    public static void prepareInstance(File instanceDir, File sharedDir, File gameRootDir) {
        for (String dirName : DIRS_TO_CREATE) {
            File dir = new File(instanceDir, dirName);
            if (!dir.exists()) dir.mkdirs();
        }

        // Delete old symlinks
        for (String linkName : SHARED_LINKS) new File(gameRootDir, linkName).delete();
        for (String linkName : INSTANCE_LINKS) new File(gameRootDir, linkName).delete();
        new File(gameRootDir, "libhorizon.so").delete();

        for (String linkName : SHARED_LINKS) {
            File source = new File(sharedDir, linkName);
            if (source.exists()) createSymlink(source, new File(gameRootDir, linkName));
        }

        for (String linkName : INSTANCE_LINKS) {
            File source = new File(instanceDir, linkName);
            if (source.exists()) createSymlink(source, new File(gameRootDir, linkName));
        }
    }

    public static void prepareEnvironment(File envDir) throws Exception {
        File homeDir = new File(envDir, "HOME");
        if (!homeDir.exists()) homeDir.mkdirs();
    
        String[] filesToChmod = {
                "bin/rustc", "bin/cargo", "bin/rsync", "bin/mold"
        };
        
        for (String f : filesToChmod) {
            File file = new File(envDir, f);
            if (file.exists()) {
                Runtime.getRuntime().exec(new String[]{"chmod", "+x", file.getAbsolutePath()}).waitFor();
            }
        }
    
        String ndkBin = new File(envDir, "toolchains/llvm/prebuilt/linux-x86_64/bin").getAbsolutePath();
        Runtime.getRuntime().exec(new String[]{"sh", "-c", "chmod +x " + ndkBin + "/clang*"}).waitFor();
        Runtime.getRuntime().exec(new String[]{"sh", "-c", "chmod +x " + ndkBin + "/llvm-*"}).waitFor();
        Runtime.getRuntime().exec(new String[]{"chmod", "+x", ndkBin + "/yasm"}).waitFor();
    }
}