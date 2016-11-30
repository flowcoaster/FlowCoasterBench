package com.itsec.jniapitester;
import android.app.Service;
import android.content.Intent;
import android.util.Log;
import android.app.Activity;
import android.os.IBinder;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.support.v4.app.NotificationCompat;
import android.app.Notification;
public class ApiTestRunner extends Service {
    public class TestThread implements Runnable {
        private int id;
        private long wait;
        public TestThread(int _id, int _wait) {
            id = _id;
            wait = _wait;
        }
        public void run() {
            doSomething(id, (id+1)*2);
        }
    }
    private static final String TAG = "APITestRunner";
    public static int testsPerCategory[] = { 3 , 2 , 12 , 1 , 7, 0, 3 , 43, 6, 2, 18, 0, 7, 7, 1};
    public static final String PARAM_IN_ITERATIONS = "iterations";
    public static final String PARAM_IN_TESTS = "tests";
    public static final String PARAM_IN_TESTSIZE = "testsize";
    public static final String PARAM_OUT_PROGRESS_TOTAL = "maxprogress";
    public static final String PARAM_OUT_PROGRESS = "progress";
    public static final String PARAM_OUT_DONE = "done";
    public File dataDir;
    private long iterations;
    private boolean[] toRun;
    private int total;
    private TestObject objectArray[] = null;
    private int numObjects = -1;
    @Override
    public void onCreate() {
        super.onCreate();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    public long progress;
    public void finish(boolean done) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(JniApiTester.ResponseReceiver.ACTION_RESP);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(PARAM_OUT_PROGRESS_TOTAL, getTotalExecutions());
        broadcastIntent.putExtra(PARAM_OUT_PROGRESS, 0L);
        broadcastIntent.putExtra(PARAM_OUT_DONE, true);
        sendBroadcast(broadcastIntent);
    }
    public void updateProgress(long p) {
        progress += p;
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(JniApiTester.ResponseReceiver.ACTION_RESP);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(PARAM_OUT_PROGRESS_TOTAL, getTotalExecutions());
        broadcastIntent.putExtra(PARAM_OUT_PROGRESS, progress);
        sendBroadcast(broadcastIntent);
        for(int i = 0; i < numObjects; i++)
            if(objectArray[i] == null)
                Log.d("WARNING", "*WARNING* objectArray contains null value at position "+i);
    }
    public long getTotalExecutions() {
        int counter = 0;
        int execs = 0;
        for(int e : testsPerCategory) {
            if(toRun[counter])
                execs += testsPerCategory[counter];
            counter++;
        }
        return execs * iterations;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
                Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.benchmark);
                Notification note = new NotificationCompat.Builder(this)
                        .setContentTitle("JNI API Benchmark")
                        .setTicker("JNI API Benchmark")
                        .setContentText("Started JNI API Benchmark")
                        .setSmallIcon(R.drawable.benchmark)
                        .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                        .build();
                startForeground(1, note);
                Date now = new Date();
                SimpleDateFormat strNow = new SimpleDateFormat("yyyyMMddHHmmss");
                dataDir = new File(getApplicationContext().getFilesDir(), strNow.format(now));
                iterations = intent.getLongExtra(PARAM_IN_ITERATIONS, 1);
                toRun = intent.getBooleanArrayExtra(PARAM_IN_TESTS);
                numObjects = 50;
                objectArray = new TestObject[numObjects];
                for (int i = 0; i < numObjects; i++)
                    objectArray[i] = new TestObject();
                progress = 0;
        Thread t = new Thread(new Runnable() {
            public void run() {
                if (toRun[0]) {
                    triggerGetClass(iterations);
                    updateProgress(iterations);
                    triggerGetMethodID(iterations);
                    updateProgress(iterations);
                    triggerGetStaticMethodID(iterations);
                    updateProgress(iterations);
                }
                if (toRun[1]) {
                    triggerGetDestroyJVM(iterations);
                    updateProgress(iterations);
                    triggerGetJNIEnv(iterations);
                    updateProgress(iterations);
                }
                if (toRun[2]) {
                    triggerNewString(iterations);
                    updateProgress(iterations);
                    triggerGetStringLength(iterations);
                    updateProgress(iterations);
                    triggerNewStringUTF(iterations);
                    updateProgress(iterations);
                    triggerGetStringUTFLength(iterations);
                    updateProgress(iterations);
                    triggerGetStringChars(iterations);
                    updateProgress(iterations);
                    triggerReleaseStringChars(iterations);
                    updateProgress(iterations);
                    triggerGetStringUTFChars(iterations);
                    updateProgress(iterations);
                    triggerReleaseStringUTFChars(iterations);
                    updateProgress(iterations);
                    triggerGetStringUTFRegion(iterations);
                    updateProgress(iterations);
                    triggerGetStringRegion(iterations);
                    updateProgress(iterations);
                    triggerGetStringCritical(iterations);
                    updateProgress(iterations);
                    triggerReleaseStringCritical(iterations);
                    updateProgress(iterations);
                }
                if (toRun[3]) {
                    triggerRegisterNatives(iterations);
                    updateProgress(iterations);
                }
                if (toRun[4]) {
                    triggerNewGlobalRef(iterations);
                    updateProgress(iterations);
                    triggerDeleteGlobalRef(iterations);
                    updateProgress(iterations);
                    triggerDeleteLocalRef(iterations);
                    updateProgress(iterations);
                    triggerIsSameObject(iterations);
                    updateProgress(iterations);
                    triggerNewLocalRef(iterations);
                    updateProgress(iterations);
                    triggerNewWeakGlobalRef(iterations);
                    updateProgress(iterations);
                    triggerDeleteWeakGlobalRef(iterations);
                    updateProgress(iterations);
                }
                if (toRun[5]) {
                }
                if (toRun[6]) {
                    triggerCallMethod(iterations);
                    updateProgress(iterations);
                    triggerCallNonvirtualMethod(iterations);
                    updateProgress(iterations);
                    triggerCallStaticMethod(iterations);
                    updateProgress(iterations);
                }
                if (toRun[7]) {
                    triggerNewObjectArray(iterations);
                    updateProgress(iterations);
                    triggerGetObjectArrayElement(iterations);
                    updateProgress(iterations);
                    triggerSetObjectArrayElement(iterations);
                    updateProgress(iterations);
                    triggerNewBooleanArray(iterations);
                    updateProgress(iterations);
                    triggerNewByteArray(iterations);
                    updateProgress(iterations);
                    triggerNewCharArray(iterations);
                    updateProgress(iterations);
                    triggerNewShortArray(iterations);
                    updateProgress(iterations);
                    triggerNewIntArray(iterations);
                    updateProgress(iterations);
                    triggerNewLongArray(iterations);
                    updateProgress(iterations);
                    triggerNewFloatArray(iterations);
                    updateProgress(iterations);
                    triggerNewDoubleArray(iterations);
                    updateProgress(iterations);
                    triggerGetBooleanArrayElements(iterations);
                    updateProgress(iterations);
                    triggerGetByteArrayElements(iterations);
                    updateProgress(iterations);
                    triggerGetCharArrayElements(iterations);
                    updateProgress(iterations);
                    triggerGetShortArrayElements(iterations);
                    updateProgress(iterations);
                    triggerGetIntArrayElements(iterations);
                    updateProgress(iterations);
                    triggerGetLongArrayElements(iterations);
                    updateProgress(iterations);
                    triggerGetFloatArrayElements(iterations);
                    updateProgress(iterations);
                    triggerGetDoubleArrayElements(iterations);
                    updateProgress(iterations);
                    triggerReleaseBooleanArrayElements(iterations);
                    updateProgress(iterations);
                    triggerReleaseByteArrayElements(iterations);
                    updateProgress(iterations);
                    triggerReleaseCharArrayElements(iterations);
                    updateProgress(iterations);
                    triggerReleaseShortArrayElements(iterations);
                    updateProgress(iterations);
                    triggerReleaseIntArrayElements(iterations);
                    updateProgress(iterations);
                    triggerReleaseLongArrayElements(iterations);
                    updateProgress(iterations);
                    triggerReleaseFloatArrayElements(iterations);
                    updateProgress(iterations);
                    triggerReleaseDoubleArrayElements(iterations);
                    updateProgress(iterations);
                    triggerGetBooleanArrayRegion(iterations);
                    updateProgress(iterations);
                    triggerGetByteArrayRegion(iterations);
                    updateProgress(iterations);
                    triggerGetCharArrayRegion(iterations);
                    updateProgress(iterations);
                    triggerGetShortArrayRegion(iterations);
                    updateProgress(iterations);
                    triggerGetIntArrayRegion(iterations);
                    updateProgress(iterations);
                    triggerGetLongArrayRegion(iterations);
                    updateProgress(iterations);
                    triggerGetFloatArrayRegion(iterations);
                    updateProgress(iterations);
                    triggerGetDoubleArrayRegion(iterations);
                    updateProgress(iterations);
                    triggerSetBooleanArrayRegion(iterations);
                    updateProgress(iterations);
                    triggerSetByteArrayRegion(iterations);
                    updateProgress(iterations);
                    triggerSetCharArrayRegion(iterations);
                    updateProgress(iterations);
                    triggerSetShortArrayRegion(iterations);
                    updateProgress(iterations);
                    triggerSetIntArrayRegion(iterations);
                    updateProgress(iterations);
                    triggerSetLongArrayRegion(iterations);
                    updateProgress(iterations);
                    triggerSetFloatArrayRegion(iterations);
                    updateProgress(iterations);
                    triggerSetDoubleArrayRegion(iterations);
                    updateProgress(iterations);
                }
                if (toRun[8]) {
                    triggerThrow(iterations);
                    updateProgress(iterations);
                    triggerThrowNew(iterations);
                    updateProgress(iterations);
                    triggerExceptionOccurred(iterations);
                    updateProgress(iterations);
                    triggerExceptionDescribe(iterations);
                    updateProgress(iterations);
                    triggerExceptionClear(iterations);
                    updateProgress(iterations);
                    triggerExceptionCheck(iterations);
                    updateProgress(iterations);
                }
                if (toRun[9]) {
                    triggerAllocObject(iterations);
                    updateProgress(iterations);
                    triggerNewObject(iterations);
                    updateProgress(iterations);
                }
                if (toRun[10]) {
                    triggerGetBooleanField(iterations);
                    updateProgress(iterations);
                    triggerGetIntField(iterations);
                    updateProgress(iterations);
                    triggerGetShortField(iterations);
                    updateProgress(iterations);
                    triggerGetCharField(iterations);
                    updateProgress(iterations);
                    triggerGetByteField(iterations);
                    updateProgress(iterations);
                    triggerGetFloatField(iterations);
                    updateProgress(iterations);
                    triggerGetDoubleField(iterations);
                    updateProgress(iterations);
                    triggerGetLongField(iterations);
                    updateProgress(iterations);
                    triggerSetBooleanField(iterations);
                    updateProgress(iterations);
                    triggerSetIntField(iterations);
                    updateProgress(iterations);
                    triggerSetShortField(iterations);
                    updateProgress(iterations);
                    triggerSetCharField(iterations);
                    updateProgress(iterations);
                    triggerSetByteField(iterations);
                    updateProgress(iterations);
                    triggerSetFloatField(iterations);
                    updateProgress(iterations);
                    triggerSetDoubleField(iterations);
                    updateProgress(iterations);
                    triggerSetLongField(iterations);
                    updateProgress(iterations);
                    triggerSetObjectField(iterations);
                    updateProgress(iterations);
                    triggerGetObjectField(iterations);
                    updateProgress(iterations);
                }
                if (toRun[11]) {
                }
                if (toRun[12]) {
                    triggerCallWithVariableArgumentSize(iterations, 1024);
                    updateProgress(iterations);
                    triggerCallWithVariableArgumentSize(iterations, 65536);
                    updateProgress(iterations);
                    triggerCallWithVariableArgumentSize(iterations, 131072);
                    updateProgress(iterations);
                    triggerCallWithVariableArgumentSize(iterations, 1310720);
                    updateProgress(iterations);
                    triggerCallWithVariableArgumentSize(iterations, 2621440);
                    updateProgress(iterations);
                    triggerCallWithVariableArgumentSize(iterations, 3932160);
                    updateProgress(iterations);
                    triggerCallWithVariableArgumentSize(iterations, 6553600);
                    updateProgress(iterations);
                }
                if (toRun[13]) {
                    triggerCallWithVariableReturnSize(iterations, 1024);
                    updateProgress(iterations);
                    triggerCallWithVariableReturnSize(iterations, 65536);
                    updateProgress(iterations);
                    triggerCallWithVariableReturnSize(iterations, 131072);
                    updateProgress(iterations);
                    triggerCallWithVariableReturnSize(iterations, 1310720);
                    updateProgress(iterations);
                    triggerCallWithVariableReturnSize(iterations, 2621440);
                    updateProgress(iterations);
                    triggerCallWithVariableReturnSize(iterations, 3932160);
                    updateProgress(iterations);
                    triggerCallWithVariableReturnSize(iterations, 6553600);
                    updateProgress(iterations);
                }
                if(toRun[14]) {
                    triggerThreadTest(5);
                    updateProgress(iterations);
                }
                finish(true);
                stopForeground(true);
            }
        });
        t.setUncaughtExceptionHandler(
                new Thread.UncaughtExceptionHandler() {
                    @Override
                    public void uncaughtException(Thread t, Throwable e) {
                        e.printStackTrace();
                    }
                });
        t.start();
        return START_STICKY;
    }
    public String printResult(TestResult result) {
        return result.seconds+"s "+
                result.nanoseconds+"ns";
    }
    public void triggerLongTest() {
        long a = 111111;
        long b = 222222;
        long[] la = {333333};
        long [] result = testLongArguments(a,la,b);
        for(int i = 0; i < 1; i++) {
            Log.d(TAG, "triggerLongTest: la[" + i + "]: " + result[i]);
        }
    }
    public void triggerAllocObject(long iterations) {
        TestResult total = new TestResult();
        testAllocObject(objectArray, numObjects, iterations, total);
        Log.d(TAG, "triggerAllocObject\t"+printResult(total));
        total.save(dataDir, "AllocObject.csv");
    }
    public void triggerNewObject(long iterations) {
        TestResult total = new TestResult();
        testNewObject(objectArray, numObjects, iterations, total);
        Log.d(TAG, "triggerNewObject\t"+printResult(total));
        total.save(dataDir, "NewObject.csv");
    }
    public void triggerThrow(long iterations) {
        TestResult total = new TestResult();
        testThrow(objectArray, numObjects, iterations, total);
        Log.d(TAG, "triggerThrow\t"+printResult(total));
        total.save(dataDir, "Throw.csv");
    }
    public void triggerThrowNew(long iterations) {
        TestResult total = new TestResult();
        testThrowNew(objectArray, numObjects, iterations, total);
        Log.d(TAG, "triggerThrowNew\t"+printResult(total));
        total.save(dataDir, "ThrowNew.csv");
    }
    public void triggerExceptionOccurred(long iterations) {
        TestResult total = new TestResult();
        testExceptionOccurred(objectArray, numObjects, iterations, total);
        Log.d(TAG, "triggerExceptionOccurred\t"+printResult(total));
        total.save(dataDir, "ExceptionOccurred.csv");
    }
    public void triggerExceptionDescribe(long iterations) {
        TestResult total = new TestResult();
        testExceptionDescribe(objectArray, numObjects, iterations, total);
        Log.d(TAG, "triggerExceptionDescribe\t"+printResult(total));
        total.save(dataDir, "ExceptionDescribe.csv");
    }
    public void triggerExceptionClear(long iterations) {
        TestResult total = new TestResult();
        testExceptionClear(objectArray, numObjects, iterations, total);
        Log.d(TAG, "triggerExceptionClear\t"+printResult(total));
        total.save(dataDir, "ExceptionClear.csv");
    }
    public void triggerExceptionCheck(long iterations) {
        TestResult total = new TestResult();
        testExceptionCheck(objectArray, numObjects, iterations, total);
        Log.d(TAG, "triggerExceptionCheck\t"+printResult(total));
        total.save(dataDir, "ExceptionCheck.csv");
    }
    public void triggerNewGlobalRef(long iterations) {
        TestResult total = new TestResult();
        testNewGlobalRef(objectArray, numObjects, iterations, total);
        Log.d(TAG, "triggerNewGlobalRef\t"+printResult(total));
        total.save(dataDir, "NewGlobalRef.csv");
    }
    public void triggerDeleteGlobalRef(long iterations) {
        TestResult total = new TestResult();
        testDeleteGlobalRef(objectArray, numObjects, iterations, total);
        Log.d(TAG, "triggerDeleteGlobalRef\t"+printResult(total));
        total.save(dataDir, "DeleteGlobalRef.csv");
    }
    public void triggerDeleteLocalRef(long iterations) {
        TestResult total = new TestResult();
        testDeleteLocalRef(objectArray, numObjects, iterations, total);
        Log.d(TAG, "triggerDeleteLocalRef\t"+printResult(total));
        total.save(dataDir, "DeleteLocalRef.csv");
    }
    public void triggerIsSameObject(long iterations) {
        TestResult total = new TestResult();
        testIsSameObject(objectArray, numObjects, iterations, total);
        Log.d(TAG, "triggerIsSameObject\t"+printResult(total));
        total.save(dataDir, "IsSameObject.csv");
    }
    public void triggerNewLocalRef(long iterations) {
        TestResult total = new TestResult();
        testNewLocalRef(objectArray, numObjects, iterations, total);
        Log.d(TAG, "triggerNewLocalRef\t"+printResult(total));
        total.save(dataDir, "NewLocalRef.csv");
    }
    public void triggerEnsureLocalCapacity(long iterations) {
        TestResult total = new TestResult();
        testEnsureLocalCapacity(objectArray, numObjects, iterations, total);
        Log.d(TAG, "triggerEnsureLocalCapacity\t"+printResult(total));
        total.save(dataDir, "EnsureLocalCapacity.csv");
    }
    public void triggerNewWeakGlobalRef(long iterations) {
        TestResult total = new TestResult();
        testNewWeakGlobalRef(objectArray, numObjects, iterations, total);
        Log.d(TAG, "triggerNewWeakGlobalRef\t"+printResult(total));
        total.save(dataDir, "NewWeakGlobalRef.csv");
    }
    public void triggerDeleteWeakGlobalRef(long iterations) {
        TestResult total = new TestResult();
        testDeleteWeakGlobalRef(objectArray, numObjects, iterations, total);
        Log.d(TAG, "triggerDeleteWeakGlobalRef\t"+printResult(total));
        total.save(dataDir, "DeleteWeakGlobalRef.csv");
    }
    public void triggerNewString(long iterations) {
        TestResult total = new TestResult();
        testNewString(iterations, total);
        Log.d(TAG, "triggerNewString\t"+printResult(total));
        total.save(dataDir, "NewString.csv");
    }
    public void triggerGetStringLength(long iterations) {
        TestResult total = new TestResult();
        testGetStringLength(iterations, total);
        Log.d(TAG, "triggerGetStringLength\t"+printResult(total));
        total.save(dataDir, "GetStringLength.csv");
    }
    public void triggerNewStringUTF(long iterations) {
        TestResult total = new TestResult();
        testNewStringUTF(iterations, total);
        Log.d(TAG, "triggerNewStringUTF\t"+printResult(total));
        total.save(dataDir, "NewStringUTF.csv");
    }
    public void triggerGetStringUTFLength(long iterations) {
        TestResult total = new TestResult();
        testGetStringUTFLength(iterations, total);
        Log.d(TAG, "triggerGetStringUTFLength\t"+printResult(total));
        total.save(dataDir, "GetStringUTFLength.csv");
    }
    public void triggerGetStringChars(long iterations) {
        TestResult total = new TestResult();
        testGetStringChars(iterations, total);
        Log.d(TAG, "triggerGetStringChars\t"+printResult(total));
        total.save(dataDir, "GetStringChars.csv");
    }
    public void triggerGetStringUTFChars(long iterations) {
        TestResult total = new TestResult();
        testGetStringUTFChars(iterations, total);
        Log.d(TAG, "triggerGetStringUTFChars\t"+printResult(total));
        total.save(dataDir, "GetStringUTFChars.csv");
    }
    public void triggerReleaseStringChars(long iterations) {
        TestResult total = new TestResult();
        testReleaseStringChars(iterations, total);
        Log.d(TAG, "triggerReleaseStringChars\t"+printResult(total));
        total.save(dataDir, "ReleaseStringChars.csv");
    }
    public void triggerGetStringCritical(long iterations) {
        TestResult total = new TestResult();
        testGetStringCritical(iterations, total);
        Log.d(TAG, "triggerGetStringCritical\t"+printResult(total));
        total.save(dataDir, "GetStringCritical.csv");
    }
    public void triggerReleaseStringCritical(long iterations) {
        TestResult total = new TestResult();
        testReleaseStringCritical(iterations, total);
        Log.d(TAG, "triggerReleaseStringCritical\t"+printResult(total));
        total.save(dataDir, "ReleaseStringCritical.csv");
    }
    public void triggerReleaseStringUTFChars(long iterations) {
        TestResult total = new TestResult();
        testReleaseStringUTFChars(iterations, total);
        Log.d(TAG, "triggerReleaseStringUTFChars\t"+printResult(total));
        total.save(dataDir, "ReleaseStringUTFChars.csv");
    }
    public void triggerGetStringRegion(long iterations) {
        TestResult total = new TestResult();
        testGetStringRegion(iterations, total);
        Log.d(TAG, "triggerGetStringRegion\t"+printResult(total));
        total.save(dataDir, "GetStringRegion.csv");
    }
    public void triggerGetStringUTFRegion(long iterations) {
        TestResult total = new TestResult();
        testGetStringUTFRegion(iterations, total);
        Log.d(TAG, "triggerGetStringUTFRegion\t"+printResult(total));
        total.save(dataDir, "GetStringUTFRegion.csv");
    }
    public void triggerGetDestroyJVM(long iterations) {
        TestResult total = new TestResult();
        testGetDestroyJVM(iterations, total);
        Log.d(TAG, "triggerGetDestroyJVM\t"+printResult(total));
        total.save(dataDir, "GetDestroyJVM.csv");
    }
    public void triggerGetJNIEnv(long iterations) {
        TestResult total = new TestResult();
        testGetJNIEnv(iterations, total);
        Log.d(TAG, "triggerGetJNIEnv\t"+printResult(total));
        total.save(dataDir, "GetJNIEnv.csv");
    }
    public void triggerGetClass(long iterations) {
        TestResult total = new TestResult();
        testGetClass(objectArray, numObjects, iterations, total);
        Log.d(TAG, "triggerGetClass\t"+printResult(total));
        total.save(dataDir, "GetClass.csv");
    }
    public void triggerGetMethodID(long iterations) {
        TestResult total = new TestResult();
        testGetMethodID(objectArray, numObjects, iterations, total);
        Log.d(TAG, "triggerGetMethodID\t"+printResult(total));
        total.save(dataDir, "GetMethodID.csv");
    }
    public void triggerGetStaticMethodID(long iterations) {
        TestResult total = new TestResult();
        testGetStaticMethodID(objectArray, numObjects, iterations, total);
        Log.d(TAG, "triggerGetStaticMethodID\t"+printResult(total));
        total.save(dataDir, "GetStaticMethodID.csv");
    }
    public void triggerRegisterNatives(long iterations) {
        TestResult total = new TestResult();
        TestObject t = new TestObject();
        testRegisterNatives(objectArray, numObjects, iterations, total);
        Log.d(TAG, "triggerRegisterNatives\t"+printResult(total));
        total.save(dataDir, "RegisterNatives.csv");
    }
    public void triggerCallMethod(long iterations) {
        TestResult total = new TestResult();
        TestObject t = new TestObject();
        testCallMethod(objectArray, numObjects, iterations, total);
        Log.d(TAG, "triggerCallMethod\t"+printResult(total));
        total.save(dataDir, "CallMethod.csv");
    }
    public void triggerCallStaticMethod(long iterations) {
        TestResult total = new TestResult();
        testCallStaticMethod(objectArray, numObjects, iterations, total);
        Log.d(TAG, "triggerCallStaticMethod\t"+printResult(total));
        total.save(dataDir, "CallStaticMethod.csv");
    }
    public void triggerCallNonvirtualMethod(long iterations) {
        TestResult total = new TestResult();
        TestObject t = new TestObject();
        ExtendedTestObject objectArray2[] = new ExtendedTestObject[numObjects];
        for(int i = 0; i < numObjects; i++)
            objectArray2[i] = new ExtendedTestObject();
        testCallNonvirtualMethod(objectArray2, numObjects, iterations, total);
        Log.d(TAG, "triggerCallNonvirtualMethod\t"+printResult(total));
        total.save(dataDir, "CallNonvirtualMethod.csv");
    }
    public void triggerCallWithVariableArgumentSize(long iterations, long args) {
        TestResult total = new TestResult();
        testCallWithVariableArgumentSize(objectArray, numObjects, args, iterations, total);
        Log.d(TAG, "triggerCallWithVariableArgumentSize"+args+"\t"+printResult(total));
        total.save(dataDir, "CallWithVariableArgumentSize_"+args+".csv");
    }
    public void triggerCallWithVariableReturnSize(long iterations, long returns) {
        TestResult total = new TestResult();
        testCallWithVariableArgumentSize(objectArray, numObjects, (int)returns, iterations, total);
        Log.d(TAG, "triggerCallWithVariableReturnSize"+returns+"\t"+printResult(total));
        total.save(dataDir, "CallWithVariableReturnSize_"+returns+".csv");
    }
    public void triggerThreadTest(int threads) {
        Log.d(TAG, "starting Threads");
        int numThreads = 4;
        RunnerThread[] rt = new RunnerThread[numThreads];
        boolean useJni = false;
        for (int i=0; i<numThreads; i++) {
            rt[i] = new RunnerThread(useJni);
            if (i + 1 == numThreads/2)
                useJni = true;
        }
        Log.d(TAG, "created "+numThreads+" threads");
        for (int i=0; i<numThreads; i++) {
            rt[i].start();
        }
        Log.d(TAG, "All threads now running");
    }
    private native int doSomething(int id, int wait);
    private native long[] testLongArguments(long a, long [] la, long b);
    private native void testCallWithVariableArgumentSize(TestObject arr[], int size, long argSize, long iterations, TestResult time);
    private native void testCallWithVariableReturnSize(TestObject arr[], int size, int argSize, long iterations, TestResult time);
    private native void testAllocObject(TestObject arr[], int size, long iterations, TestResult time);
    private native void testNewObject(TestObject arr[], int size, long iterations, TestResult time);
    private native void testThrow(TestObject arr[], int size, long iterations, TestResult time);
    private native void testThrowNew(TestObject arr[], int size, long iterations, TestResult time);
    private native void testExceptionOccurred(TestObject arr[], int size, long iterations, TestResult time);
    private native void testExceptionCheck(TestObject arr[], int size, long iterations, TestResult time);
    private native void testExceptionClear(TestObject arr[], int size, long iterations, TestResult time);
    private native void testExceptionDescribe(TestObject arr[], int size, long iterations, TestResult time);
    private native void testGetDestroyJVM(long iterations, TestResult time);
    private native void testGetJNIEnv(long iterations, TestResult time);
    private native void testGetClass(TestObject arr[], int size, long iterations, TestResult time);
    private native void testGetMethodID(TestObject arr[], int size, long iterations, TestResult time);
    private native void testGetStaticMethodID(TestObject arr[], int size, long iterations, TestResult time);
    private native void testRegisterNatives(TestObject arr[], int size, long iterations, TestResult time);
    private native void testCallMethod(TestObject arr[], int size, long iterations, TestResult time);
    private native void testCallNonvirtualMethod(TestObject arr[], int size, long iterations, TestResult time);
    private native void testCallStaticMethod(TestObject arr[], int size, long iterations, TestResult time);
    private native void testNewString(long iterations, TestResult time);
    private native void testGetStringLength(long iterations, TestResult time);
    private native void testNewStringUTF(long iterations, TestResult time);
    private native void testGetStringUTFLength(long iterations, TestResult time);
    private native void testGetStringChars(long iterations, TestResult time);
    private native void testReleaseStringChars(long iterations, TestResult time);
    private native void testGetStringUTFChars(long iterations, TestResult time);
    private native void testReleaseStringUTFChars(long iterations, TestResult time);
    private native void testGetStringCritical(long iterations, TestResult time);
    private native void testReleaseStringCritical(long iterations, TestResult time);
    private native void testGetStringRegion(long iterations, TestResult time);
    private native void testGetStringUTFRegion(long iterations, TestResult time);
    private native void testNewGlobalRef(TestObject[] array, int size, long iterations, TestResult time);
    private native void testDeleteGlobalRef(TestObject[] array, int size, long iterations, TestResult time);
    private native void testDeleteLocalRef(TestObject[] array, int size, long iterations, TestResult time);
    private native void testIsSameObject(TestObject[] array, int size, long iterations, TestResult time);
    private native void testNewLocalRef(TestObject[] array, int size, long iterations, TestResult time);
    private native void testEnsureLocalCapacity(TestObject[] array, int size, long iterations, TestResult time);
    private native void testNewWeakGlobalRef(TestObject[] array, int size, long iterations, TestResult time);
    private native void testDeleteWeakGlobalRef(TestObject[] array, int size, long iterations, TestResult time);
    public void triggerGetBooleanArrayElements(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testGetBooleanArrayElements(objectArray, numObjects, iterations, result); Log.d(TAG, "triggerGet"+"Boolean"+"ArrayElements\t"+printResult(result)); }; private native void testGetBooleanArrayElements(TestObject[] array, int size, long iterations, TestResult time);
    public void triggerGetIntArrayElements(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testGetIntArrayElements(objectArray, numObjects, iterations, result); Log.d(TAG, "triggerGet"+"Int"+"ArrayElements\t"+printResult(result)); }; private native void testGetIntArrayElements(TestObject[] array, int size, long iterations, TestResult time);
    public void triggerGetShortArrayElements(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testGetShortArrayElements(objectArray, numObjects, iterations, result); Log.d(TAG, "triggerGet"+"Short"+"ArrayElements\t"+printResult(result)); }; private native void testGetShortArrayElements(TestObject[] array, int size, long iterations, TestResult time);
    public void triggerGetCharArrayElements(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testGetCharArrayElements(objectArray, numObjects, iterations, result); Log.d(TAG, "triggerGet"+"Char"+"ArrayElements\t"+printResult(result)); }; private native void testGetCharArrayElements(TestObject[] array, int size, long iterations, TestResult time);
    public void triggerGetByteArrayElements(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testGetByteArrayElements(objectArray, numObjects, iterations, result); Log.d(TAG, "triggerGet"+"Byte"+"ArrayElements\t"+printResult(result)); }; private native void testGetByteArrayElements(TestObject[] array, int size, long iterations, TestResult time);
    public void triggerGetFloatArrayElements(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testGetFloatArrayElements(objectArray, numObjects, iterations, result); Log.d(TAG, "triggerGet"+"Float"+"ArrayElements\t"+printResult(result)); }; private native void testGetFloatArrayElements(TestObject[] array, int size, long iterations, TestResult time);
    public void triggerGetDoubleArrayElements(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testGetDoubleArrayElements(objectArray, numObjects, iterations, result); Log.d(TAG, "triggerGet"+"Double"+"ArrayElements\t"+printResult(result)); }; private native void testGetDoubleArrayElements(TestObject[] array, int size, long iterations, TestResult time);
    public void triggerGetLongArrayElements(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testGetLongArrayElements(objectArray, numObjects, iterations, result); Log.d(TAG, "triggerGet"+"Long"+"ArrayElements\t"+printResult(result)); }; private native void testGetLongArrayElements(TestObject[] array, int size, long iterations, TestResult time);
    public void triggerReleaseBooleanArrayElements(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testReleaseBooleanArrayElements(objectArray, numObjects, iterations, result); Log.d(TAG, "triggerRelease"+"Boolean"+"ArrayElements\t"+printResult(result)); }; private native void testReleaseBooleanArrayElements(TestObject[] array, int size, long iterations, TestResult time);
    public void triggerReleaseIntArrayElements(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testReleaseIntArrayElements(objectArray, numObjects, iterations, result); Log.d(TAG, "triggerRelease"+"Int"+"ArrayElements\t"+printResult(result)); }; private native void testReleaseIntArrayElements(TestObject[] array, int size, long iterations, TestResult time);
    public void triggerReleaseShortArrayElements(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testReleaseShortArrayElements(objectArray, numObjects, iterations, result); Log.d(TAG, "triggerRelease"+"Short"+"ArrayElements\t"+printResult(result)); }; private native void testReleaseShortArrayElements(TestObject[] array, int size, long iterations, TestResult time);
    public void triggerReleaseCharArrayElements(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testReleaseCharArrayElements(objectArray, numObjects, iterations, result); Log.d(TAG, "triggerRelease"+"Char"+"ArrayElements\t"+printResult(result)); }; private native void testReleaseCharArrayElements(TestObject[] array, int size, long iterations, TestResult time);
    public void triggerReleaseByteArrayElements(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testReleaseByteArrayElements(objectArray, numObjects, iterations, result); Log.d(TAG, "triggerRelease"+"Byte"+"ArrayElements\t"+printResult(result)); }; private native void testReleaseByteArrayElements(TestObject[] array, int size, long iterations, TestResult time);
    public void triggerReleaseFloatArrayElements(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testReleaseFloatArrayElements(objectArray, numObjects, iterations, result); Log.d(TAG, "triggerRelease"+"Float"+"ArrayElements\t"+printResult(result)); }; private native void testReleaseFloatArrayElements(TestObject[] array, int size, long iterations, TestResult time);
    public void triggerReleaseDoubleArrayElements(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testReleaseDoubleArrayElements(objectArray, numObjects, iterations, result); Log.d(TAG, "triggerRelease"+"Double"+"ArrayElements\t"+printResult(result)); }; private native void testReleaseDoubleArrayElements(TestObject[] array, int size, long iterations, TestResult time);
    public void triggerReleaseLongArrayElements(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testReleaseLongArrayElements(objectArray, numObjects, iterations, result); Log.d(TAG, "triggerRelease"+"Long"+"ArrayElements\t"+printResult(result)); }; private native void testReleaseLongArrayElements(TestObject[] array, int size, long iterations, TestResult time);
    public void triggerGetBooleanArrayRegion(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testGetBooleanArrayRegion(objectArray, numObjects, iterations, result); Log.d(TAG, "triggerGet"+"Boolean"+"ArrayRegion\t"+printResult(result)); }; private native void testGetBooleanArrayRegion(TestObject[] array, int size, long iterations, TestResult time);
    public void triggerGetIntArrayRegion(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testGetIntArrayRegion(objectArray, numObjects, iterations, result); Log.d(TAG, "triggerGet"+"Int"+"ArrayRegion\t"+printResult(result)); }; private native void testGetIntArrayRegion(TestObject[] array, int size, long iterations, TestResult time);
    public void triggerGetShortArrayRegion(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testGetShortArrayRegion(objectArray, numObjects, iterations, result); Log.d(TAG, "triggerGet"+"Short"+"ArrayRegion\t"+printResult(result)); }; private native void testGetShortArrayRegion(TestObject[] array, int size, long iterations, TestResult time);
    public void triggerGetCharArrayRegion(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testGetCharArrayRegion(objectArray, numObjects, iterations, result); Log.d(TAG, "triggerGet"+"Char"+"ArrayRegion\t"+printResult(result)); }; private native void testGetCharArrayRegion(TestObject[] array, int size, long iterations, TestResult time);
    public void triggerGetByteArrayRegion(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testGetByteArrayRegion(objectArray, numObjects, iterations, result); Log.d(TAG, "triggerGet"+"Byte"+"ArrayRegion\t"+printResult(result)); }; private native void testGetByteArrayRegion(TestObject[] array, int size, long iterations, TestResult time);
    public void triggerGetFloatArrayRegion(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testGetFloatArrayRegion(objectArray, numObjects, iterations, result); Log.d(TAG, "triggerGet"+"Float"+"ArrayRegion\t"+printResult(result)); }; private native void testGetFloatArrayRegion(TestObject[] array, int size, long iterations, TestResult time);
    public void triggerGetDoubleArrayRegion(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testGetDoubleArrayRegion(objectArray, numObjects, iterations, result); Log.d(TAG, "triggerGet"+"Double"+"ArrayRegion\t"+printResult(result)); }; private native void testGetDoubleArrayRegion(TestObject[] array, int size, long iterations, TestResult time);
    public void triggerGetLongArrayRegion(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testGetLongArrayRegion(objectArray, numObjects, iterations, result); Log.d(TAG, "triggerGet"+"Long"+"ArrayRegion\t"+printResult(result)); }; private native void testGetLongArrayRegion(TestObject[] array, int size, long iterations, TestResult time);
    public void triggerSetBooleanArrayRegion(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testSetBooleanArrayRegion(objectArray, numObjects, iterations, result); Log.d(TAG, "triggerSet"+"Boolean"+"ArrayRegion\t"+printResult(result)); }; private native void testSetBooleanArrayRegion(TestObject[] array, int size, long iterations, TestResult time);
    public void triggerSetIntArrayRegion(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testSetIntArrayRegion(objectArray, numObjects, iterations, result); Log.d(TAG, "triggerSet"+"Int"+"ArrayRegion\t"+printResult(result)); }; private native void testSetIntArrayRegion(TestObject[] array, int size, long iterations, TestResult time);
    public void triggerSetShortArrayRegion(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testSetShortArrayRegion(objectArray, numObjects, iterations, result); Log.d(TAG, "triggerSet"+"Short"+"ArrayRegion\t"+printResult(result)); }; private native void testSetShortArrayRegion(TestObject[] array, int size, long iterations, TestResult time);
    public void triggerSetCharArrayRegion(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testSetCharArrayRegion(objectArray, numObjects, iterations, result); Log.d(TAG, "triggerSet"+"Char"+"ArrayRegion\t"+printResult(result)); }; private native void testSetCharArrayRegion(TestObject[] array, int size, long iterations, TestResult time);
    public void triggerSetByteArrayRegion(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testSetByteArrayRegion(objectArray, numObjects, iterations, result); Log.d(TAG, "triggerSet"+"Byte"+"ArrayRegion\t"+printResult(result)); }; private native void testSetByteArrayRegion(TestObject[] array, int size, long iterations, TestResult time);
    public void triggerSetFloatArrayRegion(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testSetFloatArrayRegion(objectArray, numObjects, iterations, result); Log.d(TAG, "triggerSet"+"Float"+"ArrayRegion\t"+printResult(result)); }; private native void testSetFloatArrayRegion(TestObject[] array, int size, long iterations, TestResult time);
    public void triggerSetDoubleArrayRegion(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testSetDoubleArrayRegion(objectArray, numObjects, iterations, result); Log.d(TAG, "triggerSet"+"Double"+"ArrayRegion\t"+printResult(result)); }; private native void testSetDoubleArrayRegion(TestObject[] array, int size, long iterations, TestResult time);
    public void triggerSetLongArrayRegion(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testSetLongArrayRegion(objectArray, numObjects, iterations, result); Log.d(TAG, "triggerSet"+"Long"+"ArrayRegion\t"+printResult(result)); }; private native void testSetLongArrayRegion(TestObject[] array, int size, long iterations, TestResult time);
    public void triggerGetObjectArrayElement(long iterations) {
        TestResult total = new TestResult();
        testGetObjectArrayElement(objectArray, numObjects, iterations, total);
        Log.d(TAG, "triggerGetObjectArrayElement\t" + printResult(total));
    }
    public void triggerSetObjectArrayElement(long iterations) {
        TestResult total = new TestResult();
        testSetObjectArrayElement(objectArray, numObjects, iterations, total);
        Log.d(TAG, "triggerSetObjectArrayElement\t" + printResult(total));
    }
    private native void testGetObjectArrayElement(TestObject arr[], int size, long iterations, TestResult time);
    private native void testSetObjectArrayElement(TestObject arr[], int size, long iterations, TestResult time);
    public void triggerGetBooleanField(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testGetBooleanField(objectArray, numObjects, iterations, result); Log.d(TAG, "triggerGet"+"Boolean"+"Field\t"+printResult(result)); result.save(dataDir, "Get"+"Boolean"+"Field.csv"); }; private native void testGetBooleanField(TestObject[] array, int size, long iterations, TestResult time);
    public void triggerGetIntField(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testGetIntField(objectArray, numObjects, iterations, result); Log.d(TAG, "triggerGet"+"Int"+"Field\t"+printResult(result)); result.save(dataDir, "Get"+"Int"+"Field.csv"); }; private native void testGetIntField(TestObject[] array, int size, long iterations, TestResult time);
    public void triggerGetShortField(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testGetShortField(objectArray, numObjects, iterations, result); Log.d(TAG, "triggerGet"+"Short"+"Field\t"+printResult(result)); result.save(dataDir, "Get"+"Short"+"Field.csv"); }; private native void testGetShortField(TestObject[] array, int size, long iterations, TestResult time);
    public void triggerGetCharField(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testGetCharField(objectArray, numObjects, iterations, result); Log.d(TAG, "triggerGet"+"Char"+"Field\t"+printResult(result)); result.save(dataDir, "Get"+"Char"+"Field.csv"); }; private native void testGetCharField(TestObject[] array, int size, long iterations, TestResult time);
    public void triggerGetByteField(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testGetByteField(objectArray, numObjects, iterations, result); Log.d(TAG, "triggerGet"+"Byte"+"Field\t"+printResult(result)); result.save(dataDir, "Get"+"Byte"+"Field.csv"); }; private native void testGetByteField(TestObject[] array, int size, long iterations, TestResult time);
    public void triggerGetFloatField(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testGetFloatField(objectArray, numObjects, iterations, result); Log.d(TAG, "triggerGet"+"Float"+"Field\t"+printResult(result)); result.save(dataDir, "Get"+"Float"+"Field.csv"); }; private native void testGetFloatField(TestObject[] array, int size, long iterations, TestResult time);
    public void triggerGetDoubleField(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testGetDoubleField(objectArray, numObjects, iterations, result); Log.d(TAG, "triggerGet"+"Double"+"Field\t"+printResult(result)); result.save(dataDir, "Get"+"Double"+"Field.csv"); }; private native void testGetDoubleField(TestObject[] array, int size, long iterations, TestResult time);
    public void triggerGetLongField(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testGetLongField(objectArray, numObjects, iterations, result); Log.d(TAG, "triggerGet"+"Long"+"Field\t"+printResult(result)); result.save(dataDir, "Get"+"Long"+"Field.csv"); }; private native void testGetLongField(TestObject[] array, int size, long iterations, TestResult time);
    public void triggerGetObjectField(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testGetObjectField(objectArray, numObjects, iterations, result); Log.d(TAG, "triggerGet"+"Object"+"Field\t"+printResult(result)); result.save(dataDir, "Get"+"Object"+"Field.csv"); }; private native void testGetObjectField(TestObject[] array, int size, long iterations, TestResult time);
    public void triggerSetBooleanField(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testSetBooleanField(objectArray, numObjects, iterations, result); Log.d(TAG, "triggerSet"+"Boolean"+"Field\t"+printResult(result)); result.save(dataDir, "Set"+"Boolean"+"Field.csv"); }; private native void testSetBooleanField(TestObject[] array, int size, long iterations, TestResult time);
    public void triggerSetIntField(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testSetIntField(objectArray, numObjects, iterations, result); Log.d(TAG, "triggerSet"+"Int"+"Field\t"+printResult(result)); result.save(dataDir, "Set"+"Int"+"Field.csv"); }; private native void testSetIntField(TestObject[] array, int size, long iterations, TestResult time);
    public void triggerSetShortField(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testSetShortField(objectArray, numObjects, iterations, result); Log.d(TAG, "triggerSet"+"Short"+"Field\t"+printResult(result)); result.save(dataDir, "Set"+"Short"+"Field.csv"); }; private native void testSetShortField(TestObject[] array, int size, long iterations, TestResult time);
    public void triggerSetCharField(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testSetCharField(objectArray, numObjects, iterations, result); Log.d(TAG, "triggerSet"+"Char"+"Field\t"+printResult(result)); result.save(dataDir, "Set"+"Char"+"Field.csv"); }; private native void testSetCharField(TestObject[] array, int size, long iterations, TestResult time);
    public void triggerSetByteField(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testSetByteField(objectArray, numObjects, iterations, result); Log.d(TAG, "triggerSet"+"Byte"+"Field\t"+printResult(result)); result.save(dataDir, "Set"+"Byte"+"Field.csv"); }; private native void testSetByteField(TestObject[] array, int size, long iterations, TestResult time);
    public void triggerSetFloatField(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testSetFloatField(objectArray, numObjects, iterations, result); Log.d(TAG, "triggerSet"+"Float"+"Field\t"+printResult(result)); result.save(dataDir, "Set"+"Float"+"Field.csv"); }; private native void testSetFloatField(TestObject[] array, int size, long iterations, TestResult time);
    public void triggerSetDoubleField(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testSetDoubleField(objectArray, numObjects, iterations, result); Log.d(TAG, "triggerSet"+"Double"+"Field\t"+printResult(result)); result.save(dataDir, "Set"+"Double"+"Field.csv"); }; private native void testSetDoubleField(TestObject[] array, int size, long iterations, TestResult time);
    public void triggerSetLongField(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testSetLongField(objectArray, numObjects, iterations, result); Log.d(TAG, "triggerSet"+"Long"+"Field\t"+printResult(result)); result.save(dataDir, "Set"+"Long"+"Field.csv"); }; private native void testSetLongField(TestObject[] array, int size, long iterations, TestResult time);
    public void triggerSetObjectField(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testSetObjectField(objectArray, numObjects, iterations, result); Log.d(TAG, "triggerSet"+"Object"+"Field\t"+printResult(result)); result.save(dataDir, "Set"+"Object"+"Field.csv"); }; private native void testSetObjectField(TestObject[] array, int size, long iterations, TestResult time);
    public void triggerNewBooleanArray(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testNewBooleanArray(iterations, result); Log.d(TAG, "triggerNew"+"Boolean"+"Array\t"+printResult(result)); result.save(dataDir, "New"+"Boolean"+"Array.csv"); }; private native void testNewBooleanArray(long iterations, TestResult time);
    public void triggerNewIntArray(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testNewIntArray(iterations, result); Log.d(TAG, "triggerNew"+"Int"+"Array\t"+printResult(result)); result.save(dataDir, "New"+"Int"+"Array.csv"); }; private native void testNewIntArray(long iterations, TestResult time);
    public void triggerNewShortArray(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testNewShortArray(iterations, result); Log.d(TAG, "triggerNew"+"Short"+"Array\t"+printResult(result)); result.save(dataDir, "New"+"Short"+"Array.csv"); }; private native void testNewShortArray(long iterations, TestResult time);
    public void triggerNewCharArray(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testNewCharArray(iterations, result); Log.d(TAG, "triggerNew"+"Char"+"Array\t"+printResult(result)); result.save(dataDir, "New"+"Char"+"Array.csv"); }; private native void testNewCharArray(long iterations, TestResult time);
    public void triggerNewByteArray(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testNewByteArray(iterations, result); Log.d(TAG, "triggerNew"+"Byte"+"Array\t"+printResult(result)); result.save(dataDir, "New"+"Byte"+"Array.csv"); }; private native void testNewByteArray(long iterations, TestResult time);
    public void triggerNewFloatArray(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testNewFloatArray(iterations, result); Log.d(TAG, "triggerNew"+"Float"+"Array\t"+printResult(result)); result.save(dataDir, "New"+"Float"+"Array.csv"); }; private native void testNewFloatArray(long iterations, TestResult time);
    public void triggerNewDoubleArray(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testNewDoubleArray(iterations, result); Log.d(TAG, "triggerNew"+"Double"+"Array\t"+printResult(result)); result.save(dataDir, "New"+"Double"+"Array.csv"); }; private native void testNewDoubleArray(long iterations, TestResult time);
    public void triggerNewLongArray(long iterations) { TestObject to = new TestObject(); TestResult result = new TestResult(); testNewLongArray(iterations, result); Log.d(TAG, "triggerNew"+"Long"+"Array\t"+printResult(result)); result.save(dataDir, "New"+"Long"+"Array.csv"); }; private native void testNewLongArray(long iterations, TestResult time);
    public void triggerNewObjectArray(long iterations) {
        TestResult result = new TestResult();
        testNewObjectArray(objectArray, numObjects, iterations, result);
        Log.d(TAG, "triggerNewObjectArray\t"+printResult(result));
        result.save(dataDir, "NewObjectArray.csv");
    }
    private native void testNewObjectArray(TestObject[] array, int size, long iterations, TestResult time);
    static {
        System.loadLibrary("apitester");
    }
}
