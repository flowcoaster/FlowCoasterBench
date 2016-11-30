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

                // Toast.makeText(this, "Run tests with "+iterations+" iterations. ("+selection+")", Toast.LENGTH_SHORT).show();

        /*for (int test = 0, run = 1; test < toRunSize; test++) {
            if (!toRun[test])
                continue;
            for (int i = 0; i < iterations; i++, run++) {

                double a = 0.121;
                double b = 1.1231;
                double c = 0;

                long startTime = System.nanoTime();
                for (long l = 0; l < 10000000; l++) c = c + a / b * 2;
                long endTime = System.nanoTime();
                long elapsed = endTime - startTime;

                Log.d(TAG, "NEXT TEST " + ((long) i) + " elapsed: " + elapsed + "ns");

                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(JniApiTester.ResponseReceiver.ACTION_RESP);
                broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
                broadcastIntent.putExtra(PARAM_OUT_PROGRESS, (long) run);
                sendBroadcast(broadcastIntent);
            }
        }*/

        Thread t = new Thread(new Runnable() {
            public void run() {

                // triggerLongTest();

                // GetClass and GetMethodID
                if (toRun[0]) {
                    triggerGetClass(iterations);
                    updateProgress(iterations);
                    triggerGetMethodID(iterations);
                    updateProgress(iterations);
                    triggerGetStaticMethodID(iterations);
                    updateProgress(iterations);
                }

                // VM create/destroy and get JNIEnv
                if (toRun[1]) {
                    triggerGetDestroyJVM(iterations);
                    updateProgress(iterations);
                    triggerGetJNIEnv(iterations);
                    updateProgress(iterations);
                }

                // manipulate string
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

                // register natives
                if (toRun[3]) {
                    triggerRegisterNatives(iterations);
                    updateProgress(iterations);
                    // triggerUnRegisterNatives(iterations);
                    // updateProgress(iterations);
                }

                // modify object references
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
			/* 
			triggerEnsureLocalCapacity(iterations);
			updateProgress(iterations);
			*/
                    triggerNewWeakGlobalRef(iterations);
                    updateProgress(iterations);
                    triggerDeleteWeakGlobalRef(iterations);
                    updateProgress(iterations);
                }

                // modify threads
                if (toRun[5]) {
                    // ???
                }

                // call methods
                if (toRun[6]) {
                    triggerCallMethod(iterations);
                    updateProgress(iterations);
                    triggerCallNonvirtualMethod(iterations);
                    updateProgress(iterations);
                    triggerCallStaticMethod(iterations);
                    updateProgress(iterations);
                }

                // modify array
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

                // modify exceptions
                if (toRun[8]) {
                    triggerThrow(iterations);
                    updateProgress(iterations);
                    triggerThrowNew(iterations);
                    updateProgress(iterations);
                    triggerExceptionOccurred(iterations);
                    updateProgress(iterations);
                    // take out if measurements are imprecise
                    // in the end this method is convenience method
                    triggerExceptionDescribe(iterations);
                    updateProgress(iterations);
                    triggerExceptionClear(iterations);
                    updateProgress(iterations);
                    // FatalError - does not make sense to benchmark this function
                    triggerExceptionCheck(iterations);
                    updateProgress(iterations);
                }

                // create object instance
                if (toRun[9]) {
                    triggerAllocObject(iterations);
                    updateProgress(iterations);
                    triggerNewObject(iterations);
                    updateProgress(iterations);
                    // NewObjectV
                    // NewObjectA
                }

                // modify fields
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

                // modify direct buffers
                if (toRun[11]) {
                    // requires filesystem access and policy tracking there

                    // triggerNewDirectByteBuffer(iterations);
                    // triggerGetDirectBufferAddress(iterations);
                    // triggerGetDirectBufferCapacity(iterations);
                }

                if (toRun[12]) {
                    // 8192 Byte
                    triggerCallWithVariableArgumentSize(iterations, 1024);
                    updateProgress(iterations);
                    // 512 KB
                    triggerCallWithVariableArgumentSize(iterations, 65536);
                    updateProgress(iterations);
                    // 1 MB
                    triggerCallWithVariableArgumentSize(iterations, 131072);
                    updateProgress(iterations);
                    // 10 MB
                    triggerCallWithVariableArgumentSize(iterations, 1310720);
                    updateProgress(iterations);
                    // 20 MB
                    triggerCallWithVariableArgumentSize(iterations, 2621440);
                    updateProgress(iterations);
                    // 30 MB
                    triggerCallWithVariableArgumentSize(iterations, 3932160);
                    updateProgress(iterations);
                    // 50 MB
                    triggerCallWithVariableArgumentSize(iterations, 6553600);
                    updateProgress(iterations);
                }

                if (toRun[13]) {
                    // 8192 Byte
                    triggerCallWithVariableReturnSize(iterations, 1024);
                    updateProgress(iterations);
                    // 512 KB
                    triggerCallWithVariableReturnSize(iterations, 65536);
                    updateProgress(iterations);
                    // 1 MB
                    triggerCallWithVariableReturnSize(iterations, 131072);
                    updateProgress(iterations);
                    // 10 MB
                    triggerCallWithVariableReturnSize(iterations, 1310720);
                    updateProgress(iterations);
                    // 20 MB
                    triggerCallWithVariableReturnSize(iterations, 2621440);
                    updateProgress(iterations);
                    // 30 MB
                    triggerCallWithVariableReturnSize(iterations, 3932160);
                    updateProgress(iterations);
                    // 50 MB
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
        long[] la = {333333}; //, 444444}; //, 555555, 666666, 7777777, 8888888, 9999999};
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

#define TRIGGER_GET_TYPE_ARRAYELEMENTS(_ctype, _jname, _strType) \
    public void triggerGet##_jname##ArrayElements(long iterations) { \
        TestObject to = new TestObject(); \
        TestResult result = new TestResult(); \
        testGet##_jname##ArrayElements(objectArray, numObjects, iterations, result); \
        Log.d(TAG, "triggerGet"+_strType+"ArrayElements\t"+printResult(result)); \
    }
#define GET_TYPE_ARRAYELEMENTS(_ctype, _jname) \
    private native void testGet##_jname##ArrayElements(TestObject[] array, int size, long iterations, TestResult time)
#define MAKE_GET_ARRAYELEMENTS(_ctype, _jname, _strType) \
    TRIGGER_GET_TYPE_ARRAYELEMENTS(_ctype, _jname, _strType); \
    GET_TYPE_ARRAYELEMENTS(_ctype, _jname);

    MAKE_GET_ARRAYELEMENTS(boolean, Boolean, "Boolean")
    MAKE_GET_ARRAYELEMENTS(int, Int, "Int")
    MAKE_GET_ARRAYELEMENTS(short, Short, "Short")
    MAKE_GET_ARRAYELEMENTS(char, Char, "Char")
    MAKE_GET_ARRAYELEMENTS(byte, Byte, "Byte")
    MAKE_GET_ARRAYELEMENTS(float, Float, "Float")
    MAKE_GET_ARRAYELEMENTS(double, Double, "Double")
    MAKE_GET_ARRAYELEMENTS(long, Long, "Long")

#define TRIGGER_RELEASE_TYPE_ARRAYELEMENTS(_ctype, _jname, _strType) \
    public void triggerRelease##_jname##ArrayElements(long iterations) { \
        TestObject to = new TestObject(); \
        TestResult result = new TestResult(); \
        testRelease##_jname##ArrayElements(objectArray, numObjects, iterations, result); \
        Log.d(TAG, "triggerRelease"+_strType+"ArrayElements\t"+printResult(result)); \
    }
#define RELEASE_TYPE_ARRAYELEMENTS(_ctype, _jname) \
    private native void testRelease##_jname##ArrayElements(TestObject[] array, int size, long iterations, TestResult time)
#define MAKE_RELEASE_ARRAYELEMENTS(_ctype, _jname, _strType) \
    TRIGGER_RELEASE_TYPE_ARRAYELEMENTS(_ctype, _jname, _strType); \
    RELEASE_TYPE_ARRAYELEMENTS(_ctype, _jname);

    MAKE_RELEASE_ARRAYELEMENTS(boolean, Boolean, "Boolean")
    MAKE_RELEASE_ARRAYELEMENTS(int, Int, "Int")
    MAKE_RELEASE_ARRAYELEMENTS(short, Short, "Short")
    MAKE_RELEASE_ARRAYELEMENTS(char, Char, "Char")
    MAKE_RELEASE_ARRAYELEMENTS(byte, Byte, "Byte")
    MAKE_RELEASE_ARRAYELEMENTS(float, Float, "Float")
    MAKE_RELEASE_ARRAYELEMENTS(double, Double, "Double")
    MAKE_RELEASE_ARRAYELEMENTS(long, Long, "Long")

#define TRIGGER_GET_TYPE_ARRAYREGION(_ctype, _jname, _strType) \
    public void triggerGet##_jname##ArrayRegion(long iterations) { \
        TestObject to = new TestObject(); \
        TestResult result = new TestResult(); \
        testGet##_jname##ArrayRegion(objectArray, numObjects, iterations, result); \
        Log.d(TAG, "triggerGet"+_strType+"ArrayRegion\t"+printResult(result)); \
    }
#define GET_TYPE_ARRAYREGION(_ctype, _jname) \
    private native void testGet##_jname##ArrayRegion(TestObject[] array, int size, long iterations, TestResult time)
#define MAKE_GET_ARRAYREGION(_ctype, _jname, _strType) \
    TRIGGER_GET_TYPE_ARRAYREGION(_ctype, _jname, _strType); \
    GET_TYPE_ARRAYREGION(_ctype, _jname);

    MAKE_GET_ARRAYREGION(boolean, Boolean, "Boolean")
    MAKE_GET_ARRAYREGION(int, Int, "Int")
    MAKE_GET_ARRAYREGION(short, Short, "Short")
    MAKE_GET_ARRAYREGION(char, Char, "Char")
    MAKE_GET_ARRAYREGION(byte, Byte, "Byte")
    MAKE_GET_ARRAYREGION(float, Float, "Float")
    MAKE_GET_ARRAYREGION(double, Double, "Double")
    MAKE_GET_ARRAYREGION(long, Long, "Long")

#define TRIGGER_SET_TYPE_ARRAYREGION(_ctype, _jname, _strType) \
    public void triggerSet##_jname##ArrayRegion(long iterations) { \
        TestObject to = new TestObject(); \
        TestResult result = new TestResult(); \
        testSet##_jname##ArrayRegion(objectArray, numObjects, iterations, result); \
        Log.d(TAG, "triggerSet"+_strType+"ArrayRegion\t"+printResult(result)); \
    }
#define SET_TYPE_ARRAYREGION(_ctype, _jname) \
    private native void testSet##_jname##ArrayRegion(TestObject[] array, int size, long iterations, TestResult time)
#define MAKE_SET_ARRAYREGION(_ctype, _jname, _strType) \
    TRIGGER_SET_TYPE_ARRAYREGION(_ctype, _jname, _strType); \
    SET_TYPE_ARRAYREGION(_ctype, _jname);

    MAKE_SET_ARRAYREGION(boolean, Boolean, "Boolean")
    MAKE_SET_ARRAYREGION(int, Int, "Int")
    MAKE_SET_ARRAYREGION(short, Short, "Short")
    MAKE_SET_ARRAYREGION(char, Char, "Char")
    MAKE_SET_ARRAYREGION(byte, Byte, "Byte")
    MAKE_SET_ARRAYREGION(float, Float, "Float")
    MAKE_SET_ARRAYREGION(double, Double, "Double")
    MAKE_SET_ARRAYREGION(long, Long, "Long")

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

    #define TRIGGER_GET_TYPE_FIELD(_jname, _strType) \
    public void triggerGet##_jname##Field(long iterations) { \
        TestObject to = new TestObject(); \
        TestResult result = new TestResult(); \
        testGet##_jname##Field(objectArray, numObjects, iterations, result); \
        Log.d(TAG, "triggerGet"+_strType+"Field\t"+printResult(result)); \
        result.save(dataDir, "Get"+_strType+"Field.csv");\
    }
#define GET_TYPE_FIELD(_jname) \
    private native void testGet##_jname##Field(TestObject[] array, int size, long iterations, TestResult time)
#define MAKE_GET_FIELD(_jname, _strType) \
    TRIGGER_GET_TYPE_FIELD(_jname, _strType); \
    GET_TYPE_FIELD(_jname);

    MAKE_GET_FIELD(Boolean, "Boolean")
    MAKE_GET_FIELD(Int, "Int")
    MAKE_GET_FIELD(Short, "Short")
    MAKE_GET_FIELD(Char, "Char")
    MAKE_GET_FIELD(Byte, "Byte")
    MAKE_GET_FIELD(Float, "Float")
    MAKE_GET_FIELD(Double, "Double")
    MAKE_GET_FIELD(Long, "Long")
    MAKE_GET_FIELD(Object, "Object")

#define TRIGGER_SET_TYPE_FIELD(_ctype, _jname, _strType) \
    public void triggerSet##_jname##Field(long iterations) { \
        TestObject to = new TestObject(); \
        TestResult result = new TestResult(); \
        testSet##_jname##Field(objectArray, numObjects, iterations, result); \
        Log.d(TAG, "triggerSet"+_strType+"Field\t"+printResult(result)); \
        result.save(dataDir, "Set"+_strType+"Field.csv");\
    }
#define SET_TYPE_FIELD(_ctype, _jname) \
    private native void testSet##_jname##Field(TestObject[] array, int size, long iterations, TestResult time)
#define MAKE_SET_FIELD(_ctype, _jname, _strType) \
    TRIGGER_SET_TYPE_FIELD(_ctype, _jname, _strType); \
    SET_TYPE_FIELD(_ctype, _jname);

    MAKE_SET_FIELD(boolean, Boolean, "Boolean")
    MAKE_SET_FIELD(int, Int, "Int")
    MAKE_SET_FIELD(short, Short, "Short")
    MAKE_SET_FIELD(char, Char, "Char")
    MAKE_SET_FIELD(byte, Byte, "Byte")
    MAKE_SET_FIELD(float, Float, "Float")
    MAKE_SET_FIELD(double, Double, "Double")
    MAKE_SET_FIELD(long, Long, "Long")
    MAKE_SET_FIELD(long, Object, "Object")

#define TRIGGER_NEW_TYPE_ARRAY(_jname, _strType) \
    public void triggerNew##_jname##Array(long iterations) { \
        TestObject to = new TestObject(); \
        TestResult result = new TestResult(); \
        testNew##_jname##Array(iterations, result); \
        Log.d(TAG, "triggerNew"+_strType+"Array\t"+printResult(result)); \
        result.save(dataDir, "New"+_strType+"Array.csv");\
    }
#define NEW_TYPE_ARRAY(_jname) \
    private native void testNew##_jname##Array(long iterations, TestResult time)
#define MAKE_NEW_ARRAY(_jname, _strType) \
    TRIGGER_NEW_TYPE_ARRAY(_jname, _strType); \
    NEW_TYPE_ARRAY(_jname);

    MAKE_NEW_ARRAY(Boolean, "Boolean")
    MAKE_NEW_ARRAY(Int, "Int")
    MAKE_NEW_ARRAY(Short, "Short")
    MAKE_NEW_ARRAY(Char, "Char")
    MAKE_NEW_ARRAY(Byte, "Byte")
    MAKE_NEW_ARRAY(Float, "Float")
    MAKE_NEW_ARRAY(Double, "Double")
    MAKE_NEW_ARRAY(Long, "Long")

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
