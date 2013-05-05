package com.munchkin.util;

import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.common.Message;
import com.facebook.widget.ProfilePictureView;
import com.munchkin.core.game.BoardActivity;
import com.munchkin.network.NetworkService;

public class UpdateUI {

	/* Player panel attributes */
	static HashMap<String, TextView> playerLevel = new HashMap<String, TextView>();

	/**
	 * Helps create the font
	 * 
	 * @param tf
	 * @param view
	 */
	public static void setTypeFace(Typeface tf, TextView view) {
		view.setTypeface(tf);
		view.setTextSize(24);
		view.setTextColor(Color.WHITE);
		view.setGravity(Gravity.CENTER);
	}

	/**
	 * Static class to update the UI on the fly if needed.
	 * 
	 * @param message
	 * @param boardActivity
	 */
	public static void updateUI(Message message, BoardActivity boardActivity) {
		if (message.type.equals("allPlayers")) {
			addPlayers(boardActivity);
		} else if (message.type.equals("hand")) {
			addCardsToHand(message, boardActivity);
		} else if (message.type.equals("equipment")) {
			addCardsToEquip(message, boardActivity);
		} else if (message.type.equals("playerInfo")) {
			updatePlayers(boardActivity, message);
		} else if (message.type.equals("playerPower")) {
			updatePlayers(boardActivity, message);
		} else if (message.type.equals("treasure")) {
			// DO NOTHING
		}
	}

	private static void updatePlayers(final BoardActivity board,
			final Message message) {
		final TextView toUpdate = playerLevel.get(message.values[0]);
		toUpdate.post(new Runnable() {
			@Override
			public void run() {
				String player = message.values[0];
				toUpdate.setText(player + ":\nLevel-"
						+ board.players.get(player) + "  Bonus-"
						+ board.playerTotal.get(player));
			}
		});
	}

	/**
	 * FIX THIS
	 * 
	 * @param boardActivity
	 */
	private static void addPlayers(final BoardActivity boardActivity) {
		final LinearLayout layout = boardActivity.playerListLayout;
		for (String player : boardActivity.players.keySet()) {
			if (player != null) {
				final LinearLayout playerLayout = new LinearLayout(
						boardActivity);
				playerLayout
						.setLayoutParams(new LayoutParams(
								(LayoutParams.WRAP_CONTENT),
								LayoutParams.MATCH_PARENT));
				playerLayout.setPadding(30, 10, 0, 20);
				/* Facebook Pics */
				final ProfilePictureView playerPic = new ProfilePictureView(
						boardActivity);
				playerPic.setPresetSize(ProfilePictureView.SMALL);
				playerPic.setLayoutParams(new LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
				playerPic.setProfileId(boardActivity.playerPics.get(player));
				/* Player Names */
				final TextView playerName = new TextView(boardActivity);
				playerName.setText(player + ":\nLevel-"
						+ boardActivity.players.get(player) + "  Bonus-"
						+ boardActivity.playerTotal.get(player));
				playerName.setGravity(Gravity.CENTER);
				playerName.setPadding(30, 0, 0, 0);
				setTypeFace(boardActivity.tf, playerName);
				playerLevel.put(player, playerName);

				playerLayout.addView(playerPic);
				playerLayout.addView(playerName);
				layout.post(new Runnable() {
					@Override
					public void run() {

						layout.addView(playerLayout);
					}
				});

			}
		}

	}

	/**
	 * Add the players hand to the lower scroll view.
	 * 
	 * @param message
	 * @param boardActivity
	 */
	private static void addCardsToHand(Message message,
			BoardActivity boardActivity) {
		String[] source = message.values;
		LinearLayout layout = boardActivity.handlayout;
		ArrayList<String> messageList = new ArrayList<String>();
		ArrayList<Integer> toRemoves = new ArrayList<Integer>();
		for (int i = 0; i < source.length; i++) {
			messageList.add(source[i]);
		}
		/* If hand doesn't contain, add it */
		for (int i = 0; i < messageList.size(); i++) {
			if (!boardActivity.getHand().contains(messageList.get(i))) {
				addCardToHand(boardActivity, messageList.get(i));
				boardActivity.getHand().add(messageList.get(i));
			}
		}

		/* If hand server doens't contain, remove it */
		int removeCount = 0;
		int size = boardActivity.getHand().size();
		for (int i = 0; i < size; i++)
			if (!messageList.contains(boardActivity.getHand().get(
					i - removeCount))) {
				toRemoves.add(i);
				boardActivity.getHand().remove(i - removeCount);
				removeCount++;
			}
		if (toRemoves.size() > 0)
			removeCard(layout, toRemoves);

	}

	/**
	 * Add the players hand to the lower scroll view.
	 * 
	 * @param message
	 * @param boardActivity
	 */

	private static void addCardsToEquip(Message message,
			BoardActivity boardActivity) {
		String[] source = message.values;
		LinearLayout layout = boardActivity.equipLayout;
		ArrayList<Integer> toRemoves = new ArrayList<Integer>();
		ArrayList<String> messageList = new ArrayList<String>();
		for (int i = 0; i < source.length; i++) {
			messageList.add(source[i]);
		}

		for (int i = 0; i < messageList.size(); i++)
			if (!boardActivity.getEquip().contains(messageList.get(i))) {

				addCardToEquip(boardActivity, messageList.get(i));
				boardActivity.getEquip().add(messageList.get(i));
			}

		int removeCount = 0;
		int size = boardActivity.getEquip().size();
		for (int i = 0; i < size; i++)
			if (!messageList.contains(boardActivity.getEquip().get(
					i - removeCount))) {
				toRemoves.add(i);
				boardActivity.getEquip().remove(i - removeCount);
				removeCount++;
			}
		if (toRemoves.size() > 0)
			removeCard(layout, toRemoves);

	}

	private static void addCardToHand(final BoardActivity boardActivity,
			final String source) {
		final LinearLayout handLayout = boardActivity.handlayout;
		final ImageButton newcard = MunchkinUtil.loadDrawableByAssets(
				boardActivity, source);
		newcard.setContentDescription(source);
		newcard.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onCardInHandClick(boardActivity, v);
			}
		});

		handLayout.post(new Runnable() {
			@Override
			public void run() {
				// boardActivity.getHand().add(source);
				int y = (int) (MunchkinUtil.getScreenHeight(boardActivity) * .75);
				newcard.setLayoutParams(new LayoutParams((int) (y * .6),
						LayoutParams.MATCH_PARENT));
				handLayout.addView(newcard);
			}
		});

	}

	private static void addCardToEquip(final BoardActivity board,
			final String source) {
		final LinearLayout equipLay = board.equipLayout;
		final ImageButton newcard = MunchkinUtil.loadDrawableByAssets(board,
				source);
		newcard.setContentDescription(source);
		newcard.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onCardInEquipClick(board, v);
			}
		});

		equipLay.post(new Runnable() {
			@Override
			public void run() {
				int y = (int) (MunchkinUtil.getScreenHeight(board) * .75);
				newcard.setLayoutParams(new LayoutParams((int) (y * .6),
						LayoutParams.MATCH_PARENT));
				equipLay.addView(newcard);

			}
		});
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	public static void onCardInHandClick(final BoardActivity board, View arg0) {
		final View view = arg0;
		final String source = arg0.getContentDescription().toString();
		// make a new object for this
		final ImageView expanded = new ImageView(board);
		int y = (int) (MunchkinUtil.getScreenHeight(board) * .9);

		Bitmap bit = MunchkinUtil.drawableToBitmap(board,
				((ImageView) view).getDrawable());
		bit = Bitmap.createScaledBitmap(bit, ((int) (y * .71)), y, false);
		if (MunchkinUtil.versionGreaterThan(16))
			expanded.setImageBitmap(bit);
		else
			expanded.setBackgroundDrawable(new BitmapDrawable(bit));
		expanded.setPadding(0, 0, 0, 0);
		LinearLayout dialog = new LinearLayout(board);
		LinearLayout rWing = new LinearLayout(board);
		LinearLayout lWing = new LinearLayout(board);
		Button ret = new Button(board);
		Button use = new Button(board);
		Button equip = new Button(board);
		Button discard = new Button(board);

		dialog.setOrientation(LinearLayout.HORIZONTAL);
		dialog.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		AlertDialog.Builder builder = new AlertDialog.Builder(board);

		rWing.setOrientation(LinearLayout.VERTICAL);
		rWing.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.MATCH_PARENT));

		lWing.setOrientation(LinearLayout.VERTICAL);
		lWing.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.MATCH_PARENT));

		// highest level linear layout
		dialog.addView(lWing);
		dialog.addView(expanded);
		dialog.addView(rWing);

		// left side layout
		lWing.addView(ret);
		lWing.addView(use);

		// Right side layout
		rWing.addView(equip);
		rWing.addView(discard);

		final AlertDialog alert = builder.create();
		ret.setText("Return");
		use.setText("Use");
		equip.setText("Equip");
		discard.setText("Discard");

		ret.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (MunchkinUtil.versionGreaterThan(16))
					((BitmapDrawable) expanded.getDrawable()).getBitmap()
							.recycle();
				
				
				alert.hide();
			}
		});

		use.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String[] vals = { source };
				NetworkService.sendMessage(new Message("use", vals));
				if (MunchkinUtil.versionGreaterThan(16))
					((BitmapDrawable) expanded.getDrawable()).getBitmap()
							.recycle();
				alert.hide();

			}
		});
		use.setGravity(Gravity.BOTTOM);

		equip.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				onEquip(board, source, view);
				if (MunchkinUtil.versionGreaterThan(16))
					((BitmapDrawable) expanded.getDrawable()).getBitmap()
							.recycle();
				alert.hide();
			}
		});

		discard.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String[] vals = { source, null };
				NetworkService.sendMessage(new Message("discard", vals));
				if (MunchkinUtil.versionGreaterThan(16))
					((BitmapDrawable) expanded.getDrawable()).getBitmap()
							.recycle();
				alert.hide();
			}
		});

		discard.setGravity(Gravity.BOTTOM);

		alert.setView(dialog);
		alert.show();
	}

	@SuppressLint("NewApi")
	private static void onCardInEquipClick(BoardActivity board, View arg0) {
		final View view = arg0;
		final String source = arg0.getContentDescription().toString();
		// make a new object for this
		// Make a new object for this
		final ImageView expanded = MunchkinUtil.loadDrawableByAssets(board,
				source);
		int y = (int) (MunchkinUtil.getScreenHeight(board) * .9);

		Bitmap bit = MunchkinUtil.drawableToBitmap(board,
				((ImageView) view).getDrawable());
		bit = Bitmap.createScaledBitmap(bit, ((int) (y * .71)), y, false);
		expanded.setImageBitmap(bit);
		expanded.setPadding(0, 0, 0, 0);
		LinearLayout dialog = new LinearLayout(board);
		LinearLayout rWing = new LinearLayout(board);
		LinearLayout lWing = new LinearLayout(board);
		Button ret = new Button(board);
		Button equip = new Button(board);
		Button discard = new Button(board);

		dialog.setOrientation(LinearLayout.HORIZONTAL);
		dialog.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		AlertDialog.Builder builder = new AlertDialog.Builder(board);

		rWing.setOrientation(LinearLayout.VERTICAL);
		rWing.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.MATCH_PARENT));

		lWing.setOrientation(LinearLayout.VERTICAL);
		lWing.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.MATCH_PARENT));

		// highest level linear layout
		dialog.addView(lWing);
		dialog.addView(expanded);
		dialog.addView(rWing);

		// left side layout
		lWing.addView(ret);

		// Right side layout
		rWing.addView(equip);
		rWing.addView(discard);

		final AlertDialog alert = builder.create();
		ret.setText("Return");
		equip.setText("Activate");
		discard.setText("Discard");

		ret.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (MunchkinUtil.versionGreaterThan(16))
					((BitmapDrawable) expanded.getDrawable()).getBitmap()
							.recycle();
				alert.hide();
			}
		});
		equip.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (MunchkinUtil.versionGreaterThan(16))
					((BitmapDrawable) expanded.getDrawable()).getBitmap()
							.recycle();
				alert.hide();
			}
		});
		// final LinearLayout layout = board.handlayout;
		discard.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String[] vals = { source, null };
				NetworkService.sendMessage(new Message("discard", vals));
				if (MunchkinUtil.versionGreaterThan(16))
					((BitmapDrawable) expanded.getDrawable()).getBitmap()
							.recycle();
				alert.hide();
			}
		});

		discard.setGravity(Gravity.BOTTOM);

		alert.setView(dialog);
		alert.show();
	}

	private final static void onEquip(final BoardActivity board, String name,
			View view) {
		// Send to server what card to equip.
		String[] values = { name, "active" };
		Message message = new Message("equip", values);
		NetworkService.postMessage(message);
	}

	private static void removeCard(final LinearLayout layout,
			final ArrayList<Integer> indexes) {

		layout.post(new Runnable() {

			@Override
			public void run() {
				int removeCount = 0;
				for (int i = 0; i < indexes.size(); i++) {

					BitmapDrawable temp = ((BitmapDrawable) ((ImageView) layout
							.getChildAt(indexes.get(i) - removeCount))
							.getDrawable());
					layout.removeViewAt(indexes.get(i) - removeCount);
					temp.getBitmap().recycle();
					removeCount++;
				}
			}
		});
	}
	// /**
	// * Not being used currently
	// *
	// * @param board
	// */
	// private static void setHandEquipSize(BoardActivity board) {
	// float equipW = (float) ((((double) board.equipSize / (double)
	// board.handSize) * 100) - 5);
	// float handW = (float) ((((double) board.handSize / (double)
	// board.equipSize) * 100) - 5);
	// if (equipW < 20) {
	// equipW = 20;
	// handW = 80;
	// }
	// if (handW < 20) {
	// equipW = 80;
	// handW = 20;
	// }
	// // width height
	// LinearLayout.LayoutParams params =
	// (android.widget.LinearLayout.LayoutParams) ((HorizontalScrollView) board
	// .findViewById(R.id.hSVHand)).getLayoutParams();
	// params.weight = handW;
	// params = (android.widget.LinearLayout.LayoutParams)
	// ((HorizontalScrollView) board
	// .findViewById(R.id.hSVEquip)).getLayoutParams();
	// params.weight = equipW;
	// }
}
