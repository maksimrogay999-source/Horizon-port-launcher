package hnm.horizon.launcher;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class SetupActivity extends Activity {

    private static final int REQUEST_LOCAL = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setBackgroundColor(Color.WHITE);
        layout.setPadding(64, 64, 64, 64);

        TextView title = new TextView(this);
        title.setText("Select local.zip");
        title.setTextColor(Color.BLACK);
        title.setTextSize(28);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 64);
        layout.addView(title);

        // ТОЛЬКО КНОПКА ДЛЯ LOCAL.ZIP
        Button btnLocal = new Button(this);
        btnLocal.setText("local.zip");
        btnLocal.setTextColor(Color.BLACK);
        btnLocal.setTextSize(20);
        btnLocal.setAllCaps(false);
        
        GradientDrawable localBg = new GradientDrawable();
        localBg.setColor(Color.parseColor("#808080"));
        localBg.setCornerRadius(30f);
        btnLocal.setBackground(localBg);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 150);
        btnLocal.setLayoutParams(params);

        btnLocal.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(intent, REQUEST_LOCAL);
        });
        layout.addView(btnLocal);

        setContentView(layout);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (requestCode == REQUEST_LOCAL) {
                copyFileToPrivate(uri, "local.zip");
            }
        }
    }

    private void copyFileToPrivate(Uri uri, String fileName) {
        try {
            File targetFile = new File(getFilesDir(), fileName);
            InputStream inputStream = getContentResolver().openInputStream(uri);
            OutputStream outputStream = new FileOutputStream(targetFile);

            byte[] buffer = new byte[8192];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            inputStream.close();
            outputStream.close();

            Toast.makeText(this, "Файл " + fileName + " успешно скопирован!", Toast.LENGTH_SHORT).show();
            
            // Возвращаемся в MainActivity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка копирования: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}