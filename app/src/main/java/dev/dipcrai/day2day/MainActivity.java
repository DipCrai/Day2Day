package dev.dipcrai.day2day;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int HOUR_HEIGHT_DP = 80;

    private List<Task> mockTasks;
    private String currentView = "day";
    private Calendar selectedDate = Calendar.getInstance();
    private Calendar previousMonthSelection = null;

    private MaterialButtonToggleGroup viewToggle;
    private ViewFlipper viewFlipper;
    private LinearLayout weekDaysContainer;
    private LinearLayout complexityBadge;
    private TextView complexityLabel;
    private TextView complexityValue;
    private LinearLayout dayScheduleContainer;
    private LinearLayout weekScheduleContainer;
    private TextView monthTitle;
    private LinearLayout monthDayNames;
    private GridLayout monthGrid;
    private ScrollView dayScrollView;

    private final String[] dayNames = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};
    private final String[] monthNames = {
            "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
            "Июль", "Август", "Сентябрь", "Окторябрь", "Ноябрь", "Декабрь"
    };
    private final java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        initMockTasks();
        initViews();
        setupViewToggle();
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
                if ("day".equals(currentView) && previousMonthSelection != null) {
                    switchToMonth();
                } else {
                    finish();
                }
            }
        });
    }

    private void initMockTasks() {
        mockTasks = new ArrayList<>();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        String today = sdf.format(cal.getTime());
        String[] times = {"09:00", "09:30", "10:00", "12:00", "13:00", "14:00", "14:00", "15:00", "15:30", "16:30", "17:00", "18:00"};
        mockTasks.add(new Task("1", "Утренняя планерка", "Обсуждение задач на день с командой", today, "09:00", "09:30", 0xFF3B82F6, 2));
        mockTasks.add(new Task("2", "Разработка функционала", "Работа над новым интерфейсом планировщика", today, "10:00", "12:00", 0xFF8B5CF6, 8));
        mockTasks.add(new Task("3", "Обед", "Перерыв на обед", today, "13:00", "14:00", 0xFF22C55E, 1));
        mockTasks.add(new Task("4", "Код ревью", "Проверка pull requests от коллег", today, "14:00", "15:00", 0xFFF59E0B, 5));
        mockTasks.add(new Task("5", "Встреча с заказчиком", "Презентация прототипа нового функционала", today, "15:30", "16:30", 0xFFEC4899, 7));
        mockTasks.add(new Task("6", "Документация", "Обновление технической документации проекта", today, "17:00", "18:00", 0xFF06B6D4, 4));
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
        monthTitle = findViewById(R.id.monthTitle);
        monthDayNames = findViewById(R.id.monthDayNames);
        monthGrid = findViewById(R.id.monthGrid);
        dayScrollView = findViewById(R.id.dayView);

        findViewById(R.id.fabAddTask).setOnClickListener(v -> {
            AddTaskDialogFragment dialog = new AddTaskDialogFragment();
            dialog.setSelectedDate(dateToString(selectedDate));
            dialog.setOnTaskCreatedListener(task -> {
                mockTasks.add(task);
                if ("day".equals(currentView)) showDayView();
                else if ("week".equals(currentView)) showWeekView();
                else showMonthView();
                updateComplexityBadge();
            });
            dialog.show(getSupportFragmentManager(), "AddTask");
        });
    }

    private void setupViewToggle() {
        viewToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == R.id.toggleDay) switchToDay();
            else if (checkedId == R.id.toggleWeek) switchToWeek();
            else if (checkedId == R.id.toggleMonth) switchToMonth();
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

    private void switchToMonth() {
        currentView = "month";
        viewFlipper.setDisplayedChild(2);
        complexityBadge.setVisibility(View.GONE);
        showMonthView();
    }

    private void populateWeekDays() {
        weekDaysContainer.removeAllViews();

        Calendar today = Calendar.getInstance();
        Calendar monday = getMonday(today);

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

        for (int i = 0; i < 24; i++) {
            View hourLine = new View(this);
            FrameLayout.LayoutParams lineParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 1);
            lineParams.topMargin = i * hourHeightPx;
            hourLine.setLayoutParams(lineParams);
            hourLine.setBackgroundColor(ContextCompat.getColor(this, R.color.border));
            timelineContainer.addView(hourLine);

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

        String selectedDateStr = dateToString(selectedDate);
        List<Task> dayTasks = new ArrayList<>();
        for (Task t : mockTasks) {
            if (t.getDate() != null && t.getDate().equals(selectedDateStr)) {
                dayTasks.add(t);
            }
        }
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
            taskParams.leftMargin = (int) (8 * density);
            taskParams.rightMargin = (int) (8 * density);
            taskCard.setLayoutParams(taskParams);
            timelineContainer.addView(taskCard);
        }

        Calendar now = Calendar.getInstance();
        int nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
        int currentTopPx = (int) ((nowMinutes / 60f) * hourHeightPx);

        View timeIndicator = new View(this);
        FrameLayout.LayoutParams timeParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 3);
        timeParams.topMargin = currentTopPx;
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
        Calendar monday = getMonday(today);

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

        int dayComplexity = calculateDayComplexity(dayIndex);
        if (dayComplexity > 0) {
            View complexityBadgeView = createMiniComplexityBadge(dayComplexity);
            complexityBadgeView.setPadding(0, (int) (8 * density), 0, 0);
            layout.addView(complexityBadgeView);
        }

        List<Task> dayTasks = getTasksForDay(dayIndex);
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
        return card;
    }

    // --- Month View ---

    private void showMonthView() {
        int year = selectedDate.get(Calendar.YEAR);
        int month = selectedDate.get(Calendar.MONTH);

        monthTitle.setText(monthNames[month] + " " + year);

        monthDayNames.removeAllViews();
        for (String name : dayNames) {
            TextView tv = new TextView(this);
            tv.setText(name);
            tv.setTextSize(12);
            tv.setTextColor(ContextCompat.getColor(this, R.color.muted_foreground));
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(0, 8, 0, 8);
            monthDayNames.addView(tv, new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        }

        monthGrid.removeAllViews();

        Calendar firstDay = Calendar.getInstance();
        firstDay.set(year, month, 1);

        int daysInMonth = firstDay.getActualMaximum(Calendar.DAY_OF_MONTH);

        int firstDayOfWeek = firstDay.get(Calendar.DAY_OF_WEEK);
        firstDayOfWeek = firstDayOfWeek == Calendar.SUNDAY ? 6 : firstDayOfWeek - 1;

        Calendar prevMonth = Calendar.getInstance();
        prevMonth.set(year, month - 1, 1);
        int daysInPrevMonth = prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH);

        Calendar today = Calendar.getInstance();

        float density = getResources().getDisplayMetrics().density;
        int cellHeight = (int) (100 * density);

        for (int i = firstDayOfWeek - 1; i >= 0; i--) {
            int dayNum = daysInPrevMonth - i;
            Calendar cellDate = Calendar.getInstance();
            cellDate.set(year, month - 1, dayNum);
            monthGrid.addView(createMonthCell(dayNum, false, getTasksForMonthDay(cellDate), today, cellDate, density),
                    new GridLayout.LayoutParams(GridLayout.spec(GridLayout.UNDEFINED, 1f),
                            GridLayout.spec(GridLayout.UNDEFINED, 1f)));
            monthGrid.getChildAt(monthGrid.getChildCount() - 1).getLayoutParams().height = cellHeight;
        }

        for (int i = 1; i <= daysInMonth; i++) {
            Calendar cellDate = Calendar.getInstance();
            cellDate.set(year, month, i);
            monthGrid.addView(createMonthCell(i, true, getTasksForMonthDay(cellDate), today, cellDate, density),
                    new GridLayout.LayoutParams(GridLayout.spec(GridLayout.UNDEFINED, 1f),
                            GridLayout.spec(GridLayout.UNDEFINED, 1f)));
            monthGrid.getChildAt(monthGrid.getChildCount() - 1).getLayoutParams().height = cellHeight;
        }

        int totalCells = monthGrid.getChildCount();
        int remaining = 42 - totalCells;
        for (int i = 1; i <= remaining && i <= 42; i++) {
            Calendar cellDate = Calendar.getInstance();
            cellDate.set(year, month + 1, i);
            monthGrid.addView(createMonthCell(i, false, getTasksForMonthDay(cellDate), today, cellDate, density),
                    new GridLayout.LayoutParams(GridLayout.spec(GridLayout.UNDEFINED, 1f),
                            GridLayout.spec(GridLayout.UNDEFINED, 1f)));
            monthGrid.getChildAt(monthGrid.getChildCount() - 1).getLayoutParams().height = cellHeight;
        }
    }

    private View createMonthCell(int dayNum, boolean isCurrentMonth, List<Task> dayTasks,
                                 Calendar today, Calendar cellDate, float density) {
        boolean isToday = isSameDay(cellDate, today);

        FrameLayout cell = new FrameLayout(this);
        cell.setPadding((int) (4 * density), (int) (4 * density),
                (int) (4 * density), (int) (4 * density));

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(8 * density);

        if (isToday) {
            bg.setColor(ContextCompat.getColor(this, R.color.primary));
        } else if (isCurrentMonth) {
            bg.setColor(Color.WHITE);
        } else {
            bg.setColor(0x0A000000);
        }

        int dayComplexity = 0;
        for (Task t : dayTasks) dayComplexity += t.getComplexity();
        String complexityColor = getComplexityHexColor(dayComplexity);
        if (!isToday && dayComplexity > 0) {
            bg.setStroke((int) (2 * density), Color.parseColor(complexityColor));
        }
        cell.setBackground(bg);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);

        TextView dayText = new TextView(this);
        dayText.setText(String.valueOf(dayNum));
        dayText.setTextSize(12);
        dayText.setTextColor(isToday ? ContextCompat.getColor(this, R.color.today_fg)
                : isCurrentMonth ? ContextCompat.getColor(this, R.color.primary)
                : ContextCompat.getColor(this, R.color.muted_foreground));
        if (isToday) dayText.setTypeface(null, android.graphics.Typeface.BOLD);
        content.addView(dayText);

        if (dayTasks.size() > 0 && !isToday) {
            int showCount = Math.min(dayTasks.size(), 3);
            for (int i = 0; i < showCount; i++) {
                View bar = new View(this);
                bar.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, (int) (4 * density)));
                ((ViewGroup.MarginLayoutParams) bar.getLayoutParams()).setMargins(0, (int) (2 * density), 0, 0);
                bar.setBackgroundColor(dayTasks.get(i).getColor());
                content.addView(bar);
            }
        }

        cell.addView(content);
        cell.setOnClickListener(v -> {
            if (currentView.equals("month")) {
                selectedDate = cellDate;
                previousMonthSelection = cellDate;
                switchToDay();
            }
        });

        return cell;
    }

    private String getComplexityHexColor(int complexity) {
        if (complexity == 0) return "#D1D5DB";
        if (complexity <= 20) return "#22C55E";
        if (complexity <= 40) return "#3B82F6";
        if (complexity <= 60) return "#EAB308";
        if (complexity <= 80) return "#F97316";
        return "#EF4444";
    }

    private List<Task> getTasksForMonthDay(Calendar date) {
        String targetDate = dateToString(date);
        List<Task> result = new ArrayList<>();
        for (Task task : mockTasks) {
            String taskDate = task.getDate();
            if (taskDate != null && taskDate.equals(targetDate)) {
                result.add(task);
            }
        }
        result.sort((a, b) -> {
            int aMin = timeToMinutes(a.getStartTime());
            int bMin = timeToMinutes(b.getStartTime());
            return Integer.compare(aMin, bMin);
        });
        return result;
    }

    // --- Helpers ---

    private void updateComplexityBadge() {
        if (!"day".equals(currentView)) {
            complexityBadge.setVisibility(View.GONE);
            return;
        }
        complexityBadge.setVisibility(View.VISIBLE);

        String selectedDateStr = dateToString(selectedDate);
        int totalComplexity = 0;
        for (Task t : mockTasks) {
            if (t.getDate() != null && t.getDate().equals(selectedDateStr)) {
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

    private List<Task> getTasksForDay(int dayIndex) {
        List<Task> result = new ArrayList<>();
        Calendar cal = getMonday(Calendar.getInstance());
        cal.add(Calendar.DAY_OF_MONTH, dayIndex);
        String targetDate = dateToString(cal);
        for (Task task : mockTasks) {
            String taskDate = task.getDate();
            if (taskDate == null) continue;
            if (taskDate.equals(targetDate)) {
                result.add(task);
            }
        }
        result.sort((a, b) -> Integer.compare(timeToMinutes(a.getStartTime()), timeToMinutes(b.getStartTime())));
        return result;
    }

    private int calculateDayComplexity(int dayIndex) {
        int sum = 0;
        for (Task task : getTasksForDay(dayIndex)) {
            sum += task.getComplexity();
        }
        return sum;
    }

}
