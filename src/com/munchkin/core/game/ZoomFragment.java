package com.munchkin.core.game;

import android.annotation.SuppressLint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.common.Message;
import com.munchkin.R;
import com.munchkin.network.NetworkService;
import com.munchkin.network.ui_posts.ImageRunnable;
import com.munchkin.util.MunchkinUtil;
import com.munchkin.util.UpdateUI;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;

public class ZoomFragment extends Fragment {

	// Holds a reference to the current animator,
	// so that it can be canceled mid-way.
	private Animator mCurrentAnimator;

	// The system "short" animation time duration, in milliseconds. This
	// duration is ideal for subtle animations or animations that occur
	// very frequently.
	private int mShortAnimationDuration;

	private View view;
	private TextView headerBar;

	public void setHeaderBar(final String header, final Typeface tf) {
		headerBar.post(new Runnable() {
			@Override
			public void run() {
				headerBar.setText(header);
				if (tf != null) {
					UpdateUI.setTypeFace(tf, headerBar);
				}
			}
		});
	}

	ImageButton doorButton;
	ImageButton treasureButton;
	ImageButton dDiscardButton;
	ImageButton tDiscardButton;

	Thread clientListener;
	NetworkService networkService;

	// used to store the name of the card being drawn and the top card of each
	// pile
	private String card;
	private String[] treasures = null;

	public String getCard() {
		return card;
	}

	public void setCard(String card) {
		this.card = card;
	}

	public void setTreasure(String[] tres) {
		treasures = tres;
	}

	String dDiscardName = "blank_door";
	String tDiscardName = "blank_treasure";

	public void setDoorDiscard(String name) {
		dDiscardName = name;
		dDiscardButton.post(new Runnable() {

			@Override
			public void run() {
				MunchkinUtil.loadDrawableByAssets(getActivity(), dDiscardName);
			}
		});
	}

	public void setTreasureDiscard(String name) {
		tDiscardName = name;
		tDiscardButton.post(new Runnable() {

			@Override
			public void run() {
				MunchkinUtil.loadDrawableByAssets(getActivity(), tDiscardName);
			}
		});
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		view = inflater.inflate(R.layout.fragment_board_show_decks, container,
				false);
		card = "";
		headerBar = (TextView) view.findViewById(R.id.rotationTV);
		// // setting Image button views
		// View V = inflater.inflate(R.layout.fragment_board_show_decks,
		// (ViewGroup) container, false);
		doorButton = (ImageButton) view.findViewById(R.id.doorDeck);
		treasureButton = (ImageButton) view.findViewById(R.id.treasureDeck);
		dDiscardButton = (ImageButton) view.findViewById(R.id.doorDiscard);
		tDiscardButton = (ImageButton) view.findViewById(R.id.treasureDiscard);

		// Hook up clicks on the thumbnail views

		// for the first button the door deck
		final View dDeckView = doorButton;
		dDeckView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				NetworkService.postMessage(new Message("drawDoor", null));
				doorButton.post(new Runnable() {

					@Override
					public void run() {

						zoomImageFromThumb(dDeckView, MunchkinUtil
								.loadDrawableByAssets(getActivity(), card),
								false);
					}
				});
			}
		});

		// second button door discard deck
		final View dDiscardView = dDiscardButton;
		dDiscardView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				zoomImageFromThumb(dDiscardView, dDiscardButton, true);
			}
		});

		// third button Treasure deck
		final View tDeckView = treasureButton;
		tDeckView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				NetworkService.postMessage(new Message("drawTreasure", null));
				if (treasures != null) {
					for (int i = 0; i < treasures.length; i++) {
						final int temp = i;
						treasureButton.post(new Runnable() {

							@Override
							public void run() {
								// for (int i = 0; i < treasures.length; i++) {
								// zoomImageFromThumb(tDeckView, MunchkinUtil
								// .loadDrawableByAssets(getActivity(),
								// treasures[i]), false);
								// }
							}
						});

					}

				}

			}
		});

		// forth button treasure discard deck
		final View tDiscardView = tDiscardButton;
		// tDiscardView.setOnClickListener(new View.OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// zoomImageFromThumb(tDiscardView, tDiscardButton, true);
		// }
		// });

		// Retrieve and cache the system's default "short animation time.
		mShortAnimationDuration = getResources().getInteger(
				android.R.integer.config_shortAnimTime);
		return view;

	}

	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	private void zoomImageFromThumb(final View thumbView,
			ImageButton imageResId, final boolean click) {
		// If there's an animation in progress, cancel it
		// immediately and proceed with this one.
		if (mCurrentAnimator != null) {
			mCurrentAnimator.cancel();
		}

		// Load the high-resolution "zoomed-in" image.
		final ImageView expandedImageView = (ImageView) view
				.findViewById(R.id.expanded_image);

		expandedImageView.setImageBitmap(MunchkinUtil.drawableToBitmap(
				getActivity(), imageResId.getDrawable()));

		// Calculate the starting and ending bounds for the zoomed-in image.
		// This step involves lots of math. Yay, math.
		final Rect startBounds = new Rect();
		final Rect finalBounds = new Rect();
		final Point globalOffset = new Point();

		// The start bounds are the global visible rectangle of the thumbnail,
		// and the final bounds are the global visible rectangle of the
		// container
		// view. Also set the container view's offset as the origin for the
		// bounds, since that's the origin for the positioning animation
		// properties (X, Y).
		thumbView.getGlobalVisibleRect(startBounds);
		view.findViewById(R.id.container).getGlobalVisibleRect(finalBounds,
				globalOffset);
		startBounds.offset(-globalOffset.x, -globalOffset.y);
		finalBounds.offset(-globalOffset.x, -globalOffset.y);

		// Adjust the start bounds to be the same aspect ratio as the final
		// bounds using the "center crop" technique. This prevents undesirable
		// stretching during the animation. Also calculate the start scaling
		// factor (the end scaling factor is always 1.0).
		float startScale;
		if ((float) finalBounds.width() / finalBounds.height() > (float) startBounds
				.width() / startBounds.height()) {
			// Extend start bounds horizontally
			startScale = (float) startBounds.height() / finalBounds.height();
			float startWidth = startScale * finalBounds.width();
			float deltaWidth = (startWidth - startBounds.width()) / 2;
			startBounds.left -= deltaWidth;
			startBounds.right += deltaWidth;
		} else {
			// Extend start bounds vertically
			startScale = (float) startBounds.width() / finalBounds.width();
			float startHeight = startScale * finalBounds.height();
			float deltaHeight = (startHeight - startBounds.height()) / 2;
			startBounds.top -= deltaHeight;
			startBounds.bottom += deltaHeight;
		}

		// Hide the thumbnail and show the zoomed-in view. When the animation
		// begins, it will position the zoomed-in view in the place of the
		// thumbnail.
		// ViewHelper.setAlpha(thumbView, 0f);

		expandedImageView.setVisibility(View.VISIBLE);

		// Set the pivot point for SCALE_X and SCALE_Y transformations
		// to the top-left corner of the zoomed-in view (the default
		// is the center of the view).
		ViewHelper.setPivotX(expandedImageView, 0f);
		ViewHelper.setPivotY(expandedImageView, 0f);
		// expandedImageView.setPivotX(0f);
		// expandedImageView.setPivotY(0f);

		// Construct and run the parallel animation of the four translation and
		// scale properties (X, Y, SCALE_X, and SCALE_Y).
		AnimatorSet set = new AnimatorSet();
		set.play(
				ObjectAnimator.ofFloat(expandedImageView, "translationX",
						startBounds.left, finalBounds.left))
				.with(ObjectAnimator.ofFloat(expandedImageView, "translationY",
						startBounds.top, finalBounds.top))
				.with(ObjectAnimator.ofFloat(expandedImageView, "scaleX",
						startScale, 1f))
				.with(ObjectAnimator.ofFloat(expandedImageView, "scaleY",
						startScale, 1f));
		set.setDuration(mShortAnimationDuration);
		set.setInterpolator(new DecelerateInterpolator());
		set.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				mCurrentAnimator = null;
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				mCurrentAnimator = null;
			}
		});
		set.start();
		mCurrentAnimator = set;

		// Upon clicking the zoomed-in image, it should zoom back down
		// to the original bounds and show the thumbnail instead of
		// the expanded image.
		final float startScaleFinal = startScale;

		expandedImageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mCurrentAnimator != null) {
					mCurrentAnimator.cancel();
				}

				// Animate the four positioning/sizing properties in
				// parallel,
				// back to their original values.
				AnimatorSet set = new AnimatorSet();
				set.play(
						ObjectAnimator.ofFloat(expandedImageView,
								"translationX", startBounds.left))
						.with(ObjectAnimator.ofFloat(expandedImageView,
								"translationY", startBounds.top))
						.with(ObjectAnimator.ofFloat(expandedImageView,
								"scaleX", startScaleFinal))
						.with(ObjectAnimator.ofFloat(expandedImageView,
								"scaleY", startScaleFinal));
				set.setDuration(mShortAnimationDuration);
				set.setInterpolator(new DecelerateInterpolator());
				set.addListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						ViewHelper.setAlpha(thumbView, 1f);
						expandedImageView.setVisibility(View.GONE);
						mCurrentAnimator = null;
					}

					@Override
					public void onAnimationCancel(Animator animation) {
						ViewHelper.setAlpha(thumbView, 1f);
						expandedImageView.setVisibility(View.GONE);
						mCurrentAnimator = null;
					}
				});
				set.start();
				mCurrentAnimator = set;
			}
		});
		if (!click) {
			expandedImageView.setClickable(false);
		}
	}

}
