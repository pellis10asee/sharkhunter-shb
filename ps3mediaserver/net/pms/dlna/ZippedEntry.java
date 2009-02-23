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
package net.pms.dlna;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.pms.PMS;
import net.pms.formats.Format;
import net.pms.util.FileUtil;


public class ZippedEntry extends DLNAResource implements IPushOutput{
	
	@Override
	protected String getThumbnailURL() {
		if (getType() == Format.IMAGE || getType() == Format.AUDIO) // no thumbnail support for now for real based disk images
			return null;
		return super.getThumbnailURL();
	}

	private File z;
	private String zeName;
	private long length;
	private ZipFile zipFile;
	//private boolean nullable;
	//private byte data [];
	
	public ZippedEntry(File z, String zeName, long length) {
		this.zeName = zeName;
		this.z = z;
		this.length = length;
	}

	public InputStream getInputStream() {
		/*try {
			zipFile = new ZipFile(z);
			ZipEntry ze = zipFile.getEntry(zeName);
			InputStream in = zipFile.getInputStream(ze);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int n = -1;
			data = new byte [65536];
			while ((n=in.read(data)) > -1) {
				baos.write(data, 0, n);
			}
			in.close();
			baos.close();
			data = baos.toByteArray();
			zipFile.close();
			return new UnusedInputStream(new ByteArrayInputStream(data), this, 5000) {
				@Override
				public void unusedStreamSignal() {
					PMS.info("ZipEntry Data not asked since 5 seconds... Nullify buffer");
					data = null;
				}
			};
		} catch (IOException e) {
			throw new RuntimeException(e);
		}*/
		return null;
	}

	public String getName() {
		return zeName;
	}

	public long length() {
		if (player != null && player.type() != Format.IMAGE)
			return DLNAMediaInfo.TRANS_SIZE;
		return length;
	}

	public boolean isFolder() {
		return false;
	}

	public long lastModified() {
		return 0;
	}

	@Override
	public String getSystemName() {
		return FileUtil.getFileNameWithoutExtension(z.getAbsolutePath()) + "." + FileUtil.getExtension(zeName);
	}

	@Override
	public boolean isValid() {
		checktype();
		srtFile = FileUtil.doesSubtitlesExists(z, null);
		return ext != null;
	}
	
	@Override
	public boolean isUnderlyingSeekSupported() {
		return length() < MAX_ARCHIVE_SIZE_SEEK;
	}
	
	@Override
	public void push(final OutputStream out) throws IOException {
		Runnable r = new Runnable() {
			public void run() {
				try {
					zipFile = new ZipFile(z);
					ZipEntry ze = zipFile.getEntry(zeName);
					InputStream in = zipFile.getInputStream(ze);
					int n = -1;
					byte data [] = new byte [65536];
					while ((n=in.read(data)) > -1) {
						out.write(data, 0, n);
					}
					in.close();
				} catch (Exception e) {
					PMS.info("Unpack error, maybe it's normal, as backend can be terminated: " + e.getMessage());
				} finally {
					try {
						zipFile.close();
						out.close();
					} catch (IOException e) {}
				}
			}
		};
		new Thread(r).start();
	}
	
	@Override
	public void resolve() {
		if (ext == null || !ext.isVideo())
			return;
		boolean found = false;
		if (!found) {
			if (media == null) {
				media = new DLNAMediaInfo();
			}
			found = !media.mediaparsed && !media.parsing;
			if (ext != null) {
				InputFile input = new InputFile();
				input.push = this;
				input.size = length();
				ext.parse(media, input, getType());
			}
		}
		super.resolve();
	}

	@Override
	public InputStream getThumbnailInputStream() throws IOException {
		if (media != null && media.thumb != null)
			return media.getThumbnailInputStream();
		else return super.getThumbnailInputStream();
	}

}
