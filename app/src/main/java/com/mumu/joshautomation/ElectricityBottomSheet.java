package com.mumu.joshautomation;

import android.app.Dialog;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.Button;

import com.mumu.joshautomation.records.UserRecordHandler;

/**
 * ElectricityBottomSheet class holds the view of total usage data
 *
 * We must implement a maximum length of data in case this occupies too
 * many resources
 */
public class ElectricityBottomSheet extends BottomSheetDialogFragment {
    private int mSheetButtonAssistantPressed = 0;
    private ElectricityRecyclerViewAdapter mEHAdapter;

    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback
            = new BottomSheetBehavior.BottomSheetCallback() {

        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        }
    };

    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.electricity_bottom_sheet, null);
        dialog.setContentView(contentView);

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        final Button mSheetButton = (Button) contentView.findViewById(R.id.buttonSheetAssistant);
        mSheetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserRecordHandler.getHandler().setRecordInverse(
                        !UserRecordHandler.getHandler().getIsRecordInverse());
                mEHAdapter.notifyDataSetChanged();
            }
        });

        RecyclerView mEHRecycler = (RecyclerView) contentView.findViewById(R.id.recyclerViewElectricityList);
        StaggeredGridLayoutManager mSGLM = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        mEHRecycler.setLayoutManager(mSGLM);

        //  Setup Adapter & DataSet //
        mEHAdapter = new ElectricityRecyclerViewAdapter();
        mEHRecycler.setAdapter(mEHAdapter);

        if (behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }
    }
}
