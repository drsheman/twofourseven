package com.molamil.radio24syv;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import com.molamil.radio24syv.managers.LiveContentUpdater;
import com.molamil.radio24syv.player.RadioPlayer;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class MainFragment extends Fragment {

    public static final String TAG_TAB_LIVE = "LiveTab";
    public static final String TAG_TAB_PROGRAMS = "ProgramsTab";
    public static final String TAG_TAB_NEWS = "NewsTab";
    public static final String TAG_TAB_OFFLINE = "OfflineTab";

    public enum Dimming { NONE, DIM }
    static final long DIMMING_DURATION_MS = 500;
    Dimming dimming = Dimming.NONE;

    public enum TabSize { NORMAL, SMALL }
    TabSize tabSize = TabSize.NORMAL;
    ArrayList<ImageView> tabIcons;

    OnMainFragmentInteractionListener listener;
    FragmentTabHost tabHost;
    PlayerFragment playerFragment;

    String startupTabTag = TAG_TAB_LIVE;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setupPlayerFragment(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);

        tabHost = (FragmentTabHost)v.findViewById(android.R.id.tabhost);
        tabHost.setup(inflater.getContext(), getChildFragmentManager(), android.R.id.tabcontent);

        addTab(inflater, TAG_TAB_LIVE, R.string.tab_live, R.drawable.tab_icon_live, LiveFragment.class);
        addTab(inflater, TAG_TAB_PROGRAMS, R.string.tab_programs, R.drawable.tab_icon_podcasts, ProgramsFragment.class);
        addTab(inflater, TAG_TAB_NEWS, R.string.tab_news, R.drawable.tab_icon_news, NewsFragment.class);
        addTab(inflater, TAG_TAB_OFFLINE, R.string.tab_offline, R.drawable.tab_icon_offline, OfflineFragment.class);

        tabHost.setCurrentTabByTag(startupTabTag);

        tabHost.getTabWidget().setDividerDrawable(null);
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                if (listener != null) {
                    //Do Manual alpha on images in tab bar
                    listener.onMainTabChanged(tabId);
                }
            }
        });

        updateTabSize();

        //setupPlayerFragment(savedInstanceState);

        View dimmer = v.findViewById(R.id.dimmer);
        dimmer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDimming(Dimming.NONE); // Remove dimming when clicked. It is only clickable when dimmed.
            }
        });
        updateDimming(v, false);

        return v;
    }

    private void setupPlayerFragment(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            playerFragment = (PlayerFragment) getChildFragmentManager().findFragmentByTag(PlayerFragment.class.getName());
        }
        if (playerFragment == null) {
            playerFragment = new PlayerFragment();
        }

        // BUG: This is causing a crash when reopening app after it has been closed while the player is playing downloaded episode (podcast, live and news works fine)
        // java.lang.IllegalStateException: Activity has been destroyed
        // JJJ: Reproduce = Start app, play podcast (downloaded), exit with physical back button, crashes when relaunched
        getChildFragmentManager().beginTransaction().replace(R.id.player_fragment_container, playerFragment, PlayerFragment.class.getName()).commitAllowingStateLoss();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (OnMainFragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnMainFragmentInteractionListener");
        }

        try {
            RadioPlayer.RadioPlayerProvider radioPlayerProvider = (RadioPlayer.RadioPlayerProvider) context;
            LiveContentUpdater.getInstance().setRadioPlayerProvider(radioPlayerProvider);
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement PlayerFragment.RadioPlayerProvider");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;

        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();

        LiveContentUpdater.getInstance().stop();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        LiveContentUpdater.getInstance().start();
    }

    private void addTab(LayoutInflater inflater, String tag, int textId, int iconId, Class<?> fragment) {
        View indicator = inflater.inflate(R.layout.view_tab_indicator, tabHost.getTabWidget(), false);
        ((ImageView) indicator.findViewById(R.id.tab_indicator_icon)).setImageResource(iconId);
        ((TextView) indicator.findViewById(R.id.tab_indicator_text)).setText(getResources().getText(textId));

        tabHost.addTab(tabHost.newTabSpec(tag).setIndicator(indicator), fragment, null);
    }

    public void setDimming(Dimming dimming) {
        //Log.d("JJJ", "main setdimming " + dimming + " was " + this.dimming);
        if (dimming == this.dimming) {
            return; // Return, already dimmed like that
        }

        Dimming oldDimming = this.dimming;
        this.dimming = dimming;

        updateDimming(getView(), true);

        if (listener != null) {
            listener.onDimmingChanged(dimming, oldDimming);
        }
    }

    private void updateDimming(View parentView, boolean isAnimated) {
        if(parentView == null)
        {
            return;
        }
        View dimmer = parentView.findViewById(R.id.dimmer);

        long targetAlpha;
        if (dimming == Dimming.NONE) {
            targetAlpha = 0; // No dimming
            dimmer.setClickable(false);
        } else {
            targetAlpha = 1; // Full dimming
            dimmer.setClickable(true);
        }

        //Log.d("JJJ", "dim from " + dimmer.getAlpha() + " to " + targetAlpha);
        if (isAnimated) {
            dimmer.animate().alpha(targetAlpha).setDuration(DIMMING_DURATION_MS);
        } else {
            dimmer.setAlpha(targetAlpha);
        }
    }

    public void setTabSize(TabSize tabSize) {
        if (tabSize == this.tabSize) {
            return; // Return, already sized like that
        }

        this.tabSize = tabSize;
        updateTabSize();
    }

    private void updateTabSize() {
        int iconVisibility;
        if (tabSize == TabSize.NORMAL) {
            iconVisibility = View.VISIBLE; // Show icon for normal tab size
        } else {
            iconVisibility = View.GONE; // Hide icon for small tab size
        }

        TabWidget tabs = tabHost.getTabWidget();
        for (int i = 0; i < tabs.getTabCount(); i++) {
            View indicator = tabs.getChildTabViewAt(i);
            indicator.findViewById(R.id.tab_indicator_icon).setVisibility(iconVisibility);
        }
    }

    public void setStartupTab(String startupTabTag) {
        this.startupTabTag = startupTabTag;
    }

    public void setError(String message) {
        View v = getView();
        if (v != null) {
            TextView error = (TextView) v.findViewById(R.id.error_text);
            if ((message != null) && !message.equals("")) {
                message = getResources().getString(R.string.error_generic); // Replace with user-friendly message instead of too-technical error message. TODO meaningful error messages (and check internet connection)
                error.setText(message);
                error.setVisibility(View.VISIBLE);
            } else {
                error.setVisibility(View.GONE);
            }
        } else {
            Log.e("JJJ", "Unable to show error message because I have no view yet: " + message); // TODO store error message in variable and update ErrorView on onCreateView according to variable contents
        }
    }

    public interface OnMainFragmentInteractionListener {
        public void onMainTabChanged(String tabTag);
        public void onDimmingChanged(Dimming newDimming, Dimming oldDimming);
    }

    public PlayerFragment getPlayerFragment() {
        return playerFragment;
    }
}
