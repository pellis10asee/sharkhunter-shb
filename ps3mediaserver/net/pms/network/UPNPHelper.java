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
import java.util.Enumeration;

import java.util.Random;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;

import net.pms.PMS;



public class UPNPHelper {
	
	private final static String CRLF = "\r\n";
	private final static String ALIVE = "ssdp:alive";
	private final static String UPNP_HOST = "239.255.255.250";
	private final static int UPNP_PORT = 1900;
	private final static String BYEBYE = "ssdp:byebye";
	private static Thread listener;
	private static Thread aliveThread;
	private static SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.US);
	
	private static void sendDiscover(String host, int port, String st) throws IOException
	{
		String usn = PMS.get().usn() + "::";
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

		if (st.equals(usn))
			usn = "";
		String discovery = 
			"HTTP/1.1 200 OK" + CRLF +
			"CACHE-CONTROL: max-age=1200" + CRLF +
			"DATE: " + sdf.format(new Date(System.currentTimeMillis())) + " GMT" + CRLF + 
			"LOCATION: http://" + PMS.get().getServer().getHost() + ":" + PMS.get().getServer().getPort() + "/description/fetch" + CRLF +
			"SERVER: " + PMS.get().getServerName() + CRLF +
			"ST: " + st + CRLF +
			"EXT: " + CRLF +
			"USN: " + usn + st + CRLF +
			"Content-Length: 0" + CRLF + CRLF;		
		sendReply(host, port, discovery);
	}

	private static void sendReply(String host, int port, String msg) throws IOException
	{
		try
		{
			DatagramSocket ssdpUniSock = new DatagramSocket();

			PMS.debug( "Sending this reply [" + host + ":" + port + "]: " + StringUtils.replace(msg, CRLF, "<CRLF>"));
			InetAddress inetAddr = InetAddress.getByName(host);
			DatagramPacket dgmPacket = new DatagramPacket(msg.getBytes(), msg.length(), inetAddr, port);
			ssdpUniSock.send(dgmPacket);
			ssdpUniSock.close();

		}
		catch (Exception ex)
		{
			PMS.minimal(ex.getMessage());
		}
	}
	
	public static void sendAlive() throws IOException {
		
		PMS.info( "Sending ALIVE...");
		
		MulticastSocket ssdpSocket = getNewMulticastSocket();
		sendMessage(ssdpSocket,  "upnp:rootdevice", ALIVE);
		sendMessage(ssdpSocket,  PMS.get().usn(), ALIVE);
		sendMessage(ssdpSocket,  "urn:schemas-upnp-org:device:MediaServer:1", ALIVE);
		sendMessage(ssdpSocket,  "urn:schemas-upnp-org:service:ContentDirectory:1", ALIVE);
		sendMessage(ssdpSocket,  "urn:schemas-upnp-org:service:ConnectionManager:1", ALIVE);
		//sendMessage(ssdpSocket,  "urn:microsoft.com:service:X_MS_MediaReceiverRegistrar:1", ALIVE);
		
		//ssdpSocket.leaveGroup(getUPNPAddress());
		ssdpSocket.close();
		ssdpSocket = null;
	}
	
	private static MulticastSocket getNewMulticastSocket() throws IOException {
		MulticastSocket ssdpSocket = new MulticastSocket();
		ssdpSocket.setReuseAddress(true);
		if (PMS.getConfiguration().getServerHostname() != null && PMS.getConfiguration().getServerHostname().length() > 0) {
			PMS.debug("Searching network interface for " + PMS.getConfiguration().getServerHostname());
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
		} else if ( PMS.get().getServer().getNi() != null) {
			PMS.debug("Setting multicast network interface: " +  PMS.get().getServer().getNi());
			ssdpSocket.setNetworkInterface( PMS.get().getServer().getNi());
		}
		PMS.debug("Sending message from multicast socket on network interface: " + ssdpSocket.getNetworkInterface());
		PMS.debug("Multicast socket is on interface: " + ssdpSocket.getInterface());
		ssdpSocket.setTimeToLive(32);
		//ssdpSocket.setLoopbackMode(true);
		ssdpSocket.joinGroup(getUPNPAddress());
		PMS.debug("Socket Timeout: " + ssdpSocket.getSoTimeout());
		PMS.debug("Socket TTL: " + ssdpSocket.getTimeToLive());
		return ssdpSocket;
	}
	
	public static void sendByeBye() throws IOException {
		
		PMS.minimal( "Sending BYEBYE...");
		MulticastSocket ssdpSocket = getNewMulticastSocket();
		
		sendMessage(ssdpSocket,  "upnp:rootdevice", BYEBYE);
		//sendMessage(ssdpSocket,  PMS.get().usn(), BYEBYE);
		sendMessage(ssdpSocket,  "urn:schemas-upnp-org:device:MediaServer:1", BYEBYE);
		sendMessage(ssdpSocket,  "urn:schemas-upnp-org:service:ContentDirectory:1", BYEBYE);
		sendMessage(ssdpSocket,  "urn:schemas-upnp-org:service:ConnectionManager:1", BYEBYE);
		//sendMessage(ssdpSocket,  "urn:microsoft.com:service:X_MS_MediaReceiverRegistrar:1", BYEBYE);

		ssdpSocket.leaveGroup(getUPNPAddress());
		ssdpSocket.close();
		ssdpSocket = null;

	}

	
	private static void sendMessage(DatagramSocket socket, String nt, String message) throws IOException {
		String msg = buildMsg(nt, message);
		Random rand = new Random();
		//PMS.debug( "Sending this SSDP packet: " + CRLF + msg);// StringUtils.replace(msg, CRLF, "<CRLF>"));
		DatagramPacket ssdpPacket = new DatagramPacket(msg.getBytes(), msg.length(), getUPNPAddress(), UPNP_PORT);
		socket.send(ssdpPacket);
		try {
			Thread.sleep(rand.nextInt(1800/2));
		} catch (InterruptedException e) { }
		socket.send(ssdpPacket);
		try {
			Thread.sleep(rand.nextInt(1800/2));
		} catch (InterruptedException e) { }
		
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
							delay = 180000;
						if (delay == 10000) // after 10, and 30s
							delay = 20000;
					} catch (Exception e) {
						PMS.info("Error while sending periodic alive message: " + e.getMessage());
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
							PMS.debug("Searching network interface for " + PMS.getConfiguration().getServerHostname());
							NetworkInterface ni = NetworkInterface.getByInetAddress(InetAddress.getByName(PMS.getConfiguration().getServerHostname()));
							if (ni != null)
								socket.setNetworkInterface(ni);
						} else if ( PMS.get().getServer().getNi() != null) {
							PMS.debug("Setting multicast network interface: " +  PMS.get().getServer().getNi());
							socket.setNetworkInterface( PMS.get().getServer().getNi());
						}
						socket.setTimeToLive(4);
						socket.setReuseAddress(true);
						socket.joinGroup(getUPNPAddress());
						while(true)
						{
							byte[] buf = new byte[1024];					
							DatagramPacket packet_r = new DatagramPacket(buf, buf.length);
							socket.receive(packet_r);

						String s = new String(packet_r.getData());
						/* Does it come from me ? */
						//String lines[] = s.split(CRLF);
						
						if (s.startsWith("M-SEARCH")) {
							String remoteAddr = packet_r.getAddress().getHostAddress();
							int remotePort = packet_r.getPort();
							
							if(!(PMS.getConfiguration().getIpFilter().length() > 0 && !PMS.getConfiguration().getIpFilter().equals(remoteAddr))){
								PMS.debug("Receiving a M-SEARCH from [" + remoteAddr + ":" + remotePort + "]");
								//PMS.debug("Data: " + s);
								
								/*PMS.minimal( "Receiving search request from " + packet_r.getAddress().getHostAddress() + "! Sending DISCOVER message...");*/
								if (StringUtils.indexOf(s, "urn:schemas-upnp-org:service:ContentDirectory:1") > 0)
									sendDiscover(remoteAddr, remotePort, "urn:schemas-upnp-org:service:ContentDirectory:1");
			
								if (StringUtils.indexOf(s, "upnp:rootdevice") > 0)
									sendDiscover(remoteAddr, remotePort, "upnp:rootdevice");
			
								if (StringUtils.indexOf(s, "urn:schemas-upnp-org:device:MediaServer:1") > 0)
									sendDiscover(remoteAddr, remotePort, "urn:schemas-upnp-org:device:MediaServer:1");
			
								if (StringUtils.indexOf(s, PMS.get().usn()) > 0)
									sendDiscover(remoteAddr, remotePort, PMS.get().usn());
							}
						
						}
						else if (s.startsWith("NOTIFY")) {
							String remoteAddr = packet_r.getAddress().getHostAddress();
							int remotePort = packet_r.getPort();
							
							PMS.debug("Receiving a NOTIFY from [" + remoteAddr + ":" + remotePort + "]");
							//PMS.debug("Data: " + s);
						}
						
						}
					} catch (IOException e) {
						PMS.error("UPNP network exception", e);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e1) {}
					}
				}
		
			}};
		listener = new Thread(r);
		listener.start();
	}
	
	public static void shutDownListener() {
		listener.interrupt();
		aliveThread.interrupt();
	}
	
	private static String buildMsg(String nt, String message) {
		StringBuffer sb = new StringBuffer();
		
		sb.append("NOTIFY * HTTP/1.1" + CRLF);
		sb.append("HOST: " + UPNP_HOST + ":" + UPNP_PORT + CRLF);
		sb.append("NT: " + nt + CRLF);
		sb.append("NTS: " + message + CRLF);
		
		if (message.equals(ALIVE)) {
			sb.append("LOCATION: http://" + PMS.get().getServer().getHost() + ":" + PMS.get().getServer().getPort() + "/description/fetch" + CRLF);
		}
		sb.append("USN: " + PMS.get().usn());
		if (!nt.equals(PMS.get().usn()))
			sb.append("::" + nt);
		sb.append(CRLF);
		
		if (message.equals(ALIVE)) {
			sb.append("CACHE-CONTROL: max-age=1800" + CRLF);
		}
		
		if (message.equals(ALIVE)) {
			sb.append("SERVER: " + PMS.get().getServerName() + CRLF);
		}
		
		sb.append(CRLF);
		return sb.toString();
		
/*		sb.append("NOTIFY * HTTP/1.1");
		sb.append(CRLF);
		sb.append("HOST: ");
		sb.append(UPNP_HOST);
		sb.append(":");
		sb.append(UPNP_PORT);
		sb.append(CRLF);
		if (!message.equals(BYEBYE)) {
			sb.append("CACHE-CONTROL: max-age=1800");
			sb.append(CRLF);
			sb.append("LOCATION: http://");
			sb.append(PMS.get().getServer().getHost());
			sb.append(":");
			sb.append(PMS.get().getServer().getPort());
			sb.append("/description/fetch");
			sb.append(CRLF);
		}
		sb.append("NT: ");
		sb.append(nt);
		sb.append(CRLF);
		sb.append("NTS: ");
		sb.append(message);
		sb.append(CRLF);
		if (!message.equals(BYEBYE)) {
			sb.append("Server: ");
			sb.append(PMS.get().getServerName());
			sb.append(CRLF);
		}
		sb.append("USN: ");
		sb.append(PMS.get().usn());
		if (!nt.equals(PMS.get().usn()))
			sb.append(nt);
		sb.append(CRLF);
		sb.append(CRLF);
		return sb.toString();*/
	}
	
	private static InetAddress getUPNPAddress() throws IOException {
		return InetAddress.getByAddress(UPNP_HOST, new byte[]{(byte) 239, (byte)255, (byte)255, (byte)250});
	}

}
