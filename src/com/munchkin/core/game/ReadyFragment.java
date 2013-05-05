package com.munchkin.core.game;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.common.Message;
import com.munchkin.R;
import com.munchkin.network.NetworkService;
import com.munchkin.util.UpdateUI;

public class ReadyFragment extends Fragment {

	private View view;
	private TextView readyText;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		view = inflater.inflate(R.layout.fragment_ready, container, false);
		final Button readyButton = (Button) view.findViewById(R.id.readyButton);
		readyText = (TextView) view.findViewById(R.id.readyText);
		readyButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				NetworkService.postMessage(new Message("ready", null));
				readyButton.setBackgroundColor(Color.rgb(25, 200, 100));
				readyButton.setText("Ready!");
			}

		});
		return view;
	}

	public void reset() {
		final Button readyButton = (Button) view.findViewById(R.id.readyButton);
		readyButton.post(new Runnable() {
			@Override
			public void run() {
				readyButton.setBackgroundColor(Color.GRAY);
				readyButton.setText("Ready?");
			}
		});

	}

	public void setReadyText(final String text, final Typeface tf) {
		readyText.post(new Runnable() {
			@Override
			public void run() {
				readyText.setText(text);
				if (tf != null) {
					UpdateUI.setTypeFace(tf, readyText);
				}
			}
		});
	}
}
