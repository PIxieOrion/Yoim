package PixieOrion.yoim.module.combat;

import PixieOrion.yoim.Yoim;
import PixieOrion.yoim.module.Category;
import PixieOrion.yoim.module.Module;
import PixieOrion.yoim.settings.impl.BooleanSetting;
import PixieOrion.yoim.settings.impl.CategorySetting;
import PixieOrion.yoim.settings.impl.ModeSetting;
import PixieOrion.yoim.settings.impl.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import PixieOrion.yoim.util.InventoryUtils;
import PixieOrion.yoim.packets.PacketUtils;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public final class KillAuraModule extends Module {
    public final ModeSetting autoSwitch = setting(new ModeSetting("Switch", "Choose whether a sword is required or selected automatically.", "None", "None", "Normal", "Require"));
    public final ModeSetting hitDelay = setting(new ModeSetting("HitDelay", "The delay between attacks.", "Vanilla", "None", "Vanilla", "Custom"));
    public final NumberSetting speed = setting(new NumberSetting("Speed", "Custom attack rate.", "Speed", () -> hitDelay.getValue().equals("Custom"), 20.0f, 0.1f, 20.0f));
    public final ModeSetting rotate = setting(new ModeSetting("Rotate", "How rotations are applied to the target.", "Hold", "None", "Normal", "Hold", "Packet"));
    public final ModeSetting swing = setting(new ModeSetting("Swing", "How the hand swing is displayed or sent.", "Mainhand", "None", "Packet", "Mainhand", "Offhand", "Both"));
    public final NumberSetting range = setting(new NumberSetting("Range", "Maximum target distance.", 6.0, 0.0, 12.0));
    public final BooleanSetting raytrace = setting(new BooleanSetting("Raytrace", "Only target entities visible through blocks.", false));
    public final NumberSetting wallsRange = setting(new NumberSetting("WallsRange", "WallsRange", "Maximum target distance through walls.", () -> !raytrace.getValue(), 5.0, 0.0, 12.0));
    public final NumberSetting ticksExisted = setting(new NumberSetting("TicksExisted", "Minimum age of a target in ticks.", 50, 0, 240));

    public final CategorySetting entitiesCategory = setting(new CategorySetting("Entities", "Target selection settings."));
    public final BooleanSetting players = setting(new BooleanSetting("Players", "Players", "Target players.", () -> entitiesCategory.isOpen(), true));
    public final BooleanSetting animals = setting(new BooleanSetting("Animals", "Animals", "Target animals and aquatic creatures.", () -> entitiesCategory.isOpen(), false));
    public final BooleanSetting hostiles = setting(new BooleanSetting("Hostiles", "Hostiles", "Target hostile mobs.", () -> entitiesCategory.isOpen(), false));
    public final BooleanSetting passives = setting(new BooleanSetting("Passives", "Passives", "Allow normally passive hostile mobs.", () -> entitiesCategory.isOpen() && hostiles.getValue(), false));
    public final BooleanSetting ambient = setting(new BooleanSetting("Ambient", "Ambient", "Target ambient creatures.", () -> entitiesCategory.isOpen(), false));
    public final BooleanSetting invisibles = setting(new BooleanSetting("Invisibles", "Invisibles", "Target invisible entities.", () -> entitiesCategory.isOpen(), false));
    public final BooleanSetting boats = setting(new BooleanSetting("Boats", "Boats", "Target boats.", () -> entitiesCategory.isOpen(), false));
    public final BooleanSetting shulkerBullets = setting(new BooleanSetting("ShulkerBullets", "ShulkerBullets", "Target shulker bullets.", () -> entitiesCategory.isOpen(), true));

    private Entity target;
    private long lastAttackTime;
    private boolean attacking;
    private float silentYaw;
    private float silentPitch;
    private boolean silentRotationActive;

    public KillAuraModule() {
        super("KillAura", Category.COMBAT, "Automatically selects and attacks nearby entities.");
    }

    public Entity getTarget() {
        return target;
    }

    public void prepareTick() {
        MinecraftClient client = Yoim.mc();
        silentRotationActive = false;
        if (!isEnabled() || client.player == null || client.world == null || client.interactionManager == null) {
            target = null;
            attacking = false;
            return;
        }

        target = findTarget(client);
        attacking = target != null;
        String rotationMode = rotate.getValue();
        if (target != null && (rotationMode.equals("Hold") || (rotationMode.equals("Normal") && attackReady(client)))) {
            calculateRotation(client, target);
            silentRotationActive = true;
        }
    }

    public void attackTick() {
        MinecraftClient client = Yoim.mc();
        if (!isEnabled() || target == null || client.player == null || client.world == null || client.interactionManager == null) return;
        if (target.isRemoved() || !target.isAlive() || (target instanceof LivingEntity living && living.getHealth() <= 0.0f)) {
            target = null;
            attacking = false;
            return;
        }

        String switchMode = autoSwitch.getValue();
        int swordSlot = -1;
        if (switchMode.equals("Require") && !InventoryUtils.isSword(client.player.getMainHandStack().getItem())) return;
        if (switchMode.equals("Normal")) {
            swordSlot = InventoryUtils.findBestSword(client);
            if (swordSlot == -1) return;
        }

        long now = System.nanoTime();
        String delayMode = hitDelay.getValue();
        if (delayMode.equals("Vanilla") && client.player.getAttackCooldownProgress(0.5f) < 1.0f) return;
        if (delayMode.equals("Custom")) {
            long delayNanos = (long) ((1000.0 - speed.getValue().doubleValue() * 50.0) * 1_000_000.0);
            if (delayNanos > 0 && now - lastAttackTime < delayNanos) return;
        }

        if (rotate.getValue().equals("Packet")) rotateAndSendPacket(client, target);

        if (switchMode.equals("Normal") && swordSlot != client.player.getInventory().getSelectedSlot()) {
            client.player.getInventory().setSelectedSlot(swordSlot);
            PacketUtils.send(new net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket(swordSlot));
        }

        client.interactionManager.attackEntity(client.player, target);
        switch (swing.getValue()) {
            case "Packet" -> {
                PacketUtils.send(new net.minecraft.network.packet.c2s.play.HandSwingC2SPacket(Hand.MAIN_HAND));
            }
            case "Mainhand" -> client.player.swingHand(Hand.MAIN_HAND);
            case "Offhand" -> client.player.swingHand(Hand.OFF_HAND);
            case "Both" -> {
                client.player.swingHand(Hand.MAIN_HAND);
                client.player.swingHand(Hand.OFF_HAND);
            }
        }
        lastAttackTime = now;
    }

    public boolean hasVisualRotation() {
        return isEnabled() && silentRotationActive && target != null
                && (rotate.getValue().equals("Hold") || rotate.getValue().equals("Normal"));
    }

    public float getSilentYaw() { return silentYaw; }
    public float getSilentPitch() { return silentPitch; }

    private void calculateRotation(MinecraftClient client, Entity entity) {
        Vec3d difference = entity.getBoundingBox().getCenter().subtract(client.player.getEyePos());
        double horizontal = Math.sqrt(difference.x * difference.x + difference.z * difference.z);
        silentYaw = MathHelper.wrapDegrees((float) (Math.toDegrees(Math.atan2(difference.z, difference.x)) - 90.0));
        silentPitch = MathHelper.wrapDegrees((float) -Math.toDegrees(Math.atan2(difference.y, horizontal)));
    }

    private void rotateAndSendPacket(MinecraftClient client, Entity entity) {
        calculateRotation(client, entity);
        if (client.getNetworkHandler() != null) {
            client.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(silentYaw, silentPitch, client.player.isOnGround(), client.player.horizontalCollision));
        }
    }

    private boolean attackReady(MinecraftClient client) {
        String delayMode = hitDelay.getValue();
        if (delayMode.equals("Vanilla")) return client.player.getAttackCooldownProgress(0.5f) >= 1.0f;
        if (delayMode.equals("Custom")) {
            long delayNanos = (long) ((1000.0 - speed.getValue().doubleValue() * 50.0) * 1_000_000.0);
            return delayNanos <= 0 || System.nanoTime() - lastAttackTime >= delayNanos;
        }
        return true;
    }

    private Entity findTarget(MinecraftClient client) {
        double maxRange = range.getValue().doubleValue();
        double maxWallRange = wallsRange.getValue().doubleValue();
        double rangeSquared = MathHelper.square(maxRange);
        double wallsRangeSquared = MathHelper.square(maxWallRange);
        double scanRadius = Math.max(maxRange, maxWallRange) + 1.0;
        Vec3d eyePos = client.player.getEyePos();
        Box searchBox = client.player.getBoundingBox().expand(scanRadius);
        Entity best = null;

        for (Entity entity : client.world.getOtherEntities(client.player, searchBox, candidate -> candidate.isAlive())) {
            if (entity.age < ticksExisted.getValue().intValue()) continue;
            if (entity instanceof LivingEntity living && living.getHealth() <= 0.0f) continue;
            if (!client.world.getWorldBorder().contains(entity.getBlockPos())) continue;
            if (client.player.isTeammate(entity)) continue;
            if (!isValidEntity(entity)) continue;

            double boxDistance = entity.getBoundingBox().squaredMagnitude(eyePos);
            if (boxDistance >= rangeSquared) continue;
            boolean visible = canSee(client, entity);
            if (!visible && (raytrace.getValue() || boxDistance >= wallsRangeSquared)) continue;

            if (best == null || client.player.squaredDistanceTo(entity) < client.player.squaredDistanceTo(best)) best = entity;
        }

        return best;
    }

    private boolean isValidEntity(Entity entity) {
        if (players.getValue() && entity.getType() == EntityType.PLAYER) return true;
        if (hostiles.getValue() && entity.getType().getSpawnGroup() == SpawnGroup.MONSTER) {
            if (!passives.getValue() && entity instanceof EndermanEntity enderman && !enderman.isAngry()) return false;
            if (!passives.getValue() && entity instanceof ZombifiedPiglinEntity piglin && !piglin.isAttacking()) return false;
            return true;
        }
        if (animals.getValue() && (entity.getType().getSpawnGroup() == SpawnGroup.CREATURE || entity.getType().getSpawnGroup() == SpawnGroup.WATER_CREATURE || entity.getType().getSpawnGroup() == SpawnGroup.WATER_AMBIENT || entity.getType().getSpawnGroup() == SpawnGroup.UNDERGROUND_WATER_CREATURE || entity.getType().getSpawnGroup() == SpawnGroup.AXOLOTLS)) return true;
        if (ambient.getValue() && entity.getType().getSpawnGroup() == SpawnGroup.AMBIENT) return true;
        if (invisibles.getValue() && entity.isInvisible()) return true;
        if (boats.getValue() && entity instanceof BoatEntity) return true;
        return shulkerBullets.getValue() && entity.getType() == EntityType.SHULKER_BULLET;
    }

    private boolean canSee(MinecraftClient client, Entity entity) {
        Vec3d start = client.player.getEyePos();
        Vec3d end = new Vec3d(entity.getX(), entity.getY(), entity.getZ());
        return client.world.raycast(new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, client.player)).getType() == HitResult.Type.MISS;
    }

    @Override
    protected void onDisable() {
        target = null;
        attacking = false;
        silentRotationActive = false;
        lastAttackTime = 0L;
    }
}
