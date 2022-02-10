package com.josin.metronome.slice;

import com.josin.metronome.ResourceTable;
import com.josin.metronome.ServiceLocator;
import com.josin.metronome.TickServiceAbility;
import com.josin.metronome.math.Clamp;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.ability.IAbilityConnection;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.Operation;
import ohos.agp.components.Component;
import ohos.agp.components.Image;
import ohos.agp.components.Text;
import ohos.agp.components.element.Element;
import ohos.agp.components.element.VectorElement;
import ohos.bundle.ElementName;
import ohos.event.commonevent.*;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;

public class MainAbilitySlice extends AbilitySlice {
    public static final String EVENT_ACTION = "com.josin.metronome";

    private static final String BUNDLE_NAME = "com.josin.metronome";

    private static final int ROTATION_SPEED = 4;

    private Image playPause;
    private Text bpm;

    private Settings settings;

    private boolean isPlaying = false;

    private TickServiceAbility.TickRemoteObject remote;
    private MetronomeEventSubscriber subscriber;

    private final IAbilityConnection connection = new IAbilityConnection() {
        @Override
        public void onAbilityConnectDone(ElementName elementName, IRemoteObject remoteObject, int i) {
            if (remoteObject instanceof TickServiceAbility.TickRemoteObject) {
                remote = (TickServiceAbility.TickRemoteObject) remoteObject;
            }
        }

        @Override
        public void onAbilityDisconnectDone(ElementName elementName, int i) {
            //no_op
        }
    };

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_main);

        settings = ServiceLocator.getServiceLocator(getAbility()).getSettings();

        findComponents();
        setUpPlayPauseButton();
        setUpBpm();
        initSubscribeEvent();
        startService();
    }

    @Override
    protected void onStop() {
        try {
            CommonEventManager.unsubscribeCommonEvent(subscriber);
        } catch (RemoteException e) {
            System.out.println(e.getLocalizedMessage());
        }
        disconnectAbility(connection);
    }

    private void findComponents() {
        playPause = (Image) findComponentById(ResourceTable.Id_playPause);
        bpm = (Text) findComponentById(ResourceTable.Id_bpm);
    }

    private void setUpPlayPauseButton() {
        playPause.setClickedListener(component -> {
                    if (isPlaying) {
                        remote.pause();
                    } else {
                        remote.play();
                    }
                }
        );
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

    private void initSubscribeEvent() {
        MatchingSkills matchingSkills = new MatchingSkills();
        matchingSkills.addEvent(EVENT_ACTION);
        CommonEventSubscribeInfo subscribeInfo = new CommonEventSubscribeInfo(matchingSkills);
        subscriber = new MetronomeEventSubscriber(subscribeInfo);
        try {
            CommonEventManager.subscribeCommonEvent(subscriber);
        } catch (RemoteException e) {
            System.out.println(e.getLocalizedMessage());
        }
    }

    private void startService() {
        Intent intent = new Intent();
        Operation operation = new Intent.OperationBuilder()
                .withDeviceId("")
                .withBundleName(BUNDLE_NAME)
                .withAbilityName(TickServiceAbility.class)
                .build();
        intent.setOperation(operation);
        startAbility(intent);
        connectAbility(intent, connection);
    }

    private void setState(boolean isPlaying) {
        int imageId;
        if (isPlaying) {
            imageId = ResourceTable.Graphic_ic_baseline_pause;
        } else {
            imageId = ResourceTable.Graphic_ic_baseline_play;
        }
        Element imageElement = new VectorElement(this, imageId);
        playPause.setImageElement(imageElement);
    }

    private void applyBpm(int bpmCount) {
        settings.setBpm(bpmCount);
        bpm.setText(String.valueOf(bpmCount));
    }

    class MetronomeEventSubscriber extends CommonEventSubscriber {
        MetronomeEventSubscriber(CommonEventSubscribeInfo info) {
            super(info);
        }

        @Override
        public void onReceiveEvent(CommonEventData commonEventData) {
            Intent intent = commonEventData.getIntent();
            boolean state = intent.getBooleanParam("state", false);
            setState(state);
            isPlaying = state;
        }
    }
}
