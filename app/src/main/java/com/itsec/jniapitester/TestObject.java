package com.itsec.jniapitester;

import android.content.Context;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

class TestResult {
    public int seconds = 0;
    public long nanoseconds = 0;

    public ArrayList<TestResult> samples = null;

    TestResult() {
        seconds = 0;
        nanoseconds = 0;
    }

    TestResult(int s, long ns) {
        seconds = s;
        nanoseconds = ns;
    }

    public void setTimeSpec(int s, long ns) {
        seconds = s;
        nanoseconds = ns;
    }

    public void add(int s, long ns) {
        if(samples == null)
            samples = new ArrayList<TestResult>();
        add(new TestResult(s, ns));
    }

    public void add(TestResult other) {
        if(samples == null)
            samples = new ArrayList<TestResult>();

        samples.add(other);

        // Log.d("TESTRESULT", "Add "+other.seconds+" sec and "+other.nanoseconds+" ns");

        seconds += other.seconds;
        nanoseconds += other.nanoseconds;

        long carry = nanoseconds / 1000000000L;
        if(carry > 0) {
            seconds += (int) carry;
            nanoseconds = nanoseconds - carry * 1000000000L;
        }

        // Log.d("TESTRESULT", "New acc value: "+seconds+" sec and "+nanoseconds+" ns");
    }

    public String toString() {
        double d = seconds + (double)nanoseconds / 1000000000L;
        // return seconds + "\t" + milliseconds + "\t" + microseconds + "\t" + nanoseconds + "\t" + d + "\n";
        return d+"\n";
    }

    public void save(File dir, String filename) {
        if(samples == null) {
            Log.d("OHOH", "No test results to store in '" + filename + "'.");
            return;
        }
        try {
            dir.mkdirs();
            File f = new File(dir, filename);
            FileOutputStream fos = new FileOutputStream(f, false);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

            for(TestResult r : samples) {
                bw.write(r.toString());
            }
            bw.close();

            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class TestException extends Exception {
    public TestException() {
        super();
    }

    public TestException(String msg) {
        super(msg);
    }
}

class ExtendedTestObject extends TestObject {
    private static final String TAG = "ExtendedTestObject";

    ExtendedTestObject() {
        super();
    }

    public void callSomethingVoid(int i) {
        // Log.d(TAG, "something called with " + i);
    }

    public byte callSomethingByte() {
        // Log.d(TAG, "callSomethingByte()");
        return (byte) m_intField;
    }

    public boolean callSomethingBoolean(int i, double d) {
        // Log.d(TAG, "callSomethingBoolean(i=" + i + ", d=" + d + ")");
        return d > (double) i;
    }

    public int callSomethingInt() {
        // Log.d(TAG, "callSomethingInt()");
        return 2 * m_intField;
    }

    public Object callSomethingObject() {
        // Log.d(TAG, "CallSomethingObject()");
        return this;
    }
}

public class TestObject {
    private static final String TAG = "TestObject";

    private static TestResult m_staticTestResult = new TestResult();

    private static final int arraySizes = 20;

    public static boolean m_staticBooleanField = false;
    public static int m_staticIntField = 42;
    public static short m_staticShortField = 23;
    public static char m_staticCharField = 'a';
    public static byte m_staticByteField = 110;
    public static float m_staticFloatField = 3;
    public static double m_staticDoubleField = 0.123;
    public static long m_staticLongField = 424242;

    public boolean m_booleanField;
    public int m_intField;
    public short m_shortField;
    public char m_charField;
    public byte m_byteField;
    public float m_floatField;
    public double m_doubleField;
    public long m_longField;
    public TestObject m_objectField;

    public static boolean m_staticBooleanArrayField[] = { true, false, true, false };
    public static int m_staticIntArrayField[] = { 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42 };
    public static short m_staticShortArrayField[] = { 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42 };
    public static char m_staticCharArrayField[] = { 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42 };
    public static byte m_staticByteArrayField[] = { 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42 };
    public static float m_staticFloatArrayField[] = { 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42 };
    public static double m_staticDoubleArrayField[] = { 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42 };
    public static long m_staticLongArrayField[] = { 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42 };

    public boolean m_booleanArrayField[];
    public int m_intArrayField[];
    public short m_shortArrayField[];
    public char m_charArrayField[];
    public byte m_byteArrayField[];
    public float m_floatArrayField[];
    public double m_doubleArrayField[];
    public long m_longArrayField[];

    private long m_secretField;

    public TestObject inception;

    public TestObject() {
        Random r = new Random(System.currentTimeMillis());

        m_booleanField = r.nextBoolean();
        m_intField = r.nextInt();
        m_shortField = (short)r.nextInt(Short.MAX_VALUE);
        m_charField = (char)r.nextInt(Character.MAX_VALUE);
        m_byteField = (byte)r.nextInt(Byte.MAX_VALUE);
        m_floatField = r.nextFloat();
        m_doubleField = r.nextDouble();
        m_longField = r.nextLong();

        m_booleanArrayField = new boolean[arraySizes];
        m_intArrayField =  new int[arraySizes];
        m_shortArrayField = new short[arraySizes];
        m_charArrayField = new char[arraySizes];
        m_byteArrayField = new byte[arraySizes];
        m_floatArrayField = new float[arraySizes];
        m_doubleArrayField = new double[arraySizes];
        m_longArrayField = new long[arraySizes];
        for(int i = 0; i < arraySizes; i++) {
            m_booleanArrayField[i] = r.nextBoolean();
            m_intArrayField[i] =  r.nextInt();
            m_shortArrayField[i] =  (short)r.nextInt(Short.MAX_VALUE);
            m_charArrayField[i] =  (char)r.nextInt(Character.MAX_VALUE);
            m_byteArrayField[i] =  (byte)r.nextInt(Byte.MAX_VALUE);
            m_floatArrayField[i] =  r.nextFloat();
            m_doubleArrayField[i] =  r.nextDouble();
            m_longArrayField[i] =  r.nextLong();
        }

        inception = this;

        m_secretField = 442;
    }

    public void callThrowException() throws TestException {
        throw new TestException();
    }

    public TestObject(int intField, char charField, TestObject inception) {
        this.m_intField = intField;
        this.m_charField = charField;
        this.inception = inception;
    }

    public TestObject(int intField) {
        this.m_intField = intField;
        m_secretField = 442;
    }

    public long[] callArray(long[] array, int returnSize) {
        long[] result = null;

        if(returnSize > 0) {
            result = new long[returnSize];
        }

        return result;
    }

    public void callSomethingVoid(int i) {
        // Log.d(TAG, "something called with " + i);
    }

    public byte callSomethingByte() {
        // Log.d(TAG, "callSomethingByte()");
        return (byte) m_intField;
    }

    public boolean callSomethingBoolean(int i, double d) {
        // Log.d(TAG, "callSomethingBoolean(i=" + i + ", d=" + d + ")");
        return d > (double) i;
    }

    public int callSomethingInt() {
        // Log.d(TAG, "callSomethingInt()");
        return 2 * m_intField;
    }

    public Object callSomethingObject() {
        // Log.d(TAG, "CallSomethingObject()");
        return this;
    }

    public static void callStaticSomethingVoid(int i) {
        // Log.d(TAG, "something called with " + i);
    }

    public static byte callStaticSomethingByte() {
        // Log.d(TAG, "callSomethingByte()");
        return (byte) m_staticIntField;
    }

    public static boolean callStaticSomethingBoolean(int i, double d) {
        // Log.d(TAG, "callSomethingBoolean(i=" + i + ", d=" + d + ")");
        return d > (double) i;
    }

    public static int callStaticSomethingInt() {
        // Log.d(TAG, "callSomethingInt()");
        return 2 * m_staticIntField;
    }

    public static Object callStaticSomethingObject() {
        return TestObject.m_staticTestResult;
    }

    public static void printInt(int i) {
        // Log.d(TAG, "static printInt: this is the magic number: " + i);
    }

    public static short staticNum(short s) {
        // Log.d(TAG, "static short method with param " + s);
        return (short) (s + 1);
    }

    public native int A();
    private native int B(int x);
    private native int C(int x, double y);
    private native double D(double x, double y);
    private native String E();
    private native String F(TestObject t);
}
