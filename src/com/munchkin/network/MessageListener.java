package com.munchkin.network;

import com.common.Message;

public interface MessageListener {

	public void onMessageReceived(Message message);
}
