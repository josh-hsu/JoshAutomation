package com.mumu.joshautomation;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mumu.joshautomation.records.UserRecordHandler;

class ElectricityRecyclerViewAdapter
        extends RecyclerView.Adapter<ElectricityRecyclerViewAdapter.ViewHolder>
        implements View.OnClickListener {

    private static final String TAG = "ProjectLI";
    private int expandedPosition = -1;
    private UserRecordHandler mRecordHandler;
    private static int mLastRestoreAccountIndex = -1;

    ElectricityRecyclerViewAdapter () {
        mRecordHandler = UserRecordHandler.getHandler();

        if (!mRecordHandler.getAvailable())
            Log.e(TAG, "RecyclerView: record handler is not available");
    }

    @Override
    public ElectricityRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Create a new view by inflating the row item xml.
        View v = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.electricity_card_cell, parent, false);

        ViewHolder holder = new ViewHolder(v);

        // Sets the click adapter for the entire cell
        // to the one in this class.
        holder.itemView.setOnClickListener(ElectricityRecyclerViewAdapter.this);
        holder.itemView.setTag(holder);

        return holder;
    }

    @Override
    public int getItemCount() {
        return mRecordHandler.getCount();
    }

    @Override
    public void onBindViewHolder(ElectricityRecyclerViewAdapter.ViewHolder holder, int pos) {
        final int position = holder.getAdapterPosition();
        int serialNum = mRecordHandler.getSerialNum(position);

        holder.recordText.setText(mRecordHandler.getTitle(position));
        //holder.dateText.setText(mRecordHandler.getDateFormatted(position));

        holder.restoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Context sContext = view.getContext();
                AlertDialog.Builder ad = new AlertDialog.Builder(sContext, R.style.MyDialogStyle)
                        .setTitle(R.string.outline_restore_fgo_title)
                        .setMessage(R.string.outline_restore_fgo_msg)
                        .setPositiveButton(R.string.action_confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    String thisAccount = mRecordHandler.getRecord(position);
                                    mLastRestoreAccountIndex = position;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                        .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                ad.show();
            }
        });

        holder.updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Context sContext = view.getContext();
                AlertDialog.Builder ad = new AlertDialog.Builder(sContext, R.style.MyDialogStyle);
                ad.setTitle(R.string.outline_update_fgo_title);

                if (mLastRestoreAccountIndex == position) {
                    ad.setMessage(R.string.outline_update_fgo_msg);
                    ad.setPositiveButton(R.string.action_confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                String thisAccount = mRecordHandler.getRecord(position);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } else {
                    String warningText;
                    if (mLastRestoreAccountIndex == -1) {
                        warningText = sContext.getString(R.string.outline_update_fgo_msg_forbid3) +
                                sContext.getString(R.string.outline_update_fgo_msg_forbid2);
                    } else {
                        warningText = sContext.getString(R.string.outline_update_fgo_msg_forbid1) +
                                mRecordHandler.getTitle(mLastRestoreAccountIndex) +
                                sContext.getString(R.string.outline_update_fgo_msg_forbid2) +
                                mRecordHandler.getTitle(position);
                    }
                    ad.setMessage(warningText);
                }

                ad.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                ad.show();
            }
        });

        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(), "Not ready", Toast.LENGTH_SHORT).show();
            }
        });

        if (serialNum == -1)
            holder.incrementText.setText("1");
        else
            holder.incrementText.setText(""+serialNum);

        if (position == expandedPosition) {
            holder.llExpandArea.setVisibility(View.VISIBLE);
        } else {
            holder.llExpandArea.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        ViewHolder holder = (ViewHolder) view.getTag();
        int prev;

        // Check for an expanded view, collapse if you find one
        if (expandedPosition >= 0) {
            prev = expandedPosition;
            notifyItemChanged(prev);
        }

        // Set the current position to "expanded"
        if (expandedPosition == holder.getAdapterPosition())
            expandedPosition = -1;
        else
            expandedPosition = holder.getAdapterPosition();

        notifyItemChanged(holder.getAdapterPosition());
    }

    /**
     * Create a ViewHolder to represent your cell layout
     * and data element structure
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView recordText/*, dateText*/, incrementText;
        Button restoreButton, updateButton, deleteButton;
        LinearLayout llExpandArea;

        public ViewHolder(View itemView) {
            super(itemView);

            recordText = (TextView) itemView.findViewById(R.id.textViewAccountTitle);
            //dateText = (TextView) itemView.findViewById(R.id.textViewDateTitle);
            incrementText = (TextView) itemView.findViewById(R.id.textViewSerial);
            llExpandArea = (LinearLayout) itemView.findViewById(R.id.llExpandArea);
            restoreButton = (Button) itemView.findViewById(R.id.btn_restore_account);
            updateButton = (Button) itemView.findViewById(R.id.btn_update);
            deleteButton = (Button) itemView.findViewById(R.id.btn_delete);
        }
    }
}
