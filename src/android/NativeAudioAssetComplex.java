//
//
//  NativeAudioAssetComplex.java
//
//  Created by Sidney Bofah on 2014-06-26.
//

package com.rjfun.cordova.plugin.nativeaudio;

import java.io.IOException;
import java.util.concurrent.Callable;

import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;

public class NativeAudioAssetComplex implements OnLoadCompleteListener {
    private static final int INVALID = 0;
    private static final int PREPARED = 1;
    private static final int PENDING_PLAY = 2;
    private static final int PLAYING = 3;
    private static final int PENDING_LOOP = 4;
    private static final int LOOPING = 5;
    
    private SoundPool sp;
    private float volume;
    private int streamID = 0;
    private int soundID = 0;
    
    private int state;
    Callable<Void> completeCallback;
    
    public NativeAudioAssetComplex(AssetFileDescriptor afd, float volume)  throws IOException
    {
        state = INVALID;
        
        if (android.os.Build.VERSION.SDK_INT >= 21){
            sp = new SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(
                                new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build()
                                )
            .build();
        } else {
            sp = new SoundPool(1, AudioAttributes.USAGE_VOICE_COMMUNICATION, 0);
        }
        
        this.soundID = sp.load(afd,1);
        sp.setOnLoadCompleteListener(this);
        this.volume = volume;
    }
    
    public void play(Callable<Void> completeCb) throws IOException
    {
        completeCallback = completeCb;
        invokePlay( false );
    }
    
    private void invokePlay( Boolean loop )
    {
        if ( state ==  PLAYING || state == LOOPING)
        {
            sp.stop(this.streamID);
        }
        
        this.streamID = sp.play(this.soundID,this.volume,this.volume,0,loop?-1:0,1);
        state = loop?LOOPING:PLAYING;
    }
    
    public boolean pause()
    {
        if ( state ==  PLAYING || state == LOOPING) {
            sp.pause(this.streamID);
            state = INVALID;
            return true;
        }
        return false;
    }
    
    public void resume()
    {
        sp.resume(this.streamID);
    }
    
    public void stop()
    {
        if ( state ==  PLAYING || state == LOOPING) {
            sp.stop(this.streamID);
            state = INVALID;
        }
    }
    
    public void setVolume(float volume)
    {
        this.volume = volume;
        if ( state ==  PLAYING || state == LOOPING) {
            sp.setVolume(this.streamID,volume,volume);
        }
    }
    
    public void loop() throws IOException
    {
        invokePlay( true );
    }
    
    public void unload() throws IOException
    {
        this.stop();
        sp.release();
    }
    
    public void onLoadComplete(SoundPool soundPool, int sampleId, int status)
    {
    }
    
}
