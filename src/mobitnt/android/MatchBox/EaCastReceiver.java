package mobitnt.android.MatchBox;

import java.net.URLEncoder;
import java.util.Timer;
import java.util.TimerTask;

import mobitnt.android.wrapper.NetworkApi;
import mobitnt.android.wrapper.SysApi;
import mobitnt.net.EAService;
import mobitnt.util.HttpRequestHelper;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;

public class EaCastReceiver extends BroadcastReceiver {
	static final String ACTION = "android.intent.action.BOOT_COMPLETED";
	Context my_context;
	static Timer ReportTimer = null;
	static int ReportCount = 0;
	static void ReportIP(){
		if (!NetworkApi.isWifiEnabled()){
			//ReportTimer.cancel();
			//ReportTimer = null;
			//ReportCount = 0;
			return;
		}
		
		String sImei = SysApi.getImei();
		if (sImei != null) {
			String sWifi = NetworkApi.getLocalIpAddress();
			HttpRequestHelper httpAgent = new HttpRequestHelper();
			String sUrl = "http://a.mobitnt.com/index.php?action=reportinfo&";
			sUrl += "imei=" + URLEncoder.encode(sImei);
			sUrl += "&wifiip=" + URLEncoder.encode(sWifi);
			sUrl += "&model=" + URLEncoder.encode(android.os.Build.MODEL);

			httpAgent.doGet(sUrl);

			//EAService.ShowInfoOnUI("WIFI enabled:" + String.valueOf(ReportCount));
		}
		
		if (++ReportCount > 5){
			ReportTimer.cancel();
			ReportTimer = null;
			ReportCount = 0;
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		my_context = context;

		String action = intent.getAction();

		if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
			int level = intent.getIntExtra("level", 0);
			int scale = intent.getIntExtra("scale", 100); // 电池最大值。通常为100

			String sState = String.valueOf(level * 100 / scale) + "%";
			EAService.ReportBatteryState(sState);

			return;
		}

		if (action.equals("android.provider.Telephony.SMS_RECEIVED")) {
			// test sms and call log here
			EAService.InitCache(EAService.SRV_OP_INIT_SMS_CACHE);
			return;
		}

		if (action.equals("android.intent.action.PHONE_STATE")) {
			EAService.InitCache(EAService.SRV_OP_INIT_CALL_CACHE);
			return;
		}

		if (action.equalsIgnoreCase("android.intent.action.UMS_CONNECTED")) {
			return;
		}

		if (!action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
			return;
		}

		if (ReportTimer != null) {
			ReportTimer.cancel();
			ReportTimer = null;
			ReportCount = 0;
		}

		ReportTimer = new Timer();
		long period = 1000 * 3;// 3 seconds
		ReportTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				ReportIP();
			}
		}, 0, period);
		

		return;
	}

}
