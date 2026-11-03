package hnm.horizon.launcher;

import android.app.NativeActivity;
import android.os.Bundle;

public class HorizonActivity extends NativeActivity {
    // Нативный метод для передачи результата обратно в Rust
    private native void nativeDocumentResult(int request, int result, String uri);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onActivityResult(int request, int result, android.content.Intent data) {
        super.onActivityResult(request, result, data);
        
        // Передаем результат в нативную часть (как в оригинале)
        if (request != 4101) return;
        nativeDocumentResult(request, result,
                data == null || data.getData() == null ? null : data.getData().toString());
    }
}