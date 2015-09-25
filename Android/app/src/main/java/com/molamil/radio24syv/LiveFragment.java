package com.molamil.radio24syv;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.molamil.radio24syv.api.RestClient;
import com.molamil.radio24syv.api.model.Broadcast;
import com.molamil.radio24syv.view.RadioPlayerButton;

import java.util.List;

import retrofit.Callback;
import retrofit.Response;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LiveFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LiveFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LiveFragment extends PageFragment {

    private OnFragmentInteractionListener listener;
    private PlayerFragment.OnFragmentInteractionListener playerListener;
    private RadioPlayer.RadioPlayerProvider radioPlayerProvider;

    public LiveFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_live, container, false);

        Button scheduleButton = (Button)v.findViewById(R.id.schedule_button);
        scheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onShowSidePage(OnFragmentInteractionListener.Side.SHOW_RIGHT);
                }
            }
        });

        RestClient.getApi().getNextBroadcasts(1, 0).enqueue(new Callback<List<Broadcast>>() {
            @Override
            public void onResponse(Response<List<Broadcast>> response) {
                List<Broadcast> body = response.body();
                if (body != null) {
                    Broadcast b = body.get(0);
                    View v = getView();

                    if (v == null)
                        return; // We are still assigned as a callback to the previous instance of the fragment. TODO store the call in a variable, cancel it in onDestroy to getInstance rid of callbacks.
                    ((TextView) v.findViewById(R.id.program_title)).setText(b.getProgramName());
                    ((TextView) v.findViewById(R.id.program_time_begin)).setText(RestClient.getLocalTime(b.getBroadcastTime().getStart()));
                    ((TextView) v.findViewById(R.id.program_time_end)).setText(RestClient.getLocalTime(b.getBroadcastTime().getEnd()));
                    ((TextView) v.findViewById(R.id.program_category)).setText(b.getTopic());
                    ((TextView) v.findViewById(R.id.program_description)).setText(b.getDescriptionText());
                } else {
                    listener.onError(response.message());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                listener.onError(t.getLocalizedMessage());
                Log.d("JJJ", "fail " + t.getMessage());
                t.printStackTrace();
            }
        });

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        RadioPlayerButton playButton = (RadioPlayerButton)getView().findViewById(R.id.play_button);
        playButton.setRadioPlayer(radioPlayerProvider.getRadioPlayer()); // Setup play button. Must do this in onActivityCreated() to be sure our host activity is up and running.
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        try {
            playerListener = (PlayerFragment.OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement PlayerFragment.OnFragmentInteractionListener");
        }
        try {
            radioPlayerProvider = (RadioPlayer.RadioPlayerProvider) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement PlayerFragment.RadioPlayerProvider");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
        playerListener = null;
        radioPlayerProvider = null;
    }


}
