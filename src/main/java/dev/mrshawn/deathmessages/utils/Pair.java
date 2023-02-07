package dev.mrshawn.deathmessages.utils;

import java.util.HashMap;
import java.util.Map;


public final class Pair<L, R> {
    private final L left;
    private final R right;

    public java.lang.String toString() {
        return "Pair(left=" + left + ", right=" + right + ")";
    }

    @SuppressWarnings({"rawtypes"})
    public boolean equals(java.lang.Object obj) {
        if (obj instanceof Pair) {
            Pair p = (Pair) obj;
            return ((p.left == null && left == null) || left.equals(p.left))
                    && ((p.right == null && right == null) || right.equals(p.right));
        }
        return false;
    }

    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public L left() {
        return this.left;
    }

    public R right() {
        return this.right;
    }

    public static <L, R> Pair<L, R> of(L left, R right) {
        return new Pair<>(left, right);
    }

    @SafeVarargs
    public static <K, V> Map<K, V> mapOf(Pair<K, V>... entries) {
        Map<K, V> map = new HashMap<>();
        for (Pair<K, V> entry : entries) {
            map.put(entry.left, entry.right);
        }
        return map;
    }
}
