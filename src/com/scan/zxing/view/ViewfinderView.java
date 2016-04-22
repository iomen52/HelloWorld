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

	/** 刷新界面的时间 */
	private static final long ANIMATION_DELAY = 10L;

	/** 扫描线每次刷新移动的距离 */
	private static final int SPEEN_DISTANCE = 5;

	/** 手机的屏幕密度 */
	private static float density;

	/** 字体大小 */
	private static final int TEXT_SIZE = 16;

	/** 字体距离扫描框下面的距离 */
	private static final int TEXT_PADDING_TOP = 50;

	/** 画笔对象的引用 */
	private Paint paint;

	/** 扫描线的最顶端位置 */
	private int slideTop;

	/** 将扫描的二维码拍下来 */
	private Bitmap resultBitmap;

	/** 覆盖区域颜色 */
	private final int maskColor;

	/** 扫描后的覆盖区域颜色 */
	private final int resultColor;

	/** 是否是第一次扫描 */
	private boolean isFirst;

	/** 扫描框的背景图 */
	private Bitmap scanBackground;

	/** 扫描线 */
	private Bitmap scanLine;

	public ViewfinderView(Context context, AttributeSet attrs) {

		super(context, attrs);

		// 获取手机的屏幕密度
		density = context.getResources().getDisplayMetrics().density;

		// 画笔
		paint = new Paint();
		// 获取资源
		Resources resources = getResources();
		// 获取覆盖区域颜色
		maskColor = resources.getColor(R.color.viewfinder_mask);

		// 获取扫描后的覆盖区域颜色
		resultColor = resources.getColor(R.color.result_view);

		// 获取扫描框的背景图
		scanBackground = BitmapFactory.decodeResource(resources,
				R.drawable.capture);

		// 获取扫描线
		scanLine = BitmapFactory
				.decodeResource(resources, R.drawable.scan_line);
	}

	@Override
	public void onDraw(Canvas canvas) {
		// 中间的扫描框，要修改扫描框的大小，去CameraManager里面修改
		Rect frame = CameraManager.get().getFramingRect();
		if (frame == null) {
			return;
		}

		// 初始化中间线滑动的最上边和最下边
		if (!isFirst) {
			isFirst = true;
			slideTop = frame.top;
		}

		// 获取屏幕的宽和高
		int width = canvas.getWidth();
		int height = canvas.getHeight();

		// 是否扫描完毕，未扫描完毕和扫描完毕覆盖区域颜色不同
		paint.setColor(resultBitmap != null ? resultColor : maskColor);

		// 画出扫描框外面的覆盖区域，共四个部分，扫描框的上面到屏幕上面，扫描框的下面到屏幕下面
		// 扫描框的左边面到屏幕左边，扫描框的右边到屏幕右边
		canvas.drawRect(0, 0, width, frame.top, paint);
		canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
		canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1,
				paint);
		canvas.drawRect(0, frame.bottom + 1, width, height, paint);

		// 将扫描框的背景图画在扫面框中
		canvas.drawBitmap(scanBackground, null, frame, null);

		// 绘制中间的线,每次刷新界面，中间的线往下移动SPEEN_DISTANCE个距离
		slideTop += SPEEN_DISTANCE;
		// 扫描到最底段，返回到上面继续往下扫
		if (slideTop >= frame.bottom) {
			slideTop = frame.top;
		}

		// 扫描线区域
		Rect lineRect = new Rect();
		lineRect.left = frame.left;
		lineRect.right = frame.right;
		lineRect.top = slideTop;
		lineRect.bottom = slideTop + 18;

		// 将扫描线的背景图画在扫面框中
		canvas.drawBitmap(scanLine, null, lineRect, null);

		// 画扫描框下面的字
		paint.setColor(Color.WHITE);
		// 字号
		paint.setTextSize(TEXT_SIZE * density);
		// 不透明
		paint.setAlpha(0xFF);
		// 字体：粗体
		paint.setTypeface(Typeface.create("System", Typeface.BOLD));

		// 绘制文字，文字居中
		paint.setTextAlign(Paint.Align.CENTER);
		canvas.drawText(getResources().getString(R.string.scan_prompt_label),
				frame.centerX(),
				(float) (frame.bottom + (float) TEXT_PADDING_TOP * density),
				paint);

		// 只刷新扫描框的内容，其他地方不刷新
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
		// 扫描完毕，resultBitmap为扫描后相机拍摄的Bitmap对象
		resultBitmap = barcode;
		invalidate();
	}

	public void addPossibleResultPoint(ResultPoint point) {

	}

}
