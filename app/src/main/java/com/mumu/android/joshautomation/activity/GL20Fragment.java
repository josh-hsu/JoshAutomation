package com.mumu.android.joshautomation.activity;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mumu.android.joshautomation.script.AutoJob;
import com.mumu.android.joshautomation.script.AutoJobAction;
import com.mumu.android.joshautomation.script.AutoJobEventListener;
import com.mumu.android.joshautomation.script.AutoJobExample;
import com.mumu.joshautomation.MainActivity;
import com.mumu.joshautomation.MainFragment;
import com.mumu.joshautomation.R;
import com.mumu.libjoshgame.Log;

public class GL20Fragment extends MainFragment {
    private static final String TAG = "GL20Activity";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public GL20Fragment() {
        // Required empty public constructor
    }

    public static GL20Fragment newInstance(String param1, String param2) {
        GL20Fragment fragment = new GL20Fragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context context = getContext();
        AutoJobExample job = new AutoJobExample(context);
        AutoJobEventListener el = new AutoJobEventListener() {
            @Override
            public void onMessageReceived(String msg, Object extra) {
                Log.d(TAG, "MSG: " + msg);
            }

            @Override
            public void onActionReceived(int what, AutoJobAction action) {

            }

            @Override
            public void onJobDone(String jobName) {

            }
        };
        job.setJobEventListener(el);
        job.start();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_money, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onFabClick(View view) {
        Log.d(TAG, "Fab click from money");
        final Activity activity = getActivity();
        if (activity instanceof MainActivity) {
            final MainActivity deskClockActivity = (MainActivity) activity;
            deskClockActivity.showSnackBarMessage("這裡都這麼空了，你還想加什麼");
        }
    }

    @Override
    public void onDetailClick() {
        Log.d(TAG, "on detail click on money");
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
