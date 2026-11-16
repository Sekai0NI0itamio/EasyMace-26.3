package com.easymace.mixin;

import com.easymace.config.EasyMaceConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityHitboxMixin {
    @Inject(method = "getBoundingBox", at = @At("RETURN"), cancellable = true)
    private void easymace$expandHitbox(CallbackInfoReturnable<AABB> cir) {
        EasyMaceConfig cfg = EasyMaceConfig.get();
        if (!cfg.hitboxEnabled) return;
        if (cfg.hitboxMultiplier <= 1.0) return;

        // Only expand hitboxes when the player is holding a mace
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) return;
        ItemStack mainHand = mc.player.getMainHandItem();
        if (mainHand.getItem() != Items.MACE) return;

        // Only expand the hitbox of the entity the player is currently targeting
        Entity self = (Entity) (Object) this;
        HitResult crosshair = mc.hitResult;
        if (!(crosshair instanceof EntityHitResult entityHit)) return;
        if (entityHit.getEntity() != self) return;

        // Check if this entity type should be expanded
        if (!cfg.hitboxAllEntities) {
            Identifier typeId = BuiltInRegistries.ENTITY_TYPE.getKey(self.getType());
            String idStr = typeId != null ? typeId.toString() : "";
            if (!cfg.hitboxEntities.contains(idStr)) return;
        }

        AABB box = cir.getReturnValue();
        if (box == null) return;

        double mult = cfg.hitboxMultiplier;
        double expandX = (box.maxX - box.minX) * (mult - 1.0) / 2.0;
        double expandY = (box.maxY - box.minY) * (mult - 1.0) / 2.0;
        double expandZ = (box.maxZ - box.minZ) * (mult - 1.0) / 2.0;

        cir.setReturnValue(box.inflate(expandX, expandY, expandZ));
    }
}
