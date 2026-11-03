package hnm.horizon.launcher;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.ViewModelProvider;

import java.io.File;

public class MainActivity extends ComponentActivity {
    private MainViewModel viewModel;
    private TextView tvInstanceName;

    private final ActivityResultLauncher<String[]> envImportLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri != null) viewModel.onEnvImported(uri);
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        tvInstanceName = findViewById(R.id.tvInstanceName);

        setupUI();
        observeViewModel();

        viewModel.checkSharedUnpacked();
    }

    private void setupUI() {
        findViewById(R.id.btnImportEnv).setOnClickListener(v ->
                envImportLauncher.launch(new String[]{"*/*"})
        );
        findViewById(R.id.btnPrepareEnv).setOnClickListener(v -> viewModel.onPrepareEnvClicked());
        findViewById(R.id.btnTestEnv).setOnClickListener(v -> viewModel.onTestEnvClicked());
        findViewById(R.id.btnDelete).setOnClickListener(v -> showDeleteConfirmationDialog());
        findViewById(R.id.btnPlay).setOnClickListener(v -> viewModel.onPlayClicked());
        findViewById(R.id.btnVersion).setOnClickListener(v -> showVersionSelectionDialog());
    }

    private void observeViewModel() {
        viewModel.getUiState().observe(this, state -> {
            if (state != null) {
                tvInstanceName.setText(state.instanceName);
            }
        });

        viewModel.getEvent().observe(this, event -> {
            if (event instanceof MainUiEvent.ShowToast) {
                Toast.makeText(this, ((MainUiEvent.ShowToast) event).message, Toast.LENGTH_SHORT).show();
            } else if (event instanceof MainUiEvent.CopyToClipboard) {
                copyToClipboard(((MainUiEvent.CopyToClipboard) event).text);
            } else if (event instanceof MainUiEvent.LaunchGame) {
                startActivity(new Intent(this, HorizonActivity.class));
            } else if (event instanceof MainUiEvent.NavigateToSetup) {
                startActivity(new Intent(this, SetupActivity.class));
                finish();
            }
        });
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to delete an instance?")
                .setPositiveButton("Delete", (dialog, which) -> viewModel.onDeleteConfirmed())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showVersionSelectionDialog() {
        File[] dirs = viewModel.getInstances();
        if (dirs.length == 0) {
            Toast.makeText(this, "Папка instances пуста", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] instanceNames = new String[dirs.length];
        for (int i = 0; i < dirs.length; i++) {
            instanceNames[i] = dirs[i].getName();
        }

        new AlertDialog.Builder(this)
                .setTitle("Выберите инстанс")
                .setItems(instanceNames, (dialog, which) -> viewModel.onVersionSelected(dirs[which]))
                .show();
    }

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Env Test Result", text);
        clipboard.setPrimaryClip(clip);
    }
}