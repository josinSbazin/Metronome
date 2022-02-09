package com.josin.metronome.slice;

import com.josin.metronome.ResourceTable;
import com.josin.metronome.math.Clamp;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Component;
import ohos.agp.components.Image;
import ohos.agp.components.Text;
import ohos.agp.components.element.Element;
import ohos.agp.components.element.VectorElement;

public class MainAbilitySlice extends AbilitySlice {
    private static final int ROTATION_SPEED = 4;

    private Image playPause;
    private Text bpm;

    private Settings settings;

    private boolean isPlaying = false;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_main);

        settings = new Settings(this);

        findComponents();
        setUpPlayPauseButton();
        setUpBpm();
    }

    private void findComponents() {
        playPause = (Image) findComponentById(ResourceTable.Id_playPause);
        bpm = (Text) findComponentById(ResourceTable.Id_bpm);
    }

    private void setUpPlayPauseButton() {
        playPause.setClickedListener(component -> playPause());
    }

    private void setUpBpm() {
        applyBpm(settings.getBpm());

        bpm.setBindStateChangedListener(new Component.BindStateChangedListener() {
            @Override
            public void onComponentBoundToWindow(Component component) {
                component.setTouchFocusable(true);
                component.requestFocus();
            }

            @Override
            public void onComponentUnboundFromWindow(Component component) {
                //no_op
            }
        });

        Clamp bpmClamp = new Clamp(settings.getMinBpm(), settings.getMaxBpm());
        AdjustableRotateListener rotateListener = new AdjustableRotateListener(ROTATION_SPEED,
                value -> {
                    int newBpm = bpmClamp.clamp(settings.getBpm() - value);
                    applyBpm(newBpm);
                });
        bpm.setRotationEventListener(rotateListener);
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

    private void applyBpm(int bpmCount) {
        settings.setBpm(bpmCount);
        bpm.setText(String.valueOf(bpmCount));
    }
}
