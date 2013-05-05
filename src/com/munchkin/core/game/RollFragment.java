package com.munchkin.core.game;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.common.Message;
import com.munchkin.R;
import com.munchkin.network.NetworkService;
import com.munchkin.util.MunchkinUtil;
import com.munchkin.util.UpdateUI;

public class RollFragment extends Fragment {
	private View view;
	private TextView text;
	private ImageButton dice;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		view = inflater.inflate(R.layout.fragment_roll_die, container, false);
		text = (TextView) view.findViewById(R.id.rollText);
		text.setText("Shake the phone to roll the die!");
		UpdateUI.setTypeFace(((BoardActivity) getActivity()).tf, text);
		dice = (ImageButton) view.findViewById(R.id.rollDie);
		dice.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				NetworkService.sendMessage(new Message("ready", null));
			}
		});
		return view;
	}

	public void setDice(final int diceNum) {
		this.dice.post(new Runnable() {

			@SuppressLint("NewApi")
			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				String toShow = "die_0" + String.valueOf(diceNum);
				if (MunchkinUtil.versionGreaterThan(16)) {
					dice.setBackground(MunchkinUtil.loadDrawableByAssets(
							getActivity(), toShow).getDrawable());
				} else {
					dice.setBackgroundDrawable(MunchkinUtil
							.loadDrawableByAssets(getActivity(), toShow)
							.getDrawable());
				}
			}
		});
	}
}
