package com.munchkin.core.game;

import java.util.ArrayList;
import java.util.HashMap;

import net.simonvt.menudrawer.MenuDrawer;
import net.simonvt.menudrawer.Position;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.common.Message;
import com.facebook.Session;
import com.facebook.widget.ProfilePictureView;
import com.munchkin.R;
import com.munchkin.application.MunchkinApp;
import com.munchkin.network.MessageListener;
import com.munchkin.network.NetworkService;
import com.munchkin.plugins.ShakeEventListener;
import com.munchkin.util.MunchkinUtil;
import com.munchkin.util.UpdateUI;

/**
 * Main Activity that uses Fragments to display the current phase in the game.
 * 
 * @author johnshelley
 * 
 */
public class BoardActivity extends FragmentActivity implements MessageListener {

	/* Network Connections */
	Thread clientListener;
	NetworkService networkService;
	boolean hasLaunched = false;
	int i = 1;

	/* Items */
	private MenuItem settings;
	private MenuDrawer sideDrawer;
	private MenuDrawer bottomDrawer;
	public LinearLayout handlayout;
	public LinearLayout equipLayout;
	private ArrayList<String> inHand;
	private ArrayList<String> inEquip;
	public LinearLayout playerListLayout;
	public int handSize = 1;
	public int equipSize = 1;
	private MunchkinApp app;
	private String userName;
	private String userId;
	private boolean roll;
	private int diceToShow;
	/**
	 * SensorManager handle
	 */
	private SensorManager mSensorManager;

	/**
	 * ShakeEventListener handle
	 */
	private ShakeEventListener mSensorListener;

	/* Facebook Connections */
	private ProfilePictureView profilePictureView;
	private TextView userNameView;

	public TextView getUserNameView() {
		return userNameView;
	}

	/* Fragment Items */
	private static final int Boardfragment = 0;
	private static final int ReadyButton = 1;
	private static final int GameOver = 2;
	private static final int Battlefragment = 3;
	private static final int RollFragment = 4;
	private static final int SETTINGS = 5;
	private static final int FRAGMENT_COUNT = SETTINGS + 1;
	private Fragment[] fragments = new Fragment[FRAGMENT_COUNT];

	/* Misc */
	public Typeface tf;
	String[] card = new String[15];
	/* Player Attributes */
	// Name and Level
	public HashMap<String, Integer> players;
	// Name and Total Power
	public HashMap<String, Integer> playerTotal;
	// Name and Pics
	public HashMap<String, String> playerPics;
	// Name and ifTurn
	public HashMap<String, Boolean> playersTurn;

	PopupWindow popUp;
	LinearLayout layout;

	private boolean isReady = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// keep phone going
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		app = (MunchkinApp) getApplicationContext();
		Bundle bundle = this.getIntent().getExtras();
		userName = bundle.getString("userName");
		userId = bundle.getString("userId");
		// for handling what is in each hand;
		inHand = new ArrayList<String>();
		inEquip = new ArrayList<String>();

		setContentView(R.layout.activity_board);

		/* Setup the fragments */
		FragmentManager fm = getSupportFragmentManager();
		fragments[Boardfragment] = fm.findFragmentById(R.id.zoomFragment);
		fragments[ReadyButton] = fm.findFragmentById(R.id.readyFragment);
		fragments[GameOver] = fm.findFragmentById(R.id.gameOverFragment);
		fragments[Battlefragment] = fm.findFragmentById(R.id.combatFragment);
		fragments[RollFragment] = fm.findFragmentById(R.id.rollFragment);
		fragments[SETTINGS] = fm.findFragmentById(R.id.userSettingsFragment);
		FragmentTransaction transaction = fm.beginTransaction();
		for (int i = 0; i < fragments.length; i++) {
			transaction.hide(fragments[i]);
		}
		transaction.commit();
		// Create Type face to be passed around.
		tf = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Thin.ttf");

		addMenuDrawers();
		setUserInfo();
		setUpShakeListener();

		players = new HashMap<String, Integer>();
		playerPics = new HashMap<String, String>();
		playersTurn = new HashMap<String, Boolean>();
		playerTotal = new HashMap<String, Integer>();
		/* Set the activity to be able to listen to server messages */
		NetworkService.setOnMessageReceivedListener(this);
		// TODO: Get message from server, not just a true value.
		onGameLaunched(true);
		NetworkService.postMessage(new Message("ready", null));
		popUp = new PopupWindow(this);
		layout = new LinearLayout(this);
	}

	@Override
	public View onCreateView(String name, Context context, AttributeSet attrs) {
		return super.onCreateView(name, context, attrs);
	}

	private void addMenuDrawers() {
		/* Add the sliding panel */
		sideDrawer = MenuDrawer.attach(this, MenuDrawer.TOUCH_MODE_BEZEL,
				Position.LEFT);
		sideDrawer.setMenuSize((int) (MunchkinUtil.getScreenWidth(this) * .50));
		sideDrawer.setMenuView(R.layout.menu_scrollview);
		playerListLayout = (LinearLayout) findViewById(R.id.playerListLayout);

		/* Add the bottom panel for the player's hand */
		bottomDrawer = MenuDrawer.attach(this, MenuDrawer.TOUCH_MODE_BEZEL,
				Position.BOTTOM);
		bottomDrawer
				.setMenuSize((int) (MunchkinUtil.getScreenHeight(this) * .75));
		bottomDrawer.setMenuView(R.layout.menu_card_hand);
		handlayout = (LinearLayout) findViewById(R.id.cardHandLayout);
		equipLayout = (LinearLayout) findViewById(R.id.cardEquipLayout);
	}

	private void setUserInfo() {
		/* USER INFORMATION */
		// Find the user's profile picture custom view
		profilePictureView = (ProfilePictureView) sideDrawer
				.findViewById(R.id.selection_profile_pic);
		profilePictureView.setCropped(true);
		// Find the user's name view
		userNameView = (TextView) sideDrawer
				.findViewById(R.id.selection_user_name);
		profilePictureView.setProfileId(userId);
		userNameView.setText(userName);
	}

	private void setCurrentPlayer() {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				for (String player : playersTurn.keySet()) {
					if (playersTurn.get(player)) {
						profilePictureView.setProfileId(playerPics.get(player));
						userNameView.setText(player);
					}
				}
			}
		});
	}

	private void setUpShakeListener() {
		mSensorListener = new ShakeEventListener();
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mSensorManager.registerListener(mSensorListener,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_UI);
		mSensorListener
				.setOnShakeListener(new ShakeEventListener.OnShakeListener() {

					public void onShake() {
						rollDie();
					}
				});
	}

	private void rollDie() {
		// Dice roll
		if (roll) {
			System.out.println("ROLLING THE DICE!");
			System.out.println(diceToShow);
			((RollFragment) fragments[RollFragment]).setDice(diceToShow);
		}
	}

	private void onGameLaunched(Boolean loggedIn) {
		FragmentManager manager = getSupportFragmentManager();
		// Get the number of entries in the back stack
		int backStackSize = manager.getBackStackEntryCount();
		// Clear the back stack
		for (int i = 0; i < backStackSize; i++) {
			manager.popBackStack();
		}
		// Show the main fragment
		((ReadyFragment) fragments[ReadyButton]).setReadyText(
				"Equip any cards, then click Ready!", tf);
		showFragment(ReadyButton, false);
	}

	private void showFragment(int fragmentIndex, boolean addToBackStack) {
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction transaction = fm.beginTransaction();
		for (int i = 0; i < fragments.length; i++) {
			if (i == fragmentIndex) {
				transaction.show(fragments[i]);
			} else {
				transaction.hide(fragments[i]);
			}
		}
		if (addToBackStack) {
			transaction.addToBackStack(null);
		}
		transaction.commit();
	}

	/**
	 * Simply creates an options menu.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

	}

	@Override
	public void onResume() {
		super.onResume();
		mSensorManager.registerListener(mSensorListener,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_UI);
	}

	@Override
	protected void onResumeFragments() {
		super.onResumeFragments();
		Session session = Session.getActiveSession();
		if (session != null && session.isOpened()) {
			// if the session is already open,
			// try to show the selection fragment

			if (isReady) {
				showFragment(Boardfragment, false);
			}
		} else {
			// otherwise present the splash screen
			// and ask the user to login.

			showFragment(SETTINGS, false);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle bundle) {
		super.onSaveInstanceState(bundle);
	}

	@Override
	public void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(mSensorListener);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// only add the menu when the selection fragment is showing
		if (fragments[Boardfragment].isVisible()) {
			if (menu.size() == 0) {
				settings = menu.add(R.string.settings);
			}
			return true;
		} else {
			menu.clear();
			settings = null;
		}
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.equals(settings)) {
			showFragment(SETTINGS, true);
			return true;
		}
		return false;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	/**
	 * Categorized alphabetically - Message given by the Server
	 */
	@Override
	public void onMessageReceived(Message message) {
		/* Different players come through */
		if (message.type.equals("allPlayers")) {
			// Sets all players to level 1.
			for (int i = 0; i < message.values.length; i++) {
				if (i % 2 == 0) {
					players.put(message.values[i], 1);
					/* Set everyones turn to false */
					playersTurn.put(message.values[i], false);
					playerTotal.put(message.values[i], 0);
				} else {
					playerPics.put(message.values[i - 1], message.values[i]);
				}
			}
			UpdateUI.updateUI(message, this);
		} else if (message.type.equals("battle")) {
			if (!playersTurn.get(userName)) {
				this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						findViewById(R.id.doorDeck).setClickable(true);
						findViewById(R.id.doorDeck).performClick();
						findViewById(R.id.doorDeck).setClickable(false);
					}
				});
			}
			String monster = message.values[0];

			((BattleFragment) fragments[Battlefragment]).setMonster(monster);
			((BattleFragment) fragments[Battlefragment])
					.setMonsterPower(message.values[1]);
			for (String player : playersTurn.keySet()) {
				if (playersTurn.get(player)) {
					((BattleFragment) fragments[Battlefragment])
							.setPlayer(player);
					((BattleFragment) fragments[Battlefragment])
							.setPlayerPic(playerPics.get(player));
					if (playersTurn.get(userName)) {
						String pow = "Your power is: "
								+ String.valueOf(playerTotal.get(userName));
						((BattleFragment) fragments[Battlefragment])
								.setPlayerPower(pow);
						findViewById(R.id.runWinButton).setClickable(true);
					} else {
						String pow = player + "'s power is: "
								+ String.valueOf(playerTotal.get(player));
						((BattleFragment) fragments[Battlefragment])
								.setPlayerPower(pow);
						findViewById(R.id.runWinButton).setClickable(false);
					}
				}
			}

			// Perform click on the card shown
			this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					findViewById(R.id.expanded_image).performClick();
				}
			});
			showFragment(Battlefragment, false);
		} else if (message.type.equals("battlePower")) {
			((BattleFragment) fragments[Battlefragment]).setBonus(Integer
					.parseInt(message.values[0]));
		} else if (message.type.equals("beginTurn")) {
			isReady = true;
			/* Change who's turn it is and present card */
			for (String player : playersTurn.keySet()) {
				playersTurn.put(player, false);
			}
			playersTurn.put(message.values[0], true);
			setCurrentPlayer();
			((ZoomFragment) fragments[Boardfragment])
					.setCard(message.values[1]);
			if (playersTurn.get(userName)) {
				((ZoomFragment) fragments[Boardfragment]).setHeaderBar(
						"Please draw a card from the far left deck.", tf);
				findViewById(R.id.doorDeck).setClickable(true);
			} else {
				String player = "";
				for (String playerCheck : playersTurn.keySet()) {
					if (playersTurn.get(playerCheck)) {
						player = playerCheck;
					}
				}
				((ZoomFragment) fragments[Boardfragment]).setHeaderBar(player
						+ " is drawing a card..", tf);
				findViewById(R.id.doorDeck).setClickable(false);
			}
			findViewById(R.id.doorDiscard).setClickable(false);
			findViewById(R.id.treasureDiscard).setClickable(false);
			showFragment(Boardfragment, false);
		} else if (message.type.equals("charity")) {
			if (findViewById(R.id.expanded_image).isShown()) {
				this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						findViewById(R.id.expanded_image).performClick();
					}
				});
			}
			if (playersTurn.get(userName)) {
				((ReadyFragment) fragments[ReadyButton]).setReadyText(
						message.values[0], tf);
				((ReadyFragment) fragments[ReadyButton]).reset();
				showFragment(ReadyButton, false);
			} else {
				((ZoomFragment) fragments[Boardfragment]).setHeaderBar(
						"The charitable is giving to charity.", tf);
			}
		} else if (message.type.equals("discard")) {
			if (message.values[1].equals("treasure")) {
				((ZoomFragment) fragments[Boardfragment])
						.setTreasureDiscard(message.values[0]);
			} else if (message.values[1].equals("door")) {
				((ZoomFragment) fragments[Boardfragment])
						.setDoorDiscard(message.values[0]);
			}
		} else if (message.type.equals("equipment")) {
			/* Set the players equipment */
			UpdateUI.updateUI(message, this);
		} else if (message.type.equals("gameOver")) {
			if (userName.equals(message.values[0])) {
				((GameOver) fragments[GameOver])
						.setGameOver("Game Over\nYou Won!");
			} else {
				((GameOver) fragments[GameOver])
						.setGameOver("Game Over\nYou Lost..");
			}
			showFragment(GameOver, false);
		} else if (message.type.equals("hand")) {
			/* Sets the players initial hand */
			UpdateUI.updateUI(message, this);
		} else if (message.type.equals("kickDoor")) {
			if (!playersTurn.get(userName)) {
				this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						findViewById(R.id.doorDeck).setClickable(true);
						findViewById(R.id.doorDeck).performClick();
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						findViewById(R.id.expanded_image).performClick();
						findViewById(R.id.doorDeck).setClickable(false);
					}
				});
			} else {
				this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						findViewById(R.id.expanded_image).performClick();
					}
				});
			}
			if (playersTurn.get(userName)) {
				// Setting the card even if not drawn
				((ZoomFragment) fragments[Boardfragment])
						.setCard(message.values[0]);
				((ZoomFragment) fragments[Boardfragment]).setHeaderBar(
						"Loot the room, or look for trouble.", tf);
			} else {
				String player = "";
				for (String playerCheck : playersTurn.keySet()) {
					if (playersTurn.get(playerCheck)) {
						player = playerCheck;
					}
				}
				((ZoomFragment) fragments[Boardfragment]).setHeaderBar(player
						+ " is choosing his fate.", tf);
			}

		} else if (message.type.equals("lossRun")) {
			roll = false;
			if (playersTurn.get(userName)) {
				((ReadyFragment) fragments[ReadyButton]).setReadyText(
						message.values[0], tf);
				((ReadyFragment) fragments[ReadyButton]).reset();
				showFragment(ReadyButton, false);
			} else {
				((ZoomFragment) fragments[Boardfragment]).setHeaderBar(
						"The charitable is giving to charity.", tf);
				showFragment(Boardfragment, false);

			}

		} else if (message.type.equals("notEquip")) {
			this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(BoardActivity.this, "Can not Equip!",
							Toast.LENGTH_SHORT).show();
				}
			});
		} else if (message.type.equals("playerInfo")) {
			players.put(message.values[0], Integer.parseInt(message.values[1]));
			UpdateUI.updateUI(message, this);
			showFragment(Boardfragment, false);
		} else if (message.type.equals("playerPower")) {
			String player = message.values[0];
			playerTotal.put(player, Integer.parseInt(message.values[1]));
			UpdateUI.updateUI(message, this);
		} else if (message.type.equals("roll")) {
			roll = true;
			diceToShow = Integer.parseInt(message.values[0]);
			showFragment(RollFragment, false);
		} else if (message.type.equals("toast")) {
			final String[] toToast = message.values;
			this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(BoardActivity.this, toToast[0],
							Toast.LENGTH_SHORT).show();
				}
			});
		} else if (message.type.equals("treasure")) {
			((ZoomFragment) fragments[Boardfragment])
					.setTreasure(message.values);
			this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					findViewById(R.id.expanded_image).performClick();
				}
			});
			// Adds the treasures only if your the one in battle.
			if (playersTurn.get(userName)) {
				UpdateUI.updateUI(message, this);
			}
		} else if (message.type.equals("winBattle")) {
			findViewById(R.id.doorDeck).setClickable(false);
			((ZoomFragment) fragments[Boardfragment]).setHeaderBar(
					"Please draw your treasures from the deck.", tf);
			this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(BoardActivity.this, "You Won The Battle!",
							Toast.LENGTH_SHORT).show();
				}
			});
		} else if (message.type.equals("winRun")) {
			roll = false;
			if (playersTurn.get(userName)) {
				((ReadyFragment) fragments[ReadyButton]).setReadyText(
						message.values[0], tf);
				((ReadyFragment) fragments[ReadyButton]).reset();
				showFragment(ReadyButton, false);
			} else {
				((ZoomFragment) fragments[Boardfragment]).setHeaderBar(
						"The charitable is giving to charity.", tf);
				showFragment(Boardfragment, false);

			}
		}

	}

	public ArrayList<String> getHand() {
		return inHand;
	}

	public ArrayList<String> getEquip() {
		return inEquip;
	}
}
