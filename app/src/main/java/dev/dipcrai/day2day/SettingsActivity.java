package dev.dipcrai.day2day;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private static final String KEY_THEME = "theme_mode";

    private SharedPreferences prefs;
    private int sleepStart, sleepEnd;
    private int currentTheme;
    private int[] themePillIds = {R.id.pillThemeSystem, R.id.pillThemeDark, R.id.pillThemeLight};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("day2day", MODE_PRIVATE);
        sleepStart = prefs.getInt("sleepStart", 0);
        sleepEnd = prefs.getInt("sleepEnd", 480);
        currentTheme = prefs.getInt(KEY_THEME, 0);

        setupSleepSection();
        setupThemeSection();
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupSleepSection() {
        EditText etStart = findViewById(R.id.sleepStart);
        EditText etEnd = findViewById(R.id.sleepEnd);

        etStart.setText(String.format(Locale.getDefault(), "%02d:%02d", sleepStart / 60, sleepStart % 60));
        etEnd.setText(String.format(Locale.getDefault(), "%02d:%02d", sleepEnd / 60, sleepEnd % 60));

        findViewById(R.id.sleepSave).setOnClickListener(v -> {
            int[] startParsed = parseTimeInput(etStart.getText().toString());
            int[] endParsed = parseTimeInput(etEnd.getText().toString());
            if (startParsed == null || endParsed == null) {
                Toast.makeText(this, getString(R.string.sleep_error_format), Toast.LENGTH_SHORT).show();
                return;
            }
            sleepStart = startParsed[0] * 60 + startParsed[1];
            sleepEnd = endParsed[0] * 60 + endParsed[1];

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
    }

    private void setupThemeSection() {
        updateThemePills();

        for (int i = 0; i < 3; i++) {
            final int mode = i;
            findViewById(themePillIds[i]).setOnClickListener(v -> {
                currentTheme = mode;
                prefs.edit().putInt(KEY_THEME, currentTheme).apply();
                updateThemePills();
                applyTheme(currentTheme);
            });
        }
    }

    private void updateThemePills() {
        for (int i = 0; i < 3; i++) {
            TextView pill = findViewById(themePillIds[i]);
            boolean selected = i == currentTheme;
            pill.setBackgroundResource(selected ? R.drawable.chip_selected : R.drawable.chip_unselected);
            pill.setTextColor(ContextCompat.getColor(this,
                    selected ? R.color.primary_foreground : R.color.muted_foreground));
        }
    }

    private int[] parseTimeInput(String input) {
        input = input.trim();
        if (input.isEmpty()) return null;
        try {
            if (input.contains(":")) {
                String[] parts = input.split(":");
                int h = Integer.parseInt(parts[0]);
                int m = Integer.parseInt(parts[1]);
                if (h < 0 || h > 23 || m < 0 || m > 59) return null;
                return new int[]{h, m};
            } else {
                int h = Integer.parseInt(input);
                if (h < 0 || h > 23) return null;
                return new int[]{h, 0};
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void applyTheme(int mode) {
        switch (mode) {
            case 1:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case 2:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
        recreate();
    }
}
