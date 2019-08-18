package com.springboot.minitwit.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.text.SimpleDateFormat;
import java.util.Date;

@Getter
@Setter
public class Message {

	private int id;
	private int userId;
	private String username;
	private String text;
	private Date pubDate;

	@Setter(AccessLevel.NONE)
	private String pubDateStr;

	private String gravatar;

	public void setPubDate(Date pubDate) {
		this.pubDate = pubDate;
		if(pubDate != null) {
			pubDateStr = new SimpleDateFormat("yyyy-MM-dd @ HH:mm").format(pubDate);
		}
	}

}
