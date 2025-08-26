package dev.dubhe.anvilcraft.integration.ponder.api.instruction;

public interface Interpolation {
    double instantaneous(int time);

    double duration(double journey);

    /**
     * 创建一个线性插值器实例
     *
     * @param speed 线性运动的速度值
     * @return 返回一个Interpolation接口的实现对象，用于处理线性插值计算
     */
    static Interpolation linear(double speed) {
        // 创建并返回一个匿名内部类实例，实现Interpolation接口
        return new Interpolation() {
            @Override
            public double instantaneous(int time) {
                // 对于线性插值，任意时刻的瞬时值都等于设定的速度值
                return speed;
            }

            @Override
            public double duration(double journey) {
                // 计算完成指定路程所需的时间：时间 = 路程 / 速度
                return journey / speed;
            }
        };
    }

    /**
     * 创建一个加速度插值器
     *
     * @param acceleration 加速度值，用于计算速度和时间
     * @return 返回一个Interpolation接口的实现，用于处理加速度相关的插值计算
     */
    static Interpolation acceleration(double acceleration) {
        return new Interpolation() {
            @Override
            public double instantaneous(int time) {
                // 根据加速度和时间计算瞬时速度：v = a * t
                return acceleration * time;
            }

            @Override
            public double duration(double journey) {
                return Math.sqrt(2 * journey / acceleration);
            }
        };
    }
}
