package com.josin.metronome;

import com.josin.metronome.slice.Settings;
import ohos.aafwk.ability.Ability;

public interface ServiceLocator {
    Settings getSettings();

    static ServiceLocator getServiceLocator(Ability ability) {
        return (ServiceLocator) ability.getAbilityPackage();
    }
}
