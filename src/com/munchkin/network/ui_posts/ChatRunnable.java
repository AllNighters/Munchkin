package com.munchkin.network.ui_posts;

import com.common.Message;

public abstract class ChatRunnable implements Runnable{
	
	protected Message message;
	
	public ChatRunnable(Message msg){
		message = msg;
	}

	@Override
	public abstract void run();
	
}
