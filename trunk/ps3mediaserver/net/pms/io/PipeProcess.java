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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;

import com.sun.jna.Platform;

import net.pms.PMS;

public class PipeProcess {
	
	private String linuxPipeName;
	private WindowsNamedPipe mk;
	private boolean forcereconnect;
	
	public PipeProcess(String pipeName, OutputParams params, String...extras) {
		forcereconnect = false;
		boolean in = true;
		if (extras != null && extras.length > 0 && extras[0].equals("out"))
			in = false;
		if (extras != null) {
			for(int i=0;i<extras.length;i++) {
				if (extras[i].equals("reconnect"))
					forcereconnect = true;
			}
		}
		if (PMS.get().isWindows())
			mk = new WindowsNamedPipe(pipeName, forcereconnect, in, params);
		else
			linuxPipeName = getPipeName(pipeName);
	}
	
	public PipeProcess(String pipeName, String...extras) {
		this(pipeName, null, extras);
	}

	private static String getPipeName(String pipeName) {
		try {
			return PMS.getConfiguration().getTempFolder() + "/" + pipeName;
		} catch (IOException e) {
			PMS.error("Pipe may not be in temporary directory", e);
			return pipeName;
		}
	}
	
	public String getInputPipe() {
		if (!PMS.get().isWindows())
			return linuxPipeName;
		return mk.getPipeName();
	}

	public String getOutputPipe() {
		if (!PMS.get().isWindows())
			return linuxPipeName;
		return mk.getPipeName();
	}
	
	public ProcessWrapper getPipeProcess() {
		if (!PMS.get().isWindows()) {
			OutputParams mkfifo_vid_params = new OutputParams(PMS.getConfiguration());
			mkfifo_vid_params.maxBufferSize = 0.1;
			mkfifo_vid_params.log = true;
			String cmdArray [] = new String[] { "mkfifo", PMS.get().isWindows()?"":"--mode=777", linuxPipeName };
			if (Platform.isMac() || Platform.isFreeBSD() || Platform.isSolaris()) {
				cmdArray = Arrays.copyOf(cmdArray, cmdArray.length+1);
				cmdArray[1] = "-m";
				cmdArray[3] = cmdArray[2];
				cmdArray[2] = "777";
			}
			ProcessWrapperImpl mkfifo_vid_process = new ProcessWrapperImpl(cmdArray, mkfifo_vid_params);
			return mkfifo_vid_process;
		}
		return mk;
	}
	
	public void deleteLater() {
		if (!PMS.get().isWindows()) {
			File f = new File(linuxPipeName);
			f.deleteOnExit();
		}
	}
	
	public BufferedOutputFile getDirectBuffer() throws IOException {
		if (!PMS.get().isWindows()) {
			return null;
		}
		return mk.getDirectBuffer();
	}
	
	public InputStream getInputStream() throws IOException {
		if (!PMS.get().isWindows()) {
			PMS.debug("Opening file " + linuxPipeName + " for reading...");
			RandomAccessFile raf = new RandomAccessFile ( linuxPipeName, "r" ) ;
			return new FileInputStream(raf.getFD());
		}
		return mk.getReadable();
	}
	
	public OutputStream getOutputStream() throws IOException {
		if (!PMS.get().isWindows()) {
			PMS.debug("Opening file " + linuxPipeName + " for writing...");
			RandomAccessFile raf = new RandomAccessFile ( linuxPipeName, "rw" ) ;
			FileOutputStream fout = new FileOutputStream(raf.getFD());
			if (forcereconnect) {
				//raf = new RandomAccessFile ( linuxPipeName, "rw" ) ;
				//fout = new FileOutputStream(raf.getFD());
			}
			return fout;
		}
		return mk.getWritable();
	}
}
 
