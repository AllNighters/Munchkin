package com.munchkin.core.game;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.munchkin.R;

public class GameOver extends Fragment {

	private View view;
	TextView gameOver;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		view = inflater.inflate(R.layout.fragment_game_over, container, false);
		gameOver = (TextView) view.findViewById(R.id.endGameText);
		gameOver.setBackgroundColor(Color.rgb(25, 200, 100));
		return view;
	}

	public void setGameOver(final String message) {
		gameOver.post(new Runnable() {
			@Override
			public void run() {
				gameOver.setText(message);
			}
		});
	}
}
