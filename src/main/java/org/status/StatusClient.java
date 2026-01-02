package org.status;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.client.resources.language.I18n;

public class StatusClient implements ClientModInitializer {

    private static final int LEVEL_XP_STEP = 5;
    private static final int PADDING = 6;
    private static final int ICON_WIDTH = 19;
    private static final int ICON_HEIGHT = 22;
    private static final int TEXT_X_OFFSET = 8;
    private static final int TEXT_Y_OFFSET = -4;
    private static final int BOSS_BAR_WIDTH = 200;
    private static final int BOSS_BAR_HEIGHT = 5;
    private static final int BOSS_BAR_Y_OFFSET = 2;

    private static final Identifier BOSS_BAR_BG =
            Identifier.fromNamespaceAndPath("minecraft", "textures/gui/sprites/boss_bar/white_background.png");
    private static final Identifier BOSS_BAR_FILL =
            Identifier.fromNamespaceAndPath("minecraft", "textures/gui/sprites/boss_bar/green_progress.png");

    private static final String[] RANKS_KEYS = {
            "none", "dirtborn", "stonewalker", "ironfist", "goldheart",
            "diamondeye", "mastermind", "legend", "mythic", "immortal",
            "ascendant", "champion", "conqueror", "warlord", "titan",
            "elder", "supreme"
    };

    private int lastRankIndex = -1;
    private long joinStartTime = 0L;
    private static final long JOIN_ANIM_DURATION = 900;
    private Level lastLevel = null;

    @Override
    public void onInitializeClient() {

        HudRenderCallback.EVENT.register((graphics, tickDelta) -> {

            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.level == null) return;

            if (mc.level != lastLevel) {
                joinStartTime = System.currentTimeMillis();
                lastLevel = mc.level;
            }

            float progress = (System.currentTimeMillis() - joinStartTime) / (float) JOIN_ANIM_DURATION;
            progress = Math.min(progress, 1f);

            float ease = 1f - (float) Math.pow(1f - progress, 3);

            int slideOffsetY = (int) (-60 * (1f - ease));
            int alpha = (int) (255 * ease);
            int color = (alpha << 24) | 0xFFFFFF;

            int playerLevel = mc.player.experienceLevel;
            int rankIndex = Math.min(playerLevel / LEVEL_XP_STEP, RANKS_KEYS.length - 1);

            if (rankIndex > lastRankIndex && lastRankIndex != -1) {
                mc.level.playSound(
                        mc.player,
                        mc.player.getX(),
                        mc.player.getY(),
                        mc.player.getZ(),
                        SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                        SoundSource.PLAYERS,
                        1f,
                        1f
                );
            }
            lastRankIndex = rankIndex;

            Identifier rankIcon = Identifier.fromNamespaceAndPath(
                    "status-plus",
                    "textures/hud/rank_" + String.format("%02d", rankIndex) + ".png"
            );

            int x = PADDING;
            int y = PADDING + slideOffsetY;

            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    rankIcon,
                    x,
                    y,
                    0, 0,
                    ICON_WIDTH,
                    ICON_HEIGHT,
                    ICON_WIDTH,
                    ICON_HEIGHT
            );

            String displayText = "▶ " + mc.player.getName().getString() + " | " +
                    I18n.get("rank." + RANKS_KEYS[rankIndex]);

            int textX = x + ICON_WIDTH + TEXT_X_OFFSET;
            int textY = y + (ICON_HEIGHT / 2) - (mc.font.lineHeight / 2) + TEXT_Y_OFFSET;

            graphics.drawString(mc.font, displayText, textX, textY, color, false);

            float xpProgress = (playerLevel % LEVEL_XP_STEP + mc.player.experienceProgress) / LEVEL_XP_STEP;

            int filledWidth = (int) (BOSS_BAR_WIDTH * xpProgress);

            int barX = textX;
            int barY = textY + mc.font.lineHeight + BOSS_BAR_Y_OFFSET;

            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    BOSS_BAR_BG,
                    barX,
                    barY,
                    0, 0,
                    BOSS_BAR_WIDTH,
                    BOSS_BAR_HEIGHT,
                    BOSS_BAR_WIDTH,
                    BOSS_BAR_HEIGHT
            );

            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    BOSS_BAR_FILL,
                    barX,
                    barY,
                    0, 0,
                    filledWidth,
                    BOSS_BAR_HEIGHT,
                    filledWidth,
                    BOSS_BAR_HEIGHT
            );
        });
    }
}
