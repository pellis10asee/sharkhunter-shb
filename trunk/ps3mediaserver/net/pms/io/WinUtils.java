/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2008  A.Brochard
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2
 * of the License only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package net.pms.io;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.CharBuffer;
import java.util.prefs.Preferences;

import net.pms.PMS;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.WString;
import com.sun.jna.ptr.LongByReference;

public class WinUtils {
	
	public interface Kernel32 extends Library {
		Kernel32 INSTANCE = (Kernel32) Native.loadLibrary("kernel32",
				Kernel32.class);
		Kernel32 SYNC_INSTANCE = (Kernel32) Native
				.synchronizedLibrary(INSTANCE);
		
		int GetShortPathNameW(WString lpszLongPath, char[] lpdzShortPath, int cchBuffer);
		//NativeLong GetShortPathNameW(WString inPath, Memory outPathBuffer, NativeLong outPathBufferSize);
		int GetWindowsDirectoryW(char[] lpdzShortPath, int uSize);
		boolean GetVolumeInformationW(
				char[] lpRootPathName,
				CharBuffer lpVolumeNameBuffer,
				int nVolumeNameSize,
				LongByReference lpVolumeSerialNumber,
				LongByReference lpMaximumComponentLength,
				LongByReference lpFileSystemFlags,
				CharBuffer lpFileSystemNameBuffer,
				int nFileSystemNameSize
				);	
		
		int SetThreadExecutionState(int EXECUTION_STATE);
		    
		int ES_DISPLAY_REQUIRED = 0x00000002;
		int ES_SYSTEM_REQUIRED = 0x00000001;
		int ES_CONTINUOUS = 0x80000000;
	}

	private static final int KEY_READ = 0x20019;
	private String vlcp;
	private String vlcv;
	private boolean avis;
	private boolean kerio;
	private String avsPluginsDir;
	
	public long lastDontSleepCall = 0;
	public long lastGoToSleepCall = 0;
	
	public void disableGoToSleep() {
		if (PMS.getConfiguration().isPreventsSleep()) {
			if (Platform.isWindows()) {
				// Disable go to sleep (every 40s)
				if (System.currentTimeMillis() - lastDontSleepCall > 40000) {
					PMS.info("Calling SetThreadExecutionState ES_SYSTEM_REQUIRED");
					Kernel32.INSTANCE.SetThreadExecutionState(Kernel32.ES_SYSTEM_REQUIRED | Kernel32.ES_CONTINUOUS);
					lastDontSleepCall = System.currentTimeMillis();
				}
			}
		}
	}
	
	public void reenableGoToSleep() {
		if (PMS.getConfiguration().isPreventsSleep()) {
			if (Platform.isWindows()) {
				// Reenable go to sleep
				if (System.currentTimeMillis() - lastGoToSleepCall > 40000) {
					PMS.info("Calling SetThreadExecutionState ES_CONTINUOUS");
					Kernel32.INSTANCE.SetThreadExecutionState(Kernel32.ES_CONTINUOUS);
					lastGoToSleepCall = System.currentTimeMillis();
				}
			}
		}
	}

	public File getAvsPluginsDir() {
		if (avsPluginsDir == null)
			return null;
		File pluginsDir = new File(avsPluginsDir);
		if (!pluginsDir.exists())
			pluginsDir = null;
		return pluginsDir;
	}

	public static void main(String args[]) {
		WinUtils rb = new WinUtils();
		System.out.println(rb.getVlcp());
		System.out.println(rb.getVlcv());
		System.out.println(rb.isAvis());
		WinUtils w =	new WinUtils();
		System.out.println(w.getWindowsDirectory());
		File dir = new File("D:\\Tests\\wma");
		File wma = dir.listFiles()[0];
		for(int i=0;i<10000;i++) {
			w.getShortPathNameW(wma.getAbsolutePath());
			w.getWindowsDirectory();
		}
	}
	
	public String getShortPathNameW(String longPathName) {

		if (Platform.isWindows()) {
			boolean unicodeChars = false;
			try {
				byte b1 [] = longPathName.getBytes("UTF-8");
				byte b2 [] = longPathName.getBytes("cp1252");
			unicodeChars = b1.length != b2.length;
			} catch (Exception e) {
				return longPathName;
			}
	
			if (unicodeChars && Platform.isWindows()) {
				try {
					WString pathname = new WString(longPathName);
				
					// ISSUE 90: crash when used too many times
					/*NativeLong bufferSize = new NativeLong((pathname.length()*2)+2);
					Memory buffer = new Memory(bufferSize.longValue());
			
					if (Kernel32.INSTANCE.GetShortPathNameW(pathname, buffer, bufferSize).longValue() == 0) {
					PMS.minimal("File does not exists ? " + pathname);
					return null;
					}
					PMS.info("Forcing short path name on " + pathname);
					String str= buffer.getString(0, true);
					return str;*/
					
					char test [] = new char [2+pathname.length()*2];
					int r = Kernel32.INSTANCE.GetShortPathNameW(pathname, test, test.length);
					if (r > 0) {
						PMS.info("Forcing short path name on " + pathname);
						return Native.toString(test);
					} else {
						PMS.minimal("File does not exists ? " + pathname);
						return null;
					}
				
				} catch (Exception e) {
					return longPathName;
				}
			}
			return longPathName;
		}
		return null;
	}
	
	public String getWindowsDirectory() {
		if (Platform.isWindows()) {
			char test [] = new char [2+256*2];
			int r = Kernel32.INSTANCE.GetWindowsDirectoryW(test, 256);
			if (r > 0) {
				return Native.toString(test);
			}
		}
		return null;
	}
	
	public String getDiskLabel(File f) {
		if (Platform.isWindows()) {
			String driveName;
			try {
				driveName = f.getCanonicalPath().substring(0, 2) + "\\";
				
				char[] lpRootPathName_chars = new char[4];
				for (int i=0; i<3; i++) {
					lpRootPathName_chars[i] = driveName.charAt(i);
				}
				lpRootPathName_chars[3] = '\0';
				int nVolumeNameSize = 256;
				CharBuffer lpVolumeNameBuffer_char = CharBuffer.allocate(nVolumeNameSize);
				LongByReference lpVolumeSerialNumber = new LongByReference();
				LongByReference lpMaximumComponentLength = new LongByReference();
				LongByReference lpFileSystemFlags = new LongByReference();
				int nFileSystemNameSize = 256;
				CharBuffer lpFileSystemNameBuffer_char = CharBuffer.allocate(nFileSystemNameSize);
		
				boolean result2 = Kernel32.INSTANCE.GetVolumeInformationW(
					lpRootPathName_chars,
					lpVolumeNameBuffer_char,
					nVolumeNameSize,
					lpVolumeSerialNumber,
					lpMaximumComponentLength,
					lpFileSystemFlags,
					lpFileSystemNameBuffer_char,
					nFileSystemNameSize);
				if (!result2) {
					return null;
				}
				String diskLabel = charString2String(lpVolumeNameBuffer_char);
				return diskLabel;
			} catch(Exception e) {
				return null;
			}
		}
		return null;
	}
	
	private String charString2String(CharBuffer buf) {
		char[] chars = buf.array();
		int i;
		for (i=0; i<chars.length; i++) {
			if (chars[i]=='\0') break;
		}
		return new String(chars,0,i);
	}

	public WinUtils() {
		if (Platform.isWindows()) {
			start();
		}
	}

	private void start() {
		final Preferences userRoot = Preferences.userRoot();
		final Preferences systemRoot = Preferences.systemRoot();
		final Class<? extends Preferences> clz = userRoot.getClass();
		try {
			if (clz.getName().endsWith("WindowsPreferences")) {
				final Method openKey = clz
						.getDeclaredMethod("WindowsRegOpenKey", int.class,
								byte[].class, int.class);
				openKey.setAccessible(true);
				final Method closeKey = clz.getDeclaredMethod(
						"WindowsRegCloseKey", int.class);
				closeKey.setAccessible(true);
				final Method winRegQueryValue = clz.getDeclaredMethod(
						"WindowsRegQueryValueEx", int.class, byte[].class);
				winRegQueryValue.setAccessible(true);
				byte[] valb = null;
				String key = null;
				key = "SOFTWARE\\VideoLAN\\VLC";
				int handles[] = (int[]) openKey.invoke(systemRoot, -2147483646,
						toCstr(key), KEY_READ);
				if (handles.length == 2 && handles[0] != 0 && handles[1] == 0) {
					// do nothing
				} else {
					key = "SOFTWARE\\Wow6432Node\\VideoLAN\\VLC";
					handles = (int[]) openKey.invoke(systemRoot, -2147483646,
							toCstr(key), KEY_READ);
				}
				if (handles.length == 2 && handles[0] != 0 && handles[1] == 0) {
					valb = (byte[]) winRegQueryValue.invoke(systemRoot,
							handles[0], toCstr(""));
					vlcp = (valb != null ? new String(valb).trim() : null);
					valb = (byte[]) winRegQueryValue.invoke(systemRoot,
							handles[0], toCstr("Version"));
					vlcv = (valb != null ? new String(valb).trim() : null);
					closeKey.invoke(systemRoot, handles[0]);
				}
				key = "SOFTWARE\\AviSynth";
				handles = (int[]) openKey.invoke(systemRoot, -2147483646,
						toCstr(key), KEY_READ);
				if (handles.length == 2 && handles[0] != 0 && handles[1] == 0) {
					// do nothing
				} else {
					key = "SOFTWARE\\Wow6432Node\\AviSynth";
					handles = (int[]) openKey.invoke(systemRoot, -2147483646,
							toCstr(key), KEY_READ);
				}
				if (handles.length == 2 && handles[0] != 0 && handles[1] == 0) {
					avis = true;
					valb = (byte[]) winRegQueryValue.invoke(systemRoot,
							handles[0], toCstr("plugindir2_5"));
					avsPluginsDir = (valb != null ? new String(valb).trim() : null);
					closeKey.invoke(systemRoot, handles[0]);
				}
				key = "SOFTWARE\\Kerio";
				handles = (int[]) openKey.invoke(systemRoot, -2147483646,
						toCstr(key), KEY_READ);
				if (handles.length == 2 && handles[0] != 0 && handles[1] == 0) {
					kerio = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean isKerioFirewall() {
		return kerio;
	}

	private static byte[] toCstr(String str) {
		byte[] result = new byte[str.length() + 1];
		for (int i = 0; i < str.length(); i++) {
			result[i] = (byte) str.charAt(i);
		}
		result[str.length()] = 0;
		return result;
	}

	public String getVlcp() {
		return vlcp;
	}

	public String getVlcv() {
		return vlcv;
	}

	public boolean isAvis() {
		return avis;
	}

}
