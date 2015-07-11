package com.harman.wirelessomni;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.harman.hkwirelesscore.Util;
import com.harman.hkwirelesscore.Util.DeviceData;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class SoundRecorderFragment extends Fragment {
	private TextView tvName = null;
	private TextView tvTime = null;
	private ListView lvSounds = null;

	private Button btnRecord = null;
	private Button btnStop = null;
	private Button btnPlay = null;
	
	private static final String[] supportType = {
		".wav"
	};
	
	private String currentFormat = ".wav";
	
	private enum RecorderState
    {
        RECORD_STATE_RECORDING,
        RECORD_STATE_PLAYING,
        RECORD_STATE_NONE
    }
	private SoundAdapter adapter = null;
	
	static class SoundInfo {
		String url;
		int duration;
		
		SoundInfo() {
		}

		void setUrl(String url) {
			this.url = url;
		}
		String getUrl() {
			return url;
		}

		void setDuration(int duration) {
			this.duration = duration;
		}
		int getDuration() {
			return duration;
		}
	}
	
	List<SoundInfo> soundFile = new ArrayList<SoundInfo>();
	private int currentPosition = 0;
	
	private String currentUrl = null;
	
	private long timeMsec = 0;
	final Handler mHandler = new Handler();
    Runnable mUpdateTimer = new Runnable() {
        public void run() { updateSndRecTime(); }
    };
	private RecorderState recorderState = RecorderState.RECORD_STATE_NONE;
	
	private final String ctlFileName = "sndRecCtl.txt";
	private final String urlFlag = "URL:";
	private final String durFlag = "duration:";
	
	MainActivity activity = null;

	public static final int CMD_STOP = 1;
	public static final int CMD_STOP_PLAYING = 1;
	
	public Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what){
			case CMD_STOP_PLAYING:
				if (recorderState == RecorderState.RECORD_STATE_PLAYING)
					stop();
				break;
			default:
				break;
			}
		}
	};
	
	@Override 
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = (MainActivity)activity;
	}
	
	@Override  
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View fragmentView = inflater.inflate(R.layout.soundrecorder, container, false);
		return fragmentView;
	}
	
	@Override  
    public void onActivityCreated(Bundle savedInstanceState) {  
        super.onActivityCreated(savedInstanceState);  
        
        tvName = (TextView)(getActivity().findViewById(R.id.sound_name));
        tvTime = (TextView)(getActivity().findViewById(R.id.sing_time));
        
        btnPlay = (Button)(getActivity().findViewById(R.id.play_btn));
        btnRecord = (Button)(getActivity().findViewById(R.id.record_btn));
        btnStop = (Button)(getActivity().findViewById(R.id.stop_btn));
        
        lvSounds = (ListView)(getActivity().findViewById(R.id.sound_list));
		
		ViewOnclickListener ViewOnClickListener = new ViewOnclickListener();  
		btnPlay.setOnClickListener(ViewOnClickListener);
		btnRecord.setOnClickListener(ViewOnClickListener);
		btnStop.setOnClickListener(ViewOnClickListener); 

		initSoundInfos(getActivity()); 
		
		lvSounds.setOnItemClickListener(new SoundListItemClickListener());
		adapter = new SoundAdapter(getActivity());
		lvSounds.setAdapter(adapter);
		
		updateSndRecTime();
    }
	
	private void updateSndRecTime() {
		if (recorderState != RecorderState.RECORD_STATE_NONE) {
            String str = showTime((int)timeMsec);
            tvTime.setText(str);
            timeMsec += 1000;
            mHandler.postDelayed(mUpdateTimer, 1000);
		} else {
			 int duration;
			if (soundFile.size() > 0) {
				String url = soundFile.get(currentPosition).getUrl();
				duration = soundFile.get(currentPosition).getDuration();
			} else {
				duration = 0;
			}
            String str = showTime(duration);
            tvTime.setText(str);
		}
	}

	private class ViewOnclickListener implements OnClickListener {  
		Intent intent = new Intent();
		@Override
		public void onClick(View v) {
			switch(v.getId()) {
			case R.id.play_btn:
				if (recorderState == RecorderState.RECORD_STATE_NONE) {
					playSound();
				}
				break;
			case R.id.record_btn:
				if (recorderState == RecorderState.RECORD_STATE_NONE) {
					recordSound();
				}
				break;
			case R.id.stop_btn:
				stop();
				break;
			}
		}
	}
	
	private class SoundListItemClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			if(!soundFile.isEmpty()) {
				String path = soundFile.get(position).getUrl();
				currentPosition = position;
				playSound();
			}
			return;
		}
	}
	
	public boolean isSessionEmpty() {
	    List<DeviceData> devices = Util.getInstance().getDevices();

	    if (devices.size() <= 0)
	    	return true;
	    
	    for (int i=0; i<devices.size(); i++) {
	    	if (Util.getInstance().getDeviceStatus(i))
	    		return false;
	    }
	    return true;
	}
	
	public void playSound() {
		timeMsec = 0;
		
		if (Util.getInstance().getDevices().size() == 0) {
			Toast.makeText(getActivity(), "No device connected", 1000).show();
			return;
		}
		if (isSessionEmpty()) {
			Toast.makeText(getActivity(), "No device in use", 1000).show();
			return;
		}
		if (activity.isMusicPlayerWorking()) {
			Toast.makeText(getActivity(), "Music player is playing", 1000).show();
			return;
		}
		String url = soundFile.get(currentPosition).getUrl();
		int indx = url.lastIndexOf("/");
        final String title = url.substring(indx+1);
        tvName.setText(title);
        
        Intent intent = new Intent();
        intent.putExtra(Util.MSG_URL_MUSIC, url);
        intent.putExtra(Util.MSG_TYPE_MUSIC, Util.MSG_PCM_SNDREC_PLAY);
        intent.setAction(Util.MUSICPLAYER); 
        intent.setPackage(getActivity().getPackageName());
        getActivity().startService(intent);
        
        recorderState = RecorderState.RECORD_STATE_PLAYING;
		btnRecord.setEnabled(false);
		updateSndRecTime();
	}
	
	public void recordSound() {
		if (activity.isMusicPlayerWorking()) {
			Toast.makeText(getActivity(), "Music player is playing", 1000).show();
			return;
		}

		StringBuilder filePath = new StringBuilder();
		StringBuilder fileName = new StringBuilder();

		String curentTime = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss").format(new Date(System.currentTimeMillis()));
		fileName.append(curentTime);
		fileName.append(currentFormat);
		tvName.setText(fileName.toString());
		
		filePath.append(getSoundRecorderFilePath());
		filePath.append("/");
		filePath.append(fileName);
		currentUrl = filePath.toString();
		
		currentPosition = soundFile.size();
		SoundInfo sndInfo = new SoundInfo();
		sndInfo.setUrl(currentUrl);
		soundFile.add(sndInfo);
		adapter.notifyDataSetChanged();

		timeMsec = 0;
		
		Intent intent = new Intent();
		intent.putExtra(Util.MSG_URL_MUSIC, currentUrl);
		intent.putExtra(Util.MSG_TYPE_MUSIC, Util.MSG_PCM_SNDREC_RECORD);
		intent.setAction(Util.MUSICPLAYER);
		intent.setPackage(getActivity().getPackageName());
		getActivity().startService(intent);
		
		recorderState = RecorderState.RECORD_STATE_RECORDING;
		btnPlay.setEnabled(false);
		updateSndRecTime();
	}

	public void stop() {
		Intent intent = new Intent();
		intent.putExtra(Util.MSG_URL_MUSIC, currentUrl);
		
		if (recorderState == RecorderState.RECORD_STATE_RECORDING) {
			intent.putExtra(Util.MSG_TYPE_MUSIC, Util.MSG_PCM_SNDREC_STOP);
			SoundInfo sndInfo = soundFile.get(currentPosition);
			sndInfo.setUrl(currentUrl);
			sndInfo.setDuration((int) timeMsec);
			soundFile.set(currentPosition, sndInfo);
			updateSndRecCtlFile(sndInfo);
			adapter.notifyDataSetChanged();
		} else {
			intent.putExtra(Util.MSG_TYPE_MUSIC, Util.MSG_PCM_STOP);
		}
		
		recorderState = RecorderState.RECORD_STATE_NONE;
		intent.setAction(Util.MUSICPLAYER); 
		intent.setPackage(getActivity().getPackageName());
		getActivity().startService(intent);
		
		btnRecord.setEnabled(true);
		btnPlay.setEnabled(true);
		updateSndRecTime();
	}
	
	public String getSoundRecorderFilePath() {
		StringBuilder path = new StringBuilder();
		if (Environment.getExternalStorageState()
				.equals(Environment.MEDIA_MOUNTED)) {
			path.append(Environment.getExternalStorageDirectory().toString());
			path.append("/wirelessomni/soundRecorder");
			return path.toString();
		}
		return null;
	}

	void initSoundInfos(Context context) {
		String soundPath = getSoundRecorderFilePath();
		String[] fileUrl;
		
		List<SoundInfo> ctlSndInfo = new ArrayList<SoundInfo>();
		SoundInfo sndInfo = null;

		File file =new File(soundPath);    
		if  (!file.exists() || !file.isDirectory()) {       
		    file.mkdirs();
		}
		
		String ctlFileUrl = soundPath + "/" + ctlFileName;
		File ctlFile = new File(ctlFileUrl);
		if (!ctlFile.exists()) {
			try {
				ctlFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		InputStream instream;
		try {
			instream = new FileInputStream(ctlFileUrl);
			if (instream != null) 
	        {
	            BufferedReader buffreader = new BufferedReader(new InputStreamReader(instream));
	            String line = null;
	            String path = null;
	            int duration = 0;
	            int indx1, indx2;

	            while ((line = buffreader.readLine()) != null) {
	            	indx1 = line.indexOf(urlFlag);
	            	indx2 = line.indexOf(durFlag);
	            	path = line.substring((indx1+urlFlag.length()), indx2);
	            	duration =Integer.parseInt(line.substring((indx2+durFlag.length()), line.length()));
	            	sndInfo = new SoundInfo();
	            	sndInfo.setUrl(path);
	            	sndInfo.setDuration(duration);
	            	ctlSndInfo.add(sndInfo);
	            }
	            
	            instream.close();
	        }
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int k=0; k<ctlSndInfo.size(); k++) {
			 SoundInfo ctlSnd = ctlSndInfo.get(k);
		}
		/*
		Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		if (cursor != null) {
			for (int i = 0; i < cursor.getCount(); i++) {
				cursor.moveToNext(); 
				sndInfo = new SoundInfo();
				String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
				int isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));
				int duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
				if (isMusic != 0 && url.contains(soundPath)) {
					sndInfo.setUrl(url);
					sndInfo.setDuration(duration);
					if (!ctlSndInfo.contains(sndInfo)) {
						ctlSndInfo.add(sndInfo);
						updateSndRecCtlFile(sndInfo);
					}
				} 
			}
		}
		*/

		currentPosition = 0;
		soundFile.clear();
		fileUrl = file.list();
		SoundInfo ctlSnd;

		for (int i=0; i<fileUrl.length; i++) {
			String path = fileUrl[i];
			String suffix = path.substring(path.length()-4, path.length());
			for (int j=0; j<supportType.length; j++) {
				if (suffix.equalsIgnoreCase(supportType[j])) {
					String url = soundPath+"/"+path;
					for (int k=0; k<ctlSndInfo.size(); k++) {
						 ctlSnd = ctlSndInfo.get(k);
						if (url.equalsIgnoreCase(ctlSnd.getUrl())) {
							sndInfo = new SoundInfo();
							sndInfo.setDuration(ctlSnd.getDuration());
							sndInfo.setUrl(ctlSnd.getUrl());
							soundFile.add(sndInfo);
							break;
						} 
					}
				} 
			}
		}
		updateSndRecTime();
		return;
	}
	
	public final class ViewHolder{
        public ImageView img;
        public TextView title;
        public TextView time;
    }
	class SoundAdapter extends BaseAdapter{
        private LayoutInflater mInflater;

        public SoundAdapter(Context context){
            this.mInflater=LayoutInflater.from(context);
        }
        
        public int getCount() {
            return soundFile.size();
        }
        
        public Object getItem(int position) {
            return soundFile.get(position);
        }
        
        public long getItemId(int position) {
            return position;
        }
        
        public View getView(int position, View convertView, ViewGroup parent) {
            final int p = position;
            ViewHolder holder=null;
            
            if(convertView == null){
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.soundrecorder_list_item, null);
                holder.img = (ImageView)convertView.findViewById(R.id.sound_img);
                holder.title = (TextView)convertView.findViewById(R.id.sound_title);
                holder.time = (TextView)convertView.findViewById(R.id.sound_time);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.img.setImageResource(R.drawable.soundrecorder);
            final String url = soundFile.get(position).getUrl();
            int indx = url.lastIndexOf("/");
            final String title = url.substring(indx+1);
            holder.title.setText(title);
            
            int duration;
            if (position == soundFile.size() - 1 && recorderState == RecorderState.RECORD_STATE_RECORDING) {
            	duration = 0;
            } else {
                duration = soundFile.get(position).getDuration();
            }
            
            String str = showTime(duration);
            holder.time.setText(str);

            return convertView;
        }
    }
	
	public boolean isSndRecWorking() {
		return (recorderState != RecorderState.RECORD_STATE_NONE);
	}
	
	public String showTime(int time) {  
		  time /= 1000;  
		  int minute = time/60;
		  int second = time % 60;
		  int hour = minute/60;
		  minute %= 60; 
		  return String.format("%02d:%02d:%02d", hour, minute, second);
	}
	
	private void updateSndRecCtlFile(SoundInfo sndInfo) {
		String ctlFileUrl = getSoundRecorderFilePath() + "/" + ctlFileName;
		File file = new File(ctlFileUrl);
		if (!file.exists())	{
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		FileOutputStream out;
		try {
			out = new FileOutputStream(file, true);
			//out = getActivity().openFileOutput(ctlFileUrl, getActivity().MODE_APPEND);
			String line = urlFlag + sndInfo.getUrl() + durFlag + sndInfo.getDuration() + "\n";
			out.write(line.getBytes());
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
