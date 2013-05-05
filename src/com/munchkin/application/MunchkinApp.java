package com.munchkin.application;

import android.app.Application;

public class MunchkinApp extends Application {
	private String userName;
	private String profileId;
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getProfileId() {
		return profileId;
	}
	public void setProfileId(String profileId) {
		this.profileId = profileId;
	}
	


}
