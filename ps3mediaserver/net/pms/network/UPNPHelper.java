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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

import net.pms.PMS;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UPNPHelper {
	private static final Logger logger = LoggerFactory.getLogger(UPNPHelper.class);
	private final static String CRLF = "\r\n";
	private final static String ALIVE = "ssdp:alive";
	private final static String UPNP_HOST = "239.255.255.250";
	private final static int UPNP_PORT = 1900;
	private final static String BYEBYE = "ssdp:byebye";
	private static Thread listener;
	private static Thread aliveThread;
	private static SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.US);

	private static void sendDiscover(String host, int port, String st) throws IOException {
		String usn = PMS.get().usn() + "::";
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

		if (st.equals(usn)) {
			usn = "";
		}
		String discovery =
			"HTTP/1.1 200 OK" + CRLF
			+ "CACHE-CONTROL: max-age=1200" + CRLF
			+ "DATE: " + sdf.format(new Date(System.currentTimeMillis())) + " GMT" + CRLF
			+ "LOCATION: http://" + PMS.get().getServer().getHost() + ":" + PMS.get().getServer().getPort() + "/description/fetch" + CRLF
			+ "SERVER: " + PMS.get().getServerName() + CRLF
			+ "ST: " + st + CRLF
			+ "EXT: " + CRLF
			+ "USN: " + usn + st + CRLF
			+ "Content-Length: 0" + CRLF + CRLF;
		sendReply(host, port, discovery);
	}

	private static void sendReply(String host, int port, String msg) throws IOException {
		try {
			DatagramSocket ssdpUniSock = new DatagramSocket();

			logger.trace("Sending this reply [" + host + ":" + port + "]: " + StringUtils.replace(msg, CRLF, "<CRLF>"));
			InetAddress inetAddr = InetAddress.getByName(host);
			DatagramPacket dgmPacket = new DatagramPacket(msg.getBytes(), msg.length(), inetAddr, port);
			ssdpUniSock.send(dgmPacket);
			ssdpUniSock.close();

		} catch (Exception ex) {
			logger.info(ex.getMessage());
		}
	}

	public static void sendAlive() throws IOException {
		logger.debug("Sending ALIVE...");

		MulticastSocket ssdpSocket = getNewMulticastSocket();
		sendMessage(ssdpSocket, "upnp:rootdevice", ALIVE);
		sendMessage(ssdpSocket, PMS.get().usn(), ALIVE);
		sendMessage(ssdpSocket, "urn:schemas-upnp-org:device:MediaServer:1", ALIVE);
		sendMessage(ssdpSocket, "urn:schemas-upnp-org:service:ContentDirectory:1", ALIVE);
		sendMessage(ssdpSocket, "urn:schemas-upnp-org:service:ConnectionManager:1", ALIVE);

		ssdpSocket.close();
		ssdpSocket = null;
	}

	private static MulticastSocket getNewMulticastSocket() throws IOException {
		MulticastSocket ssdpSocket = new MulticastSocket();
		ssdpSocket.setReuseAddress(true);
		if (PMS.getConfiguration().getServerHostname() != null && PMS.getConfiguration().getServerHostname().length() > 0) {
			logger.trace("Searching network interface for " + PMS.getConfiguration().getServerHostname());
			NetworkInterface ni = NetworkInterface.getByInetAddress(InetAddress.getByName(PMS.getConfiguration().getServerHostname()));
			if (ni != null) {
				ssdpSocket.setNetworkInterface(ni);

				// force IPv4 address
				Enumeration<InetAddress> enm = ni.getInetAddresses();
				while (enm.hasMoreElements()) {
					InetAddress ia = enm.nextElement();
					if (!(ia instanceof Inet6Address)) {
						ssdpSocket.setInterface(ia);
						break;
					}
				}

			}
		} else if (PMS.get().getServer().getNi() != null) {
			logger.trace("Setting multicast network interface: " + PMS.get().getServer().getNi());
			ssdpSocket.setNetworkInterface(PMS.get().getServer().getNi());
		}
		logger.trace("Sending message from multicast socket on network interface: " + ssdpSocket.getNetworkInterface());
		logger.trace("Multicast socket is on interface: " + ssdpSocket.getInterface());
		ssdpSocket.setTimeToLive(32);
		ssdpSocket.joinGroup(getUPNPAddress());
		logger.trace("Socket Timeout: " + ssdpSocket.getSoTimeout());
		logger.trace("Socket TTL: " + ssdpSocket.getTimeToLive());
		return ssdpSocket;
	}

	public static void sendByeBye() throws IOException {

		logger.info("Sending BYEBYE...");
		MulticastSocket ssdpSocket = getNewMulticastSocket();

		sendMessage(ssdpSocket, "upnp:rootdevice", BYEBYE);
		sendMessage(ssdpSocket, "urn:schemas-upnp-org:device:MediaServer:1", BYEBYE);
		sendMessage(ssdpSocket, "urn:schemas-upnp-org:service:ContentDirectory:1", BYEBYE);
		sendMessage(ssdpSocket, "urn:schemas-upnp-org:service:ConnectionManager:1", BYEBYE);

		ssdpSocket.leaveGroup(getUPNPAddress());
		ssdpSocket.close();
		ssdpSocket = null;

	}

	private static void sendMessage(DatagramSocket socket, String nt, String message) throws IOException {
		String msg = buildMsg(nt, message);
		Random rand = new Random();
		//logger.trace( "Sending this SSDP packet: " + CRLF + msg);// StringUtils.replace(msg, CRLF, "<CRLF>"));
		DatagramPacket ssdpPacket = new DatagramPacket(msg.getBytes(), msg.length(), getUPNPAddress(), UPNP_PORT);
		socket.send(ssdpPacket);
		try {
			Thread.sleep(rand.nextInt(1800 / 2));
		} catch (InterruptedException e) {
		}
		socket.send(ssdpPacket);
		try {
			Thread.sleep(rand.nextInt(1800 / 2));
		} catch (InterruptedException e) {
		}

	}
	private static int delay = 10000;

	public static void listen() throws IOException {
		Runnable rAlive = new Runnable() {
			public void run() {
				while (true) {
					try {
						Thread.sleep(delay);
						sendAlive();
						if (delay == 20000) // every 180s
						{
							delay = 180000;
						}
						if (delay == 10000) // after 10, and 30s
						{
							delay = 20000;
						}
					} catch (Exception e) {
						logger.debug("Error while sending periodic alive message: " + e.getMessage());
					}
				}
			}
		};
		aliveThread = new Thread(rAlive);
		aliveThread.start();

		Runnable r = new Runnable() {
			public void run() {
				while (true) {
					try {
						MulticastSocket socket = new MulticastSocket(1900);
						if (PMS.getConfiguration().getServerHostname() != null && PMS.getConfiguration().getServerHostname().length() > 0) {
							logger.trace("Searching network interface for " + PMS.getConfiguration().getServerHostname());
							NetworkInterface ni = NetworkInterface.getByInetAddress(InetAddress.getByName(PMS.getConfiguration().getServerHostname()));
							if (ni != null) {
								socket.setNetworkInterface(ni);
							}
						} else if (PMS.get().getServer().getNi() != null) {
							logger.trace("Setting multicast network interface: " + PMS.get().getServer().getNi());
							socket.setNetworkInterface(PMS.get().getServer().getNi());
						}
						socket.setTimeToLive(4);
						socket.setReuseAddress(true);
						socket.joinGroup(getUPNPAddress());
						while (true) {
							byte[] buf = new byte[1024];
							DatagramPacket packet_r = new DatagramPacket(buf, buf.length);
							socket.receive(packet_r);

							String s = new String(packet_r.getData());
							/* Does it come from me ? */
							//String lines[] = s.split(CRLF);

							if (s.startsWith("M-SEARCH")) {
								String remoteAddr = packet_r.getAddress().getHostAddress();
								int remotePort = packet_r.getPort();

								if (!(PMS.getConfiguration().getIpFilter().length() > 0 && !PMS.getConfiguration().getIpFilter().equals(remoteAddr))) {
									logger.trace("Receiving a M-SEARCH from [" + remoteAddr + ":" + remotePort + "]");
									//logger.trace("Data: " + s);

									/*logger.info( "Receiving search request from " + packet_r.getAddress().getHostAddress() + "! Sending DISCOVER message...");*/
									if (StringUtils.indexOf(s, "urn:schemas-upnp-org:service:ContentDirectory:1") > 0) {
										sendDiscover(remoteAddr, remotePort, "urn:schemas-upnp-org:service:ContentDirectory:1");
									}

									if (StringUtils.indexOf(s, "upnp:rootdevice") > 0) {
										sendDiscover(remoteAddr, remotePort, "upnp:rootdevice");
									}

									if (StringUtils.indexOf(s, "urn:schemas-upnp-org:device:MediaServer:1") > 0) {
										sendDiscover(remoteAddr, remotePort, "urn:schemas-upnp-org:device:MediaServer:1");
									}

									if (StringUtils.indexOf(s, PMS.get().usn()) > 0) {
										sendDiscover(remoteAddr, remotePort, PMS.get().usn());
									}
								}
							} else if (s.startsWith("NOTIFY")) {
								String remoteAddr = packet_r.getAddress().getHostAddress();
								int remotePort = packet_r.getPort();

								logger.trace("Receiving a NOTIFY from [" + remoteAddr + ":" + remotePort + "]");
								//logger.trace("Data: " + s);
							}
						}
					} catch (IOException e) {
						logger.error("UPNP network exception", e);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e1) {
						}
					}
				}
			}
		};
		listener = new Thread(r);
		listener.start();
	}

	public static void shutDownListener() {
		listener.interrupt();
		aliveThread.interrupt();
	}

	private static String buildMsg(String nt, String message) {
		StringBuilder sb = new StringBuilder();

		sb.append("NOTIFY * HTTP/1.1" + CRLF);
		sb.append("HOST: " + UPNP_HOST + ":").append(UPNP_PORT).append(CRLF);
		sb.append("NT: ").append(nt).append(CRLF);
		sb.append("NTS: ").append(message).append(CRLF);

		if (message.equals(ALIVE)) {
			sb.append("LOCATION: http://").append(PMS.get().getServer().getHost()).append(":").append(PMS.get().getServer().getPort()).append("/description/fetch" + CRLF);
		}
		sb.append("USN: ").append(PMS.get().usn());
		if (!nt.equals(PMS.get().usn())) {
			sb.append("::").append(nt);
		}
		sb.append(CRLF);

		if (message.equals(ALIVE)) {
			sb.append("CACHE-CONTROL: max-age=1800" + CRLF);
		}

		if (message.equals(ALIVE)) {
			sb.append("SERVER: ").append(PMS.get().getServerName()).append(CRLF);
		}

		sb.append(CRLF);
		return sb.toString();
	}

	private static InetAddress getUPNPAddress() throws IOException {
		return InetAddress.getByAddress(UPNP_HOST, new byte[]{(byte) 239, (byte) 255, (byte) 255, (byte) 250});
	}
}
