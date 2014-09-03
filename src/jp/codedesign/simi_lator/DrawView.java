package jp.codedesign.simi_lator;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;

public class DrawView extends View{
	ArrayList<Color> pixelList;
	int DEFAULT_WIDTH = 32;
	int DEFAULT_HEIHGT = 8;
	Bitmap bmp;
	
	int pixels[] = {};
	
	public DrawView(Context context) {
		super(context);
	}

	public DrawView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public DrawView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	
	@Override
	public void onDraw(Canvas c) {
		if ( pixels.length != 0) {
			for (int y = 0; y < DEFAULT_HEIHGT; y++) {
				for (int x = 0; x < DEFAULT_WIDTH; x++) {
					Paint paint = new Paint();
//					paint.setColor(pixels[x + y * DEFAULT_WIDTH]);
					paint.setColor(Color.BLUE);
					Rect rect = new Rect(x * 40, y * 40 , x * 40 + 30, y * 40 + 30);
					c.drawRect(rect, paint);
				}
			}					
		}
		if(bmp != null) {
			c.drawBitmap(bmp, 0, 0, null);
		}
		invalidate();
	}

	public void setBitmap(Bitmap bmp) {
		this.bmp = bmp;
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		pixels = new int[DEFAULT_WIDTH * DEFAULT_HEIHGT];
		
		// TODO: bmp.getPixelsで正しいデータが取れていない
		bmp.getPixels(pixels, 0, DEFAULT_WIDTH, 0, 0, DEFAULT_WIDTH, DEFAULT_HEIHGT);
	}
}
