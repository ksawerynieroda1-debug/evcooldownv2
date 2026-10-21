package pl.evcooldown;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class SettingsScreen extends Screen {
    private final Screen parent;
    private final List<TextFieldWidget> nameFields = new ArrayList<>();
    private final List<TextFieldWidget> secFields = new ArrayList<>();
    private int scroll = 0;

    public SettingsScreen(Screen parent) {
        super(Text.literal("Event Cooldowns - ustawienia"));
        this.parent = parent;
    }

    private int visibleRows() {
        return Math.max(1, (height - 100) / 24);
    }

    @Override
    protected void init() {
        nameFields.clear();
        secFields.clear();
        CooldownConfig cfg = CooldownConfig.get();
        List<CooldownConfig.Entry> entries = cfg.entries;

        int maxScroll = Math.max(0, entries.size() - visibleRows());
        scroll = Math.max(0, Math.min(scroll, maxScroll));

        for (int r = 0; r < visibleRows() && scroll + r < entries.size(); r++) {
            final int idx = scroll + r;
            CooldownConfig.Entry e = entries.get(idx);
            int y = 45 + r * 24;

            TextFieldWidget n = new TextFieldWidget(textRenderer, width / 2 - 150, y, 170, 20, Text.literal("Nazwa"));
            n.setMaxLength(64);
            n.setText(e.name);
            addDrawableChild(n);
            nameFields.add(n);

            TextFieldWidget s = new TextFieldWidget(textRenderer, width / 2 + 25, y, 50, 20, Text.literal("Sek"));
            s.setMaxLength(5);
            s.setText(String.valueOf(e.seconds));
            addDrawableChild(s);
            secFields.add(s);

            addDrawableChild(ButtonWidget.builder(Text.literal("X"), b -> {
                apply();
                entries.remove(idx);
                clearAndInit();
            }).dimensions(width / 2 + 80, y, 20, 20).build());
        }

        int by = height - 50;
        addDrawableChild(ButtonWidget.builder(Text.literal("+ Dodaj przedmiot"), b -> {
            apply();
            entries.add(new CooldownConfig.Entry("Nowy przedmiot", 30));
            scroll = Math.max(0, entries.size() - visibleRows());
            clearAndInit();
        }).dimensions(width / 2 - 150, by, 150, 20).build());

        addDrawableChild(ButtonWidget.builder(
                Text.literal("HUD: " + (cfg.hudVisible ? "WLACZONY" : "WYLACZONY")), b -> {
            apply();
            cfg.hudVisible = !cfg.hudVisible;
            clearAndInit();
        }).dimensions(width / 2 + 5, by, 145, 20).build());

        addDrawableChild(ButtonWidget.builder(
                Text.literal("Tylko aktywne: " + (cfg.onlyActive ? "TAK" : "NIE")), b -> {
            apply();
            cfg.onlyActive = !cfg.onlyActive;
            clearAndInit();
        }).dimensions(width / 2 - 150, by + 24, 150, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Gotowe"), b -> close())
                .dimensions(width / 2 + 5, by + 24, 145, 20).build());
    }

    /** Przepisuje wartosci z pol tekstowych do configu i zapisuje plik. */
    private void apply() {
        List<CooldownConfig.Entry> entries = CooldownConfig.get().entries;
        for (int r = 0; r < nameFields.size(); r++) {
            int idx = scroll + r;
            if (idx >= entries.size()) break;
            CooldownConfig.Entry e = entries.get(idx);
            e.name = nameFields.get(r).getText().trim();
            try {
                int v = Integer.parseInt(secFields.get(r).getText().trim());
                e.seconds = Math.max(1, Math.min(99999, v));
            } catch (NumberFormatException ignored) {
                // zostaw poprzednia wartosc
            }
        }
        CooldownConfig.save();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        apply();
        scroll -= (int) Math.signum(verticalAmount);
        clearAndInit();
        return true;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, 12, 0xFFFFFFFF);
        ctx.drawTextWithShadow(textRenderer, "Nazwa (fragment nazwy przedmiotu)", width / 2 - 150, 32, 0xFFAAAAAA);
        ctx.drawTextWithShadow(textRenderer, "Sekundy", width / 2 + 25, 32, 0xFFAAAAAA);
    }

    @Override
    public void close() {
        apply();
        client.setScreen(parent);
    }
}
