package dev.dipcrai.day2day;

import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private int sleepStart, sleepEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("day2day", MODE_PRIVATE);
        sleepStart = prefs.getInt("sleepStart", 0);
        sleepEnd = prefs.getInt("sleepEnd", 480);

        TextView tvStart = findViewById(R.id.sleepStart);
        TextView tvEnd = findViewById(R.id.sleepEnd);

        tvStart.setText(String.format(Locale.getDefault(), "%02d:%02d", sleepStart / 60, sleepStart % 60));
        tvEnd.setText(String.format(Locale.getDefault(), "%02d:%02d", sleepEnd / 60, sleepEnd % 60));

        tvStart.setOnClickListener(v -> {
            int h = sleepStart / 60, m = sleepStart % 60;
            new TimePickerDialog(this, (picker, hour, minute) -> {
                sleepStart = hour * 60 + minute;
                tvStart.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
            }, h, m, true).show();
        });

        tvEnd.setOnClickListener(v -> {
            int h = sleepEnd / 60, m = sleepEnd % 60;
            new TimePickerDialog(this, (picker, hour, minute) -> {
                sleepEnd = hour * 60 + minute;
                tvEnd.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
            }, h, m, true).show();
        });

        findViewById(R.id.sleepSave).setOnClickListener(v -> {
            int duration;
            if (sleepStart > sleepEnd) {
                duration = (1440 - sleepStart) + sleepEnd;
            } else if (sleepStart < sleepEnd) {
                duration = sleepEnd - sleepStart;
            } else {
                Toast.makeText(this, getString(R.string.sleep_error_equal), Toast.LENGTH_SHORT).show();
                return;
            }
            if (duration < 60) {
                Toast.makeText(this, getString(R.string.sleep_error_short), Toast.LENGTH_SHORT).show();
                return;
            }
            if (duration > 720) {
                Toast.makeText(this, getString(R.string.sleep_error_long), Toast.LENGTH_SHORT).show();
                return;
            }
            prefs.edit()
                    .putInt("sleepStart", sleepStart)
                    .putInt("sleepEnd", sleepEnd)
                    .apply();
            finish();
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
}
