package dev.dipcrai.day2day;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.GestureDetector;
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

import com.google.android.material.button.MaterialButtonToggleGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import dev.dipcrai.day2day.data.local.AppDatabase;
import dev.dipcrai.day2day.data.repository.TaskRepository;
import dev.dipcrai.day2day.ui.DayViewRenderer;
import dev.dipcrai.day2day.ui.DeleteTaskDialog;
import dev.dipcrai.day2day.ui.SwipeListeners;
import dev.dipcrai.day2day.ui.WeekDaysBarView;
import dev.dipcrai.day2day.ui.WeekViewRenderer;
import dev.dipcrai.day2day.util.TaskDateUtils;

public class MainActivity extends AppCompatActivity {

    private List<Task> allTasks = new ArrayList<>();
    private TaskRepository taskRepository;
    private String currentView = "day";
    private Calendar selectedDate = Calendar.getInstance();

    private MaterialButtonToggleGroup viewToggle;
    private ViewFlipper viewFlipper;
    private LinearLayout weekDaysContainer;
    private LinearLayout complexityBadge;
    private TextView complexityLabel;
    private TextView complexityValue;
    private LinearLayout dayScheduleContainer;
    private LinearLayout weekScheduleContainer;
    private ScrollView dayScrollView;
    private ScrollView weekScrollView;

    private final String[] dayNames = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private DayViewRenderer dayViewRenderer;
    private WeekViewRenderer weekViewRenderer;
    private WeekDaysBarView weekDaysBarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        taskRepository = new TaskRepository(AppDatabase.getInstance(this).taskDao());
        initViews();
        loadTasks();
        setupViewToggle();
        setupSwipeListeners();

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
        viewToggle = findViewById(R.id.viewToggle);
        viewFlipper = findViewById(R.id.viewFlipper);
        weekDaysContainer = findViewById(R.id.weekDaysContainer);
        complexityBadge = findViewById(R.id.complexityBadge);
        complexityLabel = findViewById(R.id.complexityLabel);
        complexityValue = findViewById(R.id.complexityValue);
        dayScheduleContainer = findViewById(R.id.dayScheduleContainer);
        weekScheduleContainer = findViewById(R.id.weekScheduleContainer);
        dayScrollView = findViewById(R.id.dayView);
        weekScrollView = findViewById(R.id.weekView);

        dayViewRenderer = new DayViewRenderer(this, dateFormat);
        dayViewRenderer.setOnTaskLongClickListener(task ->
                DeleteTaskDialog.show(this, task, selectedDate, allTasks,
                        taskRepository, dateFormat, this::refreshCurrentView));
        weekViewRenderer = new WeekViewRenderer(this, dayNames, dateFormat);
        weekViewRenderer.setOnTaskLongClickListener(task ->
                DeleteTaskDialog.show(this, task, selectedDate, allTasks,
                        taskRepository, dateFormat, this::refreshCurrentView));
        weekDaysBarView = new WeekDaysBarView(this, dayNames);

        findViewById(R.id.fabAddTask).setOnClickListener(v -> {
            AddTaskDialogFragment dialog = new AddTaskDialogFragment();
            dialog.setSelectedDate(TaskDateUtils.dateToString(selectedDate, dateFormat));
            dialog.setExistingTasks(allTasks);
            dialog.setOnTaskCreatedListener(task -> {
                taskRepository.insert(task, result -> {});
                allTasks.add(task);
                if ("day".equals(currentView)) showDayView();
                else showWeekView();
                updateComplexityBadge();
                return true;
            });
            dialog.show(getSupportFragmentManager(), "AddTask");
        });
    }

    private void setupViewToggle() {
        viewToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == R.id.toggleDay) switchToDay();
            else switchToWeek();
        });
    }

    private void setupSwipeListeners() {
        GestureDetector dayGesture = new GestureDetector(this, new SwipeListeners.DaySwipeListener(
                this::goToNextDay, this::goToPreviousDay));
        dayScrollView.setOnTouchListener((v, event) -> {
            dayGesture.onTouchEvent(event);
            return false;
        });

        GestureDetector weekGesture = new GestureDetector(this, new SwipeListeners.WeekSwipeListener(
                () -> weekScrollView.post(this::goToPreviousWeek),
                () -> weekScrollView.post(this::goToNextWeek)));
        weekScrollView.setOnTouchListener((v, event) -> {
            weekGesture.onTouchEvent(event);
            return false;
        });
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
            populateWeekDays();
            if ("day".equals(currentView)) showDayView();
            else showWeekView();
            updateComplexityBadge();
        });
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
        for (Task t : allTasks) {
            if (TaskDateUtils.isTaskOnDate(t, selectedDate, dateFormat)) {
                totalComplexity += t.getComplexity();
            }
        }

        int color;
        String label;
        if (totalComplexity <= 20) { color = 0xFF22C55E; label = "Легкий день"; }
        else if (totalComplexity <= 40) { color = 0xFF3B82F6; label = "Умеренный"; }
        else if (totalComplexity <= 60) { color = 0xFFEAB308; label = "Напряженный"; }
        else if (totalComplexity <= 80) { color = 0xFFF97316; label = "Сложный день"; }
        else { color = 0xFFEF4444; label = "Очень сложный"; }

        complexityLabel.setText(label);
        complexityLabel.setTextColor(color);
        complexityValue.setText(String.valueOf(totalComplexity));
        complexityValue.setTextColor(color);

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(8 * getResources().getDisplayMetrics().density);
        bg.setColor(TaskDateUtils.lightenColor(color));
        bg.setStroke((int) (getResources().getDisplayMetrics().density), color);
        complexityBadge.setBackground(bg);
    }

    private void loadTasks() {
        taskRepository.getAllTasks(result -> {
            allTasks.clear();
            if (result != null) allTasks.addAll(result);
            if ("day".equals(currentView)) showDayView();
            else showWeekView();
            updateComplexityBadge();
        });
    }

    public void refreshCurrentView() {
        if ("day".equals(currentView)) showDayView();
        else showWeekView();
        updateComplexityBadge();
    }

    private void goToNextDay() {
        selectedDate.add(Calendar.DAY_OF_MONTH, 1);
        populateWeekDays();
        if ("day".equals(currentView)) showDayView();
        else showWeekView();
        updateComplexityBadge();
    }

    private void goToPreviousDay() {
        selectedDate.add(Calendar.DAY_OF_MONTH, -1);
        populateWeekDays();
        if ("day".equals(currentView)) showDayView();
        else showWeekView();
        updateComplexityBadge();
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
