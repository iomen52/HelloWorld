/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.scan.zxing.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import com.example.guanjiangjun2.R;
import com.google.zxing.ResultPoint;
import com.scan.zxing.camera.CameraManager;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder
 * rectangle and partial transparency outside it, as well as the laser scanner
 * animation and result points.
 * 
 */
public final class ViewfinderView extends View {

	/** ˢ�½����ʱ�� */
	private static final long ANIMATION_DELAY = 10L;

	/** ɨ����ÿ��ˢ���ƶ��ľ��� */
	private static final int SPEEN_DISTANCE = 5;

	/** �ֻ�����Ļ�ܶ� */
	private static float density;

	/** �����С */
	private static final int TEXT_SIZE = 16;

	/** �������ɨ�������ľ��� */
	private static final int TEXT_PADDING_TOP = 50;

	/** ���ʶ�������� */
	private Paint paint;

	/** ɨ���ߵ����λ�� */
	private int slideTop;

	/** ��ɨ��Ķ�ά�������� */
	private Bitmap resultBitmap;

	/** ����������ɫ */
	private final int maskColor;

	/** ɨ���ĸ���������ɫ */
	private final int resultColor;

	/** �Ƿ��ǵ�һ��ɨ�� */
	private boolean isFirst;

	/** ɨ���ı���ͼ */
	private Bitmap scanBackground;

	/** ɨ���� */
	private Bitmap scanLine;

	public ViewfinderView(Context context, AttributeSet attrs) {

		super(context, attrs);

		// ��ȡ�ֻ�����Ļ�ܶ�
		density = context.getResources().getDisplayMetrics().density;

		// ����
		paint = new Paint();
		// ��ȡ��Դ
		Resources resources = getResources();
		// ��ȡ����������ɫ
		maskColor = resources.getColor(R.color.viewfinder_mask);

		// ��ȡɨ���ĸ���������ɫ
		resultColor = resources.getColor(R.color.result_view);

		// ��ȡɨ���ı���ͼ
		scanBackground = BitmapFactory.decodeResource(resources,
				R.drawable.capture);

		// ��ȡɨ����
		scanLine = BitmapFactory
				.decodeResource(resources, R.drawable.scan_line);
	}

	@Override
	public void onDraw(Canvas canvas) {
		// �м��ɨ���Ҫ�޸�ɨ���Ĵ�С��ȥCameraManager�����޸�
		Rect frame = CameraManager.get().getFramingRect();
		if (frame == null) {
			return;
		}

		// ��ʼ���м��߻��������ϱߺ����±�
		if (!isFirst) {
			isFirst = true;
			slideTop = frame.top;
		}

		// ��ȡ��Ļ�Ŀ�͸�
		int width = canvas.getWidth();
		int height = canvas.getHeight();

		// �Ƿ�ɨ����ϣ�δɨ����Ϻ�ɨ����ϸ���������ɫ��ͬ
		paint.setColor(resultBitmap != null ? resultColor : maskColor);

		// ����ɨ�������ĸ������򣬹��ĸ����֣�ɨ�������浽��Ļ���棬ɨ�������浽��Ļ����
		// ɨ��������浽��Ļ��ߣ�ɨ�����ұߵ���Ļ�ұ�
		canvas.drawRect(0, 0, width, frame.top, paint);
		canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
		canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1,
				paint);
		canvas.drawRect(0, frame.bottom + 1, width, height, paint);

		// ��ɨ���ı���ͼ����ɨ�����
		canvas.drawBitmap(scanBackground, null, frame, null);

		// �����м����,ÿ��ˢ�½��棬�м���������ƶ�SPEEN_DISTANCE������
		slideTop += SPEEN_DISTANCE;
		// ɨ�赽��׶Σ����ص������������ɨ
		if (slideTop >= frame.bottom) {
			slideTop = frame.top;
		}

		// ɨ��������
		Rect lineRect = new Rect();
		lineRect.left = frame.left;
		lineRect.right = frame.right;
		lineRect.top = slideTop;
		lineRect.bottom = slideTop + 18;

		// ��ɨ���ߵı���ͼ����ɨ�����
		canvas.drawBitmap(scanLine, null, lineRect, null);

		// ��ɨ����������
		paint.setColor(Color.WHITE);
		// �ֺ�
		paint.setTextSize(TEXT_SIZE * density);
		// ��͸��
		paint.setAlpha(0xFF);
		// ���壺����
		paint.setTypeface(Typeface.create("System", Typeface.BOLD));

		// �������֣����־���
		paint.setTextAlign(Paint.Align.CENTER);
		canvas.drawText(getResources().getString(R.string.scan_prompt_label),
				frame.centerX(),
				(float) (frame.bottom + (float) TEXT_PADDING_TOP * density),
				paint);

		// ֻˢ��ɨ�������ݣ������ط���ˢ��
		postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top,
				frame.right, frame.bottom);

	}

	public void drawViewfinder() {
		resultBitmap = null;
		invalidate();
	}

	/**
	 * Draw a bitmap with the result points highlighted instead of the live
	 * scanning display.
	 * 
	 * @param barcode
	 *            An image of the decoded barcode.
	 */
	public void drawResultBitmap(Bitmap barcode) {
		// ɨ����ϣ�resultBitmapΪɨ�����������Bitmap����
		resultBitmap = barcode;
		invalidate();
	}

	public void addPossibleResultPoint(ResultPoint point) {

	}

}
