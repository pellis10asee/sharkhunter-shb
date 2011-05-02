version=0.23
channel SVTPlay {
	img=http://svtplay.se/img/brand/svt-play.png
	folder {
		name=A-Z
		type=ATZ
		url=http://svtplay.se/alfabetisk
		folder {
			 matcher=<a href=\"(/t/.*)\">(.*)</a>
			 order=url,name
			 url=http://svtplay.se
			 item {
				url=http://svtplay.se
		  		matcher=<a href=\"(/v/.*)\?.*\" title=\"(.*)\" .*?>[^<]*<img class=\"thumbnail\" src=\"([^\"]+)\" [^>]+>[^<]+<span >([^<]+)</span>
			  	order=url,name,thumb,name
		  		prop=auto_media
				media {
		  			matcher=url:(rtmp.*),bitrate:2400
					prop=only_first,
				}
			}
			folder {
					name=>>>
					#<li class="next "><a href="?cb,,1,f,-1/pb,a1364150,1,f,-1/pl,v,,1669414/sb,p108524,2,f,-1" class="internal "><img src="/img/button/pagination-next.gif"
					matcher=<li class=\"next \"><a href=\"([^\"]+)\" class=\"internal \"><img src=\"/img/button/pagination-next.gif\"
					order=url
					type=recurse
					prop=prepend_parenturl,ignore_match,only_first,continue_limit=6
			}
		}
	}
}
