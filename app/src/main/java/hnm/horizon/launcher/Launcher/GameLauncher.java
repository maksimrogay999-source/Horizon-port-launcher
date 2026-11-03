package hnm.horizon.launcher.Launcher;

import java.io.File;
import hnm.horizon.launcher.Files.AppFileManager;

public class GameLauncher {
    private final AppFileManager fileManager;

    public GameLauncher(AppFileManager fileManager) {
        this.fileManager = fileManager;
    }

    public boolean prepareAndLaunch(File instance) {
        File soFile = new File(instance, "libhorizon_android.so");
        if (!soFile.exists()) return false;

        InstanceManager.prepareInstance(instance, fileManager.getSharedDir(), fileManager.getGameRoot());
        InstanceManager.createSymlink(soFile, new File(fileManager.getGameRoot(), "libhorizon.so"));
        return true;
    }
}