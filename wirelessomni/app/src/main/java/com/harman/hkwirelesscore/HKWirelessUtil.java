package com.harman.hkwirelesscore;

import com.harman.hkwirelessapi.DeviceObj;
import com.harman.hkwirelessapi.GroupObj;
import com.harman.hkwirelessapi.HKWirelessHandler;
import com.harman.hkwirelessapi.HKWirelessListener;

public class HKWirelessUtil {
    private HKWirelessHandler hkwireless = new HKWirelessHandler();
    
    private static HKWirelessUtil instance = new HKWirelessUtil();
    
    private static final String KEY = "2FA8-2FD6-C27D-47E8-A256-D011-3751-2BD6";
    
    private HKWirelessUtil() {

    }
    
    public static HKWirelessUtil getInstance() {
    	return instance;
    }
    
    public void initializeHKWirelessController() {
    	hkwireless.initializeHKWirelessController(KEY);
    }
    
    public boolean isInitialized() {
    	return hkwireless.isInitialized();
    }
    
    public void registerHKWirelessControllerListener(HKWirelessListener listener){
    	hkwireless.registerHKWirelessControllerListener(listener);
    }

    public void refreshDeviceInfoOnce(){
    	hkwireless.refreshDeviceInfoOnce();
    }

    public void startRefreshDeviceInfo(){
    	hkwireless.startRefreshDeviceInfo();
    }

    public void stopRefreshDeviceInfo(){
    	hkwireless.stopRefreshDeviceInfo();
    }

    public boolean addDeviceToSession(long id){
        return hkwireless.addDeviceToSession(id);
    }

    public boolean removeDeviceFromSession(long deviceid){
        return hkwireless.removeDeviceFromSession(deviceid);
    }

    public int getGroupCount(){
        return hkwireless.getGroupCount();
    }

    public long getDeviceCountInGroupIndex(int groupIndex){
        return hkwireless.getDeviceCountInGroupIndex(groupIndex);
    }

    public int getDeviceCount(){
        return hkwireless.getDeviceCount();
    }

    public DeviceObj getDeviceInfoFromTable(int groupIndex, int deviceIndex){
        return hkwireless.getDeviceInfoFromTable(groupIndex, deviceIndex);
    }

    public DeviceObj getDeviceInfoByIndex(int deviceIndex){
        return hkwireless.getDeviceInfoByIndex(deviceIndex);
    }

    public GroupObj findDeviceGroupWithDeviceId(long deviceId){
        return hkwireless.findDeviceGroupWithDeviceId(deviceId);
    }

    public DeviceObj findDeviceFromList(long deviceId){
        return hkwireless.findDeviceFromList(deviceId);
    }

    public boolean isDeviceActive(long deviceId){
        return hkwireless.isDeviceActive(deviceId);
    }

    public void removeDeviceFromGroup(int groupId, long deviceId){
    	hkwireless.removeDeviceFromGroup(groupId, deviceId);
    }

    public GroupObj getDeviceGroupByIndex(int groupIndex){
        return hkwireless.getDeviceGroupById(groupIndex);
    }

    public GroupObj getDeviceGroupById(int groupId){
        return hkwireless.getDeviceGroupById(groupId);
    }

    public String getDeviceGroupNameByIndex(int groupIndex){
        return hkwireless.getDeviceGroupNameByIndex(groupIndex);
    }

    public long getDeviceGroupIdByIndex(int groupIndex){
        return hkwireless.getDeviceGroupIdByIndex(groupIndex);
    }

    public void setDeviceName(long deviceId, String deviceName){
    	hkwireless.setDeviceName(deviceId, deviceName);
    }

    public void setDeiceGroupName(int groupId, String groupName){
    	hkwireless.setDeviceGroupName(groupId, groupName);
    }

    public void setDeviceRole(long deviceId, int role){
    	hkwireless.setDeviceRole(deviceId, role);
    }

    public int getActiveDeviceCount(){
        return hkwireless.getActiveDeviceCount();
    }

    public int getActiveGroupCount(){
        return hkwireless.getActiveGroupCount();
    }

    public void refreshDeviceWiFiSignal(long deviceId){
    	hkwireless.refreshDeviceWiFiSignal(deviceId);
    }

    /*public HKWifiSingalStrength getWifiSignalStrengthType(int wifiSignal){
        HKWifiSingalStrength type =  HKWifiSingalStrength.values()[m_wireless.GetWifiSignalStrengthType(wifiSignal)];
        return type;
    }*/
    
}
