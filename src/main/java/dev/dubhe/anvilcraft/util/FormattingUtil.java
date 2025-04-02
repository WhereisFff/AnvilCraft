package dev.dubhe.anvilcraft.util;

import com.google.common.base.CaseFormat;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 格式化工具
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FormattingUtil {
    /**
     * 与 .to(LOWER_UNDERSCORE, string) 几乎相同，但它也会在单词和数字之间插入下划线。
     *
     * @param string 任何带有 ASCII 字符的字符串。
     * @return 全小写的字符串，在单词/数字边界前插入下划线：“maragingSteel300” -> “maraging_steel_300”
     */
    public static @NotNull String toLowerCaseUnderscore(@NotNull String string) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            if (i != 0
                && (Character.isUpperCase(string.charAt(i))
                || (Character.isDigit(string.charAt(i - 1)) ^ Character.isDigit(string.charAt(i)))))
                result.append("_");
            result.append(Character.toLowerCase(string.charAt(i)));
        }
        return result.toString();
    }

    /**
     * 与 .to(LOWER_UNDERSCORE, string) 几乎相同，但它也会在单词和数字之间插入下划线。
     *
     * @param string 任何带有 ASCII 字符的字符串。
     * @return 全小写的字符串，在单词/数字边界前插入下划线：“maragingSteel300” -> “maraging_steel_300”
     */
    public static @NotNull String toLowerCaseUnder(String string) {
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, string);
    }

    /**
     * apple_orange.juice => Apple Orange (Juice)
     */
    public static String toEnglishName(@NotNull Object internalName) {
        return Arrays.stream(internalName.toString().toLowerCase(Locale.ROOT).split("_"))
            .map(StringUtils::capitalize)
            .collect(Collectors.joining(" "));
    }

    /**
     * 30 -> 30t, 150 -> 7.50s, 1635 -> 1min21.75s
     */
    public static Component toFormattedTime(int ticks) {
        if (ticks < 20 * 5) {
            return Component.translatable("time.ticks", ticks);
        } else if (ticks < 20 * 5 * 60) {
            return Component.translatable("time.seconds", ticks / 20);
        } else {
            int seconds = ticks / 20;
            return Component.translatable(
                "time.minutes", seconds / 60, Component.translatable(
                    "time.seconds", seconds % 60, Component.translatable("time.ticks", ticks % 20)
                )
            );
        }
    }
}
