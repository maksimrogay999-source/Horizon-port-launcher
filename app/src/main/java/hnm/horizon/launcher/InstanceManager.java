package hnm.horizon.launcher;

import java.io.File;
import java.io.IOException;

public class InstanceManager {
    
    public static final String[] SHARED_LINKS = {".local", "engine-data.zip"};
    public static final String[] INSTANCE_LINKS = {
            ".horizon-cache", "horizon-mods", "mod-browser", "performance",
            "resourcepacks", "saves", "shaderpacks", "android-launcher.json",
            "horizon-extars.txt", "horizon-graphics.txt", "horizon-touch.json",
            "options.txt"
    };
    public static final String[] DIRS_TO_CREATE = {
            ".horizon-cache", "horizon-mods", "mod-browser", "performance",
            "resourcepacks", "saves", "shaderpacks"
    };

    // Создание симлинка
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

    // Подготовка симлинков для запуска инстанса
    public static void prepareInstance(File instanceDir, File sharedDir, File gameRootDir) {
        // Создаем нужные папки
        for (String dirName : DIRS_TO_CREATE) {
            File dir = new File(instanceDir, dirName);
            if (!dir.exists()) dir.mkdirs();
        }

        // Удаляем старые симлинки
        for (String linkName : SHARED_LINKS) new File(gameRootDir, linkName).delete();
        for (String linkName : INSTANCE_LINKS) new File(gameRootDir, linkName).delete();
        new File(gameRootDir, "libhorizon.so").delete();

        // Создаем симлинки из SHARED
        for (String linkName : SHARED_LINKS) {
            File source = new File(sharedDir, linkName);
            if (source.exists()) createSymlink(source, new File(gameRootDir, linkName));
        }

        // Создаем симлинки из INSTANCE
        for (String linkName : INSTANCE_LINKS) {
            File source = new File(instanceDir, linkName);
            if (source.exists()) createSymlink(source, new File(gameRootDir, linkName));
        }
    }

    // Выдача прав на выполнение (chmod +x) для папки env
    public static void prepareEnvironment(File envDir) throws Exception {
        // Создаем HOME
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