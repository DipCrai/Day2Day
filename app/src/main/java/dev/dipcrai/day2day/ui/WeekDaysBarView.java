package dev.dipcrai.day2day.ui;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import dev.dipcrai.day2day.R;
import dev.dipcrai.day2day.util.TaskDateUtils;

public class WeekDaysBarView {

    private final Context context;
    private final String[] dayNames;

    public WeekDaysBarView(Context context, String[] dayNames) {
        this.context = context;
        this.dayNames = dayNames;
    }

    public void render(LinearLayout container, Calendar selectedDate,
                       Runnable onDaySelectedListener) {
        container.removeAllViews();

        Calendar today = Calendar.getInstance();
        Calendar monday = TaskDateUtils.getMonday(selectedDate);

        for (int i = 0; i < 7; i++) {
            Calendar day = (Calendar) monday.clone();
            day.add(Calendar.DAY_OF_MONTH, i);

            View dayView = View.inflate(context, R.layout.item_week_day, container);
            TextView dayName = dayView.findViewById(R.id.dayName);
            TextView dayNumber = dayView.findViewById(R.id.dayNumber);

            dayName.setText(dayNames[i]);
            dayNumber.setText(String.valueOf(day.get(Calendar.DAY_OF_MONTH)));

            boolean isToday = TaskDateUtils.isSameDay(day, today);
            boolean isSelected = TaskDateUtils.isSameDay(day, selectedDate);

            if (isToday) {
                dayView.setBackgroundResource(R.drawable.today_bg);
                dayNumber.setTextColor(ContextCompat.getColor(context, R.color.today_fg));
                dayName.setTextColor(ContextCompat.getColor(context, R.color.today_fg));
            } else if (isSelected) {
                dayView.setSelected(true);
                dayNumber.setTextColor(ContextCompat.getColor(context, R.color.primary));
                dayName.setTextColor(ContextCompat.getColor(context, R.color.muted_foreground));
            } else {
                dayView.setSelected(false);
                dayNumber.setTextColor(ContextCompat.getColor(context, R.color.primary));
                dayName.setTextColor(ContextCompat.getColor(context, R.color.muted_foreground));
            }

            final Calendar selectedDay = day;
            dayView.setOnClickListener(v -> {
                selectedDate.setTimeInMillis(selectedDay.getTimeInMillis());
                onDaySelectedListener.run();
            });

            container.addView(dayView);
        }
    }
}
