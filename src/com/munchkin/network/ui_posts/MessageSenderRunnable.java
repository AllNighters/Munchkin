package com.munchkin.network.ui_posts;

import com.common.Message;
import com.munchkin.network.NetworkService;

public class MessageSenderRunnable implements Runnable {

	private Message message;

	public MessageSenderRunnable(Message msg) {
		message = msg;
	}

	@Override
	public void run() {
		NetworkService.sendMessage(message);
	}

}
