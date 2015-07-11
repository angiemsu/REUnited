package com.harman.hkwirelesscore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

import com.harman.hkwirelessapi.DeviceObj;

public class Util {
	
	public static final int MSG_PCM_INIT = 1;
	public static final int MSG_PCM_PLAY = 2;
	public static final int MSG_PCM_PAUSE = 3;
	public static final int MSG_PCM_STOP = 4;
	public static final int MSG_PCM_SNDREC_PLAY = 5;
	public static final int MSG_PCM_SNDREC_RECORD = 6;
	public static final int MSG_PCM_SNDREC_STOP =7;
	
	public static final String MSG_TYPE_MUSIC = "msg";
	public static final String MSG_URL_MUSIC = "url";
	public static final String MSG_FORMAT_MUSIC = "format";
	
	public static final String MUSICPLAYER = "com.harman.wirelessomni.MusicPlayer";

	public enum MusicFormat {
		MUSIC_TYPE_MP3,
		MUSIC_TYPE_WAV,
		MUSIC_TYPE_AAC,
		MUSIC_TYPE_FLAC,
		MUSIC_TYPE_M4A,
		MUSIC_TYPE_OGG
	}
	public static final Map<String,MusicFormat> supportMusicFormat = new HashMap<String,MusicFormat>();
	static {
		supportMusicFormat.put(".mp3", MusicFormat.MUSIC_TYPE_MP3);
		supportMusicFormat.put(".wav", MusicFormat.MUSIC_TYPE_WAV);
		supportMusicFormat.put(".aac", MusicFormat.MUSIC_TYPE_AAC);
		supportMusicFormat.put(".flac", MusicFormat.MUSIC_TYPE_FLAC);
		supportMusicFormat.put(".m4a", MusicFormat.MUSIC_TYPE_M4A);
		supportMusicFormat.put(".ogg", MusicFormat.MUSIC_TYPE_OGG);
	};
	
	public class DeviceData {
		public DeviceObj deviceObj;
		public Boolean status;
	}
	
	private List<DeviceData> devices = new ArrayList<DeviceData>();
	private HKWirelessUtil hkwireless = HKWirelessUtil.getInstance();
	private static Util instance = new Util();
	private static int musicTimeElapse = 0;
	
	private static boolean isInit = false;
	
	private Util() {
	}
	
	public static Util getInstance() {
		return instance;
	}

	public void initDeviceInfor() {
		synchronized (this) {
			devices.clear();
			
			if (hkwireless.isInitialized() == false)
				return;
			
			for (int i=0; i<hkwireless.getDeviceCount(); i++) {
				DeviceData device = new DeviceData();
				device.deviceObj = hkwireless.getDeviceInfoByIndex(i);
				device.status = hkwireless.isDeviceActive(device.deviceObj.deviceId);
				devices.add(device);
			}
			isInit = true;
		}
	}
	
	public void updateDeviceInfor(long deviceId) {
		synchronized (this) {
			int i;

			for (i=0; i<devices.size(); i++) {
				DeviceData device = devices.get(i);
				if (device.deviceObj.deviceId == deviceId) {
					device.deviceObj = hkwireless.findDeviceFromList(deviceId);
					if (device.deviceObj == null) {
						devices.remove(i);
					} else {
						device.status = hkwireless.isDeviceActive(device.deviceObj.deviceId);
						devices.set(i, device);
					}
					break;
				}
			}
			if (i >= devices.size()) {
				DeviceData device = new DeviceData();
				device.deviceObj = hkwireless.findDeviceFromList(deviceId);
				if (device.deviceObj == null) {
					return;
				}
				device.status = hkwireless.isDeviceActive(device.deviceObj.deviceId);
				devices.add(device);
			}
		}
	}
	
	public Boolean hasDeviceConnected() {
		synchronized (this) {
			int i;

			for (i=0; i<devices.size(); i++) {
				if (devices.get(i).status)
					return true;
			}
			return false;
		}
	}

	public List<DeviceData> getDevices() {
		synchronized (this) {  
			return devices;
		}
	}
	
	/*public ArrayList<Boolean> getDevicesStatus() {
		synchronized (this) {  
			return checkedItem;
		}
	}

	public void setDeviceStatus(int position, boolean status) {
		synchronized (this) {
			checkedItem.set(position, status);
		}
	}*/

	public boolean getDeviceStatus(int position) {
		synchronized (this) {
			return devices.get(position).status;
		}
	}
	
	public void setMusicTimeElapse(int time) {
		musicTimeElapse = time;
	}
	public int getMusicTimeElapse() {
		return musicTimeElapse;
	}
	
	public boolean removeDeviceFromSession(long deviceid){
		boolean ret = hkwireless.removeDeviceFromSession(deviceid);
		
		if (ret == true) {
			synchronized (this) {
				int i;

				for (i=0; i<devices.size(); i++) {
					DeviceData device = devices.get(i);
					if (device.deviceObj.deviceId == deviceid) {
						device.deviceObj = hkwireless.findDeviceFromList(deviceid);
						if (device.deviceObj == null) {
							devices.remove(i);
						} else {
							device.status = hkwireless.isDeviceActive(device.deviceObj.deviceId);
							devices.set(i, device);
						}
						break;
					}
				}
			}
		}

        return ret;
    }
	
	public boolean addDeviceToSession(long deviceid){
        boolean ret = hkwireless.addDeviceToSession(deviceid);
		
		if (ret == true) {
			synchronized (this) {
				int i;

				for (i=0; i<devices.size(); i++) {
					DeviceData device = devices.get(i);
					if (device.deviceObj.deviceId == deviceid) {
						device.deviceObj = hkwireless.findDeviceFromList(deviceid);
						if (device.deviceObj == null) {
							devices.remove(i);
						} else {
							device.status = hkwireless.isDeviceActive(device.deviceObj.deviceId);
							devices.set(i, device);
						}
						break;
					}
				}
			}
		}
        return ret;
    }
}
