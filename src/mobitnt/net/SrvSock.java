package mobitnt.net;

import java.io.BufferedReader;
import java.io.File;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


import java.net.URLDecoder;
import java.nio.ByteBuffer;

import java.util.Properties;
import java.util.StringTokenizer;

import android.content.res.AssetManager;
import mobitnt.android.data.MimeTypes;
import mobitnt.android.wrapper.AppApi;
import mobitnt.android.wrapper.FileApi;
import mobitnt.util.*;

public class SrvSock extends NanoHTTPD {
	
	public AssetManager htmlData = null;

	public SrvSock() throws IOException {
		super(SERVER_PORT);
	}

	public int ReadByteAry(BufferedReader in, byte[] data, int iCount) {
		int iLen = 0;
		while (iLen < iCount) {
			int iChar = -1;
			try {
				iChar = in.read();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (iChar == -1) {
				return iLen;
			}

			if (iChar > 255) {
				int temp = iChar;
				byte[] b = new byte[2];
				for (int i = b.length - 1; i > -1; i--) {
					b[i] = Integer.valueOf(temp & 0xff).byteValue();
					temp = temp >> 8;
				}

				data[iLen++] = b[0];
				data[iLen++] = b[1];
			} else {
				data[iLen++] = (byte) iChar;
			}

		}
		return iCount;
	}

	public String ReadLine(InputStream is) {
		int iReadByte = 0;
		int iPos = 0;

		byte[] httpData = new byte[EADefine.BUFF_SIZE];

		try {
			while ((iReadByte = is.read()) >= 0) {
				if (iPos >= EADefine.BUFF_SIZE) {
					// error but not handled
					// BUGTOBEFIX
					break;
				}

				httpData[iPos++] = (byte) iReadByte;
				if (iReadByte == '\n') {
					break;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (iPos <= 0) {
			return null;
		}

		byte[] stringbyte = new byte[iPos];
		for (int i = 0; i < iPos; ++i) {
			stringbyte[i] = httpData[i];
		}

		return new String(stringbyte);

	}

	private String decodePercent(String str) throws InterruptedException {

		return URLDecoder.decode(str);

		/*
		 * try { StringBuffer sb = new StringBuffer(); for (int i = 0; i <
		 * str.length(); i++) { char c = str.charAt(i); switch (c) { case '+':
		 * sb.append(' '); break; case '%': sb.append((char) Integer.parseInt(
		 * str.substring(i + 1, i + 3), 16)); i += 2; break; default:
		 * sb.append(c); break; } } return new String(sb.toString().getBytes());
		 * } catch (Exception e) { // sendError( HTTP_BADREQUEST,
		 * "BAD REQUEST: Bad percent-encoding." // ); return null; }
		 */
	}

	/**
	 * Decodes parameters in percent-encoded URI-format ( e.g.
	 * "name=Jack%20Daniels&pass=Single%20Malt" ) and adds them to given
	 * Properties. NOTE: this doesn't support multiple identical keys due to the
	 * simplicity of Properties -- if you need multiples, you might want to
	 * replace the Properties with a Hastable of Vectors or such.
	 */
	public void decodeParms(String parms, Properties p)
			throws InterruptedException {
		if (parms == null)
			return;

		StringTokenizer st = new StringTokenizer(parms, "&");
		while (st.hasMoreTokens()) {
			String e = st.nextToken();
			int sep = e.indexOf('=');
			if (sep >= 0)
				p.put(decodePercent(e.substring(0, sep)).trim(),
						decodePercent(e.substring(sep + 1)));
		}
	}

	public Response servePost(InputStream is) {
		try {
			is.read();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		MobiTNTLog.write("uploading file now...");

		String inLine = ReadLine(is);
		if (inLine == null) {
			MobiTNTLog.write("uplaod file failed");
			return new Response(
					HTTP_OK,
					MIME_XML,
					PageGen.GenRetCode(EADefine.EA_RET_SAVE_FILE_FAILED));
		}

		StringTokenizer st = new StringTokenizer(inLine);
		if (!st.hasMoreTokens()) {
			MobiTNTLog.write("uplaod file failed");
			return new Response(
					HTTP_OK,
					MIME_XML,
					PageGen.GenRetCode(EADefine.EA_RET_SAVE_FILE_FAILED));
		}

		String uri = st.nextToken();

		// Decode parameters from the URI
		Properties parms = new Properties();
		int qmi = uri.indexOf('?');
		if (qmi >= 0) {
			try {
				decodeParms(uri.substring(qmi + 1), parms);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				uri = decodePercent(uri.substring(0, qmi));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				uri = decodePercent(uri);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		Properties header = new Properties();
		if (st.hasMoreTokens()) {
			String line = ReadLine(is);
			while (line.trim().length() > 0) {
				int p = line.indexOf(':');
				header.put(line.substring(0, p).trim().toLowerCase(), line
						.substring(p + 1).trim());
				line = ReadLine(is);
			}
		}

		long size = 0x7FFFFFFFFFFFFFFFl;
		String contentLength = header.getProperty("content-length");
		if (contentLength != null) {
			try {
				size = Integer.parseInt(contentLength);
			} catch (NumberFormatException ex) {
				MobiTNTLog.write("uplaod file failed");
				return new Response(
						HTTP_OK,
						MIME_XML,
						PageGen.GenRetCode(EADefine.EA_RET_SAVE_FILE_FAILED));
			}
		}

		String sBoundary = header.getProperty("content-type");// Content-Type
		if (sBoundary == null) {
			MobiTNTLog.write("uplaod file failed");
			return new Response(
					HTTP_OK,
					MIME_XML,
					PageGen.GenRetCode(EADefine.EA_RET_SAVE_FILE_FAILED));
		}

		String sBTag = "boundary=";
		int iFoundBoundary = 0;
		long iBoundaryLen = 0;
		if (sBoundary.indexOf(sBTag) >= 0) {
			sBoundary = sBoundary.substring(
					sBoundary.indexOf(sBTag) + sBTag.length(),
					sBoundary.length());
			iBoundaryLen = sBoundary.length() + 10;
		} else {
			// not ie form format
			iFoundBoundary = -1;
		}
	
		String sFileName = FileApi.GetRealPath(uri);

		try {
			File file = new File(sFileName);
			if (file.exists()) {
				if (!file.delete()) {
					MobiTNTLog.write("uplaod file failed");
					return new Response(
							HTTP_OK,
							MIME_XML,
							PageGen.GenRetCode(EADefine.EA_RET_SAVE_FILE_FAILED));
				}

				if (!file.createNewFile()) {
					MobiTNTLog.write("uplaod file failed");
					return new Response(
							HTTP_OK,
							MIME_XML,
							PageGen.GenRetCode(EADefine.EA_RET_SAVE_FILE_FAILED));
				}
			} else {
				if (!file.createNewFile()) {
					MobiTNTLog.write("uplaod file failed");
					return new Response(
							HTTP_OK,
							MIME_XML,
							PageGen.GenRetCode(EADefine.EA_RET_SAVE_FILE_FAILED));
				}
			}

			OutputStream out = new FileOutputStream(file);

			int iResult = 0;
	
			// 先处理头部信息
			while ((size > 0) && (iFoundBoundary >= 0)) {
				String sLine = ReadLine(is);
				if (iFoundBoundary == 1) {
					size -= sLine.length();
					if (sLine.equals("\r\n")) {
						iFoundBoundary = 2;
						break;
					}
				} else {
					if (sLine.contains(sBoundary)) {
						iFoundBoundary = 1;
						size -= sLine.length();
					}
				}
			}

			if (iResult == -1) {
				MobiTNTLog.write("uplaod file failed");
				return new Response(HTTP_OK, MIME_XML,
						PageGen.GenRetCode(EADefine.EA_RET_SAVE_FILE_FAILED));
			}

			// 下面是真正的post数据
			// String sInfo = "";
			if (iBoundaryLen > 0) {
				size -= iBoundaryLen;
				size += 2;
			}

			byte[] data = new byte[EADefine.BUFF_SIZE];
			while (true) {

				int iNeedRead = EADefine.BUFF_SIZE;
				if (size < iNeedRead) {
					iNeedRead = (int) size;
				}
				
				//此处解决浏览器中断上传导致服务器阻塞的问题，使用nio比较麻烦，暂时补丁解决
				//当检测到没有数据的时候循环等待10秒左右，仍然没有数据即退出
				int iAvailable = is.available();
				if (iAvailable < 1){
					int iCount = 5;
					do{
						Thread.sleep(1000);
						--iCount;
						iAvailable = is.available();
					}while ( (iAvailable == 0) && (iCount > 0));
					
					if ((iCount <= 0) && (iAvailable == 0)){
						MobiTNTLog.write("No more data!");
						iResult = -1;
						break;
					}
				}
				
				//MobiTNTLog.write("iNeedRead:" + String.valueOf(iNeedRead));
						
				int iLastLen = is.read(data, 0, iNeedRead);
				//MobiTNTLog.write("lastlen:" + String.valueOf(iLastLen));
				size -= iLastLen;
				if (iLastLen <= 0) {
					MobiTNTLog.write("lastlen:" + String.valueOf(iLastLen) + " size:" + String.valueOf(size));
					iResult = -1;
					break;
				}

				out.write(data, 0, iNeedRead);

				// out.write(fileData);
				if (size == 0) {
					break;
				}
			}
			
			out.close();
			if (iResult == -1) {
				MobiTNTLog.write("uplaod file failed");
				return new Response(HTTP_OK, MIME_XML,
						PageGen.GenRetCode(EADefine.EA_RET_SAVE_FILE_FAILED));
			}

			
		} catch (Exception e1) {
			MobiTNTLog.write("uplaod file failed");
			return new Response(HTTP_OK, MIME_XML, PageGen.ReturnException(e1
					.toString()));
		}

		MobiTNTLog.write("uplaod file done");
		return new Response(HTTP_OK, MIME_XML, PageGen.GenRetCode(EADefine.EA_RET_OK));
	}

	public Response serve(String sUri, String method, Properties header,
			Properties parms) {
	

		if (sUri.contains(".xml")) {
			String sRespType = MIME_XML;
			String sXmlMsg = AjaxRequestParser(sUri, parms, sRespType);
			Response r = new Response(HTTP_OK, sRespType, sXmlMsg);
			r.addHeader("Content-length", "" + sXmlMsg.getBytes().length);
			r.addHeader("Charset", "utf-8");
			MobiTNTLog.write("Waiting4Req");
			return r;
		}

		String sAction = parms.getProperty("action", "n");
		if (sAction.contains("getfile")) {
			String sFile = parms.getProperty("file", "");

			// String sFile1 = java.net.URLDecoder.decode(sFile);
			return serveFileFromLocal(sUri, header, sFile);
		}

		if (sAction.contains("getappicon")) {
			String sAppName = parms.getProperty("appname", "n");
			ByteArrayOutputStream out = AppApi.GetAppIcon(sAppName);
			if (out == null) {
				return new Response(HTTP_OK, MIME_HTML, "error");
			}

			InputStream is = new ByteArrayInputStream(out.toByteArray());
			String mime = "image/png";

			// Support (simple) skipping:
			long startFrom = 0;
			// String range = header.getProperty("range");

			try {
				Response r = new Response(HTTP_OK, mime, is);
				r.addHeader("Content-length", "" + (is.available() - startFrom));
				r.addHeader(
						"Content-range",
						"" + startFrom + "-" + (is.available() - 1) + "/"
								+ is.available());

				return r;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return new Response(HTTP_OK, MIME_HTML, "error");
		}

		String sFileName = sUri;
		if (sUri.equals("/")) {
			sFileName = "/main.html";
		}

		MobiTNTLog.write("Waiting4Req");

/*		if (sFileName.contains("langtype.js")) {
			String sLangInfo = SysPageGen.GetLangType();
			Response r = new Response(HTTP_OK, MIME_XML, sLangInfo);
			r.addHeader("Content-length", "" + sLangInfo.getBytes().length);
			return r;
		}*/

		return serveFile(sUri, header, sFileName);
	}

	public String AjaxRequestParser(String sRequest, Properties parms,
			String sRespType) {

		/* here response to ajax request */
		PageGen pageGen = null;
		if (sRequest.contains("FolderList.xml")) {
			pageGen = new StorageManager();
		}  else if (sRequest.contains("CallLog.xml")) {
			pageGen = new CallLogManager();
		} else if (sRequest.contains("SysInfo.xml")) {
			pageGen = new SysManager();
		} else if (sRequest.contains("action=uploadfile")) {
			pageGen = new StorageManager();
		} else if (sRequest.contains("AppList.xml")) {
			pageGen = new AppManager();
		} else if (sRequest.contains("SmsList.xml")) {
			pageGen = new SmsManager();
		} else if (sRequest.contains("Backup.xml")) {
			pageGen = new BackupManager();
		} else if (sRequest.contains("ContactList.xml")) {
			pageGen = new ContactManager();
		} else if (sRequest.contains("Media.xml")) {
			pageGen = new MediaManager();
		} else if (sRequest.contains("LangManager.xml")) {
			pageGen = new PageGen();
		} else if (sRequest.contains("detectstatus.xml")) {
			pageGen = new SysManager();
		}

		
		if (pageGen == null){
			return null;
		}
		
		// here assume all ajax request is from GET
		pageGen.m_iReqType = EADefine.HTTP_REQ_TYPE_GET;
		String sXmlResponse = pageGen.ProcessRequest(sRequest, parms);
		sRespType = pageGen.GetRespMimeType();
		if (sXmlResponse == null) {
			MobiTNTLog.write("failed to response request:" + sRequest);
			return PageGen.GenRetCode(EADefine.EA_RET_UNKONW_REQ);
		}
		return sXmlResponse;
	}

	InputStream GetFileFromAsset(String sFileName) throws IOException {
		if (sFileName.length() < 1) {
			return null;
		}

		if (!sFileName.startsWith("/", 0)) {
			sFileName = EADefine.EA_FILE_SEPERATOR + sFileName;
		}

		String sPath = EADefine.EA_HTML_PATH + sFileName;
		return htmlData.open(sPath);
	}

	/**
	 * Serves file from homeDir and its' subdirectories (only). Uses only URI,
	 * ignores all headers and HTTP parameters.
	 */
	public Response serveFile(String uri, Properties header, String sFileName) {
		// Remove URL arguments
		uri = uri.trim().replace(File.separatorChar, '/');
		if (uri.indexOf('?') >= 0)
			uri = uri.substring(0, uri.indexOf('?'));

		// Prohibit getting out of current directory
		if (uri.startsWith("..") || uri.endsWith("..")
				|| uri.indexOf("../") >= 0) {
			String sXmlMsg = PageGen.GenRetCode(EADefine.EA_RET_ACCESS_DENINED);
			Response r = new Response(HTTP_OK, MIME_XML, sXmlMsg);
			r.addHeader("Content-length", "" + sXmlMsg.getBytes().length);
			return r;
		}

		try {
			// Get MIME type from file name extension, if possible
			String mime = MimeTypes.GetMimeTypes(sFileName);

			// Support (simple) skipping:
			long startFrom = 0;
			String range = header.getProperty("range");
			if (range != null) {
				if (range.startsWith("bytes=")) {
					range = range.substring("bytes=".length());
					int minus = range.indexOf('-');
					if (minus > 0)
						range = range.substring(0, minus);
					try {
						startFrom = Long.parseLong(range);
					} catch (NumberFormatException nfe) {
					}
				}
			}

			// AssetFileDescriptor file = GetFileFromAsset(sFileName);
			InputStream fis = GetFileFromAsset(sFileName);

			fis.skip(startFrom);
			Response r = new Response(HTTP_OK, mime, fis);
			r.addHeader("Content-length", "" + (fis.available() - startFrom));
			r.addHeader(
					"Content-range",
					"" + startFrom + "-" + (fis.available() - 1) + "/"
							+ fis.available());
			return r;
		} catch (Exception e) {
			String sXmlMsg = PageGen
					.ReturnException("FORBIDDEN: Reading file failed:"
							+ e.toString());
			Response r = new Response(HTTP_OK, MIME_XML, sXmlMsg);
			r.addHeader("Content-length", "" + sXmlMsg.getBytes().length);
			return r;
			/*
			 * return new Response(HTTP_FORBIDDEN, MIME_PLAINTEXT,
			 * "FORBIDDEN: Reading file failed.");
			 */
		}
	}

	/**
	 * Serves file from homeDir and its' subdirectories (only). Uses only URI,
	 * ignores all headers and HTTP parameters.
	 */
	public Response serveFileFromLocal(String uri, Properties header,
			String sFileName) {
		// Remove URL arguments
		uri = uri.trim().replace(File.separatorChar, '/');
		if (uri.indexOf('?') >= 0)
			uri = uri.substring(0, uri.indexOf('?'));

		// Prohibit getting out of current directory
		if (uri.startsWith("..") || uri.endsWith("..")
				|| uri.indexOf("../") >= 0) {
			String sXmlMsg = PageGen.GenRetCode(EADefine.EA_RET_ACCESS_DENINED);
			Response r = new Response(HTTP_OK, MIME_XML, sXmlMsg);
			r.addHeader("Content-length", "" + sXmlMsg.getBytes().length);
			return r;
			/*
			 * return new Response(HTTP_FORBIDDEN, MIME_PLAINTEXT,
			 * "FORBIDDEN: Won't serve ../ for security reasons.");
			 */
		}

		try {
			// Get MIME type from file name extension, if possible
			String mime = MimeTypes.GetMimeTypes(sFileName);

			// Support (simple) skipping:
			long startFrom = 0;
			String range = header.getProperty("range");
			if (range != null) {
				if (range.startsWith("bytes=")) {
					range = range.substring("bytes=".length());
					int minus = range.indexOf('-');
					if (minus > 0)
						range = range.substring(0, minus);
					try {
						startFrom = Long.parseLong(range);
					} catch (NumberFormatException nfe) {
					}
				}
			}

			FileInputStream fis = new FileInputStream(sFileName);

			fis.skip(startFrom);
			Response r = new Response(HTTP_OK, mime, fis);
			r.addHeader("Content-length", "" + (fis.available() - startFrom));
			r.addHeader(
					"Content-range",
					"" + startFrom + "-" + (fis.available() - 1) + "/"
							+ fis.available());
			return r;
		} catch (Exception e) {
			String sXmlMsg = PageGen
					.ReturnException("FORBIDDEN: Reading file failed:"
							+ e.toString());
			Response r = new Response(HTTP_OK, MIME_XML, sXmlMsg);
			r.addHeader("Content-length", "" + sXmlMsg.getBytes().length);
			return r;
			/*
			 * return new Response(HTTP_FORBIDDEN, MIME_PLAINTEXT,
			 * "FORBIDDEN: Reading file failed.");
			 */
		}
	}

	public static InputStream newInputStream(final ByteBuffer buf) {
		return new InputStream() {
			public synchronized int read() throws IOException {
				if (!buf.hasRemaining()) {
					return -1;
				}
				return buf.get();
			}

			public synchronized int read(byte[] bytes, int off, int len)
					throws IOException {
				// Read only what's left
				len = Math.min(len, buf.remaining());
				buf.get(bytes, off, len);
				return len;
			}
		};
	}

}
