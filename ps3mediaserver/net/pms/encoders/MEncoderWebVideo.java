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
package net.pms.encoders;

import java.io.IOException;

import javax.swing.JComponent;

import net.pms.configuration.PmsConfiguration;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.formats.Format;
import net.pms.io.OutputParams;
import net.pms.io.PipeProcess;
import net.pms.io.ProcessWrapper;
import net.pms.io.ProcessWrapperImpl;

public class MEncoderWebVideo extends Player {

	@Override
	public JComponent config() {
		return null;
	}
	
	public static final String ID = "mencoderwebvideo"; //$NON-NLS-1$
	
	@Override
	public String id() {
		return ID;
	}
	
	@Override
	public int purpose() {
		return VIDEO_WEBSTREAM_PLAYER;
	}

	@Override
	public boolean isTimeSeekable() {
		return false;
	}

	@Override
	public String mimeType() {
		return "video/mpeg"; //$NON-NLS-1$
	}

	protected String[] getDefaultArgs() {
		return new String [] { "-prefer-ipv4", "-nocache", "-quiet", "-oac", "lavc", "-of", "lavf", "-lavfopts", "format=dvd", "-ovc", "lavc", "-lavcopts", "vcodec=mpeg2video:vbitrate=4096:threads=2:acodec=ac3:abitrate=128", "-vf", "harddup", "-ofps", "24000/1001" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$ //$NON-NLS-13$ //$NON-NLS-14$
	}
	
	private final PmsConfiguration configuration;
	
	public MEncoderWebVideo(PmsConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public ProcessWrapper launchTranscode(String fileName, DLNAMediaInfo media,
			OutputParams params) throws IOException {
		params.minBufferSize = params.minFileSize;
		params.secondread_minsize = 100000;
		//return super.launchTranscode(fileName, media, params);
		
		PipeProcess pipe = new PipeProcess("mencoder" + System.currentTimeMillis()); //$NON-NLS-1$
		params.input_pipes [0] = pipe;
		
		String cmdArray [] = new String [args().length + 4];
		cmdArray[0] = executable();
		cmdArray[1] = fileName;
		for(int i=0;i<args().length;i++) {
			cmdArray[i+2] = args()[i];
		}
		cmdArray[cmdArray.length-2] = "-o"; //$NON-NLS-1$
		cmdArray[cmdArray.length-1] = pipe.getInputPipe();
		
		ProcessWrapper mkfifo_process = pipe.getPipeProcess();
		
		ProcessWrapperImpl pw = new ProcessWrapperImpl(cmdArray, params);
		pw.attachProcess(mkfifo_process);
		mkfifo_process.runInNewThread();
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) { }
		pipe.deleteLater();
		
		pw.runInNewThread();
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) { }
		return pw;
	}

	@Override
	public boolean avisynth() {
		return false;
	}
	
	@Override
	public String name() {
		return "MEncoder Web"; //$NON-NLS-1$
	}

	@Override
	public String[] args() {
		return getDefaultArgs();
	}

	@Override
	public String executable() {
		return configuration.getMencoderPath();
	}

	@Override
	public int type() {
		return Format.VIDEO;
	}

}
