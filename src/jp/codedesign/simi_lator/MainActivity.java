package jp.codedesign.simi_lator;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import jp.codedesign.simi_lator.util.SystemUiHider;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class MainActivity extends Activity implements Runnable {
	private static final String TAG = "BluetoothController";

	private BluetoothAdapter mAdapter;
	private BluetoothDevice mDevice;
	private final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB"); // ぶっちゃけ重複しなければOK
	private final String DEVICE_NAME = "SBDBT-001bdc067bf3";
	private BluetoothSocket mSocket;
	private Thread mThread;
	private boolean bluetoothIsRunning;
	private Context mContext;
	private final String operation = "stop";
	private TextView x_axis, y_axis, z_axis;
	private final Handler mHandler = new Handler();
	private Button aaa;

	private Camera myCamera;

	private final SurfaceHolder.Callback mSurfaceListener = new SurfaceHolder.Callback() {
		@Override
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

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			// TODO Auto-generated method stub
			myCamera.release();
			myCamera = null;
		}

		@Override
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
		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			Log.d("Camera.PreviewCallback", "onPreviewFrame");
			/*
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
				// bitmapFatoryOptions.inSampleSize = previewWidth / 32; //
				// 強制的に横32に設定 -> Bitmapの最小サイズが 80 x 60 の模様
				Bitmap bmp = BitmapFactory.decodeByteArray(jdata, 0,
						jdata.length, bitmapFatoryOptions);

				Matrix m = new Matrix();
				m.postRotate(90);
				// m.postScale(0.1f, 0.1f);
				Bitmap rotatedBitmap = Bitmap.createBitmap(bmp, 0, 0,
						bmp.getWidth(), bmp.getHeight(), m, false);

				setMosaic(rotatedBitmap);
			} catch (Exception e) {
				e.printStackTrace();
			}
			myCamera.setPreviewCallback(this);
			myCamera.startPreview();
			*/
		}
	};

	private void setMosaic(Bitmap bmp) {
		if (controlLayout != null) {
			// controlLayout.setImageBitmap(bmp);
			drawView.setBitmap(bmp);
		}
	}

	ImageView controlLayout;
	FrameLayout gridLayout;
	ArrayList<String> list;
	DrawView drawView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this.getApplicationContext();

		setContentView(R.layout.activity_main);
		controlLayout = (ImageView) findViewById(R.id.fullscreen_content);
		drawView = (DrawView) findViewById(R.id.drawview);

		list = new ArrayList<String>();

		SurfaceView mySurfaceView = (SurfaceView) findViewById(R.id.surface_view);
		SurfaceHolder holder = mySurfaceView.getHolder();
		holder.addCallback(mSurfaceListener);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		Button dummyButton = (Button) findViewById(R.id.dummy_button);
		dummyButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						mAdapter = BluetoothAdapter.getDefaultAdapter();
						Set<BluetoothDevice> devices = mAdapter
								.getBondedDevices();
						for (BluetoothDevice device : devices) {
							Log.i(TAG, "DEVICE:" + device.getName());
							if (device.getName().equals(DEVICE_NAME)) {
								mDevice = device;
								Toast.makeText(mContext,
										"デバイス名:" + device.getName(),
										Toast.LENGTH_LONG).show();
							}
						}

						// Threadを起動し、Bluetooth接続
						mThread = new Thread(MainActivity.this);
						bluetoothIsRunning = true;
						mThread.start();
					}
				});
	}

	String bufferString = "";

	// Bluetooth Background
	@Override
	public void run() {
		InputStream mmInStream = null;
		OutputStream mmOutputStream = null;
		try {
			// 取得したデバイス名を使ってBluetoothでSocket接続
			mSocket = mDevice.createRfcommSocketToServiceRecord(MY_UUID);
			mSocket.connect();
			mmInStream = mSocket.getInputStream();
			mmOutputStream = mSocket.getOutputStream();

			// InputStreamのバッファを格納
			byte[] buffer = new byte[1024];
			// 取得したバッファのサイズを格納
			int bytes;

			while (bluetoothIsRunning) {
				// InputStreamの読み込み　
//				bytes = mmInStream.read(buffer);

				// String型に変換
//				String readMsg = new String(buffer, 0, bytes);
//				bufferString = bufferString + readMsg;

				String commandId = "000999900090009999999999990000999900090009999999999990000999900090009999999999990\n";
				mmOutputStream.write(commandId.getBytes());
				
				Log.e("SERIAL", commandId);
				
				Thread.sleep(500);
			}
		} catch (Exception e) {
			Log.e(TAG, "error:" + e);
			try {
				mSocket.close();
			} catch (Exception ee) {
			}
			bluetoothIsRunning = false;
		}
	}
}
