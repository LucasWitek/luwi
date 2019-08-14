package com.minitwit.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResult {
	
	private String error;
	private User user;

}
