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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.StringTokenizer;

import net.pms.PMS;
import net.pms.configuration.RendererConfiguration;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAResource;
import net.pms.external.StartStopListenerDelegate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);
	public final static int SOCKET_BUF_SIZE = 32768;
	private Socket socket;
	private OutputStream output;
	private BufferedReader br;

	public RequestHandler(Socket socket) throws IOException {
		this.socket = socket;
		this.output = socket.getOutputStream();
		this.br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	}

	
	public void run() {
		Request request = null;
		StartStopListenerDelegate startStopListenerDelegate = new StartStopListenerDelegate(
			socket.getInetAddress().getHostAddress());

		try {
			logger.trace("Opened handler on socket " + socket);
			PMS.get().getRegistry().disableGoToSleep();
			int receivedContentLength = -1;
			boolean useragentfound = false;
			String userAgentString = null;
			DataInputStream in=new DataInputStream(socket.getInputStream());
			String headerLine = br.readLine();
			
			while (headerLine != null && headerLine.length() > 0) {
				logger.trace("Received on socket: " + headerLine);
				if (!useragentfound && headerLine != null && headerLine.toUpperCase().startsWith("USER-AGENT") && request != null) {
					userAgentString = headerLine.substring(headerLine.indexOf(":") + 1).trim();
					RendererConfiguration renderer = RendererConfiguration.getRendererConfigurationByUA(userAgentString);
					if (renderer != null) {
						PMS.get().setRendererfound(renderer);
						request.setMediaRenderer(renderer);
						useragentfound = true;
					}
				}
				if (!useragentfound && headerLine != null && request != null) {
					RendererConfiguration renderer = RendererConfiguration.getRendererConfigurationByUAAHH(headerLine);
					if (renderer != null) {
						PMS.get().setRendererfound(renderer);
						request.setMediaRenderer(renderer);
						useragentfound = true;
					}
				}
				try {
					StringTokenizer s = new StringTokenizer(headerLine);
					String temp = s.nextToken();
					if (temp.equals("GET") || temp.equals("POST") || temp.equals("HEAD")) {
						request = new Request(temp, s.nextToken().substring(1));
						if (s.hasMoreTokens() && s.nextToken().equals("HTTP/1.0")) {
							request.setHttp10(true);
						}
					} else if (request != null && temp.toUpperCase().equals("SOAPACTION:")) {
						request.setSoapaction(s.nextToken());
					} else if (headerLine.toUpperCase().contains("CONTENT-LENGTH:")) {
						receivedContentLength = Integer.parseInt(headerLine.substring(headerLine.toUpperCase().indexOf("CONTENT-LENGTH: ") + 16));
					} else if (headerLine.toUpperCase().indexOf("RANGE: BYTES=") > -1) {
						String nums = headerLine.substring(headerLine.toUpperCase().indexOf("RANGE: BYTES=") + 13).trim();
						StringTokenizer st = new StringTokenizer(nums, "-");
						if (!nums.startsWith("-")) {
							request.setLowRange(Long.parseLong(st.nextToken()));
						}
						if (!nums.startsWith("-") && !nums.endsWith("-")) {
							request.setHighRange(Long.parseLong(st.nextToken()));
						} else {
							request.setHighRange(DLNAMediaInfo.TRANS_SIZE);
						}
					} else if (headerLine.toLowerCase().indexOf("transfermode.dlna.org:") > -1) {
						request.setTransferMode(headerLine.substring(headerLine.toLowerCase().indexOf("transfermode.dlna.org:") + 22).trim());
					} else if (headerLine.toLowerCase().indexOf("getcontentfeatures.dlna.org:") > -1) {
						request.setContentFeatures(headerLine.substring(headerLine.toLowerCase().indexOf("getcontentfeatures.dlna.org:") + 28).trim());
					} else if (headerLine.toUpperCase().indexOf("TIMESEEKRANGE.DLNA.ORG: NPT=") > -1) { // firmware 2.50+
						String timeseek = headerLine.substring(headerLine.toUpperCase().indexOf("TIMESEEKRANGE.DLNA.ORG: NPT=") + 28);
						if (timeseek.endsWith("-")) {
							timeseek = timeseek.substring(0, timeseek.length() - 1);
						} else if (timeseek.indexOf("-") > -1) {
							timeseek = timeseek.substring(0, timeseek.indexOf("-"));
						}
						request.setTimeseek(Double.parseDouble(timeseek));
					} else if (headerLine.toUpperCase().indexOf("TIMESEEKRANGE.DLNA.ORG : NPT=") > -1) { // firmware 2.40
						String timeseek = headerLine.substring(headerLine.toUpperCase().indexOf("TIMESEEKRANGE.DLNA.ORG : NPT=") + 29);
						if (timeseek.endsWith("-")) {
							timeseek = timeseek.substring(0, timeseek.length() - 1);
						} else if (timeseek.indexOf("-") > -1) {
							timeseek = timeseek.substring(0, timeseek.indexOf("-"));
						}
						request.setTimeseek(Double.parseDouble(timeseek));
					}
				} catch (Exception e) {
					logger.error("Error in parsing HTTP headers", e);
				}
				headerLine = br.readLine();
				
			}

			// if client not recognized, take a default renderer config
			if (request != null) {
				if (request.getMediaRenderer() == null) {
					request.setMediaRenderer(RendererConfiguration.getDefaultConf());
					logger.trace("Using default media renderer " + request.getMediaRenderer().getRendererName()); //$NON-NLS-1$
					
					if (userAgentString != null && !userAgentString.equals("FDSSDP")) { //$NON-NLS-1$
						// we have found an unknown renderer
						logger.info("Media renderer was not recognized. HTTP User-Agent: " + userAgentString); //$NON-NLS-1$
						PMS.get().setRendererfound(request.getMediaRenderer());
					}
				} else {
					if (userAgentString != null) {
						logger.trace("HTTP User-Agent: " + userAgentString); //$NON-NLS-1$
					}
					logger.trace("Recognized media renderer " + request.getMediaRenderer().getRendererName()); //$NON-NLS-1$
				}
			}
			
			if(request!=null) {
				String method=request.getMethod();
				String arg=request.getArgument();
				if(arg!=null&&method!=null) {
					if(method.equals("POST")&&arg.startsWith("0$")) {
						String id = arg.substring(arg.indexOf("0$"), arg.lastIndexOf("/"));
						id = id.replace("%24", "$"); // popcorn hour ?
						String name=arg.substring(arg.lastIndexOf("/")+1);
						DLNAResource res=PMS.get().getRootFolder(request.getMediaRenderer()).search(id);
						if(res!=null) {
							OutputStream out=res.upload(name);
							if(out!=null) {
								request.answer(output, startStopListenerDelegate);
								DataInputStream in1=new DataInputStream(socket.getInputStream());
								byte[] buf=new byte[4096];
								int len;
								while((len=in1.read(buf))!=-1) {
									out.write(buf, 0, len);
								}
								out.flush();
								out.close();
							}
							else { // some error occured here
								output.write("HTTP/1.1 500 Interal Server Error\r\n".getBytes("UTF-8"));
								output.flush();
							}
						}
						output.close();
						return;
					}
				}
			}

			if (receivedContentLength > 0) {
				char buf[] = new char[receivedContentLength];
				br.read(buf);
				if (request != null) {
					request.setTextContent(new String(buf));
				}
			}
			
			
			if (request != null) {
				logger.trace("HTTP: " + request.getArgument() + " / " + request.getLowRange() + "-" + request.getHighRange());
			}

			if (request != null) {
				request.answer(output, startStopListenerDelegate);
			}

			if (request != null && request.getInputStream() != null) {
				request.getInputStream().close();
			}

		} catch (IOException e) {
			logger.trace("Unexpected IO error: " + e.getClass() + ": " + e.getMessage());
			if (request != null && request.getInputStream() != null) {
				try {
					logger.trace("Closing input stream: " + request.getInputStream());
					request.getInputStream().close();
				} catch (IOException e1) {
					logger.error("Error closing input stream", e);
				}
			}
		} finally {
			try {
				PMS.get().getRegistry().reenableGoToSleep();
				output.close();
				//br.close();
				socket.close();
			} catch (IOException e) {
				logger.error("Error closing connection: ", e);
			}

			startStopListenerDelegate.stop();
			logger.trace("Close connection");
		}
	}
}
