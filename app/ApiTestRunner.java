package com.itsec.jniapitester;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
public class ApiTestRunner extends IntentService {
    private static final String TAG = "APITestRunner";
    public static final String PARAM_IN_ITERATIONS = "iterations";
    public static final String PARAM_IN_TESTS = "tests";
    public static final String PARAM_IN_TESTSIZE = "testsize";
    public static final String PARAM_OUT_PROGRESS = "progress";
    private long iterations;
    private boolean[] toRun;
    private int toRunSize;
    public ApiTestRunner() {
        super("ApiTestRunner");
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        iterations = intent.getLongExtra(PARAM_IN_ITERATIONS, 1);
        toRun = intent.getBooleanArrayExtra(PARAM_IN_TESTS);
        toRunSize = (int)intent.getLongExtra(PARAM_IN_TESTSIZE, 12);
        String selection = "";
        for (boolean b : toRun)
            selection += b + ", ";
        triggerTestCharField(iterations);
    }
    public void triggerTestCharField(long iterations) {
        TestObject to = new TestObject();
        TestResult result = new TestResult();
        long time = testGetCharField(to, to.charField, iterations, result);
        long millisec = result.nanoseconds / 1000000L;
        long nanosecs = result.nanoseconds - millisec * 1000000L;
        long microsec = nanosecs / 1000L;
        nanosecs = nanosecs - microsec * 1000L;
        Log.d(TAG, "TIME TO RUN testGetCharField: "+result.seconds+"s "+result.nanoseconds+"ns");
        Log.d(TAG, "Millisecs: "+millisec);
        Log.d(TAG, "Microsecs: "+microsec);
        Log.d(TAG, "Nanosecs: "+nanosecs);
    }
        private native int testGetCharField(Object o, char result, long iterations, TestResult time);
    static {
        System.loadLibrary("apitester");
    }
}
