package dev.dipcrai.day2day.ui;

import android.view.GestureDetector;
import android.view.MotionEvent;

public class SwipeListeners {

    public static class DaySwipeListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;
        private final Runnable onSwipeLeft;
        private final Runnable onSwipeRight;

        public DaySwipeListener(Runnable onSwipeLeft, Runnable onSwipeRight) {
            this.onSwipeLeft = onSwipeLeft;
            this.onSwipeRight = onSwipeRight;
        }

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
                if (diffX > 0) onSwipeRight.run();
                else onSwipeLeft.run();
                return true;
            }
            return false;
        }
    }

    public static class WeekSwipeListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;
        private final Runnable onSwipeLeft;
        private final Runnable onSwipeRight;

        public WeekSwipeListener(Runnable onSwipeLeft, Runnable onSwipeRight) {
            this.onSwipeLeft = onSwipeLeft;
            this.onSwipeRight = onSwipeRight;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (e1 == null || e2 == null) return false;
            float diffX = e2.getX() - e1.getX();
            float diffY = e2.getY() - e1.getY();
            if (Math.abs(diffX) > Math.abs(diffY)
                    && Math.abs(diffX) > SWIPE_THRESHOLD
                    && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffX > 0) onSwipeRight.run();
                else onSwipeLeft.run();
                return true;
            }
            return false;
        }
    }
}
