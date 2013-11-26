package mobitnt.android.wrapper;

import mobitnt.util.EAUtil;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class NetworkApi {
	static public boolean isWifiEnabled() {
		ConnectivityManager cm = (ConnectivityManager) EAUtil.GetEAContext()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		// cm.getActiveNetworkInfo();
		NetworkInfo[] netInfo = cm.getAllNetworkInfo();
		if (netInfo.length < 1) {
			return false;
		}

		for (NetworkInfo ni : netInfo) {
			if (ni.getTypeName().equalsIgnoreCase("WIFI"))
				if (ni.isConnected()) {
					return true;
				}

		}
		return false;
	}

	static public String getLocalMacAddress() {
		WifiManager wifi = (WifiManager) EAUtil.GetEAContext()
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifi.getConnectionInfo();

		return info.getMacAddress();
	}

	// 2、Android 获取本机IP地址方法：
	static public String getLocalIpAddress() {
		if (!isWifiEnabled()) {
			// if no wifi available,usb USB instead
			return "127.0.0.1";
		}

		WifiManager wifi_service = (WifiManager) EAUtil.GetEAContext().getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiinfo = wifi_service.getConnectionInfo();
		int ipAddress =  wifiinfo.getIpAddress();
		
		return String.format("%d.%d.%d.%d",
				(ipAddress & 0xff),
				(ipAddress >> 8 & 0xff),
				(ipAddress >> 16 & 0xff),
				(ipAddress >> 24 & 0xff));
		  
	}

}
