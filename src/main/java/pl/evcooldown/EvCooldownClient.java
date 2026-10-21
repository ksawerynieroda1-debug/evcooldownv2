package pl.evcooldown;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public class EvCooldownClient implements ClientModInitializer {
    /** nazwa wpisu -> czas (ms) konca cooldownu */
    public static final Map<String, Long> ends = new HashMap<>();
    private static KeyBinding settingsKey;

    @Override
    public void onInitializeClient() {
        CooldownConfig.load();

        settingsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.evcooldown.settings", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, "category.evcooldown"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (settingsKey.wasPressed()) {
                client.setScreen(new SettingsScreen(client.currentScreen));
            }
        });

        UseItemCallback.EVENT.register((player, world, hand) -> {
            onUse(player, world, hand);
            return ActionResult.PASS;
        });

        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            onUse(player, world, hand);
            return ActionResult.PASS;
        });

        HudRenderCallback.EVENT.register((ctx, tickCounter) -> {
            CooldownConfig cfg = CooldownConfig.get();
            MinecraftClient mc = MinecraftClient.getInstance();
            if (!cfg.hudVisible || mc.options.hudHidden || mc.player == null) return;

            long now = System.currentTimeMillis();
            int y = cfg.hudY;
            for (CooldownConfig.Entry e : cfg.entries) {
                if (e.name == null || e.name.isBlank()) continue;
                long rem = ends.getOrDefault(e.name, 0L) - now;
                if (rem > 0) {
                    String t = e.name + ": " + String.format("%.1fs", rem / 1000.0);
                    ctx.drawTextWithShadow(mc.textRenderer, t, cfg.hudX, y, 0xFFFF5555);
                    y += 11;
                } else if (!cfg.onlyActive) {
                    ctx.drawTextWithShadow(mc.textRenderer, e.name + ": GOTOWE", cfg.hudX, y, 0xFF55FF55);
                    y += 11;
                }
            }
        });
    }

    private static void onUse(PlayerEntity player, World world, Hand hand) {
        if (!world.isClient) return;
        ItemStack stack = player.getStackInHand(hand);
        if (stack.isEmpty()) return;

        String itemName = Formatting.strip(stack.getName().getString());
        if (itemName == null) return;
        itemName = itemName.toLowerCase();

        long now = System.currentTimeMillis();
        for (CooldownConfig.Entry e : CooldownConfig.get().entries) {
            if (e.name == null || e.name.isBlank()) continue;
            if (itemName.contains(e.name.toLowerCase())) {
                // nie restartuj, jesli odliczanie juz trwa
                if (ends.getOrDefault(e.name, 0L) <= now) {
                    ends.put(e.name, now + e.seconds * 1000L);
                }
                break;
            }
        }
    }
}
