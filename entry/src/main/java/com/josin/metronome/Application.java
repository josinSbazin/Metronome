package com.josin.metronome;

import com.josin.metronome.slice.Settings;
import ohos.aafwk.ability.AbilityPackage;

public class Application extends AbilityPackage implements ServiceLocator {
    private Settings settings;

    @Override
    public void onInitialize() {
        super.onInitialize();

        settings = new Settings(this);
    }

    @Override
    public Settings getSettings() {
        return settings;
    }
}
