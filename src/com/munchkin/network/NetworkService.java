package com.munchkin.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import com.common.Message;
import com.munchkin.network.ui_posts.MessageSenderRunnable;

public class NetworkService implements Runnable {
	String ipAddress = "192.168.0.29";

	final int PORT = 8888;
	Socket socket;

	public static ObjectInputStream in;
	public static ObjectOutputStream out;

	public static ArrayList<MessageListener> listenerList;

	Message message;

	public boolean isActive = true; // This remains true to keep the thread
									// running
									// Once the app closes, then this should be
									// changed
									// to false to stop the thread.
	private String userName;
	private String userId;

	public NetworkService() {
		userName = "Phone_Name";
		listenerList = new ArrayList<MessageListener>();
	}

	public NetworkService(String userName, String userId) {
		this.userName = userName;
		this.userId = userId;
		listenerList = new ArrayList<MessageListener>();
	}

	/**
	 * Adds a MessageListener to an ArrayList. The listeners will be called
	 * every time a message is received from the server.
	 * 
	 * @param a
	 */
	public static void setOnMessageReceivedListener(MessageListener a) {
		listenerList.add(a);
	}

	@Override
	public void run() {
		try {
			System.out.println("about to connect");
			socket = new Socket(ipAddress, PORT);
			System.out.println("Connected");

			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());

			String[] values = new String[2];
			values[0] = userName;
			values[1] = userId;
			out.writeObject((new Message("name", values)));
			out.flush();

			while (isActive) {
				message = (Message) in.readObject();

				for (int i = 0; i < listenerList.size(); i++) {
					if (listenerList.size() > 1) {
						if (i == 1) {
							System.out.println("Network Recieving message");
							listenerList.get(1).onMessageReceived(message);
						}
					} else {
						listenerList.get(i).onMessageReceived(message);
					}
				}
			}

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			isActive = false;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Only the NetworkService thread should call this method to send messages.
	 * 
	 * @param msg
	 *            Message to be sent to the server.
	 */
	public synchronized static void sendMessage(Message msg) {
		try {
			System.out.println("SENDING MESSAGE: " + msg);
			out.writeObject(msg);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Only UI Thread should be calling this method to send messages.
	 * 
	 * @param msg
	 *            Message to be sent to the server.
	 */
	public static void postMessage(Message msg) {
		Thread t = new Thread(new MessageSenderRunnable(msg));
		t.start();
	}

}
