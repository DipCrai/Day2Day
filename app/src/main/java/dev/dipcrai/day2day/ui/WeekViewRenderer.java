package dev.dipcrai.day2day.ui;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import dev.dipcrai.day2day.R;
import dev.dipcrai.day2day.Task;
import dev.dipcrai.day2day.util.TaskDateUtils;

public class WeekViewRenderer {

    private java.util.function.Consumer<Task> onTaskLongClick;

    private final Context context;
    private final String[] dayNames;
    private final SimpleDateFormat dateFormat;

    public WeekViewRenderer(Context context, String[] dayNames, SimpleDateFormat dateFormat) {
        this.context = context;
        this.dayNames = dayNames;
        this.dateFormat = dateFormat;
    }

    public void setOnTaskLongClickListener(java.util.function.Consumer<Task> listener) {
        this.onTaskLongClick = listener;
    }

    public void render(LinearLayout container, List<Task> allTasks, Calendar selectedDate) {
        container.removeAllViews();

        Calendar today = Calendar.getInstance();
        Calendar monday = TaskDateUtils.getMonday(selectedDate);

        for (int i = 0; i < 7; i++) {
            Calendar day = (Calendar) monday.clone();
            day.add(Calendar.DAY_OF_MONTH, i);
            container.addView(createWeekDayCard(allTasks, day, i, today));
        }
    }

    private View createWeekDayCard(List<Task> allTasks, Calendar day, int dayIndex, Calendar today) {
        float density = context.getResources().getDisplayMetrics().density;
        boolean isToday = TaskDateUtils.isSameDay(day, today);

        com.google.android.material.card.MaterialCardView card = new com.google.android.material.card.MaterialCardView(context);
        card.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ((ViewGroup.MarginLayoutParams) card.getLayoutParams()).setMargins(0, 0, 0, (int) (12 * density));
        card.setCardElevation(2 * density);
        card.setRadius(12 * density);
        card.setUseCompatPadding(true);
        card.setCardBackgroundColor(android.graphics.Color.WHITE);

        if (isToday) {
            card.setCardBackgroundColor(0x0A030213);
            card.setStrokeWidth((int) (2 * density));
            card.setStrokeColor(ContextCompat.getColor(context, R.color.primary));
        }

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding((int) (16 * density), (int) (12 * density),
                (int) (16 * density), (int) (12 * density));

        LinearLayout headerRow = new LinearLayout(context);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        headerRow.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout dateInfo = new LinearLayout(context);
        dateInfo.setOrientation(LinearLayout.VERTICAL);
        dateInfo.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        TextView dayNameText = new TextView(context);
        dayNameText.setText(dayNames[dayIndex]);
        dayNameText.setTextSize(12);
        dayNameText.setTextColor(ContextCompat.getColor(context, R.color.muted_foreground));

        java.text.SimpleDateFormat monthFormat = new java.text.SimpleDateFormat("MMMM", new Locale("ru"));
        TextView dateText = new TextView(context);
        dateText.setText(day.get(Calendar.DAY_OF_MONTH) + " " + monthFormat.format(day.getTime()));
        dateText.setTextSize(16);
        dateText.setTypeface(null, android.graphics.Typeface.BOLD);
        dateText.setTextColor(ContextCompat.getColor(context, R.color.primary));

        dateInfo.addView(dayNameText);
        dateInfo.addView(dateText);
        headerRow.addView(dateInfo);

        if (isToday) {
            TextView todayBadge = new TextView(context);
            todayBadge.setText("Сегодня");
            todayBadge.setTextSize(11);
            todayBadge.setTextColor(ContextCompat.getColor(context, R.color.today_fg));
            todayBadge.setPadding((int) (8 * density), (int) (4 * density),
                    (int) (8 * density), (int) (4 * density));
            todayBadge.setBackgroundResource(R.drawable.today_bg);
            headerRow.addView(todayBadge);
        }

        layout.addView(headerRow);

        int dayComplexity = TaskDateUtils.calculateDayComplexity(allTasks, day, dateFormat);
        if (dayComplexity > 0) {
            layout.addView(createMiniComplexityBadge(dayComplexity, density));
        }

        List<Task> dayTasks = TaskDateUtils.getTasksForDay(allTasks, day, dateFormat);
        if (!dayTasks.isEmpty()) {
            layout.addView(createTasksDivider(density));
            for (Task task : dayTasks) {
                layout.addView(createMiniTaskCard(task, density));
            }
        } else {
            TextView emptyText = new TextView(context);
            emptyText.setText("Нет задач");
            emptyText.setTextSize(13);
            emptyText.setTextColor(ContextCompat.getColor(context, R.color.muted_foreground));
            emptyText.setGravity(Gravity.CENTER);
            emptyText.setPadding(0, (int) (24 * density), 0, (int) (24 * density));
            layout.addView(emptyText);
        }

        card.addView(layout);
        return card;
    }

    private View createMiniComplexityBadge(int complexity, float density) {
        int color;
        String label;
        if (complexity <= 20) { color = 0xFF22C55E; label = "Легкий день"; }
        else if (complexity <= 40) { color = 0xFF3B82F6; label = "Умеренный"; }
        else if (complexity <= 60) { color = 0xFFEAB308; label = "Напряженный"; }
        else if (complexity <= 80) { color = 0xFFF97316; label = "Сложный день"; }
        else { color = 0xFFEF4444; label = "Очень сложный"; }

        LinearLayout badge = new LinearLayout(context);
        badge.setOrientation(LinearLayout.HORIZONTAL);
        badge.setGravity(Gravity.CENTER_VERTICAL);
        badge.setPadding((int) (8 * density), (int) (4 * density),
                (int) (8 * density), (int) (4 * density));

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(8 * density);
        bg.setColor(TaskDateUtils.lightenColor(color));
        bg.setStroke((int) (1 * density), color);
        badge.setBackground(bg);

        TextView labelText = new TextView(context);
        labelText.setText(label);
        labelText.setTextSize(11);
        labelText.setTypeface(null, android.graphics.Typeface.BOLD);
        labelText.setTextColor(color);
        badge.addView(labelText);

        TextView valueText = new TextView(context);
        valueText.setText(String.valueOf(complexity));
        valueText.setTextSize(11);
        valueText.setTextColor(color);
        valueText.setPadding((int) (6 * density), 0, 0, 0);
        badge.addView(valueText);

        return badge;
    }

    private View createTasksDivider(float density) {
        View divider = new View(context);
        divider.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 1));
        ((ViewGroup.MarginLayoutParams) divider.getLayoutParams()).setMargins(0, (int) (8 * density), 0, (int) (8 * density));
        divider.setBackgroundColor(ContextCompat.getColor(context, R.color.border));
        return divider;
    }

    private View createMiniTaskCard(Task task, float density) {
        CardView card = new CardView(context);
        card.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ((ViewGroup.MarginLayoutParams) card.getLayoutParams()).setMargins(0, 0, 0, (int) (8 * density));
        card.setCardElevation(1 * density);
        card.setRadius(8 * density);
        card.setUseCompatPadding(true);
        card.setContentPadding((int) (12 * density), (int) (10 * density),
                (int) (12 * density), (int) (10 * density));
        card.setCardBackgroundColor(android.graphics.Color.WHITE);

        GradientDrawable border = new GradientDrawable();
        border.setShape(GradientDrawable.RECTANGLE);
        border.setCornerRadius(8 * density);
        border.setStroke((int) (3 * density), task.getColor());
        border.setColor(android.graphics.Color.WHITE);
        card.setBackground(border);

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout topRow = new LinearLayout(context);
        topRow.setOrientation(LinearLayout.HORIZONTAL);

        TextView timeText = new TextView(context);
        timeText.setText(task.getStartTime() + " - " + task.getEndTime());
        timeText.setTextSize(11);
        timeText.setTextColor(ContextCompat.getColor(context, R.color.muted_foreground));
        timeText.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        TextView compText = new TextView(context);
        compText.setText(task.getComplexity() + "/10");
        compText.setTextSize(11);
        compText.setTextColor(ContextCompat.getColor(context, R.color.muted_foreground));

        topRow.addView(timeText);
        topRow.addView(compText);
        layout.addView(topRow);

        TextView titleText = new TextView(context);
        titleText.setText(task.getTitle());
        titleText.setTextSize(13);
        titleText.setTypeface(null, android.graphics.Typeface.BOLD);
        titleText.setTextColor(ContextCompat.getColor(context, R.color.primary));
        titleText.setPadding(0, (int) (2 * density), 0, 0);
        layout.addView(titleText);

        TextView descText = new TextView(context);
        descText.setText(task.getDescription());
        descText.setTextSize(11);
        descText.setMaxLines(2);
        descText.setTextColor(ContextCompat.getColor(context, R.color.muted_foreground));
        layout.addView(descText);

        card.addView(layout);

        if (onTaskLongClick != null) {
            card.setOnLongClickListener(v -> {
                onTaskLongClick.accept(task);
                return true;
            });
        }

        return card;
    }
}
