package com.harman.hkwirelesscore;

import com.harman.hkwirelessapi.AudioCodecHandler;
import com.harman.hkwirelessapi.HKPlayerState;


public class PcmCodecUtil {

    private AudioCodecHandler pcmCodec = new AudioCodecHandler();
    
    private static PcmCodecUtil instance = new PcmCodecUtil();
    
    private PcmCodecUtil() {

    }
    
    public static PcmCodecUtil getInstance() {
    	return instance;
    }
    
    /*public void configCAFAudioPlayer(String url){
    	pcmCodec.configCAFAudioPlayer(url);
    }*/

    public void play(String url, int timeElapsed){
    	/*
    	if (timeElapsed != 0)
    		pcmCodec.playCAF(url, songName, true);
    	else
    		pcmCodec.playCAF(url, songName, false);
    		*/
    	String songName = null;
    	int indx = url.lastIndexOf("/");
    	songName = url.substring(indx);
    	pcmCodec.playCAFFromCertainTime(url, songName, timeElapsed);
    	//pcmCodec.play(url, timeElapsed);
    }

    public void pause(){
    	pcmCodec.pause();
    }

    public void stop(){
    	pcmCodec.stop();
    }

    public void playWAV(String url){
    	pcmCodec.playWAV(url);
    }

    public boolean isPlaying(){
        return pcmCodec.isPlaying();
    }

    public HKPlayerState getPlayerState(){
        return pcmCodec.getPlayerState();
    }

    public void setVolumeAll(int volume){
    	pcmCodec.setVolumeAll(volume);
    }

    public void setVolumeDevice(long deviceId, int volume){
    	pcmCodec.setVolumeDevice(deviceId, volume);
    }

    public int getVolume(){
        return pcmCodec.getVolume();
    }

    public int getDeviceVolume(long deviceId){
        return pcmCodec.getDeviceVolume(deviceId);
    }

    public int getMaximumVolumeLevel(){
        return pcmCodec.getMaximumVolumeLevel();
    }
}
