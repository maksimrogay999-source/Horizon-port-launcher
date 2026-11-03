package hnm.horizon.launcher;

import java.io.File;

public class MainUiState {
    public final File currentInstance;
    public final String instanceName;

    public MainUiState(File currentInstance, String instanceName) {
        this.currentInstance = currentInstance;
        this.instanceName = instanceName;
    }
}