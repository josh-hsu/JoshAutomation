package com.mumu.joshautomation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mumu.joshautomation.fgo.BattleArgument;
import com.mumu.joshautomation.fgo.PureBattleJob;
import com.mumu.joshautomation.script.AutoJobHandler;
import com.mumu.joshautomation.script.AutoJobEventListener;

public class OutlineFragment extends MainFragment {
    private static final String TAG = "JATool";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private Button mRunJoshCmdButton;
    private Button mStartServiceButton;
    private Button mSetParameterButton;
    private EditText mBattleParameterText;

    private OnFragmentInteractionListener mListener;

    public OutlineFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */
    public static OutlineFragment newInstance(String param1, String param2) {
        OutlineFragment fragment = new OutlineFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ontline, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onFabClick(View view) {
        Log.d(TAG, "Fab click from outline");
        final Activity activity = getActivity();
        if (activity instanceof MainActivity) {
            final MainActivity deskClockActivity = (MainActivity) activity;
            deskClockActivity.showSnackBarMessage("Test for outline");
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        prepareView(view);
        updateView();
    }

    @Override
    public void onDetailClick() {
        Log.d(TAG, "Detail click on electricity fragment");
    }

    private void prepareView(View view) {
        mRunJoshCmdButton = (Button) view.findViewById(R.id.button_test_game);
        mStartServiceButton = (Button) view.findViewById(R.id.button_start_service);
        mSetParameterButton = (Button) view.findViewById(R.id.button_set_battle_parameter);
        mBattleParameterText = (EditText) view.findViewById(R.id.edit_text_battle_parameter);

        mRunJoshCmdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        mStartServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startChatHeadService();
            }
        });

        mSetParameterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BattleArgument sBA = new BattleArgument(mBattleParameterText.getText().toString());
                AutoJobHandler.getHandler().setExtra(PureBattleJob.jobName, sBA);
            }
        });
    }

    /*
     * updateView will be called when mUpdateRunnable is triggered
     */
    private void updateView() {

    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    private void startChatHeadService() {
        if (Build.VERSION.SDK_INT >= 23) {
            //Toast.makeText(getContext(), R.string.startup_permit_system_alarm, Toast.LENGTH_SHORT).show();
            if (!Settings.canDrawOverlays(getContext())) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getContext().getPackageName()));
                startActivityForResult(intent, 10);
                Log.d(TAG, "No permission for drawing on screen, prompt one.");
            } else {
                //Toast.makeText(getContext(), R.string.headservice_how_to_stop, Toast.LENGTH_SHORT).show();
                getContext().startService(new Intent(getContext(), HeadService.class));
                //returnHomeScreen();
            }
        } else {
            Log.d(TAG, "Permission granted, starting service.");
            Toast.makeText(getContext(), R.string.headservice_how_to_stop, Toast.LENGTH_SHORT).show();
            getContext().startService(new Intent(getContext(), HeadService.class));
            //returnHomeScreen();
        }
    }

    private void returnHomeScreen() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

}
