package com.harman.wirelessomni;

import java.io.File;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;

public class BootBroadcast extends BroadcastReceiver {

	private Context context;
	private final String ctlFileName = "sndRecCtl.txt";
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		this.context = context;
		/*StringBuilder path = new StringBuilder();
		if (Environment.getExternalStorageState()
				.equals(Environment.MEDIA_MOUNTED)) {
			path.append(Environment.getExternalStorageDirectory().toString());
			path.append("/wirelessomni/soundRecorder");
			path.append("/"+ctlFileName);
			File file = new File(path.toString());
			if (file.exists())
				file.delete();
			
		}*/
	
	}

}
