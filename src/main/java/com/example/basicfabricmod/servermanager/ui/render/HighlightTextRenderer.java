package com.example.basicfabricmod.servermanager.ui.render;

import com.example.basicfabricmod.servermanager.model.SearchMatcher;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class HighlightTextRenderer {
    private HighlightTextRenderer() {
    }

    public static void drawHighlighted(DrawContext context, TextRenderer textRenderer, String text, String query, int x, int y, int color) {
        if (text == null) {
            return;
        }
        int index = SearchMatcher.matchIndex(text, query);
        if (index < 0) {
            context.drawTextWithShadow(textRenderer, text, x, y, color);
            return;
        }

        String before = text.substring(0, index);
        String match = text.substring(index, Math.min(text.length(), index + query.trim().length()));
        String after = text.substring(Math.min(text.length(), index + query.trim().length()));
        MutableText rendered = Text.literal(before)
                .append(Text.literal(match).setStyle(Style.EMPTY.withFormatting(Formatting.YELLOW)))
                .append(Text.literal(after));
        context.drawTextWithShadow(textRenderer, rendered, x, y, color);
    }
}
