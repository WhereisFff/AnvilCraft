package dev.dubhe.anvilcraft.api.heat;

import lombok.Getter;

import java.util.Optional;

public enum HeatTier implements Comparable<HeatTier> {
    NORMAL(0, 4, 0),
    HEATED(4, 12, 4),
    REDHOT(12, 32, 16),
    GLOWING(32, 80, 64),
    INCANDESCENT(80, Integer.MAX_VALUE, 128),
    ;

    public static final HeatTier[] TIERS = new HeatTier[] {NORMAL, HEATED, REDHOT, GLOWING, INCANDESCENT};

    @Getter
    private final int remainCount;
    @Getter
    private final int toNextCount;
    @Getter
    private final int powerProduce;

    HeatTier(int remainCount, int toNextCount, int powerProduce) {
        this.remainCount = remainCount;
        this.toNextCount = toNextCount;
        this.powerProduce = powerProduce;
    }

    public Optional<HeatTier> getPrevTier() {
        if (this.ordinal() - 1 < 0) return Optional.empty();
        return Optional.of(TIERS[this.ordinal() - 1]);
    }

    public Optional<HeatTier> getNextTier() {
        if (this.ordinal() + 1 >= TIERS.length) return Optional.empty();
        return Optional.of(TIERS[this.ordinal() + 1]);
    }

    public static HeatTier findTierByCount(int count) {
        for (HeatTier tier : TIERS) {
            if (count < tier.toNextCount) return tier;
        }
        throw new IllegalArgumentException("Invalid count " + count);
    }
}
