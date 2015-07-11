package com.harman.wirelessomni;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.harman.hkwirelesscore.PcmCodecUtil;
import com.harman.hkwirelesscore.Util;
import com.harman.hkwirelesscore.Util.DeviceData;
import com.harman.hkwirelesscore.Util.MusicFormat;
import com.harman.wirelessomni.DeviceListFragment.ViewHolder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MusicPlayerFragment extends Fragment {
	
	static class Mp3Info {
		long id;
		String url;
		String title;
		
		Mp3Info() {
		}
		
		void setId(long id) {
			this.id = id;
		}
		long getId() {
			return id;
		}

		void setUrl(String url) {
			this.url = url;
		}
		String getUrl() {
			return url;
		}

		void setTitle(String title) {
			this.title = title;
		}
		String getTitle() {
			return title;
		}
	}
	
	private TextView tvMusicName = null;
	private ImageButton btnPrev = null;
	private ImageButton btnNext = null;
	private ImageButton btnPlay = null;
	
	private ListView lvMusic = null;
	private SimpleAdapter mAdapter;
	private List<Mp3Info> mp3Infos = new ArrayList<Mp3Info>();  
	
	private boolean canPlay = true;//false;
	private boolean canPause= false;
	
	private int listPosition = 0;
	
	//private static final int TYPE_MP3 = 1;
	//private static final int TYPE_WAV = 2;
	
	public static final int CMD_STOP = 1;
	public static final int CMD_NEXT = 2;
	public final String SWITCH_NEXT_FLAG = "SWITCH_NEXT";

	private PcmCodecUtil pcmCodec = PcmCodecUtil.getInstance();
	
	private boolean allowAutoSwitchSong = false;
	
	MainActivity activity = null;
	
	public Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what){
			case CMD_STOP:
				if (canPause) {
					pauseMusic();
					Toast.makeText(getActivity(), "No device connected", 1000).show();
				}
				break;
			case CMD_NEXT:
				Bundle bundle = msg.getData();
				int switchFlag = 0;
				if (bundle != null) {
					switchFlag = bundle.getInt(SWITCH_NEXT_FLAG, 0);
				}
				if (canPause && (allowAutoSwitchSong || switchFlag > 0)) {
					nextMusic();
				}
				if (allowAutoSwitchSong == false) {
					allowAutoSwitchSong = true;
				}
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
		View fragmentView = inflater.inflate(R.layout.musicplayer, container, false);
		return fragmentView;
	}

	
	@Override  
    public void onActivityCreated(Bundle savedInstanceState) {  
        super.onActivityCreated(savedInstanceState); 
        
        initMusicPlayerService();
        
        tvMusicName = (TextView)(getActivity().findViewById(R.id.sing_name));
		btnPrev = (ImageButton)(getActivity().findViewById(R.id.previous_music));
		btnNext = (ImageButton)(getActivity().findViewById(R.id.next_music));
		btnPlay = (ImageButton)(getActivity().findViewById(R.id.play_music));
		lvMusic = (ListView)(getActivity().findViewById(R.id.music_list));
		
		ViewOnclickListener ViewOnClickListener = new ViewOnclickListener();  
		btnPrev.setOnClickListener(ViewOnClickListener);
		btnNext.setOnClickListener(ViewOnClickListener);
		btnPlay.setOnClickListener(ViewOnClickListener);  		
		initMp3Infos(getActivity()); 
		
		lvMusic.setOnItemClickListener(new musicListItemClickListener());  
		if (!mp3Infos.isEmpty())
			lvMusic.setAdapter(new MusicAdapter(getActivity()));
    }  

	private List<String> getData(){
		List<String> data = new ArrayList<String>();
		for (Mp3Info info:mp3Infos) {
			data.add(info.title);
		}
		return data;
	}

	private class ViewOnclickListener implements OnClickListener {  
		Intent intent = new Intent();
		@Override
		public void onClick(View v) {
			switch(v.getId()) {
			case R.id.play_music:
				allowAutoSwitchSong = false;
				if (canPlay) {
					playMusic();
				} else if (canPause) {
					pauseMusic();
				}
				break;
			case R.id.previous_music:
				allowAutoSwitchSong = false;
				prevMusic();
				break;
			case R.id.next_music:
				allowAutoSwitchSong = false;
				nextMusic();
				break;
			}
		}
	}
	
	private class musicListItemClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			if(!mp3Infos.isEmpty()) {
				//Mp3Info mp3Info = mp3Infos.get(position);
				listPosition = position;
				allowAutoSwitchSong = false;
				playMusic();
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
	
	private void playMusic() {
		if (mp3Infos.isEmpty())
			return;
		if (Util.getInstance().getDevices().size() == 0) {
			Toast.makeText(getActivity(), "No device connected", 1000).show();
			return;
		}
		if (isSessionEmpty()) {
			Toast.makeText(getActivity(), "No device in use", 1000).show();
			return;
		}
		if (activity.isSoundRecorderWorking()) {
			Toast.makeText(getActivity(), "Sound recorder is playing or recording", 1000).show();
			return;
		}
		
		Mp3Info mp3Info = mp3Infos.get(listPosition);
		Log.i("musicPlayer","wangxianghai@listPosition="+listPosition+"mp3Info="+mp3Info.getUrl());
		tvMusicName.setText(mp3Info.getTitle());
		if (canPlay) {
			btnPlay.setImageResource(R.drawable.pause_btn);
			canPlay = false;
			canPause = true;
		}
		Intent intent = new Intent();
		intent.putExtra(Util.MSG_URL_MUSIC, mp3Info.getUrl());
		intent.putExtra(Util.MSG_TYPE_MUSIC, Util.MSG_PCM_PLAY);
		intent.setAction(Util.MUSICPLAYER); 
		intent.setPackage(getActivity().getPackageName());
		getActivity().startService(intent);
	}

	private void initMusicPlayerService() {
		Intent intent = new Intent();
		intent.putExtra(Util.MSG_TYPE_MUSIC, Util.MSG_PCM_INIT);
		intent.setAction(Util.MUSICPLAYER); 
		intent.setPackage(getActivity().getPackageName());
		getActivity().startService(intent);
	}
	
	private void pauseMusic() {
		if (mp3Infos.isEmpty())
			return;
		Mp3Info mp3Info = mp3Infos.get(listPosition);
		if (canPause) {
			btnPlay.setImageResource(R.drawable.play_btn);
			canPause = false;
			canPlay = true;
		}
		Intent intent = new Intent();
		intent.putExtra(Util.MSG_URL_MUSIC, mp3Info.getUrl());
		intent.putExtra(Util.MSG_TYPE_MUSIC, Util.MSG_PCM_PAUSE);
		intent.setAction(Util.MUSICPLAYER);
		intent.setPackage(getActivity().getPackageName());
		getActivity().startService(intent);
	}
	
	private void nextMusic() {
		if (mp3Infos.isEmpty())
			return;
		int num = mp3Infos.size();
		if (++listPosition >= num)
			listPosition = 0;
		Mp3Info mp3Info = mp3Infos.get(listPosition);
		tvMusicName.setText(mp3Info.getTitle());
		if (canPause)
			playMusic();
	}
	
	private void prevMusic() {
		if (mp3Infos.isEmpty())
			return;
		if (--listPosition < 0)
			listPosition = mp3Infos.size()-1;
		Mp3Info mp3Info = mp3Infos.get(listPosition);
		tvMusicName.setText(mp3Info.getTitle());
		if (canPause)
			playMusic();
	}

	public void initMp3Infos(Context context) {
		mp3Infos.clear();
		Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		if (cursor == null)
			return;
		
		for (int i = 0; i < cursor.getCount(); i++) {
			cursor.moveToNext(); 
			Mp3Info mp3Info = new Mp3Info();
			long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
			String title = cursor.getString((cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))); 
			String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
			int isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));
			if (isMusic != 0 && isMp3Type(url) && !isSndRecFile(url)) {
				mp3Info.setId(id);
				mp3Info.setTitle(title);
				mp3Info.setUrl(url);
				mp3Infos.add(mp3Info);
			} 
		}
	}

	public boolean isMp3Type(String url) {
		int lastIndex = url.lastIndexOf(".");
		String suffix = url.substring(lastIndex, url.length());
		for (Map.Entry<String, MusicFormat> entry: Util.supportMusicFormat.entrySet()) {
			if (suffix.equalsIgnoreCase(entry.getKey())) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isSndRecFile(String url) {
		String sndRecPath = activity.getSoundRecorderDir();
		if (sndRecPath!=null && url.contains(sndRecPath)) {
			return true;
		} else {
			return false;
		}
	}

	public String getMp3fileName(Mp3Info info) {
		String url = info.getUrl();
		int lastDiv = url.lastIndexOf("/");
		return url.substring(lastDiv+1, url.length());
	}

	public final class ViewHolder{
        public ImageView img;
        public TextView title;
        public TextView name;
    }
	class MusicAdapter extends BaseAdapter{
        private LayoutInflater mInflater;
        private Context context = null;

        public MusicAdapter(Context context){
            this.mInflater=LayoutInflater.from(context);
            this.context = context;
        }
        
        public int getCount() {
            return mp3Infos.size();
        }
        
        public Object getItem(int position) {
            return mp3Infos.get(position);
        }
        
        public long getItemId(int position) {
            return position;
        }
        
        public View getView(int position, View convertView, ViewGroup parent) {
            final int p = position;
            ViewHolder holder=null;
            
            if(convertView == null){
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.musicplayer_list_item, null);
                holder.img = (ImageView)convertView.findViewById(R.id.music_img);
                holder.title = (TextView)convertView.findViewById(R.id.music_title);
                holder.name = (TextView)convertView.findViewById(R.id.music_name);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.img.setImageResource(R.drawable.music_item);
            final String title = (String)mp3Infos.get(position).getTitle();
            holder.title.setText(title);
            final String name = getMp3fileName(mp3Infos.get(position));
            holder.name.setText(name);

            return convertView;
        }
    }
	
	public boolean isMusicPlaying()
	{
		return canPause;
	}
}
