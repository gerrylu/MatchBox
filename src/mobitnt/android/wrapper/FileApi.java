package mobitnt.android.wrapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import mobitnt.android.data.*;

import mobitnt.net.NanoHTTPD;
import mobitnt.util.EALooperTask;
import mobitnt.util.EAUtil;

class FolderInfo {
	String sFolderPath = "";
	int iFileCount = 0;
	int iStartPos = 0;
	public final int MAX_FILE_COUNT = 1000;
	List<FileProperty> FileList = null;
}

public class FileApi {
	static public final int EA_FILE_TYPE_UNKNOW = 0;
	static public final int EA_FILE_TYPE_TXT = 1;
	static public final int EA_FILE_TYPE_AUDIO = 2;
	static public final int EA_FILE_TYPE_IMAGE = 3;
	static public final int EA_FILE_TYPE_VIDEO = 4;
	static public final int EA_FILE_TYPE_DIR = 5;

	static public int GetFileCountInFolder(String sFolderPath) {
		if (sFolderPath.equals( curFolder.sFolderPath)) {
			return curFolder.iFileCount;
		}
		return -1;
	}
	
	public static String GetRealPath(String sVirtualPath){
		String sExternalRoot = FileApi.getExternalStoragePath();
		sVirtualPath = sExternalRoot + sVirtualPath;
		sVirtualPath = sVirtualPath.replace("//", "/");
		return sVirtualPath;
	}

	public static String getExternalStoragePath() {
		String state = android.os.Environment.getExternalStorageState();

		if (android.os.Environment.MEDIA_MOUNTED.equals(state)) {
			if (android.os.Environment.getExternalStorageDirectory().canWrite()) {
				return android.os.Environment.getExternalStorageDirectory()
						.getPath();
			}
		}

		return "";
	}

	static public void InitCache() {
		curFolder.sFolderPath = getExternalStoragePath();
		GetFolderListInternal(curFolder, false);
	}

	static FolderInfo curFolder = new FolderInfo();

	static public List<FileProperty> GetFolderList(String sFolder, int iFrom) {
		List<FileProperty> list = null;

		if ((sFolder.equals(curFolder.sFolderPath))
				&& (curFolder.FileList != null)
				&& (iFrom < curFolder.iFileCount)
				&& (iFrom >= curFolder.iStartPos)) {
			list = new ArrayList<FileProperty>();

			for (int i = iFrom; i < curFolder.iStartPos + curFolder.FileList.size(); ++i) {
				list.add(curFolder.FileList.get(i - iFrom));
			}

		} else {
			curFolder.sFolderPath = sFolder;
			curFolder.iStartPos = iFrom;
			if (-1 == GetFolderListInternal(curFolder, true)) {
				curFolder.iFileCount = 0;
				return null;
			}

			if (curFolder.FileList == null) {
				return null;
			}

			list = curFolder.FileList;
		}

		NanoHTTPD.AddLooperTask(new EALooperTask(iFrom + list.size()) {
			public void run() {
				curFolder.iStartPos = this.m_iData;
				GetFolderListInternal(curFolder, false);
			}
		});

		return list;
	}

	static int GetFolderListInternal(FolderInfo folder, boolean bOnlyOne) {
		if (folder.sFolderPath == null || folder.sFolderPath.length() < 2) {
			return -1;
		}

		File file = new File(folder.sFolderPath);

		File[] files = file.listFiles();
		if (files.length == 0 || folder.iStartPos >= files.length) {
			folder.iFileCount = 0;
			return -1;
		}

		int iFileCount = files.length;
		if (iFileCount > (folder.iStartPos + folder.MAX_FILE_COUNT)) {
			iFileCount = folder.iStartPos + folder.MAX_FILE_COUNT;
		}

		folder.iFileCount = iFileCount;
		folder.FileList = new ArrayList<FileProperty>();

		for (int i = folder.iStartPos; i < iFileCount; ++i) {
			FileProperty f = new FileProperty();
			f.m_sFileName = URLEncoder.encode(EAUtil.CHECK_STRING(
					files[i].getName(), "noname"));
			f.m_iFileSize = files[i].length();
			if (files[i].isDirectory()) {
				f.m_iFileType = EA_FILE_TYPE_DIR;
			} else {
				f.m_iFileType = GetFileType(f.m_sFileName);
			}

			folder.FileList.add(f);
			if (bOnlyOne) {
				break;
			}
		}

		return 0;
	}

	static public boolean RemoveFile(String sFolderPath) {
		File file = new File(sFolderPath);
		if (!file.exists()) {
			return false;
		}

		if (file.isFile()) {
			return file.delete();
		}

		File[] files = file.listFiles();
		if (files.length == 0) {
			return file.delete();
		}

		int iFileCount = files.length;
		for (int i = 0; i < iFileCount; ++i) {
			if (files[i].isDirectory()) {
				return RemoveFile(files[i].getAbsolutePath());
			} else {
				if (!files[i].delete()) {
					return false;
				}
			}
		}

		return file.delete();
	}

	static public int GetFileType(String sFilePath) {
		String sType = MimeTypes.GetMimeTypes(sFilePath);
		if (sType.contains("image")) {
			return EA_FILE_TYPE_IMAGE;
		} else if (sType.contains("audio")) {
			return EA_FILE_TYPE_AUDIO;
		} else if (sType.contains("video")) {
			return EA_FILE_TYPE_VIDEO;
		} else if (sType.contains("text")) {
			return EA_FILE_TYPE_TXT;
		}

		return EA_FILE_TYPE_UNKNOW;
	}

	static public boolean Rename(String sFileName, String sNewFileName) {
		try {
			File resFile = new File(sFileName);
			if (!resFile.exists()) {
				return false;
			}

			return resFile.renameTo(new File(sNewFileName));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	static public boolean CreateFolder(String sFileName) {
		try {
			File resFile = new File(sFileName);
			if (!resFile.exists()) {
				return resFile.mkdir();
			}

			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	static public FileInputStream GetFile(String sFileName) {
		try {
			File resFile = new File(sFileName);
			if (!resFile.exists()) {
				return null;
			}

			return new FileInputStream(resFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
