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
package net.pms.network;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;

import net.pms.configuration.RendererConfiguration;
import net.pms.dlna.DLNAMediaAudio;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAResource;
import net.pms.external.StartStopListenerDelegate;
import net.pms.PMS;

import org.apache.commons.lang.StringUtils;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.stream.ChunkedStream;

public class RequestV2 extends HTTPResource {
	private final static String CRLF = "\r\n";
	private static SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.US);
	private static int BUFFER_SIZE = 8*1024;
	int sendB = 0;
	private String method;
	private String argument;
	private String soapaction;
	private String content;
	private String objectID;
	private int startingIndex;
	private int requestCount;
	private String browseFlag;
	private long lowRange;
	private InputStream inputStream;
	private RendererConfiguration mediaRenderer;
	private String transferMode;
	private String contentFeatures;
	private double timeseek;
	private long highRange;
	private boolean http10;

	public RendererConfiguration getMediaRenderer() {
		return mediaRenderer;
	}

	public void setMediaRenderer(RendererConfiguration mediaRenderer) {
		this.mediaRenderer = mediaRenderer;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public long getLowRange() {
		return lowRange;
	}

	public void setLowRange(long lowRange) {
		this.lowRange = lowRange;
	}

	public String getTransferMode() {
		return transferMode;
	}

	public void setTransferMode(String transferMode) {
		this.transferMode = transferMode;
	}

	public String getContentFeatures() {
		return contentFeatures;
	}

	public void setContentFeatures(String contentFeatures) {
		this.contentFeatures = contentFeatures;
	}

	public double getTimeseek() {
		return timeseek;
	}

	public void setTimeseek(double timeseek) {
		this.timeseek = timeseek;
	}

	public long getHighRange() {
		return highRange;
	}

	public void setHighRange(long highRange) {
		this.highRange = highRange;
	}

	public boolean isHttp10() {
		return http10;
	}

	public void setHttp10(boolean http10) {
		this.http10 = http10;
	}

	public RequestV2(String method, String argument) {
		this.method = method;
		this.argument = argument;
	}

	public String getSoapaction() {
		return soapaction;
	}

	public void setSoapaction(String soapaction) {
		this.soapaction = soapaction;
	}

	public String getTextContent() {
		return content;
	}

	public void setTextContent(String content) {
		this.content = content;
	}

	public String getMethod() {
		return method;
	}

	public String getArgument() {
		return argument;
	}

	public ChannelFuture answer(
		HttpResponse output,
		MessageEvent e,
		final boolean close,
		final StartStopListenerDelegate startStopListenerDelegate) throws IOException
	{
		ChannelFuture future = null;
		long CLoverride = -1;
		StringBuffer response = new StringBuffer();
		DLNAResource dlna = null;
		boolean xbox = mediaRenderer.isXBOX();

		if ((method.equals("GET") || method.equals("HEAD")) && argument.startsWith("console/")) {
			output.setHeader(HttpHeaders.Names.CONTENT_TYPE,  "text/html");
			response.append(HTMLConsole.servePage(argument.substring(8)));
		} else if ((method.equals("GET") || method.equals("HEAD")) && argument.startsWith("get/")) {
			String id = argument.substring(argument.indexOf("get/") + 4, argument.lastIndexOf("/"));
			id = id.replace("%24", "$"); // popcorn hour ?
			ArrayList<DLNAResource> files = PMS.get().getRootFolder(mediaRenderer).getDLNAResources(id, false, 0, 0, mediaRenderer);
			if(files==null||files.size()==0) { // nothing found
				String tmp=(String)PMS.getConfiguration().getCustomProperty("remote_control");
				if(tmp!=null&&!tmp.equalsIgnoreCase("false")) {
					ArrayList<RendererConfiguration> renders=PMS.get().getRenders();
					for(int i=0;i<renders.size();i++) {
						RendererConfiguration r=renders.get(i);
						if(r.equals(mediaRenderer))
							continue;
						files = PMS.get().getRootFolder(r).getDLNAResources(id, false, 0, 0, r);
						if(files!=null&&files.size()!=0) {
							break;
						}
					}
				}
			}
			if (transferMode != null) {
				output.setHeader("TransferMode.DLNA.ORG", transferMode);
			}

			if (files.size() == 1) {
				dlna = files.get(0);
				String fileName = argument.substring(argument.lastIndexOf("/")+1);
				if (fileName.startsWith("thumbnail0000")) {
					output.setHeader(HttpHeaders.Names.CONTENT_TYPE, files.get(0).getThumbnailContentType());
					output.setHeader(HttpHeaders.Names.ACCEPT_RANGES,  "bytes");
					output.setHeader(HttpHeaders.Names.EXPIRES,  getFUTUREDATE() + " GMT");
					output.setHeader(HttpHeaders.Names.CONNECTION,  "keep-alive");
					if (mediaRenderer.isMediaParserV2())
						dlna.checkThumbnail();
					inputStream = dlna.getThumbnailInputStream();
				} else {
					inputStream = dlna.getInputStream(lowRange, highRange, timeseek, mediaRenderer);
					if (inputStream != null) {
						startStopListenerDelegate.start(dlna);
					}
					output.setHeader(HttpHeaders.Names.CONTENT_TYPE, getRendererMimeType(files.get(0).mimeType(), mediaRenderer));
					// Ditlew - org
					String name = dlna.getDisplayName(mediaRenderer);
					if (dlna.media != null) {
						if (StringUtils.isNotBlank(dlna.media.container)) {
							name += " [container: " + dlna.media.container + "]";
						}
						if (StringUtils.isNotBlank(dlna.media.codecV)) {
							name += " [video: " + dlna.media.codecV + "]";
						}
					}
					PMS.get().getFrame().setStatusLine("Serving " + name);
					// Ditlew - org
					CLoverride = files.get(0).length(mediaRenderer);
					if (lowRange > 0 || highRange > 0) {
						long totalsize = CLoverride;
						if (highRange >= CLoverride)
							highRange = CLoverride-1;
						if (CLoverride == -1) {
							lowRange = 0;
							totalsize = inputStream.available();
							highRange = totalsize -1;
						}
						output.setHeader(HttpHeaders.Names.CONTENT_RANGE, "bytes " + lowRange + "-" + highRange + "/" +totalsize);
					}
					if (contentFeatures != null)
						output.setHeader("ContentFeatures.DLNA.ORG", files.get(0).getDlnaContentFeatures());
					output.setHeader(HttpHeaders.Names.ACCEPT_RANGES, "bytes");
					output.setHeader(HttpHeaders.Names.CONNECTION, "keep-alive");
				}
			}
		} else if ((method.equals("GET") || method.equals("HEAD")) && (argument.toLowerCase().endsWith(".png") || argument.toLowerCase().endsWith(".jpg") || argument.toLowerCase().endsWith(".jpeg"))) {
			if (argument.toLowerCase().endsWith(".png")) {
				output.setHeader(HttpHeaders.Names.CONTENT_TYPE, "image/png");
			} else {
				output.setHeader(HttpHeaders.Names.CONTENT_TYPE, "image/jpeg");
			}
			output.setHeader(HttpHeaders.Names.ACCEPT_RANGES, "bytes");
			output.setHeader(HttpHeaders.Names.CONNECTION, "keep-alive");
			output.setHeader(HttpHeaders.Names.EXPIRES,  getFUTUREDATE() + " GMT");
			inputStream = getResourceInputStream(argument);
		} else if ((method.equals("GET") || method.equals("HEAD")) && (argument.equals("description/fetch") || argument.endsWith("1.0.xml"))) {
			output.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/xml; charset=\"utf-8\"");
			output.setHeader(HttpHeaders.Names.CACHE_CONTROL, "no-cache");
			output.setHeader(HttpHeaders.Names.EXPIRES,  "0");
			output.setHeader(HttpHeaders.Names.ACCEPT_RANGES, "bytes");
			output.setHeader(HttpHeaders.Names.CONNECTION, "keep-alive");
			inputStream = getResourceInputStream((argument.equals("description/fetch")?"PMS.xml":argument));
			if (argument.equals("description/fetch")) {
				byte b [] = new byte [inputStream.available()];
				inputStream.read(b);
				String s = new String(b);
				s = s.replace("uuid:1234567890TOTO", PMS.get().usn());//.substring(0, PMS.get().usn().length()-2));
				String profileName = PMS.getConfiguration().getProfileName();
				if (PMS.get().getServer().getHost() != null) {
					s = s.replace("<host>", PMS.get().getServer().getHost());
					s = s.replace("<port>", "" +PMS.get().getServer().getPort());
				}
				if (xbox) {
					PMS.info("DLNA changes for Xbox360");
					s = s.replace("PS3 Media Server", "PS3 Media Server [" + profileName + "] : Windows Media Connect");
					s = s.replace("<modelName>PMS</modelName>", "<modelName>Windows Media Connect</modelName>");
					s = s.replace("<serviceList>", "<serviceList>" + CRLF + "<service>" + CRLF +
							"<serviceType>urn:microsoft.com:service:X_MS_MediaReceiverRegistrar:1</serviceType>" + CRLF +
							"<serviceId>urn:microsoft.com:serviceId:X_MS_MediaReceiverRegistrar</serviceId>" + CRLF +
							"<SCPDURL>/upnp/mrr/scpd</SCPDURL>" + CRLF +
							"<controlURL>/upnp/mrr/control</controlURL>" + CRLF +
							"</service>" + CRLF);
				} else {
					s = s.replace("PS3 Media Server", "PS3 Media Server [" + profileName + "]");
				}

				if (!mediaRenderer.isPS3()) {
					// hacky stuff. replace the png icon by a jpeg one. Like mpeg2 remux,
					// really need a proper format compatibility list by renderer
					s = s.replace("<mimetype>image/png</mimetype>", "<mimetype>image/jpeg</mimetype>");
					s = s.replace("/images/thumbnail-256.png", "/images/thumbnail-120.jpg");
					s = s.replace(">256<", ">120<");
				}
				response.append(s);
				inputStream = null;
			}
		} else if (method.equals("POST") && (argument.contains("MS_MediaReceiverRegistrar_control") || argument.contains("mrr/control"))) {
			output.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/xml; charset=\"utf-8\"");
			response.append(HTTPXMLHelper.XML_HEADER);
			response.append(CRLF);
			response.append(HTTPXMLHelper.SOAP_ENCODING_HEADER);
			response.append(CRLF);
			if (soapaction != null && soapaction.contains("IsAuthorized")) {
				response.append(HTTPXMLHelper.XBOX_2);
				response.append(CRLF);
			} else if (soapaction != null && soapaction.contains("IsValidated")) {
				response.append(HTTPXMLHelper.XBOX_1);
				response.append(CRLF);
			}
			response.append(HTTPXMLHelper.BROWSERESPONSE_FOOTER);
			response.append(CRLF);
			response.append(HTTPXMLHelper.SOAP_ENCODING_FOOTER);
			response.append(CRLF);
		} else if (method.equals("POST") && argument.equals("upnp/control/connection_manager")) {
			output.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/xml; charset=\"utf-8\"");
			if (soapaction.indexOf("ConnectionManager:1#GetProtocolInfo") > -1) {
				response.append(HTTPXMLHelper.XML_HEADER);
				response.append(CRLF);
				response.append(HTTPXMLHelper.SOAP_ENCODING_HEADER);
				response.append(CRLF);
				response.append(HTTPXMLHelper.PROTOCOLINFO_RESPONSE);
				response.append(CRLF);
				response.append(HTTPXMLHelper.SOAP_ENCODING_FOOTER);
				response.append(CRLF);
			}
		} else if (method.equals("POST") && argument.equals("upnp/control/content_directory")) {
			output.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/xml; charset=\"utf-8\"");
			if (soapaction.indexOf("ContentDirectory:1#GetSystemUpdateID") > -1) {
				response.append(HTTPXMLHelper.XML_HEADER);
				response.append(CRLF);
				response.append(HTTPXMLHelper.SOAP_ENCODING_HEADER);
				response.append(CRLF);
				response.append(HTTPXMLHelper.GETSYSTEMUPDATEID_HEADER);
				response.append(CRLF);
				response.append("<Id>" + DLNAResource.systemUpdateId + "</Id>");
				response.append(CRLF);
				response.append(HTTPXMLHelper.GETSYSTEMUPDATEID_FOOTER);
				response.append(CRLF);
				response.append(HTTPXMLHelper.SOAP_ENCODING_FOOTER);
				response.append(CRLF);
			} else if (soapaction.indexOf("ContentDirectory:1#GetSortCapabilities") > -1) {
				response.append(HTTPXMLHelper.XML_HEADER);
				response.append(CRLF);
				response.append(HTTPXMLHelper.SOAP_ENCODING_HEADER);
				response.append(CRLF);
				response.append(HTTPXMLHelper.SORTCAPS_RESPONSE);
				response.append(CRLF);
				response.append(HTTPXMLHelper.SOAP_ENCODING_FOOTER);
				response.append(CRLF);
			} else if (soapaction.indexOf("ContentDirectory:1#GetSearchCapabilities") > -1) {
				response.append(HTTPXMLHelper.XML_HEADER);
				response.append(CRLF);
				response.append(HTTPXMLHelper.SOAP_ENCODING_HEADER);
				response.append(CRLF);
				response.append(HTTPXMLHelper.SEARCHCAPS_RESPONSE);
				response.append(CRLF);
				response.append(HTTPXMLHelper.SOAP_ENCODING_FOOTER);
				response.append(CRLF);
			} else if (soapaction.contains("ContentDirectory:1#Browse") || soapaction.contains("ContentDirectory:1#Search")) {
				objectID = getEnclosingValue(content, "<ObjectID>", "</ObjectID>");
				String containerID = null;
				if ((objectID == null || objectID.length() == 0) /*&& xbox*/) {
					containerID = getEnclosingValue(content, "<ContainerID>", "</ContainerID>");
					if (!containerID.contains("$")) {
						objectID = "0";
					} else {
						objectID = containerID;
						containerID = null;
					}
				}
				Object sI = getEnclosingValue(content, "<StartingIndex>", "</StartingIndex>");
				Object rC = getEnclosingValue(content, "<RequestedCount>", "</RequestedCount>");
				browseFlag = getEnclosingValue(content, "<BrowseFlag>", "</BrowseFlag>");
				if (sI != null)
					startingIndex = Integer.parseInt(sI.toString());
				if (rC != null)
					requestCount = Integer.parseInt(rC.toString());

				response.append(HTTPXMLHelper.XML_HEADER);
				response.append(CRLF);
				response.append(HTTPXMLHelper.SOAP_ENCODING_HEADER);
				response.append(CRLF);

				if (soapaction.contains("ContentDirectory:1#Search")) {
					response.append(HTTPXMLHelper.SEARCHRESPONSE_HEADER);
				} else {
					response.append(HTTPXMLHelper.BROWSERESPONSE_HEADER);
				}

				response.append(CRLF);
				response.append(HTTPXMLHelper.RESULT_HEADER);
				response.append(HTTPXMLHelper.DIDL_HEADER);

				if (soapaction.contains("ContentDirectory:1#Search"))
					browseFlag = "BrowseDirectChildren";

				// XBOX virtual containers ... d'oh!
				String searchCriteria = null;
				if (xbox && PMS.getConfiguration().getUseCache() && PMS.get().getLibrary() != null && containerID != null) {
					if (containerID.equals("7") && PMS.get().getLibrary().getAlbumFolder() != null)
						objectID = PMS.get().getLibrary().getAlbumFolder().getId();
					else if (containerID.equals("6") && PMS.get().getLibrary().getArtistFolder() != null)
						objectID = PMS.get().getLibrary().getArtistFolder().getId();
					else if (containerID.equals("5") && PMS.get().getLibrary().getGenreFolder() != null)
						objectID = PMS.get().getLibrary().getGenreFolder().getId();
					else if (containerID.equals("F") && PMS.get().getLibrary().getPlaylistFolder() != null)
						objectID = PMS.get().getLibrary().getPlaylistFolder().getId();
					else if (containerID.equals("4") && PMS.get().getLibrary().getAllFolder() != null)
						objectID = PMS.get().getLibrary().getAllFolder().getId();
					else if (containerID.equals("1")) {
						String artist = getEnclosingValue(content, "upnp:artist = &quot;", "&quot;)");
						if (artist != null) {
							objectID = PMS.get().getLibrary().getArtistFolder().getId();
							searchCriteria = artist;
						}
					}
				}
				else if (soapaction.contains("ContentDirectory:1#Search")) 
					searchCriteria=getEnclosingValue(content,"<SearchCriteria>","</SearchCriteria>");

				ArrayList<DLNAResource> files = PMS.get().getRootFolder(mediaRenderer).getDLNAResources(objectID, browseFlag!=null&&browseFlag.equals("BrowseDirectChildren"), startingIndex, requestCount, mediaRenderer,
						searchCriteria);
				if (searchCriteria != null && files != null) {
					searchCriteria=searchCriteria.toLowerCase();
					for(int i=files.size()-1;i>=0;i--) {
						DLNAResource res=files.get(i);
						if(res.isSearched())
							continue;
						boolean keep=res.getName().toLowerCase().indexOf(searchCriteria)!=-1;
						if(res.media!=null) {
							for(int j=0;j<res.media.audioCodes.size();j++) {
								DLNAMediaAudio audio=res.media.audioCodes.get(j);
								keep|=audio.album.toLowerCase().indexOf(searchCriteria)!=-1;
								keep|=audio.artist.toLowerCase().indexOf(searchCriteria)!=-1;
								keep|=audio.songname.toLowerCase().indexOf(searchCriteria)!=-1;
							}
						}
						if(!keep) // dump it
							files.remove(i);
					}
					if(xbox)
						if (files.size() > 0) {
							files = files.get(0).getChildren();
						}
				}

				int minus = 0;
				if (files != null) {
					for(DLNAResource uf:files) {
						if (xbox && containerID != null)
							uf.setFakeParentId(containerID);
						if (uf.isCompatible(mediaRenderer) && (uf.getPlayer() == null || uf.getPlayer().isPlayerCompatible(mediaRenderer)))
							response.append(uf.toString(mediaRenderer));
						else
							minus++;
					}
				}

				response.append(HTTPXMLHelper.DIDL_FOOTER);
				response.append(HTTPXMLHelper.RESULT_FOOTER);
				response.append(CRLF);
				int filessize = 0;
				if (files != null)
					filessize = files.size();
				response.append("<NumberReturned>" + (filessize - minus) + "</NumberReturned>");
				response.append(CRLF);
				DLNAResource parentFolder = null;
				if (files != null && filessize > 0)
					parentFolder = files.get(0).getParent();
				if (browseFlag!=null&&browseFlag.equals("BrowseDirectChildren") && mediaRenderer.isMediaParserV2() && !mediaRenderer.isAnalyzeFolderAllFiles()) {
					// with the new parser, files are parsed and analyzed *before* creating the DLNA tree,
					// every 10 items (the ps3 asks 10 by 10),
					// so we do not know exactly the total number of items in the DLNA folder to send
					// (regular files, plus the #transcode folder, maybe the #imdb one, also files can be
					// invalidated and hidden if format is broken or encrypted, etc.).
					// let's send a fake total size to force the renderer to ask following items
					int totalCount = startingIndex + requestCount + 1; // returns 11 when 10 asked
					if (filessize - minus <= 0) // if no more elements, send the startingIndex
						totalCount = startingIndex;
					response.append("<TotalMatches>" + totalCount + "</TotalMatches>");
				} else if(browseFlag!=null && browseFlag.equals("BrowseDirectChildren")) {
					response.append("<TotalMatches>" + (((parentFolder!=null)?parentFolder.childrenNumber():filessize) - minus) + "</TotalMatches>");
				} else { //from upnp spec: If BrowseMetadata is specified in the BrowseFlags then TotalMatches = 1
					response.append("<TotalMatches>1</TotalMatches>");
				}
				response.append(CRLF);
				response.append("<UpdateID>");
				if (parentFolder != null) {
					response.append(parentFolder.getUpdateId());
				} else {
					response.append("1");
				}
				response.append("</UpdateID>");
				response.append(CRLF);
				if (soapaction.contains("ContentDirectory:1#Search")) {
					response.append(HTTPXMLHelper.SEARCHRESPONSE_FOOTER);
				} else {
					response.append(HTTPXMLHelper.BROWSERESPONSE_FOOTER);
				}
				response.append(CRLF);
				response.append(HTTPXMLHelper.SOAP_ENCODING_FOOTER);
				response.append(CRLF);
				// PMS.debug(response.toString());
			}
		} else if(method.equals("SUBSCRIBE")) {
			output.setHeader("SID", PMS.get().usn());
			output.setHeader("TIMEOUT", "Second-1800");
		} else if(method.equals("NOTIFY")) {
			output.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/xml");
			output.setHeader("NT", "upnp:event");
			output.setHeader("NTS", "upnp:propchange");
			output.setHeader("SID", PMS.get().usn());
			output.setHeader("SEQ", "0");
			response.append("<e:propertyset xmlns:e=\"urn:schemas-upnp-org:event-1-0\">");
			response.append("<e:property>");
			response.append("<TransferIDs></TransferIDs>");
			response.append("</e:property>");
			response.append("<e:property>");
			response.append("<ContainerUpdateIDs></ContainerUpdateIDs>");
			response.append("</e:property>");
			response.append("<e:property>");
			response.append("<SystemUpdateID>" + DLNAResource.systemUpdateId + "</SystemUpdateID>");
			response.append("</e:property>");
			response.append("</e:propertyset>");
		}

		// output(output, "DATE: " + getDATE() + " GMT");
		// output(output, "LAST-MODIFIED: " + getOLDDATE() + " GMT");
		output.setHeader("Server",PMS.get().getServerName());

		if (response.length() > 0) {
			byte responseData [] = response.toString().getBytes("UTF-8");
			output.setHeader(HttpHeaders.Names.CONTENT_LENGTH, "" + responseData.length);
			// output(output, "");
			if (!method.equals("HEAD")) {
				ChannelBuffer buf = ChannelBuffers.copiedBuffer(responseData);
				output.setContent(buf);
				// PMS.debug(response.toString());
			}
			future = e.getChannel().write(output);
			if (close) {
				future.addListener(ChannelFutureListener.CLOSE);
			}
		} else if (inputStream != null) {
			if (CLoverride > -1) {
				if (lowRange > 0 && highRange > 0) {
					output.setHeader(HttpHeaders.Names.CONTENT_LENGTH, "" + (highRange-lowRange+1));
				} else if (CLoverride != DLNAMediaInfo.TRANS_SIZE) {
					// since 2.50, it's wiser not to send an arbitrary Content length,
					// as the PS3 displays a network error and asks the last seconds of the transcoded video
					// deprecated since the "-1" size sent anyway
					output.setHeader(HttpHeaders.Names.CONTENT_LENGTH, "" + CLoverride);
				}
			} else {
				int cl = inputStream.available();
				PMS.debug("Available Content-Length: " + cl);
				output.setHeader(HttpHeaders.Names.CONTENT_LENGTH, "" + cl);
			}

			if (timeseek > 0 && dlna != null) {
				String timeseekValue = DLNAMediaInfo.getDurationString(timeseek);
				String timetotalValue = dlna.media.duration;
				output.setHeader("TimeSeekRange.dlna.org", "npt=" + timeseekValue + "-" + timetotalValue + "/" + timetotalValue);
				output.setHeader("X-Seek-Range", "npt=" + timeseekValue + "-" + timetotalValue + "/" + timetotalValue);
			}
			// output(output, "");
			future = e.getChannel().write(output);

			if (lowRange != DLNAMediaInfo.ENDFILE_POS && !method.equals("HEAD")) {
				ChannelFuture chunkWriteFuture = e.getChannel().write(new ChunkedStream(inputStream, BUFFER_SIZE));

				chunkWriteFuture.addListener(new ChannelFutureListener() {
					public void operationComplete(ChannelFuture future) {
						try {
							PMS.get().getRegistry().reenableGoToSleep();
							inputStream.close();
						} catch (IOException e) { }

						// always closed because of freeze at the end of video due to no channel close sent
						future.getChannel().close();
						startStopListenerDelegate.stop();
					}
				});
			} else {
				try {
					PMS.get().getRegistry().reenableGoToSleep();
					inputStream.close();
				} catch (IOException ioe) { }
				if (close) {
					future.addListener(ChannelFutureListener.CLOSE);
				}
				startStopListenerDelegate.stop();
			}
			// PMS.debug( "Sending stream: " + sendB + " bytes of " + argument);
			// PMS.get().getFrame().setStatusLine(null);
		} else {
			if (lowRange > 0 && highRange > 0) {
				output.setHeader(HttpHeaders.Names.CONTENT_LENGTH, "" + (highRange-lowRange+1));
			} else {
				output.setHeader(HttpHeaders.Names.CONTENT_LENGTH, "0");
			}
			// output(output, "");
			future = e.getChannel().write(output);
			if (close) {
				future.addListener(ChannelFutureListener.CLOSE);
			}
		}

		Iterator<String> it = output.getHeaderNames().iterator();

		while (it.hasNext()) {
			String headerName = it.next();
			PMS.debug("Sent to socket: " + headerName + ": " + output.getHeader(headerName));
		}

		return future;
	}

	private String getFUTUREDATE() {
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		return sdf.format(new Date(10000000000L + System.currentTimeMillis()));
	}

	private String getEnclosingValue(String content, String leftTag, String rightTag) {
		String result = null;
		int leftTagPos = content.indexOf(leftTag);
		int rightTagPos =  content.indexOf(rightTag, leftTagPos+1);
		if (leftTagPos > -1 && rightTagPos > leftTagPos) {
			result = content.substring(leftTagPos + leftTag.length(), rightTagPos);
		}
		return result;
	}
}
