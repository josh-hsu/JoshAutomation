package com.mumu.android.joshautomation.scripts.claudia;

import android.util.Log;

import com.mumu.android.joshautomation.autojob.AutoJob;
import com.mumu.android.joshautomation.autojob.AutoJobEventListener;
import com.mumu.android.joshautomation.content.AppPreferenceValue;
import com.mumu.libjoshgame.GameLibrary20;
import com.mumu.libjoshgame.ScreenPoint;

public class ClaudiaAutoEventJob extends AutoJob {
    private static final String TAG = "ClaudiaAutoJob";
    private MainJobRoutine mRoutine;
    private GameLibrary20 mGL;
    private ClaudiaRoutine mClaudia;
    private AutoJobEventListener mListener;

    public ClaudiaAutoEventJob() {
        super(TAG);
    }

    /*
     * start
     * called by AutoJobHandler to start MainJobRoutine
     */
    @Override
    public void start() {
        super.start();
        Log.d(TAG, "starting job " + getJobName());
        mClaudia = new ClaudiaRoutine(mGL, mListener);
        mGL.useHardwareSimulatedInput(false);
        mRoutine = null;
        mRoutine = new MainJobRoutine();
        mRoutine.start();
    }

    /*
     * stop
     * called by AutoJobHandler to stop MainJobRoutine
     */
    @Override
    public void stop() {
        super.stop();
        Log.d(TAG, "stopping job " + getJobName());

        mRoutine.interrupt();
    }

    /*
     * setExtra
     * called by caller to set any data to you
     */
    @Override
    public void setExtra(Object object) {
        if (object instanceof GameLibrary20)
            mGL = (GameLibrary20) object;
    }

    /*
     * setJobEventListener
     * called by caller to receiver your message
     */
    public void setJobEventListener(AutoJobEventListener el) {
        mListener = el;
    }

    /*
     * SendEvent
     * Your can send anything back to caller whoever register listener
     */
    private void sendEvent(String msg, Object extra) {
        if (mListener != null) {
            mListener.onMessageReceived(msg, extra);
        } else {
            Log.w(TAG, "There is no event listener registered.");
        }
    }

    private void sendMessage(String msg) {
        Log.d(TAG, msg);
        sendEvent(msg, this);
    }

    /*
     * MainJobRoutine
     * Your script implementation should be here
     */
    private class MainJobRoutine extends Thread {

        private void main() throws Exception {
            boolean shouldRunning = true;
            int battleCount = Integer.parseInt(AppPreferenceValue.getInstance().getPrefs().getString("claudiaPerfBattleCount", "1000"));
            boolean battleUseMH = AppPreferenceValue.getInstance().getPrefs().getBoolean("claudiaPerfBattleUseMonsterHunter", true);
            boolean battleUseMHFriend = AppPreferenceValue.getInstance().getPrefs().getBoolean("claudiaPerfBattleUseMonsterHunterFriend", true);
            boolean battleUseGemSupply = AppPreferenceValue.getInstance().getPrefs().getBoolean("claudiaPerfBattleUseGemSupply", false);

            while (shouldRunning) {
                // setup gl for game spec
                mGL.setScreenMainOrientation(ScreenPoint.SO_Portrait);
                mGL.useHardwareSimulatedInput(false);
                mGL.setScreenAmbiguousRange(new int[]{25,25,25});

                sendMessage("開始識別");
                while (battleCount > 0) {
                    switch (mClaudia.getCurrentStage()) {
                        case ClaudiaRoutine.STAGE_IN_GO_BATTLE:
                            sendMessage("GO");
                            mClaudia.preBattle();
                            break;
                        case ClaudiaRoutine.STAGE_IN_BATTLE:
                            sendMessage("戰鬥");
                            mClaudia.battleRoutine(true, battleUseMH);
                            battleCount--;
                            break;
                        case ClaudiaRoutine.STAGE_IN_BATTLE_RESULT:
                            sendMessage("結果");
                            mClaudia.postBattle();
                            break;
                        case ClaudiaRoutine.STAGE_IN_RESULT_EVENT:
                            sendMessage("結果新石記");
                            mClaudia.eventResult();
                            break;
                        case ClaudiaRoutine.STAGE_IN_SELECT_FRIEND:
                            sendMessage("選朋友畫面");
                            mClaudia.preBattleSelectFriend(battleUseMHFriend);
                            break;
                        case ClaudiaRoutine.STAGE_IN_BATTLE_AGAIN:
                            sendMessage("再戰畫面");
                            mClaudia.battleAgain();
                            break;
                        case ClaudiaRoutine.STAGE_IN_NETWORK_ERROR:
                            sendMessage("網路錯誤");
                            mClaudia.handleNetworkError();
                            break;
                        case ClaudiaRoutine.STAGE_IN_FRIEND_REQUEST:
                            sendMessage("請求朋友");
                            mClaudia.handleFriendRequest();
                            break;
                        case ClaudiaRoutine.STAGE_IN_GEM_SUPPLY:
                            sendMessage("寶玉判斷");
                            mClaudia.handleGemSupply(battleUseGemSupply);
                            break;
                    }
                    sleep(500);
                }

                shouldRunning = false;
                mListener.onJobDone(TAG);
            }
        }

        public void run() {
            try {
                main();
            } catch (Exception e) {
                Log.e(TAG, "Routine caught an exception " + e.getMessage());
            }
        }
    }
}
