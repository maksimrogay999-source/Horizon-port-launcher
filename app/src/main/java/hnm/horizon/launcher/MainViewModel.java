package hnm.horizon.launcher;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.File;
import hnm.horizon.launcher.Files.AppFileManager;
import hnm.horizon.launcher.Env.EnvironmentManager;
import hnm.horizon.launcher.Launcher.GameLauncher;

public class MainViewModel extends AndroidViewModel {
    private final AppFileManager fileManager;
    private final EnvironmentManager envManager;
    private final GameLauncher gameLauncher;

    private final MutableLiveData<MainUiState> uiState = new MutableLiveData<>();
    private final MutableLiveData<MainUiEvent> event = new MutableLiveData<>();

    public MainViewModel(@NonNull Application application) {
        super(application);
        fileManager = new AppFileManager(application);
        envManager = new EnvironmentManager(fileManager);
        gameLauncher = new GameLauncher(fileManager);
        loadInitialInstance();
    }

    public LiveData<MainUiState> getUiState() { return uiState; }
    public LiveData<MainUiEvent> getEvent() { return event; }

    public void onPrepareEnvClicked() {
        event.setValue(new MainUiEvent.ShowToast("Подготовка env..."));
        runInBackground(() -> {
            try {
                envManager.prepareEnv();
                event.postValue(new MainUiEvent.ShowToast("Env готов к работе!"));
            } catch (Exception e) {
                event.postValue(new MainUiEvent.ShowToast("Ошибка подготовки: " + e.getMessage()));
            }
        });
    }

    public void onTestEnvClicked() {
        event.setValue(new MainUiEvent.ShowToast("Запуск теста..."));
        runInBackground(() -> {
            try {
                String result = envManager.testEnvironment();
                event.postValue(new MainUiEvent.CopyToClipboard(result));
                event.postValue(new MainUiEvent.ShowToast("Результат скопирован в буфер!"));
            } catch (Exception e) {
                event.postValue(new MainUiEvent.CopyToClipboard("Ошибка: " + e.getMessage()));
                event.postValue(new MainUiEvent.ShowToast("Ошибка! Результат в буфере."));
            }
        });
    }

    public void onPlayClicked() {
        MainUiState state = uiState.getValue();
        if (state == null || state.currentInstance == null) {
            event.setValue(new MainUiEvent.ShowToast("Сначала выберите версию!"));
            return;
        }
        if (gameLauncher.prepareAndLaunch(state.currentInstance)) {
            event.setValue(new MainUiEvent.LaunchGame());
        } else {
            event.setValue(new MainUiEvent.ShowToast("Нет .so файла в инстансе!"));
        }
    }

    public void onDeleteConfirmed() {
        MainUiState state = uiState.getValue();
        if (state != null && state.currentInstance != null) {
            fileManager.deleteInstance(state.currentInstance);
            event.setValue(new MainUiEvent.ShowToast("Инстанс удалён"));
            loadInitialInstance();
        }
    }

    public void onVersionSelected(File instance) {
        uiState.setValue(new MainUiState(instance, instance.getName()));
    }

    public void onEnvImported(Uri uri) {
        runInBackground(() -> {
            try {
                fileManager.importEnvZip(uri, getApplication());
                event.postValue(new MainUiEvent.ShowToast("Env успешно импортирован!"));
            } catch (Exception e) {
                event.postValue(new MainUiEvent.ShowToast("Ошибка импорта: " + e.getMessage()));
            }
        });
    }

    public void checkSharedUnpacked() {
        if (!fileManager.getLocalZipExists()) {
            event.setValue(new MainUiEvent.NavigateToSetup());
            return;
        }
        runInBackground(() -> {
            try {
                fileManager.unpackShared();
            } catch (Exception e) {
                event.postValue(new MainUiEvent.ShowToast("Ошибка распаковки: " + e.getMessage()));
            }
        });
    }

    public File[] getInstances() {
        return fileManager.getInstances();
    }

    private void loadInitialInstance() {
        File[] dirs = fileManager.getInstances();
        if (dirs.length > 0) {
            uiState.setValue(new MainUiState(dirs[0], dirs[0].getName()));
        } else {
            uiState.setValue(new MainUiState(null, "Нет инстансов"));
        }
    }

    private void runInBackground(Runnable task) {
        new Thread(task).start();
    }
}