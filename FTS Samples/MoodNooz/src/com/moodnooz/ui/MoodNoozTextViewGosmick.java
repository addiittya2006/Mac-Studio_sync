
package com.moodnooz.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class MoodNoozTextViewGosmick extends TextView {

	public MoodNoozTextViewGosmick(Context context, AttributeSet attrs) {
		super(context, attrs);
		Typeface font = Typeface.createFromAsset(context.getAssets(), "font/GosmickSansBold.ttf");
		setTypeface(font);
	}

	@Override
	public void setTypeface(Typeface tf) {
		super.setTypeface(tf);
	}
}
