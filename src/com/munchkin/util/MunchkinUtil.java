package com.munchkin.util;

import java.io.IOException;
import java.io.InputStream;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.Display;
import android.view.View;
import android.widget.ImageButton;

public class MunchkinUtil {

	public static int findIdByString(Activity thisA, String ID) {
		int resID = thisA.getResources()
				.getIdentifier(ID, "id", "com.munchkin");
		return resID;
	}

	public static int findDrawableByString(Activity activity, String draw) {
		int resID = activity.getResources().getIdentifier(draw, "drawable",
				"com.munchkin");
		return resID;
	}

	public static View findViewByString(Activity thisA, String ID) {
		return thisA.findViewById(findIdByString(thisA, ID));
	}

	@SuppressLint("NewApi")
	public static ImageButton loadDrawableByAssets(Activity activity,
			String file) {
		// To load image
		ImageButton image = new ImageButton(activity);

		try {
			// Get input stream
			InputStream ims = activity.getAssets().open(
					"images/" + file + ".jpg");
			int y = (int) (MunchkinUtil.getScreenHeight(activity) * .75);
			Bitmap newImage = Bitmap.createScaledBitmap(
					BitmapFactory.decodeStream(ims), (int) (y * .6), y, false);

			image.setImageBitmap(newImage);
			ims.close();

		} catch (IOException ex) {
			System.out.println(ex.toString());
		}
		return image;
	}

	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	public static Point getScreenSize(Activity a) {

		Display display = a.getWindowManager().getDefaultDisplay();
		Point size = new Point();
		if (versionGreaterThan(Build.VERSION_CODES.HONEYCOMB_MR1))
			display.getRealSize(size);
		else {
			size.x = display.getWidth();
			size.y = display.getHeight();
		}

		return size;
	}

	public static int getScreenHeight(Activity a) {
		return getScreenSize(a).y;
	}

	public static int getScreenWidth(Activity a) {
		return getScreenSize(a).x;
	}

	/*
	 * for reference visit
	 * http://developer.android.com/guide/topics/manifest/uses
	 * -sdk-element.html#ApiLevels
	 */
	public static boolean versionGreaterThan(int minV) {
		return android.os.Build.VERSION.SDK_INT >= minV;
	}

	public static Bitmap drawableToBitmap(Activity activity, Drawable drawable) {
		if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable) drawable).getBitmap();
		}

		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);

		int y = (int) (MunchkinUtil.getScreenHeight(activity));
		bitmap = Bitmap.createScaledBitmap(bitmap, (int) (y * .6), y, false);

		return bitmap;
	}

	public void scaleBitmap(Bitmap toScale) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 4;

	}
}
