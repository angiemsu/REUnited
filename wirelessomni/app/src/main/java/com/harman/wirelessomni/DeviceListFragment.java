package com.harman.wirelessomni;

import com.harman.hkwirelessapi.DeviceObj;
import com.harman.hkwirelesscore.HKWirelessUtil;
import com.harman.hkwirelesscore.Util;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class DeviceListFragment extends Fragment {
	
	private ListView deviceList = null;
	private Button btnRefresh = null;
	private DeviceAdapter adapter;

	HKWirelessUtil khWireless = HKWirelessUtil.getInstance();

	public Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			adapter.notifyDataSetChanged();
		}
	};

	@Override  
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View fragmentView = inflater.inflate(R.layout.devicelist, container, false);
		return fragmentView;
	}

	@Override  
    public void onActivityCreated(Bundle savedInstanceState) {  
        super.onActivityCreated(savedInstanceState);

        deviceList = (ListView)(getActivity().findViewById(R.id.device_list));
        Util.getInstance().initDeviceInfor();

        adapter = new DeviceAdapter(getActivity());
        deviceList.setAdapter(adapter);

        btnRefresh = (Button)(getActivity().findViewById(R.id.refresh_btn));
        btnRefresh.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View v)
        	{
        		khWireless.refreshDeviceInfoOnce();
        	}
        });
    }


	public final class ViewHolder{
        public ImageView img;
        public TextView name;
        public CheckBox checked;
    }
	
	class DeviceAdapter extends BaseAdapter{
        private LayoutInflater mInflater;

        public DeviceAdapter(Context context){
            this.mInflater=LayoutInflater.from(context);
        }
        
        public int getCount() {
            return Util.getInstance().getDevices().size();
        }
        
        public Object getItem(int position) {
            return Util.getInstance().getDevices().get(position);
        }
        
        public long getItemId(int position) {
            return position;
        }
        
        public View getView(int position, View convertView, ViewGroup parent) {
            final int p = position;
            final CheckBox checkbox;
            ViewHolder holder=null;
            
            if(convertView == null){
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.device_list_item, null);
                holder.img = (ImageView)convertView.findViewById(R.id.device_img);
                holder.name = (TextView)convertView.findViewById(R.id.device_name);
                holder.checked = (CheckBox)convertView.findViewById(R.id.select);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.img.setImageResource(R.drawable.music);
            
            final String s = (String)Util.getInstance().getDevices().get(position).deviceObj.deviceName;
            holder.name.setText(s);
            if (Util.getInstance().getDeviceStatus(position)) {
            	 holder.checked.setChecked(true);
            } else {
            	 holder.checked.setChecked(false);
            }
            checkbox = holder.checked;
            
            if (Util.getInstance().getDeviceStatus(p)) {
            	checkbox.setChecked(true);
            } else {
            	checkbox.setChecked(false);
            }
            
            convertView.setClickable(true); 
            convertView.setLongClickable(true);
            convertView.setOnLongClickListener(new OnLongClickListener(){
				@Override
				public boolean onLongClick(View arg0) {
					// TODO Auto-generated method stub
					showDeviceInfor(p);
					return true;
				}
            });
            convertView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					if (Util.getInstance().getDeviceStatus(p)) {
						Util.getInstance().removeDeviceFromSession(Util.getInstance().getDevices().get(p).deviceObj.deviceId);
						//HKWirelessUtil.getInstance().removeDeviceFromSession(Util.getInstance().getDevices().get(p).deviceObj.deviceId);
						checkbox.setChecked(false);
		            } else {
		            	Util.getInstance().addDeviceToSession(Util.getInstance().getDevices().get(p).deviceObj.deviceId);
		            	//HKWirelessUtil.getInstance().addDeviceToSession(Util.getInstance().getDevices().get(p).deviceObj.deviceId);
						checkbox.setChecked(true);
		            }
				}
            });
            return convertView;
        }
    }

	private void showDeviceInfor(int index) {
		DeviceObj deviceInfo = Util.getInstance().getDevices().get(index).deviceObj;
		
		StringBuilder infor = new StringBuilder();
		StringBuilder group = new StringBuilder();
		infor.append("ID:"+deviceInfo.deviceId+"\n");
		infor.append("Name:"+deviceInfo.deviceName+"\n");
		infor.append("IP:"+deviceInfo.ipAddress+"\n");
		infor.append("Port:"+deviceInfo.port+"\n");
		infor.append("Role:"+deviceInfo.role+"\n");
		infor.append("Version:"+deviceInfo.version+"\n");
		infor.append("Volume:"+deviceInfo.volume+"\n");
		infor.append("WIFI:"+deviceInfo.wifiSignalStrength+"\n");
		infor.append("Zone:"+deviceInfo.zoneName+"\n");
		infor.append("Active:"+deviceInfo.active+"\n");
		
		/*for (int i=0; i<deviceInfo.groupList.length; i++)
			group.append(deviceInfo.groupList[i] + ";");
		infor.append("Group:"+group+"\n");*/
		
		new AlertDialog.Builder(getActivity())
		.setTitle(getString(R.string.device_infor))
		.setMessage(infor.toString())
		.setPositiveButton("È·¶¨", null)
		.show();
	}
}
