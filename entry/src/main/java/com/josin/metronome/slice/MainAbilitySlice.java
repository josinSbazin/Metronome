package com.josin.metronome.slice;

import com.josin.metronome.ResourceTable;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Button;
import ohos.agp.components.Image;
import ohos.agp.components.element.Element;
import ohos.agp.components.element.VectorElement;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.vibrator.agent.VibratorAgent;

import java.util.List;

public class MainAbilitySlice extends AbilitySlice {
    private static final HiLogLabel LOG_LABEL
            = new HiLogLabel(HiLog.DEBUG, 0x0, "MainAbility");

    private Image playPause;

    private boolean isPlaying = false;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_main);

        playPause = (Image) findComponentById(ResourceTable.Id_playPause);

        playPause.setClickedListener(component -> {
            playPause();
        });
    }

    private void playPause() {

        int imageId;
        if (isPlaying) {
            imageId = ResourceTable.Graphic_ic_baseline_pause;
        } else {
            imageId = ResourceTable.Graphic_ic_baseline_play;
        }
        Element imageElement = new VectorElement(this, imageId);
        playPause.setImageElement(imageElement);
        isPlaying = !isPlaying;
    }
}
