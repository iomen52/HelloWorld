package com.scan.zxing.listener;

import com.google.zxing.Result;
import com.scan.zxing.view.ViewfinderView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;

/**
 * ɨ��ӿ�
 * 
 * @author Hitoha
 * @version 1.00 2015/04/27 �½�
 */
public interface OnCaptureListener {

	/**
	 * ��ȡHandler
	 * 
	 * @return Handler
	 */
	public Handler getHandler();

	/**
	 * ���ؽ������
	 * 
	 * @param result
	 *            ���������
	 * @param barcode
	 *            ��������ͼ��
	 */
	public void handleDecode(Result result, Bitmap barcode);

	/**
	 * ��ȡɨ��View
	 * 
	 * @return ɨ��View
	 */
	public ViewfinderView getViewfinderView();

	/**
	 * ����ɨ��View
	 */
	public void drawViewfinder();

	/**
	 * ���÷���ǰ��������
	 * 
	 * @param resultCode
	 *            ResultCode
	 * @param data
	 *            Intent
	 */
	public void onSetResult(int resultCode, Intent data);

	/**
	 * ������ת
	 * 
	 * @param intent
	 *            Intent
	 */
	public void onStartActivity(Intent intent);

	/**
	 * �������
	 */
	public void onFinish();
}
