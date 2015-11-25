package fr.ralmn.wakemeup;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;

import fr.ralmn.wakemeup.object.Alarm;

/**
 * Created by ralmn on 24/09/15.
 */
public class AlarmKlaxon {

    private static final long[] sVibratePattern = new long[] { 0, 750, 500 };

    // Volume suggested by media team for in-call alarms.
    private static final float IN_CALL_VOLUME = 0.125f;

    private static boolean started = false;
    private static MediaPlayer mediaPlayer;

    public static boolean isStarted(){
        return started;
    }

    public static void stop(Context context) {

        if (started) {
            started = false;
            // Stop audio playing
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                AudioManager audioManager = (AudioManager)
                        context.getSystemService(Context.AUDIO_SERVICE);
                audioManager.abandonAudioFocus(null);
                mediaPlayer.release();
                mediaPlayer = null;
            }


            ((Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE)).cancel();
        }
    }


    public static void start(final Context context, Alarm alarm,
                             boolean inTelephoneCall) {
        stop(context);

        boolean vibrate = context.getSharedPreferences(Utils.PREF_NAME, Context.MODE_PRIVATE).getBoolean("default_vibrate", false);
        Uri alarmNoise = Uri.parse(context.getSharedPreferences(Utils.PREF_NAME, Context.MODE_PRIVATE).getString("default_ringtone",
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString()));

        // Fall back on the default alarm if the database does not have an
        // alarm stored.
        if (alarmNoise == null) {
            alarmNoise = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        }

        // TODO: Reuse mMediaPlayer instead of creating a new one and/or use RingtoneManager.
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                AlarmKlaxon.stop(context);
                return true;
            }
        });

        try {
            // Check if we are in a call. If we are, use the in-call alarm
            // resource at a low volume to not disrupt the call.
            if (inTelephoneCall) {
                mediaPlayer.setVolume(IN_CALL_VOLUME, IN_CALL_VOLUME);
                //TODO : setDataSourceFromResource(context, mediaPlayer, R.raw.in_call_alarm);
            } else {
                mediaPlayer.setDataSource(context, alarmNoise);
            }
            startAlarm(context, mediaPlayer);
        } catch (Exception ex) {
            // The alarmNoise may be on the sd card which could be busy right
            // now. Use the fallback ringtone.
            try {
                // Must reset the media player to clear the error state.
                mediaPlayer.reset();
                //setDataSourceFromResource(context, mediaPlayer, R.raw.fallbackring);
                startAlarm(context, mediaPlayer);
            } catch (Exception ex2) {
                // At this point we just don't play anything.
            }
        }
        Log.d("RALMN", vibrate + " vibrator");

        if (vibrate) {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (Build.VERSION.SDK_INT >= 21) {
                AudioAttributes VIBRATION_ATTRIBUTES = new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build();
                vibrator.vibrate(sVibratePattern, 0, VIBRATION_ATTRIBUTES);
            }else{
                vibrator.vibrate(sVibratePattern, 0);
            }
        }
        started = true;
    }

    // Do the common stuff when starting the alarm.
    private static void startAlarm(Context context, MediaPlayer player) throws IOException {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        // do not play alarms if stream volume is 0 (typically because ringer mode is silent).
        if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
            player.setAudioStreamType(AudioManager.STREAM_ALARM);
            player.setLooping(true);
            player.prepare();
            audioManager.requestAudioFocus(null,
                    AudioManager.STREAM_ALARM, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            player.start();
        }
    }

    private static void setDataSourceFromResource(Context context, MediaPlayer player, int res)
            throws IOException {
        AssetFileDescriptor afd = context.getResources().openRawResourceFd(res);
        if (afd != null) {
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
        }
    }





}
