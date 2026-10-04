package com.fusen.autosolver;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(40, 50, 40, 40);
        TextView title = new TextView(this);
        title.setText("Fusen Auto Solver\n\n1. Enable the accessibility service below.\n2. Open your block puzzle.\n3. Press the floating AUTO button.\n\nThis version is tuned for the 9×9 layout shown in your screenshot. It reads the screen locally and performs drag gestures.");
        title.setTextSize(18);
        Button settings = new Button(this);
        settings.setText("Open Accessibility Settings");
        settings.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        box.addView(title);
        box.addView(settings);
        setContentView(box);
    }
}
