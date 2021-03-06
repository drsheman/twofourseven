package com.molamil.radio24syv.view;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.molamil.radio24syv.R;
import com.molamil.radio24syv.player.RadioPlayer;
import com.molamil.radio24syv.storage.model.ProgramInfo;
import com.molamil.radio24syv.util.DisplayUtil;

import java.util.List;

/**
 * Since the list of programs is used so many places, this class is an easy way to show such one.
 * Created by jens on 21/09/15.
 */
public class ProgramListView extends ListView {

    public ProgramListView(Activity activity, List<ProgramInfo> programs, RadioPlayer radioPlayer) {
        super(activity);

        setDivider(new ColorDrawable(getResources().getColor(R.color.transparent)));
        setDividerHeight(DisplayUtil.dpToPx(activity.getApplicationContext(), (int)getResources().getDimension(R.dimen.app_margin_half25)));

        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setAdapter(new ProgramListViewAdapter(activity, programs.toArray(new ProgramInfo[0]), radioPlayer));
//        setRecyclerListener(new RecycleHolder()); // This is never called anyway
        setListViewHeightBasedOnChildren(this); // Expand to full height when put inside a ScrollView
    }


//    public class RecycleHolder implements AbsListView.RecyclerListener {
//        @Override
//        public void onMovedToScrapHeap(final View view) {
//            ((ProgramImageView) view.findViewById(R.id.image)).setImageUrl(null);
//            Log.d("JJJ", "RecyclerListenerRecyclerListenerRecyclerListenerRecyclerListenerRecyclerListenerRecyclerListenerRecyclerListenerRecyclerListenerRecyclerListenerRecyclerListenerRecyclerListener");
//        }
//    }

    // From: http://stackoverflow.com/questions/18367522/android-list-view-inside-a-scroll-view
    /**** Method for Setting the Height of the ListView dynamically.
     **** Hack to fix the issue of not showing all the items of the ListView
     **** when placed inside a ScrollView  ****/
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }
}