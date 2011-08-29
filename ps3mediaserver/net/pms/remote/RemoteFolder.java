package net.pms.remote;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.pms.dlna.virtual.VirtualFolder;
import net.pms.xmlwise.Plist;
import net.pms.xmlwise.XmlElement;
import net.pms.xmlwise.Xmlwise;

public class RemoteFolder extends VirtualFolder {
	
	private final static String CRLF = "\r\n";
	
	private String id;
	private RemoteServer server;
	
	public RemoteFolder(RemoteServer srv,String id,String name,String thumb) {
		super(name,thumb);
		this.id=id;
		this.server=srv;
	}
	
	private String buildBrowse() {
		StringBuilder sb=new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		sb.append(CRLF);
		sb.append("<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"");
		sb.append("s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">");
		sb.append(CRLF);
		sb.append("<s:Body>");
		sb.append(CRLF);
		sb.append("<u:Browse xmlns:u=\"urn:schemas-upnp-org:service:ContentDirectory:1\">");
		sb.append(CRLF);
		sb.append("<ObjectID>");
		sb.append(id);
		sb.append("</ObjectID>");
		sb.append(CRLF);
		sb.append("<BrowseFlag>BrowseDirectChildren</BrowseFlag>");
		sb.append(CRLF);
		sb.append("<Filter></Filter>");
		sb.append(CRLF);
		sb.append("<StartingIndex>0</StartingIndex>");
		sb.append(CRLF);
		sb.append("<RequestedCount>0</RequestedCount>");
		sb.append(CRLF);
		sb.append("<SortCriteria></SortCriteria>");
		sb.append(CRLF);
		sb.append("</u:Browse>");
		sb.append(CRLF);
		sb.append("</s:Body>");
		sb.append(CRLF);
		sb.append("</s:Envelope>");
		return sb.toString();
	}
	
	private String getVal(XmlElement node,String nodeName) {
		try {
			XmlElement e=node.getUnique(nodeName);
			return e.getValue();
		}
		catch (Exception e) {
			return "";
		}
	}
	
	public void discoverChildren() {
		String browse=buildBrowse();
		try {
			String raw=server.dlnaAction(browse,
					"\"urn:schemas-upnp-org:service:ContentDirectory:1#Browse\"");
			XmlElement top=Xmlwise.createXml(raw);
			List<XmlElement> l=top.get("container");
			for(XmlElement e:l) {
				String id=e.getAttribute("id");
				String name=getVal(e,"dc:title");
				String thumb=getVal(e,"res");
				RemoteFolder f=new RemoteFolder(server,id,name,thumb);
				addChild(f);
			}
			List<XmlElement> l1=top.get("item");
			for(XmlElement e:l1) {
				String name=getVal(e,"dc:title");
				XmlElement res=e.getUnique("res");
				String streamUrl=res.getValue();
				String albumArt=getVal(e,"upnp:albumArtURI");
				String artist=getVal(e,"upnp:artist");
				String album=getVal(e,"upnp:album");
				/*RemoteItem item=new RemoteItem(streamUrl,name,albumArt);
				addChild(item);*/
			}
		}
		catch (Exception e) {
		}
	}

}
