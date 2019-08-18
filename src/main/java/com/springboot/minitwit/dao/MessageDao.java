package com.springboot.minitwit.dao;

import com.springboot.minitwit.model.Message;
import com.springboot.minitwit.model.User;

import java.util.List;


public interface MessageDao {
	List<Message> getUserTimelineMessages(User user);
	
	List<Message> getUserFullTimelineMessages(User user);
	
	List<Message> getPublicTimelineMessages();
	
	void insertMessage(Message m);
}
