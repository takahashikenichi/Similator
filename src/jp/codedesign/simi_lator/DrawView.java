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

public class DrawView extends View {
	ArrayList<Color> pixelList;
	int DEFAULT_WIDTH = 32;
	int DEFAULT_HEIGHT = 12;
	Bitmap bmp;
	int width;
	int height;

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
		if (pixels.length != 0) {
			for (int y = 0; y < DEFAULT_HEIGHT; y++) {
				for (int x = 0; x < DEFAULT_WIDTH; x++) {
					Paint paint = new Paint();
					paint.setColor(pixels[x * ( width / DEFAULT_WIDTH ) + y * (height / DEFAULT_HEIGHT / 2) * width + (height * width / 4) ]);
					// paint.setColor(Color.BLUE);
					Rect rect = new Rect(x * 32 + 32, y * 32 + 32, x * 32 + 62,
							y * 32 + 62);
					c.drawRect(rect, paint);
				}
			}
		}
		if (bmp != null) {
//			c.drawBitmap(bmp, 0, 0, null);
		}
		invalidate();
	}

	public void setBitmap(Bitmap bmp) {
		this.bmp = bmp.copy(Bitmap.Config.ARGB_8888, true);
		width = bmp.getWidth();
		height = bmp.getHeight();
		// pixels = new int[DEFAULT_WIDTH * DEFAULT_HEIGHT];
		pixels = new int[width * height];

		// TODO: bmp.getPixelsで正しいデータが取れていない
		// bmp.getPixels(pixels, 0, DEFAULT_WIDTH, 0, 0, DEFAULT_WIDTH,
		// DEFAULT_HEIGHT);
		bmp.getPixels(pixels, 0, width, 0, 0, width, height);
		// ドットの中の平均値を使う
	    int dot = 16;
	    int square = dot * dot;
	    for(int w = 0, widthEnd = width / dot; w < widthEnd; w++){
	        for(int h = 0, heightEnd = height / dot; h < heightEnd; h++){
	            // ドットの中の平均値を使う
	            int r = 0;
	            int g = 0;
	            int b = 0;
	            int moveX = w * dot;
	            int moveY = h * dot;
	            for(int dw = 0; dw < dot; dw++){
	                for(int dh = 0; dh < dot; dh++){
	                    int dotColor = pixels[moveX + dw + (moveY + dh) * width];
	                    r += Color.red(dotColor);
	                    g += Color.green(dotColor);
	                    b += Color.blue(dotColor);
	                }
	            }
	            r = r / square;
	            g = g / square;
	            b = b / square;
	            for(int dw = 0; dw < dot; dw++){
	                for(int dh = 0; dh < dot; dh++){
	                    pixels[moveX + dw + (moveY + dh) * width] = Color.rgb(r, g, b);
	                }
	            }
	        }
	    }
	}
}
