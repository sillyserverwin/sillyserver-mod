package sillyserver.format;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import sillyserver.Rank;

import java.util.*;

public class PlayerNameFormatter {
    public static Component formatPlayerName(Rank rank, String playerName) {
        MutableComponent root = Component.empty();
        Stack<String> styles = new Stack<>();

        String fmt = rank.format();
        int i = 0;
        int len = fmt.length();

        while (i < len) {
            char c =  fmt.charAt(i);

            if (c == '{' && i + 1 < len) {
                if (fmt.charAt(i+1) == '$') {
                    // var
                    int close = fmt.indexOf('}', i);
                    if (close != -1) {
                        String varName = fmt.substring(i+2, close);

                        String replacement = switch (varName) {
                            case "name" -> playerName;
                            default -> "$" + varName;
                        };
                        root.append(applyStyles(replacement, styles));

                        i = close + 1;
                        continue;
                    }
                } else {
                    // style
                    int close = fmt.indexOf('}', i);
                    if (close != -1 && close + 1 < len && fmt.charAt(close + 1) == '(') {
                        String styleName = fmt.substring(i+1, close);
                        styles.push(styleName);
                        i = close + 2;
                        continue;
                    }
                }
            }
            if (c == ')') {
                if (!styles.isEmpty()) styles.pop();
                i++;
                continue;
            }

            StringBuilder builder = new StringBuilder();
            while (i < fmt.length()) {
                char c2 =  fmt.charAt(i);
                if (c2 == '{' || c2 == ')') {
                    break;
                }

                builder.append(c2);
                i++;
            }

            if (!builder.isEmpty()) {
                root.append(applyStyles(builder.toString(), styles));
            }
        }

        return root;
    }

    public static Component applyStyles(String text, Stack<String> styles) {
        MutableComponent cmp = Component.literal(text);

        Style styleConfig = Style.EMPTY;

        for (String style : styles) {
            ChatFormatting color = ChatFormatting.getByName(style);
            if (color != null) {
                styleConfig = styleConfig.applyFormat(color);
            }
        }
        return cmp.withStyle(styleConfig);
    }
}
