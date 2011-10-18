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

import net.pms.PMS;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.encoders.MEncoderVideo;
import net.pms.encoders.Player;
import net.pms.encoders.TSMuxerVideo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileTranscodeVirtualFolder extends VirtualFolder {
	private static final Logger logger = LoggerFactory.getLogger(FileTranscodeVirtualFolder.class);
	private boolean resolved;

	public FileTranscodeVirtualFolder(String name, String thumbnailIcon, boolean copy) {
		super(name, thumbnailIcon);
	}

	@Override
	public void resolve() {
		super.resolve();
		if (!resolved && children.size() == 1) { //OK
			DLNAResource child = children.get(0);
			child.resolve();
			if (child.ext.getProfiles() != null) {
				DLNAResource ref = child;
				Player tsMuxer = null;
				for (int i = 0; i < child.ext.getProfiles().size(); i++) {
					Player pl = PMS.get().getPlayer(child.ext.getProfiles().get(i), child.ext);
					if (pl != null && !child.player.equals(pl)) {
						DLNAResource avisnewChild = (DLNAResource) child.clone();
						avisnewChild.player = pl;
						avisnewChild.noName = true;
						avisnewChild.media = child.media;
						if (avisnewChild.player.id().equals(MEncoderVideo.ID)) {
							ref = avisnewChild;
						}
						if (avisnewChild.player.id().equals(TSMuxerVideo.ID)) {
							tsMuxer = pl;
						}
						addChildInternal(avisnewChild);
						addChapterFile(avisnewChild);
					}
				}
				for (int i = 0; i < child.media.audioCodes.size(); i++) {
					DLNAResource newChildNoSub = (DLNAResource) ref.clone();
					newChildNoSub.player = ref.player;
					newChildNoSub.media = ref.media;
					newChildNoSub.noName = true;
					newChildNoSub.media_audio = ref.media.audioCodes.get(i);
					newChildNoSub.media_subtitle = new DLNAMediaSubtitle();
					newChildNoSub.media_subtitle.id = -1;
					addChildInternal(newChildNoSub);

					addChapterFile(newChildNoSub);

					for (int j = 0; j < child.media.subtitlesCodes.size(); j++) {
						DLNAResource newChild = (DLNAResource) ref.clone();
						newChild.player = ref.player;
						newChild.media = ref.media;
						newChild.noName = true;
						newChild.media_audio = ref.media.audioCodes.get(i);
						newChild.media_subtitle = ref.media.subtitlesCodes.get(j);
						addChildInternal(newChild);
						addChapterFile(newChild);

						logger.debug("Duplicate " + ref.getName() + " with player: " + ref.player.toString() + " and aid: " + newChild.media_audio.id + " and sid: " + newChild.media_subtitle);
					}
				}

				if (tsMuxer != null) {
					for (int i = 0; i < child.media.audioCodes.size(); i++) {
						DLNAResource newChildNoSub = (DLNAResource) ref.clone();
						newChildNoSub.player = tsMuxer;
						newChildNoSub.media = ref.media;
						newChildNoSub.noName = true;
						newChildNoSub.media_audio = ref.media.audioCodes.get(i);
						addChildInternal(newChildNoSub);
						addChapterFile(newChildNoSub);

					}
				}

				// meskibob: I think it'd be a good idea to add a "Stream" option (for PS3 compatible containers) to the #Transcode# folder in addition to the current options already in there.
				DLNAResource justStreamed = (DLNAResource) ref.clone();
				if (justStreamed.ext != null && (justStreamed.ext.ps3compatible() || justStreamed.skipTranscode)) {
					justStreamed.player = null;
					justStreamed.media = ref.media;
					justStreamed.noName = true;
					addChildInternal(justStreamed);
					addChapterFile(justStreamed);
				}
			}
		}
		resolved = true;
	}

	private void addChapterFile(DLNAResource source) {
		if (PMS.getConfiguration().getChapterInterval() > 0 && PMS.getConfiguration().isChapterSupport()) {
			ChapterFileTranscodeVirtualFolder chapterFolder = new ChapterFileTranscodeVirtualFolder("Chapters:" + source.getDisplayName(), null, PMS.getConfiguration().getChapterInterval());
			DLNAResource newSeekChild = (DLNAResource) source.clone();
			newSeekChild.noName = true;
			chapterFolder.addChildInternal(newSeekChild);
			addChildInternal(chapterFolder);
		}
	}

	public FileTranscodeVirtualFolder(String name, String thumbnailIcon) {
		super(name, thumbnailIcon);
	}
}
