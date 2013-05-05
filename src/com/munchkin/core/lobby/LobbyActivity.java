package com.munchkin.core.lobby;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.common.Message;
import com.facebook.widget.ProfilePictureView;
import com.munchkin.application.MunchkinApp;
import com.munchkin.core.game.BoardActivity;
import com.munchkin.network.MessageListener;
import com.munchkin.network.NetworkService;
import com.munchkin.network.ui_posts.ChatRunnable;
import com.munchkin.network.ui_posts.MessageSenderRunnable;
import com.munchkin.network.ui_posts.PostRunnable;
import com.munchkin.util.MunchkinUtil;

public class LobbyActivity extends Activity implements OnClickListener,
		MessageListener {

	private Button leave;
	private Button play;
	private Button send;
	private TextView plrsRdy;
	private TextView chat;
	public TextView[] plrNames = new TextView[9];
	public CheckBox[] cbplrRdy = new CheckBox[9];
	private ProfilePictureView[] profilePictureView = new ProfilePictureView[9];
	public boolean isRDY;
	private EditText toSend;
	public LinearLayout[] playerView = new LinearLayout[9];
	public ArrayList<String> PlayerNames;
	private String userName;
	private String userId;

	Thread clientListener;
	NetworkService networkService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// keep phone going
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(com.munchkin.R.layout.ui_lobby);
		// Set all buttons properly.
		setInstances();
		// Retrieve sent bundle
		Bundle bundle = this.getIntent().getExtras();
		userName = bundle.getString("userName");
		userId = bundle.getString("userId");

		clientListener = new Thread(networkService = new NetworkService(
				userName, userId));
		NetworkService.setOnMessageReceivedListener(this);
		clientListener.start();

	}

	// defalt everything
	private void setInstances() {
		// Initialize!!
		isRDY = false;
		leave = (Button) findViewById(com.munchkin.R.id.bleave);
		play = (Button) findViewById(com.munchkin.R.id.bplay);
		send = (Button) findViewById(com.munchkin.R.id.bSend);
		plrsRdy = (TextView) findViewById(com.munchkin.R.id.tvNumRDY);
		chat = (TextView) findViewById(com.munchkin.R.id.tvChatText);
		toSend = (EditText) findViewById(com.munchkin.R.id.etSend);
		chat.setText("");
		for (int i = 0; i < 9; i++) {

			String TextViewID = "tvplayername" + i;
			String CheckBoxID = "cbplayer" + i;
			String playerID = "playerinfo" + i;
			String profpicID = "profPic" + i;
			plrNames[i] = (TextView) MunchkinUtil.findViewByString(this,
					TextViewID);
			cbplrRdy[i] = (CheckBox) MunchkinUtil.findViewByString(this,
					CheckBoxID);
			playerView[i] = (LinearLayout) MunchkinUtil.findViewByString(this,
					playerID);
			profilePictureView[i] = (ProfilePictureView) MunchkinUtil
					.findViewByString(this, profpicID);
		}
		// set onclicks
		leave.setOnClickListener(this);
		play.setOnClickListener(this);
		send.setOnClickListener(this);
	}

	// send the names to this when ever you need to update the list
	// may need to make sinronized(sp?)
	public void updateNamestoView() {
		for (int i = 0; i < PlayerNames.size(); i++)
			plrNames[i].setText(PlayerNames.get(i));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onStop() {
		super.onStop();
		// networkService.isActive = false;
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case com.munchkin.R.id.bplay:
			play();
			break;
		case com.munchkin.R.id.bleave:
			leave();
			break;
		case com.munchkin.R.id.bSend:
			send();
		}
	}

	// Called when player clicks button to send a chat message
	private void send() {
		String toadd = toSend.getText().toString();
		chat.setText(chat.getText() + userName + ": " + toadd + "\n");
		toSend.setText("");
		String[] vals = new String[2];
		vals[0] = userName;
		vals[1] = toadd;
		NetworkService.postMessage(new Message("chat", vals));
	}

	private void leave() {
		finish();
	}

	private void play() {

		NetworkService.postMessage(new Message("ready", null));
	}

	int isReady = 0;

	@Override
	public void onMessageReceived(final Message message) {
		// TODO Auto-generated method stub
		Log.d("SYS", "Message type: " + message.type);
		if (message.type.equals("lobby")) {
			PlayerNames = new ArrayList<String>();
			int i;
			int j = 0;
			for (i = 0; i < message.values.length; i++) {
				this.runOnUiThread(new PostRunnable(i, j) {

					@Override
					public void run() {
						if (a % 2 == 0) {
							plrNames[a - b].setText(message.values[a]
									.split(":")[0]);
							if (message.values[a].split(":")[1].equals("true")) {
								if (!cbplrRdy[a - b].isChecked()) {
									cbplrRdy[a - b].setChecked(true);
									isReady++;
								}
							}
							playerView[a - b].setVisibility(View.VISIBLE);

						} else {
							if (profilePictureView[a - b].getProfileId() != message.values[a])
								profilePictureView[a - b]
										.setProfileId(message.values[a]);

							profilePictureView[a - b]
									.setVisibility(View.VISIBLE);
						}
						plrsRdy.setText("" + isReady);
					}

				});

				if (i % 2 == 0)
					j++;
			}

		} else if (message.type.equals("connection")) {
			Log.d("SYS", "Sending message");
			String[] vals = new String[1];
			vals[0] = "Yes!";
			NetworkService.sendMessage(new Message("connection", vals));
		} else if (message.type.equals("chat")) {
			chat.post(new ChatRunnable(message) {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					chat.setText(chat.getText() + message.values[0] + ": "
							+ message.values[1] + "\n");
				}

			});
		} else if (message.type.equals("startgame")) {
			// Remove this activity from messageListener

			Intent myIntent = new Intent(getBaseContext(), BoardActivity.class);
			myIntent.putExtra("userName", userName);
			myIntent.putExtra("userId", userId);
			startActivity(myIntent);
			isRDY = true;
		}
	}
}
