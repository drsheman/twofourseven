package com.molamil.radio24syv.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.molamil.radio24syv.R;
import com.molamil.radio24syv.player.RadioPlayer;
import com.molamil.radio24syv.storage.Storage;

/**
 * Created by jens on 11/09/15.
 */
public class RadioPlayerButton extends Button implements
        View.OnClickListener,
        RadioPlayer.OnPlaybackListener {

    private int action;
    private String url;
    private String title;
    private String description;
    private boolean adaptActionAfterPlay = true; // This is really a terrible name I know. It means that if the action was PLAY, the button will change to PAUSE or STOP depending on the audio source (local or streamed).

    private RadioPlayer player;
    private boolean isAvailable = true;

    public RadioPlayerButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Apply attributes from XML
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.RadioPlayerButton,
                0, 0);

        try {
            setAction(a.getInteger(R.styleable.RadioPlayerButton_action, RadioPlayer.ACTION_PLAY));
            setUrl(a.getString(R.styleable.RadioPlayerButton_audioUrl));
            setTitle(a.getString(R.styleable.RadioPlayerButton_audioTitle));
            setDescription(a.getString(R.styleable.RadioPlayerButton_audioDescription));
            setAdaptActionAfterPlay(a.getBoolean(R.styleable.RadioPlayerButton_adaptActionAfterPlay, true));
        } finally {
            a.recycle();
        }

        setOnClickListener(this);
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean getAdaptActionAfterPlay() {
        return adaptActionAfterPlay;
    }

    public void setAdaptActionAfterPlay(boolean adaptActionAfterPlay) {
        this.adaptActionAfterPlay = adaptActionAfterPlay;
    }

    public void setRadioPlayer(RadioPlayer player) {
        this.player = player;
        player.addListener(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        // View is now attached
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // View is now detached, and about to be destroyed
        if (player != null) {
            player.removeListener(this);
            player = null;
        }
    }

    @Override
    public void onClick(View v) {
        Log.d("JJJ", "action " + action + " player " + player + " url " + url + " title " + title);
        if (player == null) {
            return;
        }

        if (!isAvailable()) {
            Log.d("JJJ", "Unable to click button (id " + getId() + ") because it is not available");
            return; // Return, cannot click button
        }

        switch (action) {
            case RadioPlayer.ACTION_PLAY:
                player.play(getUrl(), getTitle(), getDescription());
                break;
            case RadioPlayer.ACTION_STOP:
                player.stop();
                break;
            case RadioPlayer.ACTION_PAUSE:
                player.pause();
                break;
            case RadioPlayer.ACTION_NEXT:
                player.next();
                break;
            case RadioPlayer.ACTION_PREVIOUS:
                player.previous();
                break;
        }
    }


    static Paint greenPaint = null;
    static Paint redPaint = null;

    @Override
    protected void onDraw(Canvas canvas) {
        //setEnabled(isAvailable()); // Disable if action is not available due to the current state of the player

        if (greenPaint == null) {
            greenPaint = new Paint();
            greenPaint.setTextSize(12 * getResources().getDisplayMetrics().scaledDensity);
            greenPaint.setColor(getResources().getColor(android.R.color.holo_green_light));
        }
        if (redPaint == null) {
            redPaint = new Paint();
            redPaint.setTextSize(12 * getResources().getDisplayMetrics().scaledDensity);
            redPaint.setColor(getResources().getColor(android.R.color.holo_red_light));
        }

        Paint p;
        if (isAvailable()) {
            p = greenPaint;
        } else {
            p = redPaint;
        }
        drawCenter(canvas, p, RadioPlayer.getActionName(action));

        super.onDraw(canvas);
    }

    // http://stackoverflow.com/a/32081250
    private void drawCenter(Canvas canvas, Paint paint, String text) {
        int cHeight = canvas.getClipBounds().height();
        int cWidth = canvas.getClipBounds().width();
        Rect r = new Rect();
        paint.setTextAlign(Paint.Align.LEFT);
        paint.getTextBounds(text, 0, text.length(), r);
        float x = cWidth / 2f - r.width() / 2f - r.left;
        //float y = cHeight / 2f + r.height() / 2f - r.bottom;
        float y = cHeight / 2f + 4; // Hack to use same y position no matter text height
        canvas.drawText(text, x, y, paint);
    }

    @Override
    public void OnBusy(RadioPlayer player) {
        switch (action) {
            case RadioPlayer.ACTION_PLAY:
                setIsAvailable(false);
                break;
            case RadioPlayer.ACTION_STOP:
                setIsAvailable(false); // TODO true
                break;
            case RadioPlayer.ACTION_PAUSE:
                setIsAvailable(false);
                break;
            case RadioPlayer.ACTION_NEXT:
                setIsAvailable(false);
                break;
            case RadioPlayer.ACTION_PREVIOUS:
                setIsAvailable(false);
                break;
        }
    }

    @Override
    public void OnStarted(RadioPlayer player) {
        switch (action) {
            case RadioPlayer.ACTION_PLAY:
                boolean isPlayingMyUrl = isPlayingMyUrl(player);
                if (adaptActionAfterPlay) {
                    if (isPlayingMyUrl) {
                        if (RadioPlayer.isLocalUrl(player.getUrl())) {
                            setAction(RadioPlayer.ACTION_PAUSE);
                        } else {
                            setAction(RadioPlayer.ACTION_STOP);
                        }
                    } else {
                        setAction(RadioPlayer.ACTION_PLAY);
                    }
                    setIsAvailable(true);
                } else {
                    setIsAvailable(!isPlayingMyUrl); // Enable if not playing our stream
                }
                break;
            case RadioPlayer.ACTION_STOP:
                if (adaptActionAfterPlay) {
                    if (!isPlayingMyUrl(player)) {
                        setAction(RadioPlayer.ACTION_PLAY);
                    }
                }
                setIsAvailable(true);
                break;
            case RadioPlayer.ACTION_PAUSE:
                if (adaptActionAfterPlay) {
                    if (!isPlayingMyUrl(player)) {
                        setAction(RadioPlayer.ACTION_PLAY);
                    }
                }
                setIsAvailable(true);
                break;
            case RadioPlayer.ACTION_NEXT:
                setIsAvailable(true);
                break;
            case RadioPlayer.ACTION_PREVIOUS:
                setIsAvailable(true);
                break;
        }
    }

    @Override
    public void OnStopped(RadioPlayer player) {
        switch (action) {
            case RadioPlayer.ACTION_PLAY:
                setIsAvailable(true);
                break;
            case RadioPlayer.ACTION_STOP:
                setIsAvailableAndPlayAction(player);
                break;
            case RadioPlayer.ACTION_PAUSE:
                setIsAvailable(false);
                break;
            case RadioPlayer.ACTION_NEXT:
                setIsAvailable(true);
                break;
            case RadioPlayer.ACTION_PREVIOUS:
                setIsAvailable(true);
                break;
        }
    }

    @Override
    public void OnPaused(RadioPlayer player) {
        switch (action) {
            case RadioPlayer.ACTION_PLAY:
                setIsAvailable(true);
                break;
            case RadioPlayer.ACTION_STOP:
                setIsAvailable(true);
                break;
            case RadioPlayer.ACTION_PAUSE:
                setIsAvailableAndPlayAction(player);
                break;
            case RadioPlayer.ACTION_NEXT:
                setIsAvailable(true);
                break;
            case RadioPlayer.ACTION_PREVIOUS:
                setIsAvailable(true);
                break;
        }
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    private void setIsAvailable(boolean isAvailable) {
        this.isAvailable = isAvailable;
        postInvalidate(); // Redraw view next frame
    }

    private void setIsAvailableAndPlayAction(RadioPlayer player) {
        if (adaptActionAfterPlay) {
            boolean isPlayingMyUrl = isPlayingMyUrl(player);
            if (isPlayingMyUrl) {
                setAction(RadioPlayer.ACTION_PLAY);
            }
            setIsAvailable(isPlayingMyUrl);
        } else {
            setIsAvailable(false);
        }
    }

    private boolean isPlayingMyUrl(RadioPlayer player) {
        String playerUrl;
        if (player == null) {
            playerUrl = null;
        } else {
            playerUrl = player.getUrl();
        }

        return (playerUrl != null) && (playerUrl.equals(url));
    }


}
