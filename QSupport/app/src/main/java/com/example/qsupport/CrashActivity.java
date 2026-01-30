package com.example.qsupport;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;

public class CrashActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 20, 20, 20);

        TextView tv = new TextView(this);
        final String error = getIntent().getStringExtra("error");
        tv.setText(error);

        Button btn = new Button(this);
        btn.setText("СКОПИРОВАТЬ ОШИБКУ");
        btn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
					ClipData clip = ClipData.newPlainText("error", error);
					clipboard.setPrimaryClip(clip);
					Toast.makeText(CrashActivity.this, "Скопировано!", Toast.LENGTH_SHORT).show();
				}
			});

        ScrollView scroll = new ScrollView(this);
        scroll.addView(tv);

        layout.addView(btn);
        layout.addView(scroll);
        setContentView(layout);
    }
}

