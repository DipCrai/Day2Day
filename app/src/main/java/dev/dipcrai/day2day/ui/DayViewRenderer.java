package dev.dipcrai.day2day.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import dev.dipcrai.day2day.R;
import dev.dipcrai.day2day.Task;
import dev.dipcrai.day2day.util.TaskDateUtils;

public class DayViewRenderer {

    private static final int HOUR_HEIGHT_DP = 80;
    private Consumer<Task> onTaskLongClick;

    private final Context context;
    private final float density;
    private final SimpleDateFormat dateFormat;

    public DayViewRenderer(Context context, SimpleDateFormat dateFormat) {
        this.context = context;
        this.density = context.getResources().getDisplayMetrics().density;
        this.dateFormat = dateFormat;
    }

    public void setOnTaskLongClickListener(Consumer<Task> listener) {
        this.onTaskLongClick = listener;
    }

    public void render(LinearLayout container, ScrollView scrollView,
                       List<Task> allTasks, Calendar selectedDate) {
        container.removeAllViews();

        Calendar now = Calendar.getInstance();
        int hourHeightPx = (int) (HOUR_HEIGHT_DP * density);
        int labelWidthPx = (int) (48 * density);

        FrameLayout timelineContainer = new FrameLayout(context);
        timelineContainer.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, hourHeightPx * 24));

        for (int i = 0; i < 24; i++) {
            View hourLine = new View(context);
            FrameLayout.LayoutParams lineParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 1);
            lineParams.topMargin = i * hourHeightPx;
            hourLine.setLayoutParams(lineParams);
            hourLine.setBackgroundColor(ContextCompat.getColor(context, R.color.border));
            timelineContainer.addView(hourLine);

            TextView hourLabel = new TextView(context);
            hourLabel.setText(String.format(Locale.getDefault(), "%02d:00", i));
            hourLabel.setTextSize(10);
            hourLabel.setTextColor(ContextCompat.getColor(context, R.color.muted_foreground));
            FrameLayout.LayoutParams labelParams = new FrameLayout.LayoutParams(
                    labelWidthPx, ViewGroup.LayoutParams.WRAP_CONTENT);
            labelParams.topMargin = i * hourHeightPx - (int) (6 * density);
            labelParams.leftMargin = (int) (4 * density);
            hourLabel.setLayoutParams(labelParams);
            timelineContainer.addView(hourLabel);

            if (i < 23) {
                View halfLine = new View(context);
                FrameLayout.LayoutParams halfParams = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 1);
                halfParams.topMargin = i * hourHeightPx + hourHeightPx / 2;
                halfLine.setLayoutParams(halfParams);
                halfLine.setBackgroundColor(0x1A000000);
                timelineContainer.addView(halfLine);
            }
        }

        List<Task> dayTasks = TaskDateUtils.getTasksForDate(allTasks, selectedDate, dateFormat);
        for (Task task : dayTasks) {
            int startMinutes = TaskDateUtils.timeToMinutes(task.getStartTime());
            int endMinutes = TaskDateUtils.timeToMinutes(task.getEndTime());
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

        int nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
        int currentTopPx = (int) ((nowMinutes / 60f) * hourHeightPx);

        int timeIndentPx = (int) (52 * density);
        View timeIndicator = new View(context);
        FrameLayout.LayoutParams timeParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 3);
        timeParams.topMargin = currentTopPx;
        timeParams.leftMargin = timeIndentPx;
        timeIndicator.setLayoutParams(timeParams);
        timeIndicator.setBackgroundColor(ContextCompat.getColor(context, R.color.destructive));
        timelineContainer.addView(timeIndicator);

        View timeDot = new View(context);
        int dotSize = (int) (12 * density);
        FrameLayout.LayoutParams dotParams = new FrameLayout.LayoutParams(dotSize, dotSize);
        dotParams.topMargin = currentTopPx - dotSize / 2;
        dotParams.leftMargin = -(int) (density);
        timeDot.setLayoutParams(dotParams);
        timeDot.setBackgroundResource(R.drawable.time_dot);
        timelineContainer.addView(timeDot);

        container.addView(timelineContainer);

        int scrollToHour = Math.max(0, now.get(Calendar.HOUR_OF_DAY) - 2);
        scrollView.post(() -> scrollView.scrollTo(0, scrollToHour * hourHeightPx));
    }

    private View createTaskCardView(Task task, int heightPx) {
        CardView card = new CardView(context);
        card.setCardElevation(2 * density);
        card.setRadius(8 * density);
        card.setContentPadding((int) (10 * density), (int) (8 * density),
                (int) (10 * density), (int) (8 * density));
        card.setCardBackgroundColor(Color.WHITE);
        card.setUseCompatPadding(true);

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout topRow = new LinearLayout(context);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView timeText = new TextView(context);
        timeText.setText(task.getStartTime() + " - " + task.getEndTime());
        timeText.setTextSize(11);
        timeText.setTextColor(ContextCompat.getColor(context, R.color.muted_foreground));
        timeText.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        TextView complexityText = new TextView(context);
        complexityText.setText(context.getString(R.string.complexity_label, task.getComplexity()));
        complexityText.setTextSize(11);
        complexityText.setTextColor(ContextCompat.getColor(context, R.color.muted_foreground));

        topRow.addView(timeText);
        topRow.addView(complexityText);
        layout.addView(topRow);

        boolean isCompact = heightPx < 70 * density;

        if (!isCompact) {
            TextView titleText = new TextView(context);
            titleText.setText(task.getTitle());
            titleText.setTextSize(14);
            titleText.setTypeface(null, android.graphics.Typeface.BOLD);
            titleText.setTextColor(ContextCompat.getColor(context, R.color.primary));
            titleText.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            titleText.setPadding(0, (int) (4 * density), 0, 0);
            layout.addView(titleText);

            TextView descText = new TextView(context);
            descText.setText(task.getDescription());
            descText.setTextSize(12);
            descText.setMaxLines(2);
            descText.setTextColor(ContextCompat.getColor(context, R.color.muted_foreground));
            descText.setPadding(0, (int) (2 * density), 0, 0);
            layout.addView(descText);
        } else {
            TextView titleText = new TextView(context);
            titleText.setText(task.getTitle());
            titleText.setTextSize(13);
            titleText.setTypeface(null, android.graphics.Typeface.BOLD);
            titleText.setTextColor(ContextCompat.getColor(context, R.color.primary));
            titleText.setMaxLines(1);
            layout.addView(titleText);
        }

        card.addView(layout);

        if (onTaskLongClick != null) {
            card.setOnLongClickListener(v -> {
                onTaskLongClick.accept(task);
                return true;
            });
        }

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
        bg.setCornerRadius(8 * density);
        bg.setColor(TaskDateUtils.lightenColor(color));
        return bg;
    }
}
