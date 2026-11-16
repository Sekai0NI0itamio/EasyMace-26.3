package com.easymace;

import com.easymace.client.EasyMaceClientHooks;
import net.fabricmc.api.ClientModInitializer;

public class EasyMaceMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EasyMaceClientHooks.init();
    }
}
