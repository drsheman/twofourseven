package com.molamil.radio24syv.components;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by patriksvensson on 16/10/15.
 */
public class TextViewNeueHaasGrotesk55Rg extends TextView
{
    public TextViewNeueHaasGrotesk55Rg(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setFont(context);
    }

    public TextViewNeueHaasGrotesk55Rg(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.setFont(context);
    }

    public TextViewNeueHaasGrotesk55Rg(Context context) {
        super(context);
        this.setFont(context);
    }

    private void setFont(Context context)
    {
        Typeface font = Typeface.createFromAsset(context.getAssets(), "fonts/NHaasGroteskDSPro-55Rg.otf");
        this.setTypeface(font);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
        {
            this.setFontFeatureSettings("ss01");
        }
    }
}