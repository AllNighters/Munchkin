package com.munchkin.network.facebook;

import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.munchkin.R;
import com.munchkin.application.MunchkinApp;
import com.munchkin.core.lobby.LobbyActivity;

public class FBLoggedIn extends Fragment {

	private static final String TAG = FBLoggedIn.class.getSimpleName();
	private static final String POST_ACTION_PATH = "me/fb_sample_scrumps:eat";
	private static final String PENDING_ANNOUNCE_KEY = "pendingAnnounce";
	private static final Uri M_FACEBOOK_URL = Uri
			.parse("http://m.facebook.com");

	private static final int REAUTH_ACTIVITY_CODE = 100;
	private static final List<String> PERMISSIONS = Arrays
			.asList("publish_actions");


	private String userName = "";
	private String userId = "";
	private UiLifecycleHelper uiHelper;
	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(final Session session, final SessionState state,
				final Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		uiHelper = new UiLifecycleHelper(getActivity(), callback);
		uiHelper.onCreate(savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();
		uiHelper.onResume();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.fragment_fb_logged_in, container,
				false);
		ImageButton start = (ImageButton) view.findViewById(R.id.startGame);
		start.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startGame(v);
			}
		});
		ImageButton exit = (ImageButton) view.findViewById(R.id.exitGame);
		exit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				exitGame(v);
			}
		});
		return view;

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REAUTH_ACTIVITY_CODE) {
			uiHelper.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle bundle) {
		super.onSaveInstanceState(bundle);
		uiHelper.onSaveInstanceState(bundle);
	}

	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}

	private void onSessionStateChange(final Session session,
			SessionState state, Exception exception) {
		if (session != null && session.isOpened()) {
			makeMeRequest(session);
		}
	}

	private void makeMeRequest(final Session session) {
		Request request = Request.newMeRequest(session,
				new Request.GraphUserCallback() {
					@Override
					public void onCompleted(GraphUser user, Response response) {
						MunchkinApp userInfo = null;
						if (session == Session.getActiveSession()) {
							if (user != null) {
								userId = (user.getId());
								userName = (user.getName());
							}
						}
						if (response.getError() != null) {
							handleError(response.getError());
						}

					}

				});
		request.executeAsync();

	}

	/* USER INTERACTION SECTION */
	/**
	 * Called when the user clicks the Start Game button (look at xml)
	 * 
	 * @param view
	 */
	public void startGame(View view) {
		// Do something in response to button
		Intent intent = new Intent(getActivity(), LobbyActivity.class);
		intent.putExtra("userName", userName);
		intent.putExtra("userId", userId);
		startActivity(intent);
	}

	/**
	 * Called when the user clicks the Exit Game button (look at xml)
	 * 
	 * @param v
	 */
	public void exitGame(View v) {
		getActivity().finish();
		System.exit(0);
	}

	private void handleError(FacebookRequestError error) {
		DialogInterface.OnClickListener listener = null;
		String dialogBody = null;

		if (error == null) {
			dialogBody = getString(R.string.error_dialog_default_text);
		} else {
			switch (error.getCategory()) {
			case AUTHENTICATION_RETRY:
				// tell the user what happened by getting the message id, and
				// retry the operation later
				String userAction = (error.shouldNotifyUser()) ? ""
						: getString(error.getUserActionMessageId());
				dialogBody = getString(R.string.error_authentication_retry,
						userAction);
				listener = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						Intent intent = new Intent(Intent.ACTION_VIEW,
								M_FACEBOOK_URL);
						startActivity(intent);
					}
				};
				break;

			case AUTHENTICATION_REOPEN_SESSION:
				// close the session and reopen it.
				dialogBody = getString(R.string.error_authentication_reopen);
				listener = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						Session session = Session.getActiveSession();
						if (session != null && !session.isClosed()) {
							session.closeAndClearTokenInformation();
						}
					}
				};
				break;

			case PERMISSION:
				// request the publish permission
				dialogBody = getString(R.string.error_permission);
				break;

			case SERVER:
			case THROTTLING:
				// this is usually temporary, don't clear the fields, and
				// ask the user to try again
				dialogBody = getString(R.string.error_server);
				break;

			case BAD_REQUEST:
				// this is likely a coding error, ask the user to file a bug
				dialogBody = getString(R.string.error_bad_request,
						error.getErrorMessage());
				break;

			case OTHER:
			case CLIENT:
			default:
				// an unknown issue occurred, this could be a code error, or
				// a server side issue, log the issue, and either ask the
				// user to retry, or file a bug
				dialogBody = getString(R.string.error_unknown,
						error.getErrorMessage());
				break;
			}
		}

		new AlertDialog.Builder(getActivity())
				.setPositiveButton(R.string.error_dialog_button_text, listener)
				.setTitle(R.string.error_dialog_title).setMessage(dialogBody)
				.show();
	}

}
