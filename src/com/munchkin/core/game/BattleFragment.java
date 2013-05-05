package com.munchkin.core.game;

import android.annotation.SuppressLint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.common.Message;
import com.facebook.widget.ProfilePictureView;
import com.munchkin.R;
import com.munchkin.network.NetworkService;
import com.munchkin.util.MunchkinUtil;
import com.munchkin.util.UpdateUI;

public class BattleFragment extends Fragment {

	private View view;
	// Monster name, Monster power
	// used to add bonuses from one shot cards
	private int bonus = 0;

	ProfilePictureView profilePictureView;
	ImageView monster;
	TextView monsterPower;
	TextView player;
	TextView playerPower;
	Button battleButton;

	@Override
	public void onDestroy() {
		super.onDestroy();
		((BitmapDrawable) monster.getDrawable()).getBitmap().recycle();
	}

	@SuppressLint("NewApi")
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		view = inflater.inflate(R.layout.fragment_combat, container, false);

		profilePictureView = (ProfilePictureView) view
				.findViewById(R.id.battlePlayerPic);
		monster = (ImageView) view.findViewById(R.id.battleMonster);
		monsterPower = (TextView) view.findViewById(R.id.monsterPower);
		player = (TextView) view.findViewById(R.id.battlePlayer);
		playerPower = (TextView) view.findViewById(R.id.playerPower);
		battleButton = (Button) view.findViewById(R.id.runWinButton);

		/* Sets button initial text */
		battleButton.setText("Ready?");
		UpdateUI.setTypeFace(((BoardActivity) getActivity()).tf, battleButton);
		final View BButtonview = battleButton;
		BButtonview.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				NetworkService.postMessage(new Message("ready", null));
			}
		});

		return view;
	}

	public void setMonster(final String card) {
		this.monster.post(new Runnable() {
			@SuppressLint("NewApi")
			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				if (MunchkinUtil.versionGreaterThan(16)) {
					monster.setBackground(MunchkinUtil.loadDrawableByAssets(
							getActivity(), card).getDrawable());
				} else {
					monster.setBackgroundDrawable(MunchkinUtil
							.loadDrawableByAssets(getActivity(), card)
							.getDrawable());
				}
				// MAKE THE CARD LOOK BETTER
				// monster.setLayoutParams(new LayoutParams((int) (MunchkinUtil
				// .getScreenHeight(getActivity()) * .71), MunchkinUtil
				// .getScreenHeight(getActivity())));
			}
		});
	}

	public void setMonsterPower(final String monsterPow) {
		this.monsterPower.post(new Runnable() {
			@Override
			public void run() {
				monsterPower.setText("The monster's power is: " + monsterPow);
				UpdateUI.setTypeFace(((BoardActivity) getActivity()).tf,
						monsterPower);
			}
		});
	}

	public void setPlayer(final String playerText) {
		this.player.post(new Runnable() {
			@Override
			public void run() {
				player.setText(playerText);
				UpdateUI.setTypeFace(((BoardActivity) getActivity()).tf, player);
			}
		});
	}

	public void setPlayerPower(final String playerPowr) {
		this.playerPower.post(new Runnable() {
			@Override
			public void run() {
				playerPower.setText(playerPowr);
				UpdateUI.setTypeFace(((BoardActivity) getActivity()).tf,
						playerPower);
			}
		});
	}

	public void setBonus(int bonus) {
		this.bonus = bonus;
		setPlayerPower("The new power is " + bonus);
	}

	public void setPlayerPic(final String pic) {
		this.profilePictureView.post(new Runnable() {
			@Override
			public void run() {
				profilePictureView.setProfileId(pic);
			}
		});
	}

	/**
	 * 
	 * @param action
	 *            is false if running and true if battling
	 */
	public void setBattleButton(final String message) {
		this.battleButton.post(new Runnable() {
			@Override
			public void run() {
				battleButton.setText(message);
				UpdateUI.setTypeFace(((BoardActivity) getActivity()).tf,
						battleButton);
			}
		});

	}

}
