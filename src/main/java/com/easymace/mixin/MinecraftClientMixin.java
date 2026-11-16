package com.easymace.mixin;

import com.easymace.EasyMaceMod;
import com.easymace.config.EasyMaceConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.core.component.DataComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {
    private static int easymace$originalSlot = -1;
    private static boolean easymace$swapping = false;

    @Inject(method = "startAttack", at = @At("HEAD"))
    private void easymace$beforeAttack(CallbackInfoReturnable<Boolean> cir) {
        if (!EasyMaceConfig.get().enabled) return;
        Minecraft client = (Minecraft) (Object) this;
        if (client.player == null) return;
        if (easymace$swapping) return;

        LocalPlayer player = client.player;
        boolean isSlam = player.fallDistance >= (float) EasyMaceConfig.get().slamHeightThreshold;
        String preferred = isSlam
                ? EasyMaceConfig.get().preferredEnchantForSlam
                : EasyMaceConfig.get().preferredEnchantForNormal;

        int maceSlot = easymace$findMaceSlot(player, preferred);
        if (maceSlot == -1) return;

        int currentSlot = easymace$getSelectedSlot(player.getInventory());
        if (currentSlot == maceSlot) return;

        easymace$originalSlot = currentSlot;
        easymace$swapping = true;

        easymace$setSelectedSlot(player.getInventory(), maceSlot);
        if (player.connection != null) {
            player.connection.send(new ServerboundSetCarriedItemPacket(maceSlot));
        }
    }

    @Inject(method = "startAttack", at = @At("RETURN"))
    private void easymace$afterAttack(CallbackInfoReturnable<Boolean> cir) {
        if (!easymace$swapping) return;
        Minecraft client = (Minecraft) (Object) this;
        if (client.player != null && easymace$originalSlot >= 0) {
            easymace$setSelectedSlot(client.player.getInventory(), easymace$originalSlot);
            if (client.player.connection != null) {
                client.player.connection.send(new ServerboundSetCarriedItemPacket(easymace$originalSlot));
            }
        }
        easymace$originalSlot = -1;
        easymace$swapping = false;
    }

    private int easymace$findMaceSlot(LocalPlayer player, String preferredEnchant) {
        Inventory inv = player.getInventory();
        int firstMace = -1;
        int preferredMace = -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() == Items.MACE) {
                if (firstMace == -1) firstMace = i;
                if (preferredMace == -1 && easymace$hasEnchantment(stack, preferredEnchant)) {
                    preferredMace = i;
                }
            }
        }

        if (preferredMace != -1) return preferredMace;
        return firstMace;
    }

    private boolean easymace$hasEnchantment(ItemStack stack, String enchantName) {
        if (enchantName == null || enchantName.isEmpty()) return false;
        Object enchantments = stack.get(DataComponents.ENCHANTMENTS);
        if (enchantments == null) return false;
        String lower = enchantName.toLowerCase();
        String str = enchantments.toString().toLowerCase();
        if (str.contains(lower)) return true;
        for (java.lang.reflect.Method m : enchantments.getClass().getDeclaredMethods()) {
            if (m.getParameterCount() == 0) {
                try {
                    m.setAccessible(true);
                    Object r = m.invoke(enchantments);
                    if (r != null && r.toString().toLowerCase().contains(lower)) return true;
                } catch (Exception ignored) {}
            }
        }
        return false;
    }
    private int easymace$getSelectedSlot(net.minecraft.world.entity.player.Inventory inv) {
        try {
            java.lang.reflect.Field f = net.minecraft.world.entity.player.Inventory.class.getDeclaredField("selected");
            f.setAccessible(true);
            return f.getInt(inv);
        } catch (Exception e) { return 0; }
    }
    private void easymace$setSelectedSlot(net.minecraft.world.entity.player.Inventory inv, int slot) {
        try {
            java.lang.reflect.Field f = net.minecraft.world.entity.player.Inventory.class.getDeclaredField("selected");
            f.setAccessible(true);
            f.setInt(inv, slot);
        } catch (Exception ignored) {}
    }
}
