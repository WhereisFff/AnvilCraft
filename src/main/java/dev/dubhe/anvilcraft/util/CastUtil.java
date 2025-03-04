package dev.dubhe.anvilcraft.util;

public class CastUtil {
    /**
     * 转换一个使用 <code>(Example) value</code> 方法转换时编译器会丢出警告的值。
     * 请确保传入的值可以被正常转换！否则仍会报错。
     * @param value 需要转换类型的值
     * @return 转换类型后的值
     * @param <T> 将会转换成的类型
     */
    @SuppressWarnings("unchecked")
    public static <T> T cast(Object value) throws ClassCastException {
        return (T) value;
    }
}
