package sillyserver;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayerNameFormatter {
    private static final Pattern VAR_PATTERN = Pattern.compile("%%([^%]+)%%");

    public static Component formatPlayerName(Rank rank, String playerName) {
        MutableComponent cmp = MutableComponent.create(PlainTextContents.LiteralContents.EMPTY);
        String format = rank.format();
        Matcher matcher = VAR_PATTERN.matcher(format);

        List<PlayerNameNode> nodes = new ArrayList<>();

        int last = 0;
        while (matcher.find()) {
            String varName = matcher.group(1);
            int start =  matcher.start();
            int end = matcher.end();
            String substr =  format.substring(last, start);
            if (!substr.isEmpty())
                nodes.add(new PlayerNameLiteralNode(substr));
            ChatFormatting color;
            if ((color = ChatFormatting.getByName(varName)) != null) {
                nodes.add(new PlayerNameColorNode(varName, color));
            } else {
                nodes.add(new PlayerNameVarNode(varName));
            }
            last = end;
        }

        String substr = format.substring(last);
        if (!substr.isEmpty())
            nodes.add(new PlayerNameLiteralNode(substr));

        for (PlayerNameNode node : nodes) {
            if (node instanceof PlayerNameVarNode) {
                String replacement = switch (node.getStr()) {
                    case "name" -> playerName;
                    default -> "%%" + node.getStr() + "%%";
                };

                node.setStr(replacement);
            }
        }

        Queue<PlayerNameNode> it = new LinkedList<>(nodes);

        while (!it.isEmpty()) {
            PlayerNameNode node = it.remove();
            if (node instanceof PlayerNameLiteralNode || node instanceof PlayerNameVarNode) {
                cmp.append(Component.literal(node.getStr()));
            } else if (node instanceof PlayerNameColorNode pnc) {
                while (!(it.peek() instanceof PlayerNameColorNode) && !it.isEmpty()) {
                    PlayerNameNode node2 = it.remove();
                    cmp.append(Component.literal(node2.getStr()).withStyle(pnc.getColor()));
                }
            }
        }

        return cmp;
    }

    private static abstract class PlayerNameNode {
        private String str;

        PlayerNameNode(String str) {
            this.str = str;
        }

        public String getStr() {
            return this.str;
        }

        public void setStr(String str) {
            this.str = str;
        }
    }

    private static class PlayerNameVarNode extends PlayerNameNode {
        PlayerNameVarNode(String str) {
            super(str);
        }
    }

    private static class PlayerNameColorNode extends PlayerNameNode {
        private ChatFormatting color;

        PlayerNameColorNode(String str, ChatFormatting color) {
            super(str);
            this.color = color;
        }

        public ChatFormatting getColor() {
            return this.color;
        }
    }

    private static class PlayerNameLiteralNode extends PlayerNameNode {
        PlayerNameLiteralNode(String str) {
            super(str);
        }
    }
}
