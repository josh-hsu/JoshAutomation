package com.mumu.test;

import android.content.Context;

import com.mumu.libjoshgame.GameLibrary20;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class is used for function test of GL20+
 */
public class GL20Test {
    private final static int TEST_FAIL_INTERNAL_FATAL = -100;
    private final static int TEST_FAIL_INTERNAL_ILLEGAL = -99;
    private final static int TEST_FAIL_GL_RETURN_NOT_EXPECTED = -2;
    private final static int TEST_FAIL_GL_DEV_NOT_INIT = -1;
    private final static int TEST_PASS = 0;

    private ArrayList<TestCase> mTestCases = new ArrayList<TestCase>() {{
        add(testGL20GetResolution);
        add(testGL20GetDeviceName);
        add(testGL20GetSystemType);
    }};

    private TestCase testGL20GetResolution = new TestCase() {
        @Override public String name() {return "testGL20GetResolution";}
        @Override public int runTest(Object arg) {
            if (arg instanceof GameLibrary20) {
                GameLibrary20 gl = (GameLibrary20) arg;
                try {
                    int[] resolution = gl.getDeviceResolution();
                    if (resolution.length != 2)
                        return TEST_FAIL_GL_RETURN_NOT_EXPECTED;
                } catch (GameLibrary20.DeviceNotInitializedException e) {
                    return TEST_FAIL_GL_DEV_NOT_INIT;
                }
                return TEST_PASS;
            }
            return TEST_FAIL_INTERNAL_ILLEGAL;
        }
    };

    private TestCase testGL20GetDeviceName = new TestCase() {
        @Override public String name() {return "testGL20GetDeviceName";}
        @Override public int runTest(Object arg) {
            if (arg instanceof GameLibrary20) {
                GameLibrary20 gl = (GameLibrary20) arg;
                try {
                    String name = gl.getDeviceName();
                    if (name == null || name.equals(""))
                        return TEST_FAIL_GL_RETURN_NOT_EXPECTED;
                } catch (GameLibrary20.DeviceNotInitializedException e) {
                    return TEST_FAIL_GL_DEV_NOT_INIT;
                }
                return TEST_PASS;
            }
            return TEST_FAIL_INTERNAL_ILLEGAL;
        }
    };

    private TestCase testGL20GetSystemType = new TestCase() {
        @Override public String name() {return "testGL20GetSystemType";}
        @Override public int runTest(Object arg) {
            return TEST_PASS;
        }
    };

    //
    // Test main and interface utility
    //
    public static void main(String[] args) {
        Log.d("GL20 TestCases preparing ...");

        GL20Test mTest = new GL20Test();
        GameLibrary20 mGL = mTest.testOnGL20AndroidInit();
        if (mGL == null) {
            Log.f("Fatal Exception, could not initial GameLibrary");
        }

        Log.d("GL20 TestCases starting  ...");
        mTest.runAllTestCases(mGL);
        Log.d("GL20 TestCases end       ...");

    }

    private void runAllTestCases(GameLibrary20 gl) {
        int rc;
        for (TestCase test : mTestCases) {
            Log.i("testing on " + test.name() + " ...");
            rc = test.runTest(gl);
            if (rc == TEST_PASS)
                Log.i("testing on " + test.name() + " PASS");
            else
                Log.i("testing on " + test.name() + " FAIL          failure code: " + rc);
        }
    }

    private GameLibrary20 testOnGL20AndroidInit() {
        int ret;
        GameLibrary20 mGL = new GameLibrary20();

        ret = mGL.chooseDevice(GameLibrary20.DEVICE_TYPE_ANDROID_INTERNAL);
        if (ret < 0) {
            System.out.println("Device is not here?");
            return null;
        }

        ret = mGL.setDeviceEssentials(null);
        if (ret < 0) {
            System.out.println("Set device essentials failed");
            return null;
        }

        ret = mGL.initDevice(prepareInitAndroidInternal());
        if (ret < 0) {
            System.out.println("Initial AndroidInternal failed");
            return null;
        }

        return mGL;
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

    private interface TestCase {
        int runTest(Object arg);
        String name();
    }

    private static class Log {
        static void d(String log) { System.out.println("D: " + log);}
        static void e(String log) { System.out.println("E: " + log);}
        static void w(String log) { System.out.println("W: " + log);}
        static void i(String log) { System.out.println("I: " + log);}
        static void f(String log) { System.out.println("F: " + log); System.exit(TEST_FAIL_INTERNAL_FATAL);}
    }
}
