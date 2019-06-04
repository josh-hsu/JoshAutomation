package com.mumu.test;

import android.content.Context;

import com.mumu.libjoshgame.GameLibrary20;

import java.util.HashMap;

/**
 * This class is used for demonstrating how developer use GL20
 */
public class GL20Test {

    public static void main(String[] args) {
        int testResult = 0;
        System.out.println("GL20 TestCases start");

        GL20Test mTest = new GL20Test();

       testResult = mTest.testOnGL20Android();
       if (testResult < 0)
           System.out.println("testOnGL20Android  ....  FAILED");
       else
           System.out.println("testOnGL20Android  ....  PASSED");
    }

    private int testOnGL20Android() {
        int ret;
        GameLibrary20 mGL = new GameLibrary20();

        ret = mGL.chooseDevice(GameLibrary20.DEVICE_TYPE_ANDROID_INTERNAL);
        if (ret < 0) {
            System.out.println("Device is not here?");
            return -1;
        }

        ret = mGL.setDeviceEssentials(null);
        if (ret < 0) {
            System.out.println("Set device essentials failed");
            return -2;
        }

        ret = mGL.initDevice(prepareInitAndroidInternal());
        if (ret < 0) {
            System.out.println("Initial AndroidInternal failed");
            return -3;
        }

        return 0;
    }

    private Object[] prepareInitAndroidInternal() {
        Context someContextFromApp = null;
        String hackSSPackageName = "com.mumu.joshautomationservice";
        String hackSSServiceName = ".CommandService";
        String hackSSIntfName = "";
        String hackSSTransactCode = "1";

        HashMap<String, String> hackSSParameters = new HashMap<>();
        hackSSParameters.put("packageName", hackSSPackageName);
        hackSSParameters.put("serviceName", hackSSServiceName);
        hackSSParameters.put("interfaceName", hackSSIntfName);
        hackSSParameters.put("hackSSTransactCode", hackSSTransactCode);

        return new Object[] {someContextFromApp, hackSSParameters};
    }
}
