package mobitnt.android.wrapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import mobitnt.util.EADefine;
import mobitnt.util.EAUtil;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;


public class SysApi {
	static List<String> m_BatteryInfo = new ArrayList<String>();
	static boolean m_iBatteryInfoAvailable = false;

	static public List<String> getSysInfo() {
		String[] sMemInfo = getMemoryInfo();
		String[] sStorageInfo = getStorageInfo();
		String[] sModelInfo = getModelInfo();
		String[] sCpuInfo = getCpuInfo();
		
		List<String> sysInfo = new ArrayList<String>();
			
		if (m_iBatteryInfoAvailable){
			for (int i = 0; i < m_BatteryInfo.size(); ++i){
				sysInfo.add(m_BatteryInfo.get(i));
			}
		}
		
		if (sMemInfo != null) {
			for (int i = 0; i < sMemInfo.length; ++i) {
				sysInfo.add(sMemInfo[i]);
			}
		}

		if (sStorageInfo != null) {
			for (int i = 0; i < sStorageInfo.length; ++i) {
				sysInfo.add(sStorageInfo[i]);
			}
		}

		if (sModelInfo != null) {
			for (int i = 0; i < sModelInfo.length; ++i) {
				sysInfo.add(sModelInfo[i]);
			}
		}

		if (sCpuInfo != null) {
			for (int i = 0; i < sCpuInfo.length; ++i) {
				sysInfo.add(sCpuInfo[i]);
			}
		}
		
		return sysInfo;
	}
	
	public static void SetPowerInfo(String sInfo){
		m_BatteryInfo.clear();
		m_BatteryInfo.add(EADefine.EA_ACT_SYS_BATTERY_TAG + ":" + sInfo);
		
		m_iBatteryInfoAvailable = true;	
	}
	
	static public String getImei() {
		TelephonyManager manager = (TelephonyManager) EAUtil.GetEAContext()
				.getSystemService(Context.TELEPHONY_SERVICE);

		return manager.getDeviceId();
	}

	static public String formatSize(long iSpaceSize) {
		if (iSpaceSize <= 0) {
			return "0 byte";
		}

		if (iSpaceSize < 1024) {
			return String.format("%d bytes", iSpaceSize);
		}

		String sSize = "";
		int iSizeInG = (int) (iSpaceSize / (1024 * 1024 * 1024));
		if (iSizeInG > 0) {
			sSize = String.format("%d GB", iSizeInG);
		}

		int iSizeInM = 0;
		iSpaceSize -= iSizeInG * 1024 * 1024 * 1024;
		iSizeInM = (int) (iSpaceSize) / (1024 * 1024);
		if (iSizeInM > 0) {
			sSize += String.format(" %d MB", iSizeInM);
		}

		int iSizeInKB = 0;

		iSpaceSize -= iSizeInM * 1024 * 1024;
		iSizeInKB = (int) (iSpaceSize) / (1024);
		if (iSizeInKB > 0) {
			sSize += String.format(" %d KB", iSizeInKB);
		}

		return sSize;
	}

	static String[] getModelInfo() {
		String[] modelInfo = { EADefine.EA_ACT_SYS_PRODUCT_MODEL_TAG + ":"
				+ android.os.Build.MODEL/*
										 * , String.format("SDK version:%s",
										 * android.os.Build.VERSION.SDK),
										 * String.format("Release Version:%s",
										 * android.os.Build.VERSION.RELEASE),
										 */
		};
		// String.format("SDCard total space:%s", sTotalSize) };
		return modelInfo;
	}

	static String[] getStorageInfo() {
		String sdcard = Environment.getExternalStorageDirectory().getPath();
		File file = new File(sdcard);
		StatFs statFs = new StatFs(file.getPath());
/*		int bs = statFs.getBlockSize();
		int ab = statFs.getAvailableBlocks();
		int bc = statFs.getBlockCount();*/
		long lAvailableSpace = statFs.getBlockSize()
				* (statFs.getAvailableBlocks() - 4);
		long lTotalSpace = statFs.getBlockCount() * statFs.getBlockSize();

		//String sASize = formatSize(lAvailableSpace);
		//String sTotalSize = formatSize(lTotalSpace);

		String[] storageInfo = {
				EADefine.EA_ACT_SYS_EXT_AVAILABLE_SPACE + ":" + String.valueOf(lAvailableSpace),
				EADefine.EA_ACT_SYS_EXT_TOTAL_SPACE + ":" + String.valueOf(lTotalSpace) };

		return storageInfo;
	}

	static String[] getMemoryInfo() {
		ActivityManager activityManager = (ActivityManager) EAUtil.GetEAContext()
				.getSystemService(Context.ACTIVITY_SERVICE);
		ActivityManager.MemoryInfo outInfo = new ActivityManager.MemoryInfo();
		activityManager.getMemoryInfo(outInfo);

		String sAvailableMem = String.valueOf(outInfo.availMem/1024);//formatSize(outInfo.availMem);
		String sTotalRAM = getTotalRAM();
		
		sTotalRAM = sTotalRAM.toUpperCase();
		sTotalRAM = sTotalRAM.replace("KB", "");

		String[] sysInfo = { EADefine.EA_ACT_SYS_AVAIL_RAM + ":" + sAvailableMem ,
				EADefine.EA_ACT_SYS_TOTAL_RAM + ":" + sTotalRAM 	
		};
		return sysInfo;
	}

/*	static public boolean UserLogin(String sUsr, String sPwd) {
		if (sUsr.equals("admin") && sPwd.equals("123")) {
			return true;
		}
		return false;
	}*/

	// android的总内存(ram)大小信息存放在系统的/proc/meminfo文件里面，可以通过读取这个文件来获取这些信息：
	static String getTotalRAM() {
		String sPath = "/proc/meminfo";
		String sItem = "";
		try {
			FileReader fr = new FileReader(sPath);
			BufferedReader localBufferedReader = new BufferedReader(fr, 8192);
			while ((sItem = localBufferedReader.readLine()) != null) {
				if (sItem.contains("MemTotal")){
					sItem = sItem.replace("MemTotal:", "");
					return sItem.trim();
				}
			}
		} catch (IOException e) {
		}
		
		return "UNKONW";
	}


	// proc/cpuinfo文件中第一行是CPU的型号，第二行是CPU的频率，可以通过读文件，读取这些数据！
	static String[] getCpuInfo() {
		String sPath = "/proc/cpuinfo";
		String sItem = "";
		String[] cpuInfo = { "", "" };
		String[] arrayOfString;
		try {
			FileReader fr = new FileReader(sPath);
			BufferedReader localBufferedReader = new BufferedReader(fr, 8192);
			sItem = localBufferedReader.readLine();
			arrayOfString = sItem.split("\\s+");
			for (int i = 2; i < arrayOfString.length; i++) {
				cpuInfo[0] = cpuInfo[0] + arrayOfString[i] + " ";
			}
			sItem = localBufferedReader.readLine();
			arrayOfString = sItem.split("\\s+");
			cpuInfo[1] += arrayOfString[2];
			localBufferedReader.close();
		} catch (IOException e) {
		}
		
		String[] info = {EADefine.EA_ACT_SYS_CPU_MODEL_TAG + ":"+cpuInfo[0],EADefine.EA_ACT_SYS_CPU_FREQ_TAG + ":"+cpuInfo[1]};
		return info;
	}


}
