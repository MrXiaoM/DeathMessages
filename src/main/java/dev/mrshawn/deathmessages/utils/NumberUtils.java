package dev.mrshawn.deathmessages.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NumberUtils {
    @NotNull
    private static final String[] suffixes = {"th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th"};
    /*@NotNull
    private static final TreeMap<Integer, String> numeralMap = new TreeMap<>(mapOf(
            Pair.of(1000, "M"),
            Pair.of(900, "CM"),
            Pair.of(500, "D"),
            Pair.of(400, "CD"),
            Pair.of(100, "C"),
            Pair.of(90, "XC"),
            Pair.of(50, "L"),
            Pair.of(40, "XL"),
            Pair.of(10, "X"),
            Pair.of(9, "IX"),
            Pair.of(5, "V"),
            Pair.of(4, "IV"),
            Pair.of(1, "I")
    ));*/

    @NotNull
    public static String toOrdinal(int i) {
        switch (i % 100) {
            case 11:
            case 12:
            case 13:
                return i + "th";
            default:
                return i + suffixes[i % 10];
        }
    }

    @NotNull
    public static Time toTime(long originalMillis) {
        long millis = originalMillis % 1000;
        long second = (originalMillis / 1000) % 60;
        long minute = (originalMillis / 60000) % 60;
        long hour = (originalMillis / 3600000) % 24;
        return new Time(hour, minute, second, millis);
    }

    public static final class Time {
        private final long hour;
        private final long minute;
        private final long second;
        private final long millis;

        public long component1() {
            return this.hour;
        }

        public long component2() {
            return this.minute;
        }

        public long component3() {
            return this.second;
        }

        public long component4() {
            return this.millis;
        }

        @NotNull
        public Time copy(long hour, long minute, long second, long millis) {
            return new Time(hour, minute, second, millis);
        }

        public static Time copy$default(Time time, long j, long j2, long j3, long j4, int i, Object obj) {
            if ((i & 1) != 0) {
                j = time.hour;
            }
            if ((i & 2) != 0) {
                j2 = time.minute;
            }
            if ((i & 4) != 0) {
                j3 = time.second;
            }
            if ((i & 8) != 0) {
                j4 = time.millis;
            }
            return time.copy(j, j2, j3, j4);
        }

        @NotNull
        public String toString() {
            return "Time(hour=" + this.hour + ", minute=" + this.minute + ", second=" + this.second + ", millis=" + this.millis + ')';
        }

        public int hashCode() {
            int result = Long.hashCode(this.hour);
            return (((((result * 31) + Long.hashCode(this.minute)) * 31) + Long.hashCode(this.second)) * 31) + Long.hashCode(this.millis);
        }

        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (other instanceof Time) {
                Time time = (Time) other;
                return this.hour == time.hour && this.minute == time.minute && this.second == time.second && this.millis == time.millis;
            }
            return false;
        }

        public Time(long hour, long minute, long second, long millis) {
            this.hour = hour;
            this.minute = minute;
            this.second = second;
            this.millis = millis;
        }

        public long getHour() {
            return this.hour;
        }

        public long getMinute() {
            return this.minute;
        }

        public long getSecond() {
            return this.second;
        }

        public long getMillis() {
            return this.millis;
        }
    }
}
