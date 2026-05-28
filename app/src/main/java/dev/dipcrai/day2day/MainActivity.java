package dev.dipcrai.day2day;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import dev.dipcrai.day2day.data.local.AppDatabase;
import dev.dipcrai.day2day.data.repository.TaskRepository;
import dev.dipcrai.day2day.ui.DayViewRenderer;
import dev.dipcrai.day2day.ui.DeleteTaskDialog;
import dev.dipcrai.day2day.ui.WeekDaysBarView;
import dev.dipcrai.day2day.ui.WeekViewRenderer;
import dev.dipcrai.day2day.util.TaskDateUtils;

public class MainActivity extends AppCompatActivity {

    private List<Task> allTasks = new ArrayList<>();
    private TaskRepository taskRepository;
    private String currentView = "day";
    private Calendar selectedDate = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private ViewFlipper viewFlipper;
    private LinearLayout weekDaysContainer;
    private LinearLayout complexityBadge;
    private TextView complexityLabel;
    private TextView complexityValue;
    private TextView monthBadge;
    private LinearLayout dayScheduleContainer;
    private LinearLayout weekScheduleContainer;
    private ScrollView dayScrollView;
    private ScrollView weekScrollView;

    private DayViewRenderer dayViewRenderer;
    private WeekViewRenderer weekViewRenderer;
    private WeekDaysBarView weekDaysBarView;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("day2day", MODE_PRIVATE);

        taskRepository = new TaskRepository(AppDatabase.getInstance(this).taskDao());
        initViews();
        initRenderers();
        loadTasks();
        setupViewToggle();
        setupSwipes();
        setupSleepSettings();
        populateWeekDays();
        showDayView();
        updateComplexityBadge();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    private void initViews() {
        viewFlipper = findViewById(R.id.viewFlipper);
        weekDaysContainer = findViewById(R.id.weekDaysContainer);
        complexityBadge = findViewById(R.id.complexityBadge);
        complexityLabel = findViewById(R.id.complexityLabel);
        complexityValue = findViewById(R.id.complexityValue);
        monthBadge = findViewById(R.id.monthBadge);
        dayScheduleContainer = findViewById(R.id.dayScheduleContainer);
        weekScheduleContainer = findViewById(R.id.weekScheduleContainer);
        dayScrollView = findViewById(R.id.dayView);
        weekScrollView = findViewById(R.id.weekView);

        findViewById(R.id.fabAddTask).setOnClickListener(v -> {
            AddTaskDialogFragment dialog = new AddTaskDialogFragment();
            dialog.setSelectedDate(TaskDateUtils.dateToString(selectedDate, dateFormat));
            dialog.setExistingTasks(TaskDateUtils.getTasksForDate(allTasks, selectedDate, dateFormat));
            dialog.setAllTasks(allTasks);
            dialog.setSleepStart(prefs.getInt("sleepStart", 0));
            dialog.setSleepEnd(prefs.getInt("sleepEnd", 480));
            dialog.setOnTaskCreatedListener(task -> {
                taskRepository.insert(task, result -> {});
                allTasks.add(task);
                refreshCurrentView();
                return true;
            });
            dialog.show(getSupportFragmentManager(), "AddTask");
        });
    }

    private void initRenderers() {
        dayViewRenderer = new DayViewRenderer(this, dateFormat);
        dayViewRenderer.setOnTaskLongClickListener(task ->
                DeleteTaskDialog.show(this, task, selectedDate, allTasks,
                        taskRepository, dateFormat, this::refreshCurrentView));
        weekViewRenderer = new WeekViewRenderer(this, dateFormat);
        weekViewRenderer.setOnTaskLongClickListener(task ->
                DeleteTaskDialog.show(this, task, selectedDate, allTasks,
                        taskRepository, dateFormat, this::refreshCurrentView));
        weekDaysBarView = new WeekDaysBarView(this);
    }

    private void setupViewToggle() {
        TextView toggleDay = findViewById(R.id.toggleDay);
        TextView toggleWeek = findViewById(R.id.toggleWeek);

        toggleDay.setOnClickListener(v -> {
            if ("day".equals(currentView)) return;
            switchToDay();
            toggleDay.setBackgroundResource(R.drawable.toggle_option_active);
            toggleDay.setTextColor(ContextCompat.getColor(this, R.color.primary));
            toggleWeek.setBackgroundResource(R.drawable.toggle_option_inactive);
            toggleWeek.setTextColor(ContextCompat.getColor(this, R.color.muted_foreground));
        });

        toggleWeek.setOnClickListener(v -> {
            if ("week".equals(currentView)) return;
            switchToWeek();
            toggleWeek.setBackgroundResource(R.drawable.toggle_option_active);
            toggleWeek.setTextColor(ContextCompat.getColor(this, R.color.primary));
            toggleDay.setBackgroundResource(R.drawable.toggle_option_inactive);
            toggleDay.setTextColor(ContextCompat.getColor(this, R.color.muted_foreground));
        });
    }

    private void setupSwipes() {
        GestureDetector daySwipe = new GestureDetector(this, new SwipeListener(
                () -> goToPreviousDay(), () -> goToNextDay()));
        dayScrollView.setOnTouchListener((v, e) -> { daySwipe.onTouchEvent(e); return false; });

        GestureDetector weekSwipe = new GestureDetector(this, new SwipeListener(
                () -> goToPreviousWeek(), () -> goToNextWeek()));
        weekScrollView.setOnTouchListener((v, e) -> { weekSwipe.onTouchEvent(e); return false; });

        GestureDetector stripSwipe = new GestureDetector(this, new SwipeListener(
                () -> goToPreviousWeek(), () -> goToNextWeek()));
        findViewById(R.id.weekStrip).setOnTouchListener((v, e) -> { stripSwipe.onTouchEvent(e); return false; });
    }

    private void setupSleepSettings() {
        findViewById(R.id.btnSleepSettings).setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));
    }

    private class SwipeListener extends GestureDetector.SimpleOnGestureListener {
        private final Runnable onPrev, onNext;

        SwipeListener(Runnable onPrev, Runnable onNext) {
            this.onPrev = onPrev;
            this.onNext = onNext;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float vx, float vy) {
            if (e1 == null || e2 == null) return false;
            float dx = e2.getX() - e1.getX();
            float dy = e2.getY() - e1.getY();
            if (Math.abs(dx) > Math.abs(dy)) {
                if (dx > 0) onPrev.run();
                else onNext.run();
                return true;
            }
            return false;
        }
    }



    private void switchToDay() {
        currentView = "day";
        viewFlipper.setDisplayedChild(0);
        complexityBadge.setVisibility(android.view.View.VISIBLE);
        populateWeekDays();
        showDayView();
        updateComplexityBadge();
    }

    private void switchToWeek() {
        currentView = "week";
        viewFlipper.setDisplayedChild(1);
        complexityBadge.setVisibility(android.view.View.GONE);
        populateWeekDays();
        showWeekView();
    }

    private void populateWeekDays() {
        weekDaysBarView.render(weekDaysContainer, selectedDate, () -> {
            if ("day".equals(currentView)) showDayView();
            else showWeekView();
            updateComplexityBadge();
        });
        SimpleDateFormat monthFormat = new SimpleDateFormat("LLLL yyyy", new Locale("ru"));
        monthBadge.setText(monthFormat.format(selectedDate.getTime()));
    }

    private void showDayView() {
        dayViewRenderer.render(dayScheduleContainer, dayScrollView, allTasks, selectedDate);
    }

    private void showWeekView() {
        weekViewRenderer.render(weekScheduleContainer, allTasks, selectedDate);
    }

    private void updateComplexityBadge() {
        if (!"day".equals(currentView)) {
            complexityBadge.setVisibility(android.view.View.GONE);
            return;
        }
        complexityBadge.setVisibility(android.view.View.VISIBLE);

        int totalComplexity = 0;
        int count = 0;
        for (Task t : allTasks) {
            if (TaskDateUtils.isTaskOnDate(t, selectedDate, dateFormat)) {
                totalComplexity += t.getComplexity();
                count++;
            }
        }
        totalComplexity = count == 0 ? 0 : Math.round((float) totalComplexity / count);

        int color;
        String label;
        if (totalComplexity <= 4) {
            color = ContextCompat.getColor(this, R.color.complexity_easy);
            label = getString(R.string.complexity_easy_day);
        } else if (totalComplexity <= 7) {
            color = ContextCompat.getColor(this, R.color.complexity_busy);
            label = getString(R.string.complexity_moderate_day);
        } else {
            color = ContextCompat.getColor(this, R.color.complexity_extreme);
            label = getString(R.string.complexity_hard_day);
        }

        complexityLabel.setText(label);
        complexityLabel.setTextColor(color);
        complexityValue.setText(String.valueOf(totalComplexity));
        complexityValue.setTextColor(color);

        float density = getResources().getDisplayMetrics().density;
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        bg.setCornerRadius(8 * density);
        bg.setColor(TaskDateUtils.lightenColor(color));
        bg.setStroke((int) density, color);
        complexityBadge.setBackground(bg);
    }

    private void loadTasks() {
        taskRepository.getAllTasks(result -> {
            allTasks.clear();
            if (result != null) allTasks.addAll(result);
            refreshCurrentView();
        });
    }

    private void refreshCurrentView() {
        if ("day".equals(currentView)) showDayView();
        else showWeekView();
        updateComplexityBadge();
    }

    private void goToNextDay() {
        selectedDate.add(Calendar.DAY_OF_MONTH, 1);
        populateWeekDays();
        refreshCurrentView();
    }

    private void goToPreviousDay() {
        selectedDate.add(Calendar.DAY_OF_MONTH, -1);
        populateWeekDays();
        refreshCurrentView();
    }

    private void goToNextWeek() {
        selectedDate.add(Calendar.WEEK_OF_YEAR, 1);
        populateWeekDays();
        if ("week".equals(currentView)) showWeekView();
        else showDayView();
        updateComplexityBadge();
    }

    private void goToPreviousWeek() {
        selectedDate.add(Calendar.WEEK_OF_YEAR, -1);
        populateWeekDays();
        if ("week".equals(currentView)) showWeekView();
        else showDayView();
        updateComplexityBadge();
    }
}
