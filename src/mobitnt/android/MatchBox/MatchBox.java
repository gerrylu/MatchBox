package mobitnt.android.MatchBox;

import java.util.Locale;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import mobitnt.net.iEASrvCtrl;
import mobitnt.util.EAUtil;

public class MatchBox extends Activity implements
		android.view.View.OnClickListener {


	public static final int ENABLE_LOG = Menu.FIRST + 4;
	public static final int SEND_LOG = Menu.FIRST + 3;
	public static final int STOP_SRV = Menu.FIRST + 2;
	public static final int QUIT = Menu.FIRST + 1;
	public static final int OPTIONS = Menu.FIRST;


	final String ServiceAction = "mobitnt.net.EASERVICE";
	Intent serviceintent = new Intent(ServiceAction);

	private WebView webview;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, QUIT, 0, R.string.QuitApp);
		// menu.add(0, OPTIONS, 0, "Options");
		menu.add(0, STOP_SRV, 0, R.string.StopSrv);

		menu.add(0, SEND_LOG, 0, R.string.SendLog);

		boolean bLogState = EAUtil.GetLogState();
		if (bLogState) {
			menu.add(0, ENABLE_LOG, 0, R.string.DisableLog);
		} else {
			menu.add(0, ENABLE_LOG, 0, R.string.EnableLog);
		}

		return super.onCreateOptionsMenu(menu);
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();

		menu.add(0, QUIT, 0, R.string.QuitApp);
		// menu.add(0, OPTIONS, 0, "Options");
		menu.add(0, STOP_SRV, 0, R.string.StopSrv);

		menu.add(0, SEND_LOG, 0, R.string.SendLog);

		boolean bLogState = EAUtil.GetLogState();
		if (bLogState) {
			menu.add(0, ENABLE_LOG, 0, R.string.DisableLog);
		} else {
			menu.add(0, ENABLE_LOG, 0, R.string.EnableLog);
		}

		return super.onPrepareOptionsMenu(menu);
	}

	/*
	 * op 0:send log 1 :eanble/disable log
	 */
	public void EnableLog() {
		ServiceConnection serConn = new ServiceConnection() {
			// 此方法在系统建立服务连接时调用
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.v("MatchBox", "onServiceConnected() called");
				iEASrvCtrl ctrl = iEASrvCtrl.Stub.asInterface(service);
				
				String sMail = EAUtil.GetLogReplyMail();
				if (sMail == null || sMail.length() < 2){
					return;
				}
				
				try {
					ctrl.SendLog(1);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			// 此方法在销毁服务连接时调用
			public void onServiceDisconnected(ComponentName name) {
				Log.v("MatchBox", "onServiceDisconnected()");
			}
		};

		bindService(serviceintent, serConn, Context.BIND_AUTO_CREATE);
	};

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case QUIT:
			// System.exit(0);
			this.finish();
			break;
		case OPTIONS:
			// Intent intent = new Intent(PEAClient.this, PEOptions.class);
			// startActivity(intent);
			break;
		case STOP_SRV:
			stopService(serviceintent);
			break;
		case ENABLE_LOG:
			EnableLog();
			break;
		case SEND_LOG:
			Intent startNewActivityOpen = new Intent(this, LogOptionActivity.class);
			startActivityForResult(startNewActivityOpen, 0);
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		webview = new WebView(this);
		
		String sFileName = "UI-Main.html";
		String sCountry = Locale.getDefault().getCountry();
		if (sCountry.equalsIgnoreCase("CN")) {
			sFileName = "CN-" + sFileName;
		}

		webview.loadUrl("file:///android_asset/ui/" + sFileName);

		setContentView(webview);
		webview.setBackgroundColor(0xFFf6f6f6);
	}


	public void onStart() {
		super.onStart();

		startService(serviceintent);
	}

	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		
	}
}
