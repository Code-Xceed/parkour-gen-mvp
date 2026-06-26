package dev.codexceed.parkour.prng;

/**
 * Deterministic xoroshiro128++ PRNG.
 * Same seed -> identical stream on every JVM/platform. No platform RNG quirks.
 */
public final class DeterministicRng {
    private long s0, s1;

    public DeterministicRng(long seed) {
        long z = seed + 0x9E3779B97F4A7C15L;
        this.s0 = mix(z);
        z = (seed ^ 0xD1B54A32D192ED03L) + 0x9E3779B97F4A7C15L;
        this.s1 = mix(z);
        if (s0 == 0 && s1 == 0) s1 = 1;
    }

    private static long mix(long z) {
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    public long nextLong() {
        long x = s0, y = s1;
        long result = Long.rotateLeft(x + y, 17) + x;
        y ^= x;
        s0 = Long.rotateLeft(x, 49) ^ y ^ (y << 21);
        s1 = Long.rotateLeft(y, 28);
        return result;
    }

    public int nextInt(int bound) {
        if (bound <= 0) throw new IllegalArgumentException("bound must be > 0");
        long r = nextLong() >>> 1;
        return (int) (r % bound);
    }

    public double nextDouble() {
        return (nextLong() >>> 11) * 0x1.0p-53;
    }

    public boolean nextBoolean() {
        return (nextLong() & 1L) != 0;
    }
}
