package mobitnt.android.MatchBox;

import mobitnt.net.iEASrvCtrl;
import mobitnt.util.EAUtil;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class LogOptionActivity extends Activity implements
android.view.View.OnClickListener {
	final String ServiceAction = "mobitnt.net.EASERVICE";
	Intent serviceintent = new Intent(ServiceAction);
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.logoption);
		
		String sMail = EAUtil.GetLogReplyMail();
		EditText t1 = (EditText)findViewById(R.id.logReplyMail);
		t1.setText(sMail);
		
		Button btn = (Button)findViewById(R.id.btnMailOK);
        btn.setOnClickListener((OnClickListener) this);
        
        btn = (Button)findViewById(R.id.btnMailCancel);
        btn.setOnClickListener((OnClickListener) this);

	}
	
	/*
	 * op 0:send log 1 :eanble/disable log
	 */
	public void SendLog() {
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
					ctrl.SendLog(0);
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


	public void onClick(View v) {
		if (R.id.btnMailOK == v.getId()) {
			EditText t1 = (EditText)findViewById(R.id.logReplyMail);
			String sMail = t1.getText().toString();
			EAUtil.SetLogReplyMail(sMail);
			SendLog();
			finish();
		}

		if (R.id.btnMailCancel == v.getId()) {
			finish();
		}

	}
}
