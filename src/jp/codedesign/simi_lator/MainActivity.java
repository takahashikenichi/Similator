package jp.codedesign.simi_lator;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import jp.codedesign.simi_lator.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class MainActivity extends Activity {
	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = true;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

	/**
	 * If set, will toggle the system UI visibility upon interaction. Otherwise,
	 * will show the system UI visibility upon interaction.
	 */
	private static final boolean TOGGLE_ON_CLICK = true;

	/**
	 * The flags to pass to {@link SystemUiHider#getInstance}.
	 */
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider mSystemUiHider;

	private Camera myCamera;

	private SurfaceHolder.Callback mSurfaceListener = new SurfaceHolder.Callback() {
		public void surfaceCreated(SurfaceHolder holder) {
			// TODO Auto-generated method stub
			myCamera = Camera.open();
			try {
				myCamera.setPreviewDisplay(holder);
				myCamera.setDisplayOrientation(90);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			// TODO Auto-generated method stub
			myCamera.release();
			myCamera = null;
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			// Log.d("SurfaceHolder.Callback", "surfaceChanged");
			// TODO Auto-generated method stub
			Camera.Parameters parameters = myCamera.getParameters();
			// parameters.setPreviewSize(width, height);
			myCamera.setPreviewCallback(editPreviewImage);
			myCamera.setParameters(parameters);
			myCamera.startPreview();
		}
	};

	private final Camera.PreviewCallback editPreviewImage = new Camera.PreviewCallback() {
		public void onPreviewFrame(byte[] data, Camera camera) {
			Log.d("Camera.PreviewCallback", "onPreviewFrame");
			// myCamera.setPreviewCallback(null); // プレビューコールバックを解除
			myCamera.stopPreview();
			// Preview時の処理を開始
			int previewWidth = camera.getParameters().getPreviewSize().width;
			int previewHeight = camera.getParameters().getPreviewSize().height;

			YuvImage yuvImage = new YuvImage(data, camera.getParameters()
					.getPreviewFormat(), previewWidth, previewHeight, null);

			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				yuvImage.compressToJpeg(new Rect(0, 0, previewWidth,
						previewHeight), 100, baos);
				byte[] jdata = baos.toByteArray();
				BitmapFactory.Options bitmapFatoryOptions = new BitmapFactory.Options();
				bitmapFatoryOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
//				bitmapFatoryOptions.inSampleSize = previewWidth / 32; // 強制的に横32に設定 -> Bitmapの最小サイズが 80 x 60 の模様
				Bitmap bmp = BitmapFactory.decodeByteArray(jdata, 0,
						jdata.length, bitmapFatoryOptions);

				Matrix m = new Matrix();
				m.postRotate(90);
//				m.postScale(0.1f, 0.1f);
				Bitmap rotatedBitmap = Bitmap.createBitmap(bmp, 0, 0,
						bmp.getWidth(), bmp.getHeight(), m, false);

				setMosaic(rotatedBitmap);
			} catch (Exception e) {
				e.printStackTrace();
			}

			myCamera.setPreviewCallback(this);
			myCamera.startPreview();
		}
	};

	private void setMosaic(Bitmap bmp) {
		if (controlLayout != null) {
			controlLayout.setImageBitmap(bmp);
		}

		drawView.setBitmap(bmp);
	}

	ImageView controlLayout;
	FrameLayout gridLayout;
	ArrayList<String> list;
	DrawView drawView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		controlLayout = (ImageView) findViewById(R.id.fullscreen_content);
		drawView = (DrawView) findViewById(R.id.drawview);
		
		list = new ArrayList<String>();
		
		SurfaceView mySurfaceView = (SurfaceView) findViewById(R.id.surface_view);
		SurfaceHolder holder = mySurfaceView.getHolder();
		holder.addCallback(mSurfaceListener);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		final View controlsView = findViewById(R.id.fullscreen_content_controls);
		final View contentView = findViewById(R.id.fullscreen_content);

		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, contentView,
				HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider
				.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
					// Cached values.
					int mControlsHeight;
					int mShortAnimTime;

					@Override
					@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
					public void onVisibilityChange(boolean visible) {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
							// If the ViewPropertyAnimator API is available
							// (Honeycomb MR2 and later), use it to animate the
							// in-layout UI controls at the bottom of the
							// screen.
							if (mControlsHeight == 0) {
								mControlsHeight = controlsView.getHeight();
							}
							if (mShortAnimTime == 0) {
								mShortAnimTime = getResources().getInteger(
										android.R.integer.config_shortAnimTime);
							}
							controlsView
									.animate()
									.translationY(visible ? 0 : mControlsHeight)
									.setDuration(mShortAnimTime);
						} else {
							// If the ViewPropertyAnimator APIs aren't
							// available, simply show or hide the in-layout UI
							// controls.
							controlsView.setVisibility(visible ? View.VISIBLE
									: View.GONE);
						}

						if (visible && AUTO_HIDE) {
							// Schedule a hide().
							delayedHide(AUTO_HIDE_DELAY_MILLIS);
						}
					}
				});

		// Set up the user interaction to manually show or hide the system UI.
		contentView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (TOGGLE_ON_CLICK) {
					mSystemUiHider.toggle();
				} else {
					mSystemUiHider.show();
				}
			}
		});

		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		findViewById(R.id.dummy_button).setOnTouchListener(
				mDelayHideTouchListener);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		delayedHide(100);
	}

	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the
	 * system UI. This is to prevent the jarring behavior of controls going away
	 * while interacting with activity UI.
	 */
	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
			return false;
		}
	};

	Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			mSystemUiHider.hide();
		}
	};

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}
}
