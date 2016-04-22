package com.scan.zxing.activity;

import java.io.IOException;
import java.util.Vector;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.example.guanjiangjun2.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.guanjiangjun2.activity.LoginActivity;
import com.scan.zxing.camera.CameraManager;
import com.scan.zxing.decoding.CaptureActivityHandler;
import com.scan.zxing.decoding.InactivityTimer;
import com.scan.zxing.listener.OnCaptureListener;
import com.scan.zxing.view.ViewfinderView;

/**
 * 二维码扫描画面
 * 
 * @author Hitoha
 * @version 1.00 2015/04/27 新建
 */
public class CaptureActivity extends Activity implements Callback,
		OnCaptureListener {

	/** 扫描Handler */
	private CaptureActivityHandler handler;

	/** 扫描View */
	private ViewfinderView viewfinderView;

	/** 是否有SurfaceView */
	private boolean hasSurface;

	/** 解析Fromat */
	private Vector<BarcodeFormat> decodeFormats;

	/** 文字编码 */
	private String characterSet;

	/** InactivityTimer */
	private InactivityTimer inactivityTimer;

	/** 音频播放器 */
	private MediaPlayer mediaPlayer;

	/** 是否播放 */
	private boolean playBeep;

	/** 声音 */
	private static final float BEEP_VOLUME = 0.10f;

	/** 震动 */
	private boolean vibrate;

	/** 返回按钮 */
	private Button btnBack;

	/** 震动持续时间 */
	private static final long VIBRATE_DURATION = 200L;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		super.onCreate(savedInstanceState);

		// 扫描画面
		setContentView(R.layout.layout_scan);

		// 初始化Camera
		CameraManager.init(getApplication());

		// 扫描View
		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinderView);

		// 返回按钮
		btnBack = (Button) findViewById(R.id.btnBack);

		// 返回按钮添加点击监听
		btnBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 本画面结束
				finish();
			}
		});

		// 还没有初始化SurfaceView
		hasSurface = false;

		// 初始化InactivityTimer
		inactivityTimer = new InactivityTimer(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		// 初始化SurfaceView
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.previewView);
		// 获取SurfaceHolder
		SurfaceHolder surfaceHolder = surfaceView.getHolder();

		// 初始化SurfaceView时，直接打开Camera
		if (hasSurface) {
			initCamera(surfaceHolder);
		} else {
			// 未初始化SurfaceView时，通过回调打开Camera
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		decodeFormats = null;
		characterSet = null;

		// 播放声音
		playBeep = true;
		AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);

		// 铃声类型不为标准时，不播放声音
		if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
			playBeep = false;
		}

		// 初始化播放声音
		initBeepSound();

		// 震动
		vibrate = true;

	}

	@Override
	protected void onPause() {
		// 画面暂停时
		super.onPause();
		// 取消Handler
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		// 关闭Camera
		CameraManager.get().closeDriver();
	}

	@Override
	protected void onDestroy() {
		// 画面结束时停止InactivityTimer
		inactivityTimer.shutdown();
		super.onDestroy();
	}

	/**
	 * 打开Camera
	 * 
	 * @param surfaceHolder
	 *            SurfaceHolder
	 */
	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			// 打开Camera
			CameraManager.get().openDriver(surfaceHolder);
		} catch (IOException ioe) {
			return;
		} catch (RuntimeException e) {
			return;
		}
		if (handler == null) {
			// 新建Handler
			handler = new CaptureActivityHandler(this, decodeFormats,
					characterSet);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// 初始化SurfaceView，但是标记位还标记着未初始化SurfaceView时
		if (!hasSurface) {
			// 更改标记位状态
			hasSurface = true;
			// 打开Camera
			initCamera(holder);
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// SurfaceView销毁，标记位更改为未初始化SurfaceView
		hasSurface = false;

	}

	@Override
	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();

	}

	/**
	 * 初始化播放声音
	 */
	private void initBeepSound() {
		if (playBeep && mediaPlayer == null) {
			// The volume on STREAM_SYSTEM is not adjustable, and users found it
			// too loud,
			// so we now play on the music stream.
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			// MediaPlayer
			mediaPlayer = new MediaPlayer();
			// 播放模式
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			// 添加播放完成监听
			mediaPlayer.setOnCompletionListener(beepListener);

			// 声音文件
			AssetFileDescriptor file = getResources().openRawResourceFd(
					R.raw.beep);
			try {
				// 设置声音文件
				mediaPlayer.setDataSource(file.getFileDescriptor(),
						file.getStartOffset(), file.getLength());
				file.close();
				// 左右声道声音大小
				mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
				// 准备播放
				mediaPlayer.prepare();
			} catch (IOException e) {
				mediaPlayer = null;
			}
		}
	}

	/**
	 * 扫面完成后播放声音及震动
	 */
	private void playBeepSoundAndVibrate() {
		// 播放声音
		if (playBeep && mediaPlayer != null) {
			mediaPlayer.start();
		}
		if (vibrate) {
			// 震动手机
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATE_DURATION);
		}
	}

	/**
	 * When the beep has finished playing, rewind to queue up another one.
	 */
	private final OnCompletionListener beepListener = new OnCompletionListener() {
		public void onCompletion(MediaPlayer mediaPlayer) {
			// 复位
			mediaPlayer.seekTo(0);
		}
	};

	@Override
	public Handler getHandler() {
		return handler;
	}

	/**
	 * 处理扫描结果
	 * 
	 * @param result
	 * @param barcode
	 */
	@Override
	public void handleDecode(Result result, Bitmap barcode) {
		inactivityTimer.onActivity();
		// 播放声音及震动
		playBeepSoundAndVibrate();
		// 获取解析的二维码信息
		String resultString = result.getText();
		// 解析失败
		if (TextUtils.isEmpty(resultString)) {
			// 弹出吐司
			Toast.makeText(CaptureActivity.this, R.string.scan_failed_prompt,
					Toast.LENGTH_SHORT).show();
		} else {
			// 返回前画面
			// Intent
			Intent resultIntent = new Intent();
			resultIntent.putExtra("result", resultString);
			// Bundle
			//Bundle bundle = new Bundle();
			// 二维码存储的文本信息
			//bundle.putString("result", resultString);
			//bundle.putString("result", "201504011502018");
			// 解析是拍摄的二维码图片
			//bundle.putParcelable("bitmap", barcode);
			// 设置Bundle
			//resultIntent.putExtras(bundle);
			// 将数据传递给前画面
			this.setResult(RESULT_OK, resultIntent);
			Log.e("CaptureActivity", "CaptureActivity + "+resultString);
		}

		// 本画面结束
		CaptureActivity.this.finish();
	}

	@Override
	public void onSetResult(int resultCode, Intent data) {
		setResult(resultCode, data);
	}

	@Override
	public void onStartActivity(Intent intent) {
		startActivity(intent);
	}

	@Override
	public void onFinish() {
		finish();
	}
}