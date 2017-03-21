package com.mumu.joshautomation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.mumu.joshautomation.records.UserRecordHandler;
import com.mumu.joshautomation.records.UserRecordParser;
import com.mumu.joshautomation.script.FGOJobHandler;
import com.mumu.joshautomation.script.JobEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class OutlineFragment extends MainFragment implements JobEventListener {
    private static final String TAG = "JATool";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private FGOJobHandler mFGOJobs;

    // Data Holder
    private UserRecordHandler mRecordHandler;

    private Button mRunJoshCmdButton;
    private Button mScreenCaptureButton;
    private TextView mAccountNumText;

    private OnFragmentInteractionListener mListener;
    private final Handler mHandler = new Handler();
    final Runnable mUpdateRunnable = new Runnable() {
        public void run() {
            updateView();
        }
    };

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

        mFGOJobs = FGOJobHandler.getHandler();
        mFGOJobs.setJobEventListener(FGOJobHandler.AUTO_TRAVERSE_JOB, this);
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
        prepareData();
        updateView();
    }

    @Override
    public void onDetailClick() {
        Log.d(TAG, "Detail click on electricity fragment");
        showBottomSheet();
    }

    private void prepareView(View view) {
        mRunJoshCmdButton = (Button) view.findViewById(R.id.button_test_game);
        mAccountNumText = (TextView) view.findViewById(R.id.textViewAccountNum);
        mScreenCaptureButton = (Button) view.findViewById(R.id.button_screenshot);

        mRunJoshCmdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runAutoLoginRoutine();
            }
        });

        mScreenCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startChatHeadService();
            }
        });
    }

    private void prepareData() {
        mRecordHandler = UserRecordHandler.getHandler();
        mRecordHandler.initOnce(getActivity().getResources(), getActivity().getFilesDir().getAbsolutePath());
    }

    /*
     * updateView will be called when mUpdateRunnable is triggered
     */
    private void updateView() {
        String accountNumText = getString(R.string.outline_account_num);
        accountNumText = accountNumText + " " + mRecordHandler.getCount();

        mAccountNumText.setText(accountNumText);
        mRunJoshCmdButton.setText(R.string.outline_list_all_scripts);
    }

    private void showBottomSheet() {
        SheetBottomSheet ebs = new SheetBottomSheet();
        ebs.show(getFragmentManager(), ebs.getTag());
    }

    private void runAutoLoginRoutine() {
        mFGOJobs.setExtra(FGOJobHandler.AUTO_TRAVERSE_JOB, UserRecordHandler.getHandler());
        mFGOJobs.startJob(FGOJobHandler.AUTO_TRAVERSE_JOB);
        mFGOJobs.stopJob(FGOJobHandler.AUTO_TRAVERSE_JOB);
    }

    @Override
    public void onEventReceived(String msg, Object extra) {
        Log.d(TAG, "Message Received " + msg);
        mHandler.post(mUpdateRunnable);
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    private void startChatHeadService() {
        if (Build.VERSION.SDK_INT >= 23) {
            Toast.makeText(getContext(), R.string.startup_permit_system_alarm, Toast.LENGTH_SHORT).show();
            if (!Settings.canDrawOverlays(getContext())) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getContext().getPackageName()));
                startActivityForResult(intent, 10);
                Log.d(TAG, "No permission for drawing on screen, prompt one.");
            } else {
                Toast.makeText(getContext(), R.string.headservice_how_to_stop, Toast.LENGTH_SHORT).show();
                getContext().startService(new Intent(getContext(), HeadService.class));
                returnHomeScreen();
            }
        } else {
            Log.d(TAG, "Permission granted, starting service.");
            Toast.makeText(getContext(), R.string.headservice_how_to_stop, Toast.LENGTH_SHORT).show();
            getContext().startService(new Intent(getContext(), HeadService.class));
            returnHomeScreen();
        }
    }

    private void returnHomeScreen() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    /*
     *  Add record
     */
    private void showAddDialog() {
        new MaterialDialog.Builder(getContext())
                .title(getString(R.string.electric_add))
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input(getString(R.string.electric_add_field_holder), mRecordHandler.getTitle(0), new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        Log.d(TAG, "Get input " + input);
                        try {
                            String nextSerial = mRecordHandler.getNextSerial();
                            addNewRecordFromUser("account" + nextSerial, "NOW", input.toString());
                            updateView();
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                }).negativeText(getString(R.string.electric_add_cancel)).show();
    }

    private int addNewRecordFromUser(String record, String date, String title) {
        String targetDate;

        if (date.equals("NOW")) {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US);
            targetDate = df.format(Calendar.getInstance().getTime());
        } else {
            targetDate = date;
        }

        try {
            mRecordHandler.addRecord(new UserRecordParser.Entry(mRecordHandler.getNextSerial(), targetDate, record, title));
            mRecordHandler.refreshFromFile();
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Fail to add record " + e.getMessage());
        }

        return -1;
    }
}
