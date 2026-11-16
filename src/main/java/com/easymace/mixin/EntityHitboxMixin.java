package com.easymace.mixin;

import com.easymace.config.EasyMaceConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityHitboxMixin {
    @Inject(method = "getBoundingBox", at = @At("RETURN"), cancellable = true)
    private void easymace$expandHitbox(CallbackInfoReturnable<AABB> cir) {
        if (!EasyMaceConfig.get().hitboxEnabled) return;
        Entity self = (Entity) (Object) this;
        Identifier typeId = BuiltInRegistries.ENTITY_TYPE.getKey(self.getType());
        String idStr = typeId != null ? typeId.toString() : "";
        if (!EasyMaceConfig.get().hitboxAllEntities) {
            boolean found = false;
            for (String e : EasyMaceConfig.get().hitboxEntities) {
                if (e.equalsIgnoreCase(idStr)) { found = true; break; }
            }
            if (!found) return;
        }
        AABB box = cir.getReturnValue();
        if (box == null) return;
        double mult = EasyMaceConfig.get().hitboxMultiplier;
        if (mult <= 1.0) return;
        double expandX = (box.maxX - box.minX) * (mult - 1.0) / 2.0;
        double expandY = (box.maxY - box.minY) * (mult - 1.0) / 2.0;
        double expandZ = (box.maxZ - box.minZ) * (mult - 1.0) / 2.0;
        cir.setReturnValue(box.inflate(expandX, expandY, expandZ));
    }
}
