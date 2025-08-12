package dev.dubhe.anvilcraft.api.amulet;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.function.Function;
import java.util.function.IntUnaryOperator;

/**
 * 存储了玩家目前护符抽取的额外概率
 *
 * @param map 存储概率，键为类型，值为该类型目前的概率
 */
public record AmuletRaffleProbability(Object2IntOpenHashMap<AmuletType> map) {
    public static final AmuletRaffleProbability EMPTY = new AmuletRaffleProbability(new Object2IntOpenHashMap<>());
    public static final Codec<AmuletRaffleProbability> CODEC = Codec.unboundedMap(AmuletType.CODEC, Codec.INT)
        .xmap(Object2IntOpenHashMap::new, Function.identity())
        .xmap(AmuletRaffleProbability::new, AmuletRaffleProbability::map);

    /**
     * 获取该类型在此处存储的概率。
     *
     * @param type 类型
     * @return 概率
     */
    public int getProbability(AmuletType type) {
        return this.map.getInt(type);
    }

    /**
     * 向此处存储该类型的概率。
     *
     * @param type        类型
     * @param probability 新概率
     * @return 旧概率
     */
    public int setProbability(AmuletType type, int probability) {
        return this.map.put(type, probability);
    }

    public int setProbability(AmuletType type, IntUnaryOperator probability) {
        return this.map.put(type, probability.applyAsInt(this.map.getInt(type)));
    }
}
