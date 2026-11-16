package com.easymace.client;

import com.easymace.config.EasyMaceConfig;

public class EasyMaceClientHooks {
    public static void init() {
        EasyMaceConfig.load();
    }
}
