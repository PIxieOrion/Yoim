package PixieOrion.yoim.core;

import PixieOrion.yoim.Yoim;
import PixieOrion.yoim.mixin.accessor.EntityRenderManagerAccessor;
import PixieOrion.yoim.mixin.accessor.ItemRenderStateAccessor;
import PixieOrion.yoim.mixin.accessor.LayerRenderStateAccessor;
import PixieOrion.yoim.module.render.NameTagsModule;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

public final class NameTagsRenderer {
    private static final int ICON_SIZE = 16;
    private static final int SLOT_SPACING = 18;
    private static final int SLOT_COUNT = 6;

    private NameTagsRenderer() {}

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(NameTagsRenderer::render);
    }

    private static void render(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || context.matrices() == null || context.consumers() == null) return;
        NameTagsModule module = Yoim.MODULES.get(NameTagsModule.class);
        if (module == null || !module.isEnabled()) return;

        Camera camera = client.gameRenderer.getCamera();
        Vec3d cameraPos = camera.getCameraPos();
        MatrixStack matrices = context.matrices();
        VertexConsumerProvider consumers = context.consumers();
        TextRenderer font = client.textRenderer;
        float scaleSetting = module.scale.getValue().floatValue();
        ItemModelManager itemModels = ((EntityRenderManagerAccessor) client.getEntityRenderDispatcher()).yoim$getItemModelManager();

        for (PlayerEntity player : client.world.getPlayers()) {
            if (player == client.player || player.isRemoved() || player.isInvisible()) continue;

            double x = player.getX();
            double y = player.getY() + (player.isSneaking() ? 1.9 : 2.1);
            double z = player.getZ();
            Vec3d delta = new Vec3d(x - cameraPos.x, y - cameraPos.y, z - cameraPos.z);
            double distanceSq = cameraPos.squaredDistanceTo(x, y, z);
            float distance = (float) Math.sqrt(distanceSq);
            float scaling = 0.0018f + (scaleSetting / 10000.0f) * distance;
            if (distance <= 8.0f) scaling = 0.0245f;

            Text label = Text.literal(player.getName().getString()).formatted(player.isSneaking() ? Formatting.GOLD : Formatting.WHITE);
            if (module.ping.getValue() && client.getNetworkHandler() != null) {
                var entry = client.getNetworkHandler().getPlayerListEntry(player.getUuid());
                if (entry != null) label = label.copy().append(Text.literal(" " + entry.getLatency() + "ms").formatted(Formatting.GRAY));
            }
            if (module.health.getValue()) {
                float hp = player.getHealth() + player.getAbsorptionAmount();
                Formatting hpColor = hp > 15.0f ? Formatting.GREEN : hp > 7.0f ? Formatting.YELLOW : Formatting.RED;
                label = label.copy().append(Text.literal(" " + String.format(java.util.Locale.ROOT, "%.1f", hp)).formatted(hpColor));
            }

            int width = font.getWidth(label);
            matrices.push();
            matrices.translate(delta.x, delta.y, delta.z);
            matrices.multiply(camera.getRotation());
            matrices.scale(scaling, -scaling, scaling);

            if (module.items.getValue()) {
                ItemStack[] stacks = getStacks(player);
                int rowWidth = SLOT_COUNT * SLOT_SPACING;
                float startX = -rowWidth / 2.0f + 1.0f;
                float itemY = -font.fontHeight - ICON_SIZE - 3.0f;
                for (int i = 0; i < stacks.length; i++) {
                    ItemStack stack = stacks[i];
                    if (stack.isEmpty()) continue;
                    float slotX = startX + i * SLOT_SPACING;
                    renderItemIcon(client, itemModels, stack, player, matrices, consumers,
                            slotX + 1.0f, itemY, player.getId() + i);
                    if (module.durability.getValue() && stack.isDamageable()) {
                        int remaining = Math.round((stack.getMaxDamage() - stack.getDamage()) * 100.0f / stack.getMaxDamage());
                        Formatting durabilityColor = remaining > 60 ? Formatting.GREEN : remaining > 25 ? Formatting.YELLOW : Formatting.RED;
                        Text durabilityText = Text.literal(remaining + "%").formatted(durabilityColor);
                        matrices.push();
                        matrices.translate(slotX + (SLOT_SPACING - font.getWidth(durabilityText) * 0.5f) / 2.0f,
                                itemY - font.fontHeight * 0.55f, 0.0f);
                        matrices.scale(0.5f, -0.5f, 0.5f);
                        font.draw(durabilityText, 0.0f, 0.0f, 0xFFFFFFFF, true,
                                matrices.peek().getPositionMatrix(), consumers, TextRenderer.TextLayerType.SEE_THROUGH,
                                0x80000000, LightmapTextureManager.MAX_LIGHT_COORDINATE);
                        matrices.pop();
                    }
                }
            }

            font.draw(label, -width / 2.0f, -font.fontHeight, 0xFFFFFFFF, true,
                    matrices.peek().getPositionMatrix(), consumers, TextRenderer.TextLayerType.SEE_THROUGH,
                    0x80000000, LightmapTextureManager.MAX_LIGHT_COORDINATE);
            matrices.pop();
        }
    }

    private static ItemStack[] getStacks(PlayerEntity player) {
        return new ItemStack[] {
                player.getMainHandStack(),
                player.getEquippedStack(EquipmentSlot.HEAD),
                player.getEquippedStack(EquipmentSlot.CHEST),
                player.getEquippedStack(EquipmentSlot.LEGS),
                player.getEquippedStack(EquipmentSlot.FEET),
                player.getOffHandStack()
        };
    }

    private static void renderItemIcon(MinecraftClient client, ItemModelManager itemModels, ItemStack stack,
                                       PlayerEntity owner, MatrixStack matrices, VertexConsumerProvider consumers,
                                       float x, float y, int seed) {
        ItemRenderState state = new ItemRenderState();
        itemModels.updateForLivingEntity(state, stack, ItemDisplayContext.GUI, owner);
        if (state.isEmpty()) return;
        ItemRenderStateAccessor stateAccessor = (ItemRenderStateAccessor) state;

        matrices.push();
        matrices.translate(x + ICON_SIZE / 2.0f, y + ICON_SIZE / 2.0f, 0.0f);
        matrices.scale(ICON_SIZE, -ICON_SIZE, -0.001f);
        for (int i = 0; i < stateAccessor.yoim$getLayerCount(); i++) {
            var layer = stateAccessor.yoim$getLayers()[i];
            LayerRenderStateAccessor layerAccessor = (LayerRenderStateAccessor) layer;
            if (layerAccessor.yoim$getSpecialModelType() != null || layer.getQuads().isEmpty()) continue;
            matrices.push();
            layerAccessor.yoim$getTransform().apply(false, matrices.peek());
            ItemRenderer.renderItem(ItemDisplayContext.GUI, matrices, consumers,
                    LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV,
                    layerAccessor.yoim$getTints(), layer.getQuads(), layerAccessor.yoim$getRenderLayer(), layerAccessor.yoim$getGlint());
            matrices.pop();
        }
        matrices.pop();
    }
}
