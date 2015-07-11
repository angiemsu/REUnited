package com.harman.wirelessomni;

import java.util.List;

import com.harman.hkwirelessapi.HKErrorCode;
import com.harman.hkwirelessapi.HKPlayerState;
import com.harman.hkwirelessapi.HKWirelessListener;
import com.harman.hkwirelesscore.HKWirelessUtil;
import com.harman.hkwirelesscore.PcmCodecUtil;
import com.harman.hkwirelesscore.Util;
import com.harman.hkwirelesscore.Util.DeviceData;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.widget.Toast;

public class MainActivity extends FragmentActivity  {
	
	private ViewPager mViewPager = null;

	public final static int TAB_INDEX_DEVICELIST = 0;  
	public final static int TAB_INDEX_MUSICPLAYER = 1;  
	public final static int TAB_INDEX_SOUNDRECORDER = 2;  
	public final static int TAB_COUNT = 3;
	
	private static HKWirelessUtil hkwireless = HKWirelessUtil.getInstance();
	private static PcmCodecUtil pcmCodec = PcmCodecUtil.getInstance();

	private DeviceListFragment deviceListFragment = null;
	private MusicPlayerFragment musicPlayerFragment = null;
	private SoundRecorderFragment soundRecorderFragment = null;
	
	private final String ERROR_CODE = "error_code";
	private final String ERROR_MSG = "error_msg";
	private int lastErrorCode = -1;
	
	private Context context = null;
	
	public Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			Bundle bundle = msg.getData();
			String errorMsg = bundle.getString(ERROR_MSG);
			int errorCode = bundle.getInt(ERROR_CODE, -1);
			if (lastErrorCode == errorCode && errorCode == HKErrorCode.ERROR_DISC_TIMEOUT.ordinal())
				return;
			Toast.makeText(context, errorMsg, 1000).show();
			lastErrorCode = errorCode;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		context = this;
		
		hkwireless.registerHKWirelessControllerListener(new HKWirelessListener(){

			@Override
			public void onPlayEnded() {
				// TODO Auto-generated method stub
				Util.getInstance().setMusicTimeElapse(0);
				Log.i("HKWirelessListener","onPlayEnded");
				if (musicPlayerFragment != null) {
					Message msg = new Message();
					msg.what = musicPlayerFragment.CMD_NEXT;
					musicPlayerFragment.handler.sendMessage(msg);
				}
				
				if (soundRecorderFragment != null) {
					Message msg = new Message();
					msg.what = soundRecorderFragment.CMD_STOP_PLAYING;
					soundRecorderFragment.handler.sendMessage(msg);
				}
			}

			@Override
			public void onPlaybackStateChanged(int arg0) {
				// TODO Auto-generated method stub
				if (arg0 == HKPlayerState.EPlayerState_Stop.ordinal())
					Util.getInstance().setMusicTimeElapse(0);
			}

			@Override
			public void onPlaybackTimeChanged(int arg0) {
				// TODO Auto-generated method stub
				Util.getInstance().setMusicTimeElapse(arg0);
			}

			@Override
			public void onVolumeLevelChanged(long deviceId, int deviceVolume,
					int avgVolume) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onDeviceStateUpdated(long deviceId, int reason) {
				// TODO Auto-generated method stub
				Util.getInstance().updateDeviceInfor(deviceId);
				if (deviceListFragment != null) {
					deviceListFragment.handler.sendMessage(new Message());
				}
				if (!Util.getInstance().hasDeviceConnected()) {
					if (musicPlayerFragment != null) {
						Message msg = new Message();
						msg.what = musicPlayerFragment.CMD_STOP;
						musicPlayerFragment.handler.sendMessage(msg);
					}
					
					if (soundRecorderFragment != null) {
						Message msg = new Message();
						msg.what = soundRecorderFragment.CMD_STOP_PLAYING;
						soundRecorderFragment.handler.sendMessage(msg);
					}
				}
			}

			@Override
			public void onErrorOccurred(int errorCode, String errorMesg) {
				// TODO Auto-generated method stub
				Log.i("HKWirelessListener","hkwErrorOccurred,errorCode="+errorCode+",errorMesg="+errorMesg);

				Message errMsg = new Message();
				Bundle bundle = new Bundle();
				bundle.putInt(ERROR_CODE, errorCode);
				bundle.putString(ERROR_MSG, errorMesg);
				errMsg.setData(bundle);
				handler.sendMessage(errMsg);
				
				if (musicPlayerFragment != null) {
					Message msg = new Message();
					if (errorCode == HKErrorCode.ERROR_MEDIA_UNSUPPORTED.ordinal()) {
						msg.what = musicPlayerFragment.CMD_NEXT;
						Bundle data = new Bundle();
						data.putInt(musicPlayerFragment.SWITCH_NEXT_FLAG, 1);
						msg.setData(data);
					} else {
						msg.what = musicPlayerFragment.CMD_STOP;
					}
					musicPlayerFragment.handler.sendMessage(msg);
				}
				if (soundRecorderFragment != null) {
					Message msg = new Message();
					msg.what = soundRecorderFragment.CMD_STOP_PLAYING;
					soundRecorderFragment.handler.sendMessage(msg);
				}
			}
		});

		if (!hkwireless.isInitialized()) {
			hkwireless.initializeHKWirelessController();
			if (hkwireless.isInitialized()) {
				Toast.makeText(this, "Wireless controller init success", 1000).show();
			} else {
				Toast.makeText(this, "Wireless controller init fail", 1000).show();
			}
		}

		getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		getActionBar().setDisplayShowTitleEnabled(false);
		getActionBar().setDisplayShowHomeEnabled(false);
		
		mViewPager = (ViewPager)findViewById(R.id.pager);
		getFragmentManager();
		
		mViewPager.setAdapter(new viewPagerAdapter(getSupportFragmentManager()));  
		mViewPager.setOnPageChangeListener(new pagerListener());  
		mViewPager.setCurrentItem(TAB_INDEX_DEVICELIST);
		
		setupDeviceList();
		setupMusicPlayer();
		setupSoundRecorder();
	}

	private void setupDeviceList(){
		Tab tab = this.getActionBar().newTab();
		tab.setContentDescription(getString(R.string.device_list));
		tab.setText(getString(R.string.device_list));
		tab.setTabListener(mTabListener);
		getActionBar().addTab(tab);
	}
	
	private void setupMusicPlayer(){
		Tab tab = this.getActionBar().newTab();
		tab.setContentDescription(getString(R.string.music_player));
		tab.setText(getString(R.string.music_player));
		tab.setTabListener(mTabListener);
		getActionBar().addTab(tab);
	}
	
	private void setupSoundRecorder(){
		Tab tab = this.getActionBar().newTab();
		tab.setContentDescription(getString(R.string.sound_recorder));
		tab.setText(getString(R.string.sound_recorder));
		tab.setTabListener(mTabListener);
		getActionBar().addTab(tab);
	}
	
	private final TabListener mTabListener = new TabListener() {
		private final static String TAG = "TabListener";
		
		@Override
		public void onTabReselected(Tab tab, android.app.FragmentTransaction ft) {
			// TODO Auto-generated method stub
		}
		
		@Override
		public void onTabSelected(Tab tab, android.app.FragmentTransaction ft) {
			// TODO Auto-generated method stub
			if (mViewPager != null)
				mViewPager.setCurrentItem(tab.getPosition());
		}
		
		@Override
		public void onTabUnselected(Tab tab, android.app.FragmentTransaction ft) {
			// TODO Auto-generated method stub
		}
	}; 
	
	class pagerListener implements OnPageChangeListener{
		@Override
		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub
		}
		
		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub
		}
		
		@Override
		public void onPageSelected(int arg0) {
			getActionBar().selectTab(getActionBar().getTabAt(arg0));
		}
	}
	
	public class viewPagerAdapter extends FragmentPagerAdapter {
		
		public viewPagerAdapter(FragmentManager fm) {
			super(fm);
			// TODO Auto-generated constructor stub
		}
		
		@Override
		public Fragment getItem(int arg0) {
			// TODO Auto-generated method stub
			switch (arg0) {
			case TAB_INDEX_DEVICELIST:
				return (deviceListFragment = new DeviceListFragment());
			case TAB_INDEX_MUSICPLAYER:
				return (musicPlayerFragment = new MusicPlayerFragment());
			case TAB_INDEX_SOUNDRECORDER:
				return (soundRecorderFragment = new SoundRecorderFragment());
			}
			throw new IllegalStateException("No fragment at position " + arg0);
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return TAB_COUNT;
		}
	}  

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
		List<DeviceData> devices = Util.getInstance().getDevices();

        switch (keyCode) {
        case KeyEvent.KEYCODE_VOLUME_UP:
        	for (int i=0; i<devices.size(); i++) {
        		if (Util.getInstance().getDeviceStatus(i)) {
        			long deviceId = devices.get(i).deviceObj.deviceId;
        			int volume = pcmCodec.getDeviceVolume(deviceId);
        			volume+=5;
        			if (volume <= pcmCodec.getMaximumVolumeLevel())
        				pcmCodec.setVolumeDevice(deviceId, volume);
        		}
        	}
            return true;

        case KeyEvent.KEYCODE_VOLUME_DOWN:
        	for (int i=0; i<devices.size(); i++) {
        		if (Util.getInstance().getDeviceStatus(i)) {
        			long deviceId = devices.get(i).deviceObj.deviceId;
        			int volume = pcmCodec.getDeviceVolume(deviceId);
        			volume -= 5;
        			if (volume > 0)
        				pcmCodec.setVolumeDevice(deviceId, volume);
        		}
        	}
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
	
	public boolean isMusicPlayerWorking()
	{
		//Log
		if (musicPlayerFragment == null) {
			return false;
		} else {
			return musicPlayerFragment.isMusicPlaying();
		}
	}
	
	public boolean isSoundRecorderWorking()
	{
		if (soundRecorderFragment == null) {
			return false;
		} else {
			return soundRecorderFragment.isSndRecWorking();
		}
	}
	
	public String getSoundRecorderDir()
	{
		StringBuilder path = new StringBuilder();
		if (Environment.getExternalStorageState()
				.equals(Environment.MEDIA_MOUNTED)) {
			path.append(Environment.getExternalStorageDirectory().toString());
			path.append("/wirelessomni/soundRecorder");
			return path.toString();
		}
		return null;
	}
}
