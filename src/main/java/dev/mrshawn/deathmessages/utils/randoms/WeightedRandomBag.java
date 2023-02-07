package dev.mrshawn.deathmessages.utils.randoms;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class WeightedRandomBag<T> {
    private double accumulatedWeight;
    @NotNull
    private final List<WeightedRandomBag<T>.Entry> entries = new ArrayList<>();
    @NotNull
    private final Random rand = new Random();

    private final class Entry {
        @Nullable
        private final T entryObject;
        private double accumulatedWeight;

        public Entry(@Nullable T t, double accumulatedWeight) {
            this.entryObject = t;
            this.accumulatedWeight = accumulatedWeight;
        }

        @Nullable
        public T getEntryObject() {
            return this.entryObject;
        }

        public double getAccumulatedWeight() {
            return this.accumulatedWeight;
        }

        public void setAccumulatedWeight(double d) {
            this.accumulatedWeight = d;
        }
    }

    public void addEntry(T t, double weight) {
        this.accumulatedWeight += weight;
        this.entries.add(new Entry(t, this.accumulatedWeight));
    }

    @Nullable
    public T getRandom() {
        double randomWeight = this.rand.nextDouble() * this.accumulatedWeight;
        for (Entry entry : this.entries) {
            if (entry.getAccumulatedWeight() >= randomWeight) {
                return entry.getEntryObject();
            }
        }
        return null;
    }
}
