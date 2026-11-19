package com.easymace.mixin;

import com.easymace.config.EasyMaceConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Expands the TARGETING margin (getPickRadius), which is the extra radius added to
 * an entity's bounding box during the crosshair raycast. Same hook as Meteor Client's
 * Hitboxes module. Does NOT touch the collision box, so blocks/movement stay normal.
 */
@Mixin(Entity.class)
public class EntityHitboxMixin {
    @Inject(method = "getPickRadius", at = @At("HEAD"), cancellable = true)
    private void easymace$expandPickRadius(CallbackInfoReturnable<Float> cir) {
        EasyMaceConfig cfg = EasyMaceConfig.get();
        if (!cfg.hitboxEnabled) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) return;
        ItemStack mainHand = mc.player.getMainHandItem();
        if (mainHand.getItem() != Items.MACE) return;

        Entity self = (Entity) (Object) this;
        if (self instanceof Player pe && pe.getUUID().equals(mc.player.getUUID())) return;

        if (!cfg.hitboxAllEntities) {
            Identifier typeId = BuiltInRegistries.ENTITY_TYPE.getKey(self.getType());
            if (typeId == null || !cfg.hitboxEntities.contains(typeId.toString())) return;
        }

        cir.setReturnValue((float) cfg.hitboxExpand);
    }
}
