package dev.dipcrai.day2day;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import dev.dipcrai.day2day.data.local.AppDatabase;
import dev.dipcrai.day2day.data.repository.TaskRepository;

public class MainActivity extends AppCompatActivity {

    private static final int HOUR_HEIGHT_DP = 80;

    private List<Task> allTasks = new ArrayList<>();
    private TaskRepository taskRepository;
    private GestureDetector gestureDetector;
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

    private final String[] dayNames = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};
    private final java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        taskRepository = new TaskRepository(AppDatabase.getInstance(this).taskDao());
        initViews();
        loadTasks();
        setupViewToggle();
        populateWeekDays();
        showDayView();
        updateComplexityBadge();

        gestureDetector = new GestureDetector(this, new DaySwipeListener());
        dayScrollView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return false;
        });

        GestureDetector weekGestureDetector = new GestureDetector(this, new WeekSwipeListener());
        weekDaysContainer.setOnTouchListener((v, event) -> weekGestureDetector.onTouchEvent(event));

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

    private int getTaskColor(int colorHex) {
        return colorHex;
    }

    private int lightenColor(int colorHex) {
        int alpha = 10;
        int red = Color.red(colorHex);
        int green = Color.green(colorHex);
        int blue = Color.blue(colorHex);
        return Color.argb(alpha, red, green, blue);
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

        findViewById(R.id.fabAddTask).setOnClickListener(v -> {
            AddTaskDialogFragment dialog = new AddTaskDialogFragment();
            dialog.setSelectedDate(dateToString(selectedDate));
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

    private void switchToDay() {
        currentView = "day";
        viewFlipper.setDisplayedChild(0);
        complexityBadge.setVisibility(View.VISIBLE);
        populateWeekDays();
        showDayView();
        updateComplexityBadge();
    }

    private void switchToWeek() {
        currentView = "week";
        viewFlipper.setDisplayedChild(1);
        complexityBadge.setVisibility(View.GONE);
        populateWeekDays();
        showWeekView();
    }

    private void populateWeekDays() {
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
    }

    private Calendar getMonday(Calendar date) {
        Calendar cal = (Calendar) date.clone();
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int diff = dayOfWeek == Calendar.SUNDAY ? -6 : Calendar.MONDAY - dayOfWeek;
        cal.add(Calendar.DAY_OF_MONTH, diff);
        return cal;
    }

    private boolean isSameDay(Calendar c1, Calendar c2) {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }

    // --- Day View ---

    private void showDayView() {
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
                halfLine.setBackgroundColor(0x1A000000);
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
        timeText.setText(task.getStartTime() + " - " + task.getEndTime());
        timeText.setTextSize(11);
        timeText.setTextColor(ContextCompat.getColor(this, R.color.muted_foreground));
        timeText.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        TextView complexityText = new TextView(this);
        complexityText.setText("Сложность: " + task.getComplexity() + "/10");
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

    private void showWeekView() {
        weekScheduleContainer.removeAllViews();

        Calendar today = Calendar.getInstance();
        Calendar monday = getMonday(selectedDate);

        for (int i = 0; i < 7; i++) {
            Calendar day = (Calendar) monday.clone();
            day.add(Calendar.DAY_OF_MONTH, i);

            View weekDayCard = createWeekDayCard(day, i, today);
            weekScheduleContainer.addView(weekDayCard);
        }
    }

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
            card.setCardBackgroundColor(0x0A030213);
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
            todayBadge.setText("Сегодня");
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
            emptyText.setText("Нет задач");
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
        if (complexity <= 20) { color = 0xFF22C55E; label = "Легкий день"; }
        else if (complexity <= 40) { color = 0xFF3B82F6; label = "Умеренный"; }
        else if (complexity <= 60) { color = 0xFFEAB308; label = "Напряженный"; }
        else if (complexity <= 80) { color = 0xFFF97316; label = "Сложный день"; }
        else { color = 0xFFEF4444; label = "Очень сложный"; }

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
        timeText.setText(task.getStartTime() + " - " + task.getEndTime());
        timeText.setTextSize(11);
        timeText.setTextColor(ContextCompat.getColor(this, R.color.muted_foreground));
        timeText.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        TextView compText = new TextView(this);
        compText.setText(task.getComplexity() + "/10");
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

    private void updateComplexityBadge() {
        if (!"day".equals(currentView)) {
            complexityBadge.setVisibility(View.GONE);
            return;
        }
        complexityBadge.setVisibility(View.VISIBLE);

        int totalComplexity = 0;
        for (Task t : allTasks) {
            if (isTaskOnDate(t, selectedDate)) {
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
        bg.setColor(lightenColor(color));
        bg.setStroke((int) (getResources().getDisplayMetrics().density), color);
        complexityBadge.setBackground(bg);
    }

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
        int sum = 0;
        for (Task task : getTasksForDay(day)) {
            sum += task.getComplexity();
        }
        return sum;
    }

    private void deleteWithConfirmation(Task task) {
        String type = task.getRecurrenceType();
        boolean isRecurring = type != null && !"once".equals(type);

        if (!isRecurring) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Удалить задачу")
                    .setMessage("Удалить \"" + task.getTitle() + "\"?")
                    .setPositiveButton("Удалить", (dialog, which) -> {
                        taskRepository.deleteById(task.getId(), result -> {});
                        allTasks.remove(task);
                        refreshCurrentView();
                    })
                    .setNegativeButton("Отмена", null)
                    .show();
            return;
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Удалить повторяющуюся задачу")
                .setMessage("Удалить \"" + task.getTitle() + "\"?")
                .setPositiveButton("Только это", (dialog, which) -> {
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
                .setNeutralButton("Все", (dialog, which) -> {
                    taskRepository.deleteById(task.getId(), result -> {});
                    allTasks.remove(task);
                    refreshCurrentView();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void refreshCurrentView() {
        if ("day".equals(currentView)) showDayView();
        else showWeekView();
        updateComplexityBadge();
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

    private class DaySwipeListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float diffX = e2.getX() - e1.getX();
            float diffY = e2.getY() - e1.getY();
            if (Math.abs(diffX) > Math.abs(diffY)
                    && Math.abs(diffX) > SWIPE_THRESHOLD
                    && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffX > 0) goToPreviousDay();
                else goToNextDay();
                return true;
            }
            return false;
        }
    }

    private class WeekSwipeListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 50;
        private static final int SWIPE_VELOCITY_THRESHOLD = 50;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float diffX = e2.getX() - e1.getX();
            float diffY = e2.getY() - e1.getY();
            if (Math.abs(diffX) > Math.abs(diffY)
                    && Math.abs(diffX) > SWIPE_THRESHOLD
                    && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffX > 0) goToPreviousWeek();
                else goToNextWeek();
                return true;
            }
            return false;
        }
    }

}
