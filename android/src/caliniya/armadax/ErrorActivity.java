package caliniya.armadax;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.*;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import cat.ereza.customactivityoncrash.CustomActivityOnCrash;
import cat.ereza.customactivityoncrash.config.CaocConfig;

public class ErrorActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.error_activity); // 加载你的布局文件
        // 获取错误信息（由 customActivityOnCrash 提供）
        String errorDetails = CustomActivityOnCrash.getStackTraceFromIntent(getIntent());
        String errorMessage = "游戏遇见了错误，以下为错误堆栈" + errorDetails;
        // 设置错误信息到 TextView
        TextView errorInfoTextView = findViewById(R.id.error_info_text_view);
        errorInfoTextView.setText(errorMessage);
        // 处理“复制错误报告”按钮
        Button copyButton = findViewById(R.id.copy_button);
        copyButton.setOnClickListener(v -> {
        ClipboardManager clipboard = (ClipboardManager) 
        getSystemService(CLIPBOARD_SERVICE);
        if (clipboard != null) {
        ClipData clip = ClipData.newPlainText("错误报告", errorDetails);
        clipboard.setPrimaryClip(clip);
                    }
            Toast.makeText(this, "已复制错误报告", Toast.LENGTH_SHORT).show();
        });
        // 处理“重启应用”按钮
        Button restartButton = findViewById(R.id.restart_button);
        restartButton.setOnClickListener(v -> {
            CaocConfig config = CustomActivityOnCrash.getConfigFromIntent(
                    getIntent()
            );    
            CustomActivityOnCrash.restartApplication(ErrorActivity.this ,config);
        });
    }
}