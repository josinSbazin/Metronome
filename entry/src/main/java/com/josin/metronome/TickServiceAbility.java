package com.josin.metronome;

import com.josin.metronome.slice.Settings;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.ability.LocalRemoteObject;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.Operation;
import ohos.event.commonevent.CommonEventData;
import ohos.event.commonevent.CommonEventManager;
import ohos.event.notification.NotificationHelper;
import ohos.event.notification.NotificationRequest;
import ohos.event.notification.NotificationSlot;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;
import ohos.vibrator.agent.VibratorAgent;

import java.util.Timer;
import java.util.TimerTask;

import static com.josin.metronome.slice.MainAbilitySlice.EVENT_ACTION;

public class TickServiceAbility extends Ability {
    private static final int VIBRATION_DURATION = 50;

    private Settings settings;
    private VibratorAgent vibratorAgent;

    private Timer timer = new Timer();

    private boolean isPlaying;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);

        settings = ServiceLocator.getServiceLocator(this).getSettings();
        vibratorAgent = new VibratorAgent();
    }

    @Override
    public void onStop() {
        super.onStop();
        cancelTimerTask();
        cancelNotification();
    }

    @Override
    public void onCommand(Intent intent, boolean restart, int startId) {
        super.onCommand(intent, restart, startId);
        sendEvent();
    }

    @Override
    public IRemoteObject onConnect(Intent intent) {
        return new TickRemoteObject(this);
    }

    public void play() {
        isPlaying = true;
        createTimerTask(0, toInterval(settings.getBpm()));
        sendNotification("Playing - " + settings.getBpm());
        sendEvent();
    }

    public void pause() {
        cancelTimerTask();
        cancelNotification();
        isPlaying = false;
        sendEvent();
    }

    private void sendEvent() {
        try {
            Intent intent = new Intent();
            Operation operation = new Intent.OperationBuilder()
                    .withAction(EVENT_ACTION)
                    .build();
            intent.setOperation(operation);
            intent.setParam("state", isPlaying);
            CommonEventData eventData = new CommonEventData(intent);
            CommonEventManager.publishCommonEvent(eventData);
        } catch (RemoteException e) {
            System.out.println(e.getLocalizedMessage());
        }
    }

    private void sendNotification(String message) {
        String slotId = "tickServiceId";
        String slotName = "tickServiceName";
        NotificationSlot slot = new NotificationSlot(slotId, slotName, NotificationSlot.LEVEL_MIN);
        slot.setDescription("Tick-tack");
        slot.setEnableVibration(false);
        try {
            NotificationHelper.addNotificationSlot(slot);
        } catch (RemoteException e) {
            System.out.println(e.getLocalizedMessage());
        }
        int notificationId = 1;
        NotificationRequest request = new NotificationRequest(notificationId);
        request.setSlotId(slot.getId());
        String title = "Metronome";
        String text = "The Metronome is in " + message;
        NotificationRequest.NotificationNormalContent content = new NotificationRequest.NotificationNormalContent();
        content.setTitle(title)
                .setText(text);
        NotificationRequest.NotificationContent notificationContent =
                new NotificationRequest.NotificationContent(content);
        request.setContent(notificationContent);
        keepBackgroundRunning(notificationId, request);
    }

    private void cancelNotification() {
        cancelBackgroundRunning();
    }

    private void createTimerTask(int delay, int interval) {
        Timer oldTimer = timer;
        timer = new Timer();
        TimerTask timerTask = new MetronomeTask();
        timer.scheduleAtFixedRate(timerTask, delay, interval);
        oldTimer.cancel();
    }

    private void cancelTimerTask() {
        if (timer != null) {
            timer.cancel();
        }
    }

    private static int toInterval(int bpm) {
        return 60000 / bpm;
    }

    private class MetronomeTask extends TimerTask {
        private int interval = -1;

        @Override
        public void run() {
            if (isPlaying) {
                if (interval == -1L) {
                    interval = toInterval(settings.getBpm());
                }

                vibratorAgent.startOnce(VIBRATION_DURATION);

                if (interval != toInterval(settings.getBpm())) {
                    interval = toInterval(settings.getBpm());
                    createTimerTask(interval, interval);
                }
            }
        }
    }

    public static class TickRemoteObject extends LocalRemoteObject {
        private final TickServiceAbility tickService;

        TickRemoteObject(TickServiceAbility tickService) {
            this.tickService = tickService;
        }

        public void play() {
            tickService.play();
        }

        /**
         * pausePlay Music
         */
        public void pause() {
            tickService.pause();
        }
    }
}