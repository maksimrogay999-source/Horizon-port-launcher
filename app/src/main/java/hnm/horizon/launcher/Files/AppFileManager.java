package hnm.horizon.launcher.Files;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AppFileManager {
    private final File filesDir;
    private final File instancesDir;
    private final File sharedDir;

    public AppFileManager(Context context) {
        this.filesDir = context.getFilesDir();
        this.instancesDir = new File(filesDir, "instances");
        this.sharedDir = new File(filesDir, "shared");

        if (!instancesDir.exists()) instancesDir.mkdirs();
        if (!sharedDir.exists()) sharedDir.mkdirs();
    }

    public File[] getInstances() {
        File[] dirs = instancesDir.listFiles(File::isDirectory);
        return dirs != null ? dirs : new File[0];
    }

    public File getEnvDir() { return new File(filesDir, "env"); }
    public File getGameRoot() { return filesDir; }
    public File getSharedDir() { return sharedDir; }
    public boolean getLocalZipExists() { return new File(filesDir, "local.zip").exists(); }

    public void deleteInstance(File instance) {
        FileManager.deleteDirectory(instance);
    }

    public boolean unpackShared() throws IOException {
        File localZip = new File(filesDir, "local.zip");
        if (!localZip.exists()) return false;

        File marker = new File(sharedDir, ".local");
        if (marker.exists()) return true;

        FileManager.unzip(localZip, sharedDir);
        try { marker.createNewFile(); } catch (IOException ignored) {}
        return true;
    }

    public void importEnvZip(Uri uri, Context context) throws IOException {
        File targetZip = new File(filesDir, "env.zip");
        try (InputStream is = context.getContentResolver().openInputStream(uri);
             OutputStream os = new FileOutputStream(targetZip)) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = is.read(buffer)) > 0) os.write(buffer, 0, len);
        }

        File envDir = getEnvDir();
        if (envDir.exists()) FileManager.deleteDirectory(envDir);
        FileManager.unzip(targetZip, filesDir);
    }
}