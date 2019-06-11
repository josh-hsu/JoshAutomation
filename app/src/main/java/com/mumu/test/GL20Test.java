package com.mumu.test;

import android.content.Context;

import com.mumu.libjoshgame.GameDevice;
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

    private ArrayList<TestCase> mTestCases;

    private TestCase testGL20GetResolution = new TestCase() {
        @Override public String name() {return "testGL20GetResolution";}
        @Override public int runTest(Object arg) {
            if (arg instanceof GameLibrary20) {
                GameLibrary20 gl = (GameLibrary20) arg;
                try {
                    int[] resolution = gl.getDeviceResolution();
                    if (resolution == null)
                        return TEST_FAIL_GL_RETURN_NOT_EXPECTED;
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
            if (arg instanceof GameLibrary20) {
                GameLibrary20 gl = (GameLibrary20) arg;
                try {
                    int sysType = gl.getDeviceSystemType();
                    if (sysType < 0)
                        return TEST_FAIL_GL_RETURN_NOT_EXPECTED;
                    else if (sysType != GameDevice.DEVICE_SYS_WINDOWS &&
                            sysType != GameDevice.DEVICE_SYS_LINUX &&
                            sysType != GameDevice.DEVICE_SYS_DARWIN)
                        return TEST_FAIL_GL_RETURN_NOT_EXPECTED;
                } catch (GameLibrary20.DeviceNotInitializedException e) {
                    return TEST_FAIL_GL_DEV_NOT_INIT;
                }
                return TEST_PASS;
            }
            return TEST_FAIL_INTERNAL_ILLEGAL;
        }
    };

    private void prepareTestCases(GL20Test self) {
        self.mTestCases = new ArrayList<>();
        self.mTestCases.add(testGL20GetResolution);
        self.mTestCases.add(testGL20GetDeviceName);
        self.mTestCases.add(testGL20GetSystemType);
    }

    //
    // Test main and interface utility
    //
    public static void main(String[] args) {
        Log.d("GL20 TestCases preparing ...");

        GL20Test mTest = new GL20Test();
        mTest.prepareTestCases(mTest);

        //GameLibrary20 mGL = mTest.testOnGL20AndroidInit();
        GameLibrary20 mGL = mTest.testOnGL20NoxInit();
        if (mGL == null) {
            Log.f("Fatal Exception, could not initial GameLibrary");
        }

        Log.d("GL20 TestCases starting  ...");
        mTest.runAllTestCases(mTest, mGL);
        Log.d("GL20 TestCases end       ...");

    }

    private void runAllTestCases(GL20Test self, GameLibrary20 gl) {
        int rc;
        Log.i("Found total " + self.mTestCases.size() + " tests");
        for (int i = 0; i < self.mTestCases.size(); i++) {
            TestCase test = self.mTestCases.get(i);

            if (test == null) {
                Log.w("test null ???");
                continue;
            }

            Log.t("  [" + i + "] " + test.name() + "");
            rc = test.runTest(gl);
            if (rc == TEST_PASS) {
                Log.r("           PASS");
            } else {
                Log.r("           FAIL          failure code: " + rc);
            }
        }
    }

    private GameLibrary20 testOnGL20AndroidInit() {
        int ret;
        GameLibrary20 mGL = new GameLibrary20();

        ret = mGL.chooseDevice(GameLibrary20.DEVICE_TYPE_ANDROID_INTERNAL);
        if (ret < 0) {
            Log.e("Device is not here?");
            return null;
        }

        ret = mGL.setDeviceEssentials(null);
        if (ret < 0) {
            Log.e("Set device essentials failed");
            return null;
        }

        ret = mGL.initDevice(prepareInitAndroidInternal());
        if (ret < 0) {
            Log.e("Initial AndroidInternal failed");
            return null;
        }

        return mGL;
    }

    private GameLibrary20 testOnGL20NoxInit() {
        int ret;
        GameLibrary20 mGL = new GameLibrary20();

        ret = mGL.chooseDevice(GameLibrary20.DEVICE_TYPE_NOX_PLAYER);
        if (ret < 0) {
            Log.e("Device is not here?");
            return null;
        }

        ret = mGL.setDeviceEssentials(null);
        if (ret < 0) {
            Log.e("Set device essentials failed");
            return null;
        }

        ret = mGL.initDevice(null);
        if (ret < 0) {
            Log.e("Initial AndroidInternal failed");
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
        static void t(String log) { System.out.print(log);}
        static void r(String log) { System.out.println(log);}

        static void d(String log) { System.out.println("D: " + log);}
        static void e(String log) { System.out.println("E: " + log);}
        static void w(String log) { System.out.println("W: " + log);}
        static void i(String log) { System.out.println("I: " + log);}
        static void f(String log) { System.out.println("F: " + log); System.exit(TEST_FAIL_INTERNAL_FATAL);}
    }
}
