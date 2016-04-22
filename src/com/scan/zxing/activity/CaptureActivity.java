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
 * ��ά��ɨ�軭��
 * 
 * @author Hitoha
 * @version 1.00 2015/04/27 �½�
 */
public class CaptureActivity extends Activity implements Callback,
		OnCaptureListener {

	/** ɨ��Handler */
	private CaptureActivityHandler handler;

	/** ɨ��View */
	private ViewfinderView viewfinderView;

	/** �Ƿ���SurfaceView */
	private boolean hasSurface;

	/** ����Fromat */
	private Vector<BarcodeFormat> decodeFormats;

	/** ���ֱ��� */
	private String characterSet;

	/** InactivityTimer */
	private InactivityTimer inactivityTimer;

	/** ��Ƶ������ */
	private MediaPlayer mediaPlayer;

	/** �Ƿ񲥷� */
	private boolean playBeep;

	/** ���� */
	private static final float BEEP_VOLUME = 0.10f;

	/** �� */
	private boolean vibrate;

	/** ���ذ�ť */
	private Button btnBack;

	/** �𶯳���ʱ�� */
	private static final long VIBRATE_DURATION = 200L;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		super.onCreate(savedInstanceState);

		// ɨ�軭��
		setContentView(R.layout.layout_scan);

		// ��ʼ��Camera
		CameraManager.init(getApplication());

		// ɨ��View
		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinderView);

		// ���ذ�ť
		btnBack = (Button) findViewById(R.id.btnBack);

		// ���ذ�ť��ӵ������
		btnBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// ���������
				finish();
			}
		});

		// ��û�г�ʼ��SurfaceView
		hasSurface = false;

		// ��ʼ��InactivityTimer
		inactivityTimer = new InactivityTimer(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		// ��ʼ��SurfaceView
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.previewView);
		// ��ȡSurfaceHolder
		SurfaceHolder surfaceHolder = surfaceView.getHolder();

		// ��ʼ��SurfaceViewʱ��ֱ�Ӵ�Camera
		if (hasSurface) {
			initCamera(surfaceHolder);
		} else {
			// δ��ʼ��SurfaceViewʱ��ͨ���ص���Camera
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		decodeFormats = null;
		characterSet = null;

		// ��������
		playBeep = true;
		AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);

		// �������Ͳ�Ϊ��׼ʱ������������
		if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
			playBeep = false;
		}

		// ��ʼ����������
		initBeepSound();

		// ��
		vibrate = true;

	}

	@Override
	protected void onPause() {
		// ������ͣʱ
		super.onPause();
		// ȡ��Handler
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		// �ر�Camera
		CameraManager.get().closeDriver();
	}

	@Override
	protected void onDestroy() {
		// �������ʱֹͣInactivityTimer
		inactivityTimer.shutdown();
		super.onDestroy();
	}

	/**
	 * ��Camera
	 * 
	 * @param surfaceHolder
	 *            SurfaceHolder
	 */
	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			// ��Camera
			CameraManager.get().openDriver(surfaceHolder);
		} catch (IOException ioe) {
			return;
		} catch (RuntimeException e) {
			return;
		}
		if (handler == null) {
			// �½�Handler
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
		// ��ʼ��SurfaceView�����Ǳ��λ�������δ��ʼ��SurfaceViewʱ
		if (!hasSurface) {
			// ���ı��λ״̬
			hasSurface = true;
			// ��Camera
			initCamera(holder);
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// SurfaceView���٣����λ����Ϊδ��ʼ��SurfaceView
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
	 * ��ʼ����������
	 */
	private void initBeepSound() {
		if (playBeep && mediaPlayer == null) {
			// The volume on STREAM_SYSTEM is not adjustable, and users found it
			// too loud,
			// so we now play on the music stream.
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			// MediaPlayer
			mediaPlayer = new MediaPlayer();
			// ����ģʽ
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			// ��Ӳ�����ɼ���
			mediaPlayer.setOnCompletionListener(beepListener);

			// �����ļ�
			AssetFileDescriptor file = getResources().openRawResourceFd(
					R.raw.beep);
			try {
				// ���������ļ�
				mediaPlayer.setDataSource(file.getFileDescriptor(),
						file.getStartOffset(), file.getLength());
				file.close();
				// ��������������С
				mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
				// ׼������
				mediaPlayer.prepare();
			} catch (IOException e) {
				mediaPlayer = null;
			}
		}
	}

	/**
	 * ɨ����ɺ󲥷���������
	 */
	private void playBeepSoundAndVibrate() {
		// ��������
		if (playBeep && mediaPlayer != null) {
			mediaPlayer.start();
		}
		if (vibrate) {
			// ���ֻ�
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATE_DURATION);
		}
	}

	/**
	 * When the beep has finished playing, rewind to queue up another one.
	 */
	private final OnCompletionListener beepListener = new OnCompletionListener() {
		public void onCompletion(MediaPlayer mediaPlayer) {
			// ��λ
			mediaPlayer.seekTo(0);
		}
	};

	@Override
	public Handler getHandler() {
		return handler;
	}

	/**
	 * ����ɨ����
	 * 
	 * @param result
	 * @param barcode
	 */
	@Override
	public void handleDecode(Result result, Bitmap barcode) {
		inactivityTimer.onActivity();
		// ������������
		playBeepSoundAndVibrate();
		// ��ȡ�����Ķ�ά����Ϣ
		String resultString = result.getText();
		// ����ʧ��
		if (TextUtils.isEmpty(resultString)) {
			// ������˾
			Toast.makeText(CaptureActivity.this, R.string.scan_failed_prompt,
					Toast.LENGTH_SHORT).show();
		} else {
			// ����ǰ����
			// Intent
			Intent resultIntent = new Intent();
			resultIntent.putExtra("result", resultString);
			// Bundle
			//Bundle bundle = new Bundle();
			// ��ά��洢���ı���Ϣ
			//bundle.putString("result", resultString);
			//bundle.putString("result", "201504011502018");
			// ����������Ķ�ά��ͼƬ
			//bundle.putParcelable("bitmap", barcode);
			// ����Bundle
			//resultIntent.putExtras(bundle);
			// �����ݴ��ݸ�ǰ����
			this.setResult(RESULT_OK, resultIntent);
			Log.e("CaptureActivity", "CaptureActivity + "+resultString);
		}

		// ���������
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