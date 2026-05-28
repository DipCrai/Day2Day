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

<<<<<<< Updated upstream
import com.google.android.material.button.MaterialButtonToggleGroup;
=======
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
>>>>>>> Stashed changes

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

<<<<<<< Updated upstream
    private final String[] dayNames = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private DayViewRenderer dayViewRenderer;
    private WeekViewRenderer weekViewRenderer;
    private WeekDaysBarView weekDaysBarView;
=======
    private String[] dayNames;
    private final java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
>>>>>>> Stashed changes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        taskRepository = new TaskRepository(AppDatabase.getInstance(this).taskDao());
        dayNames = new String[] {
                getString(R.string.day_mon), getString(R.string.day_tue),
                getString(R.string.day_wed), getString(R.string.day_thu),
                getString(R.string.day_fri), getString(R.string.day_sat),
                getString(R.string.day_sun)
        };
        initViews();
        loadTasks();
        setupViewToggle();
<<<<<<< Updated upstream
        setupSwipeListeners();
=======
        setupSwipes();
        populateWeekDays();
        updateMonthBadge();
        showDayView();
        updateComplexityBadge();
>>>>>>> Stashed changes

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
<<<<<<< Updated upstream
            dialog.setSelectedDate(TaskDateUtils.dateToString(selectedDate, dateFormat));
            dialog.setExistingTasks(allTasks);
=======
            dialog.setSelectedDate(dateToString(selectedDate));
            dialog.setExistingTasks(getTasksForDate(selectedDate));
>>>>>>> Stashed changes
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
        TextView toggleDay = findViewById(R.id.toggleDay);
        TextView toggleWeek = findViewById(R.id.toggleWeek);
        LinearLayout toggleContainer = findViewById(R.id.viewToggle);

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
<<<<<<< Updated upstream
        weekDaysBarView.render(weekDaysContainer, selectedDate, () -> {
            populateWeekDays();
            if ("day".equals(currentView)) showDayView();
            else showWeekView();
            updateComplexityBadge();
        });
=======
        weekDaysContainer.removeAllViews();

        Calendar today = Calendar.getInstance();
        Calendar monday = getMonday(selectedDate);

        for (int i = 0; i < 7; i++) {
            Calendar day = (Calendar) monday.clone();
            day.add(Calendar.DAY_OF_MONTH, i);

            View dayView = getLayoutInflater().inflate(R.layout.item_week_day, weekDaysContainer, false);
            TextView dayName = dayView.findViewById(R.id.dayName);
            TextView dayNumber = dayView.findViewById(R.id.dayNumber);

            dayName.setText(dayNames[i]);
            dayNumber.setText(String.valueOf(day.get(Calendar.DAY_OF_MONTH)));

            boolean isToday = isSameDay(day, today);
            boolean isSelected = isSameDay(day, selectedDate);

            if (isToday) {
                dayView.setBackgroundResource(R.drawable.today_bg);
                dayNumber.setTextColor(ContextCompat.getColor(this, R.color.today_fg));
                dayName.setTextColor(ContextCompat.getColor(this, R.color.today_fg));
            } else if (isSelected) {
                dayView.setSelected(true);
                dayNumber.setTextColor(ContextCompat.getColor(this, R.color.primary));
                dayName.setTextColor(ContextCompat.getColor(this, R.color.muted_foreground));
            } else {
                dayView.setSelected(false);
                dayNumber.setTextColor(ContextCompat.getColor(this, R.color.primary));
                dayName.setTextColor(ContextCompat.getColor(this, R.color.muted_foreground));
            }

            final int dayIndex = i;
            final Calendar selectedDay = day;
            dayView.setOnClickListener(v -> {
                selectedDate = selectedDay;
                populateWeekDays();
                if ("day".equals(currentView)) showDayView();
                else if ("week".equals(currentView)) showWeekView();
                updateComplexityBadge();
            });

            weekDaysContainer.addView(dayView);
        }
        updateMonthBadge();
>>>>>>> Stashed changes
    }

    private void showDayView() {
<<<<<<< Updated upstream
        dayViewRenderer.render(dayScheduleContainer, dayScrollView, allTasks, selectedDate);
    }

=======
        dayScheduleContainer.removeAllViews();

        String[] hourLabels = new String[24];
        for (int i = 0; i < 24; i++) {
            hourLabels[i] = String.format(Locale.getDefault(), "%02d:00", i);
        }

        float density = getResources().getDisplayMetrics().density;
        int hourHeightPx = (int) (HOUR_HEIGHT_DP * density);

        FrameLayout timelineContainer = new FrameLayout(this);
        timelineContainer.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, hourHeightPx * 24));

        int labelWidthPx = (int) (48 * density);

        for (int i = 0; i < 24; i++) {
            View hourLine = new View(this);
            FrameLayout.LayoutParams lineParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 1);
            lineParams.topMargin = i * hourHeightPx;
            hourLine.setLayoutParams(lineParams);
            hourLine.setBackgroundColor(ContextCompat.getColor(this, R.color.border));
            timelineContainer.addView(hourLine);

            TextView hourLabel = new TextView(this);
            hourLabel.setText(hourLabels[i]);
            hourLabel.setTextSize(10);
            hourLabel.setTextColor(ContextCompat.getColor(this, R.color.muted_foreground));
            FrameLayout.LayoutParams labelParams = new FrameLayout.LayoutParams(
                    labelWidthPx, ViewGroup.LayoutParams.WRAP_CONTENT);
            labelParams.topMargin = i * hourHeightPx - (int) (6 * density);
            labelParams.leftMargin = (int) (4 * density);
            hourLabel.setLayoutParams(labelParams);
            timelineContainer.addView(hourLabel);

            if (i < 23) {
                View halfLine = new View(this);
                FrameLayout.LayoutParams halfParams = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 1);
                halfParams.topMargin = i * hourHeightPx + hourHeightPx / 2;
                halfLine.setLayoutParams(halfParams);
                halfLine.setBackgroundColor(ContextCompat.getColor(this, R.color.border));
                timelineContainer.addView(halfLine);
            }
        }

        List<Task> dayTasks = getTasksForDate(selectedDate);
        for (Task task : dayTasks) {
            int startMinutes = timeToMinutes(task.getStartTime());
            int endMinutes = timeToMinutes(task.getEndTime());
            int durationMinutes = Math.max(endMinutes - startMinutes, 30);
            int topPx = (int) ((startMinutes / 60f) * hourHeightPx);
            int heightPx = (int) ((durationMinutes / 60f) * hourHeightPx);

            View taskCard = createTaskCardView(task, heightPx);
            FrameLayout.LayoutParams taskParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, heightPx);
            taskParams.topMargin = topPx;
            taskParams.leftMargin = (int) (56 * density);
            taskParams.rightMargin = (int) (8 * density);
            taskCard.setLayoutParams(taskParams);
            timelineContainer.addView(taskCard);
        }

        Calendar now = Calendar.getInstance();
        int nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
        int currentTopPx = (int) ((nowMinutes / 60f) * hourHeightPx);

        View timeIndicator = new View(this);
        int timeIndentPx = (int) (52 * density);
        FrameLayout.LayoutParams timeParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 3);
        timeParams.topMargin = currentTopPx;
        timeParams.leftMargin = timeIndentPx;
        timeIndicator.setLayoutParams(timeParams);
        timeIndicator.setBackgroundColor(ContextCompat.getColor(this, R.color.destructive));
        timelineContainer.addView(timeIndicator);

        View timeDot = new View(this);
        int dotSize = (int) (12 * density);
        FrameLayout.LayoutParams dotParams = new FrameLayout.LayoutParams(dotSize, dotSize);
        dotParams.topMargin = currentTopPx - dotSize / 2;
        dotParams.leftMargin = -(int) (density);
        timeDot.setLayoutParams(dotParams);
        timeDot.setBackgroundResource(R.drawable.time_dot);
        timelineContainer.addView(timeDot);

        dayScheduleContainer.addView(timelineContainer);

        int scrollToHour = Math.max(0, now.get(Calendar.HOUR_OF_DAY) - 2);
        dayScrollView.post(() -> dayScrollView.scrollTo(0, scrollToHour * hourHeightPx));
    }

    private View createTaskCardView(Task task, int heightPx) {
        float density = getResources().getDisplayMetrics().density;

        CardView card = new CardView(this);
        card.setCardElevation(2 * density);
        card.setRadius(8 * density);
        card.setContentPadding((int) (10 * density), (int) (8 * density),
                (int) (10 * density), (int) (8 * density));
        card.setCardBackgroundColor(Color.WHITE);
        card.setUseCompatPadding(true);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        View colorBar = new View(this);
        LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(
                (int) (4 * density), ViewGroup.LayoutParams.MATCH_PARENT);
        barParams.setMargins(0, 0, (int) (8 * density), 0);
        colorBar.setLayoutParams(barParams);
        colorBar.setBackgroundColor(task.getColor());
        colorBar.setVisibility(View.GONE);

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView timeText = new TextView(this);
        timeText.setText(getString(R.string.time_range, task.getStartTime(), task.getEndTime()));
        timeText.setTextSize(11);
        timeText.setTextColor(ContextCompat.getColor(this, R.color.muted_foreground));
        timeText.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        TextView complexityText = new TextView(this);
        complexityText.setText(getString(R.string.complexity_label, task.getComplexity()));
        complexityText.setTextSize(11);
        complexityText.setTextColor(ContextCompat.getColor(this, R.color.muted_foreground));

        topRow.addView(timeText);
        topRow.addView(complexityText);
        layout.addView(topRow);

        boolean isCompact = heightPx < 70 * density;

        if (!isCompact) {
            TextView titleText = new TextView(this);
            titleText.setText(task.getTitle());
            titleText.setTextSize(14);
            titleText.setTypeface(null, android.graphics.Typeface.BOLD);
            titleText.setTextColor(ContextCompat.getColor(this, R.color.primary));
            titleText.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            titleText.setPadding(0, (int) (4 * density), 0, 0);
            layout.addView(titleText);

            TextView descText = new TextView(this);
            descText.setText(task.getDescription());
            descText.setTextSize(12);
            descText.setMaxLines(2);
            descText.setTextColor(ContextCompat.getColor(this, R.color.muted_foreground));
            descText.setPadding(0, (int) (2 * density), 0, 0);
            layout.addView(descText);
        } else {
            TextView titleText = new TextView(this);
            titleText.setText(task.getTitle());
            titleText.setTextSize(13);
            titleText.setTypeface(null, android.graphics.Typeface.BOLD);
            titleText.setTextColor(ContextCompat.getColor(this, R.color.primary));
            titleText.setMaxLines(1);
            layout.addView(titleText);
        }

        card.addView(layout);

        card.setOnLongClickListener(v -> {
            deleteWithConfirmation(task);
            return true;
        });

        GradientDrawable border = new GradientDrawable();
        border.setShape(GradientDrawable.RECTANGLE);
        border.setCornerRadius(8 * density);
        border.setStroke((int) (3 * density), task.getColor());

        LayerDrawable layerDrawable = new LayerDrawable(new android.graphics.drawable.Drawable[]{
                createBackgroundDrawable(task.getColor()),
                border
        });
        card.setBackground(layerDrawable);

        return card;
    }

    private GradientDrawable createBackgroundDrawable(int color) {
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(8 * getResources().getDisplayMetrics().density);
        bg.setColor(lightenColor(color));
        return bg;
    }

    // --- Week View ---

>>>>>>> Stashed changes
    private void showWeekView() {
        weekViewRenderer.render(weekScheduleContainer, allTasks, selectedDate);
    }

<<<<<<< Updated upstream
=======
    private View createWeekDayCard(Calendar day, int dayIndex, Calendar today) {
        float density = getResources().getDisplayMetrics().density;
        boolean isToday = isSameDay(day, today);

        MaterialCardView card = new MaterialCardView(this);
        card.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ((ViewGroup.MarginLayoutParams) card.getLayoutParams()).setMargins(0, 0, 0, (int) (12 * density));
        card.setCardElevation(2 * density);
        card.setRadius(12 * density);
        card.setUseCompatPadding(true);
        card.setCardBackgroundColor(Color.WHITE);

        if (isToday) {
            card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.primary_alpha_6));
            card.setStrokeWidth((int) (2 * density));
            card.setStrokeColor(ContextCompat.getColor(this, R.color.primary));
        }

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding((int) (16 * density), (int) (12 * density),
                (int) (16 * density), (int) (12 * density));

        LinearLayout headerRow = new LinearLayout(this);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        headerRow.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout dateInfo = new LinearLayout(this);
        dateInfo.setOrientation(LinearLayout.VERTICAL);
        dateInfo.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        TextView dayNameText = new TextView(this);
        dayNameText.setText(dayNames[dayIndex]);
        dayNameText.setTextSize(12);
        dayNameText.setTextColor(ContextCompat.getColor(this, R.color.muted_foreground));

        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", new Locale("ru"));
        TextView dateText = new TextView(this);
        dateText.setText(day.get(Calendar.DAY_OF_MONTH) + " " + monthFormat.format(day.getTime()));
        dateText.setTextSize(16);
        dateText.setTypeface(null, android.graphics.Typeface.BOLD);
        dateText.setTextColor(ContextCompat.getColor(this, R.color.primary));

        dateInfo.addView(dayNameText);
        dateInfo.addView(dateText);
        headerRow.addView(dateInfo);

        if (isToday) {
            TextView todayBadge = new TextView(this);
            todayBadge.setText(getString(R.string.today));
            todayBadge.setTextSize(11);
            todayBadge.setTextColor(ContextCompat.getColor(this, R.color.today_fg));
            todayBadge.setPadding((int) (8 * density), (int) (4 * density),
                    (int) (8 * density), (int) (4 * density));
            todayBadge.setBackgroundResource(R.drawable.today_bg);
            headerRow.addView(todayBadge);
        }

        layout.addView(headerRow);

        int dayComplexity = calculateDayComplexity(day);
        if (dayComplexity > 0) {
            View complexityBadgeView = createMiniComplexityBadge(dayComplexity);
            complexityBadgeView.setPadding(0, (int) (8 * density), 0, 0);
            layout.addView(complexityBadgeView);
        }

        List<Task> dayTasks = getTasksForDay(day);
        if (!dayTasks.isEmpty()) {
            layout.addView(createTasksDivider(density));
            for (Task task : dayTasks) {
                layout.addView(createMiniTaskCard(task, density));
            }
        } else {
            TextView emptyText = new TextView(this);
            emptyText.setText(getString(R.string.no_tasks));
            emptyText.setTextSize(13);
            emptyText.setTextColor(ContextCompat.getColor(this, R.color.muted_foreground));
            emptyText.setGravity(Gravity.CENTER);
            emptyText.setPadding(0, (int) (24 * density), 0, (int) (24 * density));
            layout.addView(emptyText);
        }

        card.addView(layout);
        return card;
    }

    private View createMiniComplexityBadge(int complexity) {
        int color;
        String label;
        if (complexity <= 4) { color = ContextCompat.getColor(this, R.color.complexity_easy); label = getString(R.string.complexity_easy_day); }
        else if (complexity <= 7) { color = ContextCompat.getColor(this, R.color.complexity_busy); label = getString(R.string.complexity_moderate_day); }
        else { color = ContextCompat.getColor(this, R.color.complexity_extreme); label = getString(R.string.complexity_hard_day); }

        float density = getResources().getDisplayMetrics().density;

        LinearLayout badge = new LinearLayout(this);
        badge.setOrientation(LinearLayout.HORIZONTAL);
        badge.setGravity(Gravity.CENTER_VERTICAL);
        badge.setPadding((int) (8 * density), (int) (4 * density),
                (int) (8 * density), (int) (4 * density));

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(8 * density);
        bg.setColor(lightenColor(color));
        bg.setStroke((int) (1 * density), color);
        badge.setBackground(bg);

        TextView labelText = new TextView(this);
        labelText.setText(label);
        labelText.setTextSize(11);
        labelText.setTypeface(null, android.graphics.Typeface.BOLD);
        labelText.setTextColor(color);
        badge.addView(labelText);

        TextView valueText = new TextView(this);
        valueText.setText(String.valueOf(complexity));
        valueText.setTextSize(11);
        valueText.setTextColor(color);
        valueText.setPadding((int) (6 * density), 0, 0, 0);
        badge.addView(valueText);

        return badge;
    }

    private View createTasksDivider(float density) {
        View divider = new View(this);
        divider.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 1));
        ((ViewGroup.MarginLayoutParams) divider.getLayoutParams()).setMargins(0, (int) (8 * density), 0, (int) (8 * density));
        divider.setBackgroundColor(ContextCompat.getColor(this, R.color.border));
        return divider;
    }

    private View createMiniTaskCard(Task task, float density) {
        CardView card = new CardView(this);
        card.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ((ViewGroup.MarginLayoutParams) card.getLayoutParams()).setMargins(0, 0, 0, (int) (8 * density));
        card.setCardElevation(1 * density);
        card.setRadius(8 * density);
        card.setUseCompatPadding(true);
        card.setContentPadding((int) (12 * density), (int) (10 * density),
                (int) (12 * density), (int) (10 * density));
        card.setCardBackgroundColor(Color.WHITE);

        GradientDrawable border = new GradientDrawable();
        border.setShape(GradientDrawable.RECTANGLE);
        border.setCornerRadius(8 * density);
        border.setStroke((int) (3 * density), task.getColor());
        border.setColor(Color.WHITE);
        card.setBackground(border);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);

        TextView timeText = new TextView(this);
        timeText.setText(getString(R.string.time_range, task.getStartTime(), task.getEndTime()));
        timeText.setTextSize(11);
        timeText.setTextColor(ContextCompat.getColor(this, R.color.muted_foreground));
        timeText.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        TextView compText = new TextView(this);
        compText.setText(String.valueOf(task.getComplexity()) + "/10");
        compText.setTextSize(11);
        compText.setTextColor(ContextCompat.getColor(this, R.color.muted_foreground));

        topRow.addView(timeText);
        topRow.addView(compText);
        layout.addView(topRow);

        TextView titleText = new TextView(this);
        titleText.setText(task.getTitle());
        titleText.setTextSize(13);
        titleText.setTypeface(null, android.graphics.Typeface.BOLD);
        titleText.setTextColor(ContextCompat.getColor(this, R.color.primary));
        titleText.setPadding(0, (int) (2 * density), 0, 0);
        layout.addView(titleText);

        TextView descText = new TextView(this);
        descText.setText(task.getDescription());
        descText.setTextSize(11);
        descText.setMaxLines(2);
        descText.setTextColor(ContextCompat.getColor(this, R.color.muted_foreground));
        layout.addView(descText);

        card.addView(layout);

        card.setOnLongClickListener(v -> {
            deleteWithConfirmation(task);
            return true;
        });

        return card;
    }

    // --- Helpers ---

    private void updateMonthBadge() {
        SimpleDateFormat monthFormat = new SimpleDateFormat("LLLL yyyy", new Locale("ru"));
        monthBadge.setText(monthFormat.format(selectedDate.getTime()));
    }

>>>>>>> Stashed changes
    private void updateComplexityBadge() {
        if (!"day".equals(currentView)) {
            complexityBadge.setVisibility(android.view.View.GONE);
            return;
        }
        complexityBadge.setVisibility(android.view.View.VISIBLE);

        int sum = 0;
        int count = 0;
        for (Task t : allTasks) {
<<<<<<< Updated upstream
            if (TaskDateUtils.isTaskOnDate(t, selectedDate, dateFormat)) {
                totalComplexity += t.getComplexity();
=======
            if (isTaskOnDate(t, selectedDate)) {
                sum += t.getComplexity();
                count++;
>>>>>>> Stashed changes
            }
        }
        int totalComplexity = count == 0 ? 0 : Math.round((float) sum / count);

        int color;
        String label;
        if (totalComplexity <= 4) { color = ContextCompat.getColor(this, R.color.complexity_easy); label = getString(R.string.complexity_easy_day); }
        else if (totalComplexity <= 7) { color = ContextCompat.getColor(this, R.color.complexity_busy); label = getString(R.string.complexity_moderate_day); }
        else { color = ContextCompat.getColor(this, R.color.complexity_extreme); label = getString(R.string.complexity_hard_day); }

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

<<<<<<< Updated upstream
=======
    private int timeToMinutes(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    private String dateToString(Calendar cal) {
        return dateFormat.format(cal.getTime());
    }

    private Calendar parseDate(String dateStr) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(dateFormat.parse(dateStr));
            return cal;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isTaskOnDate(Task task, Calendar date) {
        String taskDate = task.getDate();
        if (taskDate == null) return false;

        String type = task.getRecurrenceType();
        if (type == null) type = "once";

        String dateStr = dateToString(date);

        if ("once".equals(type)) {
            return taskDate.equals(dateStr);
        }

        String excluded = task.getExcludedDates();
        if (excluded != null && !excluded.isEmpty()) {
            for (String d : excluded.split(",")) {
                if (d.trim().equals(dateStr)) return false;
            }
        }

        Calendar startDate = parseDate(taskDate);
        if (startDate == null) return false;

        if (date.before(startDate) && !dateStr.equals(taskDate)) return false;

        String endDateStr = task.getRecurrenceEndDate();
        if (endDateStr != null) {
            Calendar endDate = parseDate(endDateStr);
            if (endDate != null && date.after(endDate)) return false;
        }

        switch (type) {
            case "daily":
                return true;
            case "weekly":
                return date.get(Calendar.DAY_OF_WEEK) == startDate.get(Calendar.DAY_OF_WEEK);
            case "weekdays":
                int dow = date.get(Calendar.DAY_OF_WEEK);
                return dow >= Calendar.MONDAY && dow <= Calendar.FRIDAY;
            case "custom_days":
                String daysStr = task.getRecurrenceDays();
                if (daysStr == null || daysStr.isEmpty()) return false;
                int targetDow = date.get(Calendar.DAY_OF_WEEK);
                for (String d : daysStr.split(",")) {
                    if (Integer.parseInt(d.trim()) == targetDow) return true;
                }
                return false;
        }
        return false;
    }

    private List<Task> getTasksForDate(Calendar date) {
        List<Task> result = new ArrayList<>();
        for (Task t : allTasks) {
            if (isTaskOnDate(t, date)) {
                result.add(t);
            }
        }
        return result;
    }

    private List<Task> getTasksForDay(Calendar cal) {
        List<Task> result = new ArrayList<>();
        for (Task task : allTasks) {
            if (isTaskOnDate(task, cal)) {
                result.add(task);
            }
        }
        result.sort((a, b) -> Integer.compare(timeToMinutes(a.getStartTime()), timeToMinutes(b.getStartTime())));
        return result;
    }

    private int calculateDayComplexity(Calendar day) {
        List<Task> tasks = getTasksForDay(day);
        if (tasks.isEmpty()) return 0;
        int sum = 0;
        for (Task task : tasks) {
            sum += task.getComplexity();
        }
        return Math.round((float) sum / tasks.size());
    }

    private void deleteWithConfirmation(Task task) {
        String type = task.getRecurrenceType();
        boolean isRecurring = type != null && !"once".equals(type);

        if (!isRecurring) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(getString(R.string.delete_task_title))
                    .setMessage(getString(R.string.delete_task_message, task.getTitle()))
                    .setPositiveButton(getString(R.string.delete_positive), (dialog, which) -> {
                        taskRepository.deleteById(task.getId(), result -> {});
                        allTasks.remove(task);
                        refreshCurrentView();
                    })
                    .setNegativeButton(getString(R.string.delete_cancel), null)
                    .show();
            return;
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.delete_recurring_title))
                .setMessage(getString(R.string.delete_task_message, task.getTitle()))
                .setPositiveButton(getString(R.string.delete_this), (dialog, which) -> {
                    String today = dateToString(selectedDate);
                    String excluded = task.getExcludedDates();
                    if (excluded == null || excluded.isEmpty()) {
                        task.setExcludedDates(today);
                    } else {
                        task.setExcludedDates(excluded + "," + today);
                    }
                    taskRepository.update(task, result -> {});
                    refreshCurrentView();
                })
                .setNeutralButton(getString(R.string.delete_all), (dialog, which) -> {
                    taskRepository.deleteById(task.getId(), result -> {});
                    allTasks.remove(task);
                    refreshCurrentView();
                })
                .setNegativeButton(getString(R.string.delete_cancel), null)
                .show();
    }

    private void refreshCurrentView() {
        if ("day".equals(currentView)) showDayView();
        else showWeekView();
        updateComplexityBadge();
    }

>>>>>>> Stashed changes
    private void loadTasks() {
        taskRepository.getAllTasks(result -> {
            allTasks.clear();
            if (result != null) allTasks.addAll(result);
            if ("day".equals(currentView)) showDayView();
            else showWeekView();
            updateComplexityBadge();
        });
    }

<<<<<<< Updated upstream
    public void refreshCurrentView() {
        if ("day".equals(currentView)) showDayView();
        else showWeekView();
        updateComplexityBadge();
=======
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
>>>>>>> Stashed changes
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
<<<<<<< Updated upstream
=======

>>>>>>> Stashed changes
}
