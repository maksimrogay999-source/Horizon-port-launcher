package hnm.horizon.launcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends Activity {
    private File instancesDir;
    private File sharedDir;
    private File gameRootDir;
    
    private File currentInstance;
    private TextView tvInstanceName;

    private static final int REQUEST_ENV = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        instancesDir = new File(getFilesDir(), "instances");
        sharedDir = new File(getFilesDir(), "shared");
        gameRootDir = getFilesDir();

        if (!instancesDir.exists()) instancesDir.mkdirs();
        if (!sharedDir.exists()) sharedDir.mkdirs();

        checkAndUnpackShared();

        RelativeLayout layout = new RelativeLayout(this);
        layout.setBackgroundColor(Color.parseColor("#121212"));

        // Gui
        Button btnImportEnv = new Button(this);
        btnImportEnv.setText("Import\nenv");
        btnImportEnv.setTextColor(Color.BLACK);
        btnImportEnv.setTextSize(18);
        btnImportEnv.setTypeface(null, android.graphics.Typeface.BOLD);
        btnImportEnv.setAllCaps(false);
        GradientDrawable envBg = new GradientDrawable();
        envBg.setColor(Color.parseColor("#00BFFF"));
        envBg.setCornerRadius(16f);
        btnImportEnv.setBackground(envBg);
        
        RelativeLayout.LayoutParams envParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        envParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        envParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        envParams.setMargins(64, 64, 0, 0);
        btnImportEnv.setPadding(48, 24, 48, 24);
        btnImportEnv.setLayoutParams(envParams);
        btnImportEnv.setId(10);
        layout.addView(btnImportEnv);

        btnImportEnv.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(intent, REQUEST_ENV);
        });

        Button btnPrepareEnv = new Button(this);
        btnPrepareEnv.setText("Prepare env");
        btnPrepareEnv.setTextColor(Color.BLACK);
        btnPrepareEnv.setTextSize(18);
        btnPrepareEnv.setTypeface(null, android.graphics.Typeface.BOLD);
        btnPrepareEnv.setAllCaps(false);
        GradientDrawable prepBg = new GradientDrawable();
        prepBg.setColor(Color.parseColor("#00BFFF"));
        prepBg.setCornerRadius(16f);
        btnPrepareEnv.setBackground(prepBg);
        
        RelativeLayout.LayoutParams prepParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        prepParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        prepParams.addRule(RelativeLayout.RIGHT_OF, 10);
        prepParams.setMargins(24, 64, 0, 0);
        btnPrepareEnv.setPadding(48, 24, 48, 24);
        btnPrepareEnv.setLayoutParams(prepParams);
        btnPrepareEnv.setId(11);
        layout.addView(btnPrepareEnv);

        btnPrepareEnv.setOnClickListener(v -> prepareEnv());

        Button btnTestEnv = new Button(this);
        btnTestEnv.setText("Test env");
        btnTestEnv.setTextColor(Color.BLACK);
        btnTestEnv.setTextSize(18);
        btnTestEnv.setTypeface(null, android.graphics.Typeface.BOLD);
        btnTestEnv.setAllCaps(false);
        GradientDrawable testBg = new GradientDrawable();
        testBg.setColor(Color.parseColor("#4CAF50"));
        testBg.setCornerRadius(16f);
        btnTestEnv.setBackground(testBg);
        
        RelativeLayout.LayoutParams testParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        testParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        testParams.addRule(RelativeLayout.RIGHT_OF, 11);
        testParams.setMargins(24, 64, 0, 0);
        btnTestEnv.setPadding(48, 24, 48, 24);
        btnTestEnv.setLayoutParams(testParams);
        layout.addView(btnTestEnv);

        btnTestEnv.setOnClickListener(v -> testEnvironment());

        tvInstanceName = new TextView(this);
        tvInstanceName.setTextColor(Color.WHITE);
        tvInstanceName.setTextSize(36);
        tvInstanceName.setTypeface(null, android.graphics.Typeface.BOLD);
        RelativeLayout.LayoutParams nameParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        nameParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        nameParams.addRule(RelativeLayout.ABOVE, 3);
        nameParams.setMargins(0, 0, 64, 24);
        tvInstanceName.setLayoutParams(nameParams);
        tvInstanceName.setId(1);
        layout.addView(tvInstanceName);

        Button btnDelete = new Button(this);
        btnDelete.setText("Delete");
        btnDelete.setTextColor(Color.BLACK);
        btnDelete.setTextSize(24);
        btnDelete.setTypeface(null, android.graphics.Typeface.BOLD);
        btnDelete.setAllCaps(false);
        btnDelete.setPadding(0, 24, 0, 24);
        GradientDrawable delBg = new GradientDrawable();
        delBg.setColor(Color.RED);
        delBg.setCornerRadius(30f);
        btnDelete.setBackground(delBg);
        
        RelativeLayout.LayoutParams delParams = new RelativeLayout.LayoutParams(400, ViewGroup.LayoutParams.WRAP_CONTENT);
        delParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        delParams.addRule(RelativeLayout.ABOVE, 2);
        delParams.setMargins(0, 0, 64, 16); 
        btnDelete.setLayoutParams(delParams);
        btnDelete.setId(3);
        layout.addView(btnDelete);

        btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog());

        Button btnPlay = new Button(this);
        btnPlay.setText("Play");
        btnPlay.setTextColor(Color.BLACK);
        btnPlay.setTextSize(28);
        btnPlay.setTypeface(null, android.graphics.Typeface.BOLD);
        btnPlay.setAllCaps(false);
        btnPlay.setPadding(0, 24, 0, 24);
        GradientDrawable playBg = new GradientDrawable();
        playBg.setColor(Color.parseColor("#4CAF50"));
        playBg.setCornerRadius(16f);
        btnPlay.setBackground(playBg);
        
        RelativeLayout.LayoutParams playParams = new RelativeLayout.LayoutParams(400, ViewGroup.LayoutParams.WRAP_CONTENT);
        playParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        playParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        playParams.setMargins(0, 0, 64, 150); 
        btnPlay.setLayoutParams(playParams);
        btnPlay.setId(2);
        layout.addView(btnPlay);

        btnPlay.setOnClickListener(v -> {
            if (currentInstance != null) launchGame(currentInstance);
            else Toast.makeText(this, "Сначала выберите версию!", Toast.LENGTH_SHORT).show();
        });

        // 7. Кнопка "Выбрать"
        Button btnVersion = new Button(this);
        btnVersion.setText("Выбрать");
        btnVersion.setTextColor(Color.WHITE);
        btnVersion.setTextSize(18);
        btnVersion.setAllCaps(false);
        btnVersion.setPadding(0, 24, 0, 24);
        GradientDrawable verBg = new GradientDrawable();
        verBg.setColor(Color.parseColor("#333333"));
        verBg.setCornerRadius(16f);
        btnVersion.setBackground(verBg);
        
        RelativeLayout.LayoutParams verParams = new RelativeLayout.LayoutParams(300, ViewGroup.LayoutParams.WRAP_CONTENT);
        verParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        verParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        verParams.setMargins(64, 0, 0, 64);
        btnVersion.setLayoutParams(verParams);
        layout.addView(btnVersion);

        btnVersion.setOnClickListener(v -> showVersionSelectionDialog());

        setContentView(layout);
        loadInitialInstance();
    }

    private void testEnvironment() {
        File envDir = new File(getFilesDir(), "env");
        if (!envDir.exists()) {
            Toast.makeText(this, "Папка env не найдена!", Toast.LENGTH_SHORT).show();
            return;
        }
    
        Toast.makeText(this, "Запуск теста (может занять время)...", Toast.LENGTH_SHORT).show();
    
        new Thread(() -> {
            try {
                // 1. Создаем временный проект
                File testDir = new File(envDir, "hello_test");
                if (testDir.exists()) FileManager.deleteDirectory(testDir);
                testDir.mkdirs();
    
                File cargoToml = new File(testDir, "Cargo.toml");
                File srcDir = new File(testDir, "src");
                srcDir.mkdirs();
                File mainRs = new File(srcDir, "main.rs");
    
                // Пишем простой проект
                writeToFile(cargoToml, "[package]\nname = \"hello_test\"\nversion = \"0.1.0\"\nedition = \"2021\"\n");
                writeToFile(mainRs, "fn main() { println!(\"HELLO FROM ANDROID ENV!\"); }\n");
    
                // 2. Собираем (ПЕРЕДАЕМ testDir КАК РАБОЧУЮ ДИРЕКТОРИЮ)
                String[] buildCmd = {"cargo", "build", "--target", "aarch64-linux-android"};
                String buildLog = RubRun.runSync(envDir, testDir, buildCmd); // <--- ТУТ
    
                // 3. Запускаем
                File binPath = new File(testDir, "target/aarch64-linux-android/debug/hello_test");
                
                if (!binPath.exists()) {
                    showResultInClipboard("Сборка упала:\n" + buildLog);
                    return;
                }
    
                // Даем права и запускаем
                Runtime.getRuntime().exec(new String[]{"chmod", "+x", binPath.getAbsolutePath()}).waitFor();
                String runLog = RubRun.runSync(envDir, testDir, new String[]{binPath.getAbsolutePath()}); // <--- ТУТ
    
                // 4. Показываем результат
                showResultInClipboard("УСПЕХ!\nВывод:\n" + runLog + "\n\nЛог сборки:\n" + buildLog);
    
            } catch (Exception e) {
                showResultInClipboard("Ошибка: " + e.getMessage());
            }
        }).start();
    }
    
    private void showResultInClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Env Test Result", text);
        clipboard.setPrimaryClip(clip);

        runOnUiThread(() -> Toast.makeText(this, "Результат скопирован в буфер обмена!", Toast.LENGTH_LONG).show());
    }

    private void writeToFile(File file, String content) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(content.getBytes());
        }
    }

    private void prepareEnv() {
        Toast.makeText(this, "Подготовка env (chmod +x)...", Toast.LENGTH_SHORT).show();
        File envDir = new File(getFilesDir(), "env");
        
        if (!envDir.exists()) {
            Toast.makeText(this, "Папка env не найдена. Сначала импортируйте её.", Toast.LENGTH_LONG).show();
            return;
        }

        new Thread(() -> {
            try {
                InstanceManager.prepareEnvironment(envDir);
                runOnUiThread(() -> Toast.makeText(this, "Env готов к работе!", Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Ошибка подготовки: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && requestCode == REQUEST_ENV) {
            Uri uri = data.getData();
            if (uri != null) {
                File targetZip = new File(getFilesDir(), "env.zip");
                try {
                    InputStream is = getContentResolver().openInputStream(uri);
                    OutputStream os = new FileOutputStream(targetZip);
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = is.read(buffer)) > 0) os.write(buffer, 0, len);
                    is.close();
                    os.close();

                    Toast.makeText(this, "Распаковка env.zip...", Toast.LENGTH_SHORT).show();
                    
                    File envDir = new File(getFilesDir(), "env");
                    if (envDir.exists()) {
                        FileManager.deleteDirectory(envDir);
                    }
                    
                    FileManager.unzip(targetZip, getFilesDir());
                    
                    Toast.makeText(this, "Env успешно импортирован! Нажмите Prepare env.", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Ошибка импорта env: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void showDeleteConfirmationDialog() {
        if (currentInstance == null) return;

        new AlertDialog.Builder(this)
            .setMessage("Are you sure you want to delete an instance?")
            .setPositiveButton("Delete", (dialog, which) -> {
                if (FileManager.deleteDirectory(currentInstance)) {
                    Toast.makeText(this, "Инстанс удалён", Toast.LENGTH_SHORT).show();
                    loadInitialInstance(); 
                } else {
                    Toast.makeText(this, "Ошибка удаления", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void loadInitialInstance() {
        File[] dirs = instancesDir.listFiles(File::isDirectory);
        if (dirs != null && dirs.length > 0) {
            setCurrentInstance(dirs[0]);
        } else {
            tvInstanceName.setText("Нет инстансов");
            currentInstance = null;
        }
    }

    private void setCurrentInstance(File instance) {
        currentInstance = instance;
        tvInstanceName.setText(instance.getName());
    }

    private void showVersionSelectionDialog() {
        File[] dirs = instancesDir.listFiles(File::isDirectory);
        if (dirs == null || dirs.length == 0) {
            Toast.makeText(this, "Папка instances пуста", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] instanceNames = new String[dirs.length];
        for (int i = 0; i < dirs.length; i++) {
            instanceNames[i] = dirs[i].getName();
        }

        new AlertDialog.Builder(this)
            .setTitle("Выберите инстанс")
            .setItems(instanceNames, (dialog, which) -> {
                setCurrentInstance(dirs[which]);
            })
            .show();
    }

    private void launchGame(File instance) {
        File soFile = new File(instance, "libhorizon_android.so");
        if (!soFile.exists()) {
            Toast.makeText(this, "Нет .so файла в инстансе!", Toast.LENGTH_SHORT).show();
            return;
        }

        InstanceManager.prepareInstance(instance, sharedDir, gameRootDir);
        InstanceManager.createSymlink(soFile, new File(gameRootDir, "libhorizon.so"));

        Intent intent = new Intent(this, HorizonActivity.class);
        startActivity(intent);
    }

    private void checkAndUnpackShared() {
        File localZip = new File(getFilesDir(), "local.zip");

        if (!localZip.exists()) {
            Intent intent = new Intent(this, SetupActivity.class);
            startActivity(intent);
            finish();
            return;
        }
    
        File testFile = new File(sharedDir, ".local");
        if (testFile.exists()) return;
    
        Toast.makeText(this, "Распаковка local.zip...", Toast.LENGTH_SHORT).show();
        try {
            FileManager.unzip(localZip, sharedDir);
            Toast.makeText(this, "Распаковано!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}