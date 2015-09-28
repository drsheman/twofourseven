package com.molamil.radio24syv.receiver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.molamil.radio24syv.RadioPlayer;
import com.molamil.radio24syv.RadioPlayerService;

public class AudioNoisyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
            Log.d("JJJ", "Headphones unplugged");
            Intent i = new Intent(context, RadioPlayerService.class);
            RadioPlayerService.RadioPlayerServiceBinder binder = (RadioPlayerService.RadioPlayerServiceBinder) peekService(context, i);
            if (binder != null) {
                Log.d("JJJ", "Pausing playback");
                RadioPlayerService player = binder.getService();
                player.setAction(player.getUrl(), RadioPlayer.ACTION_PAUSE);
            } else {
                Log.d("JJJ", "Unable to pause playback because the service is not started (null)");
            }
        }
    }
}