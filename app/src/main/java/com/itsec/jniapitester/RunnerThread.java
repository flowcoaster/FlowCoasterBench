package com.itsec.jniapitester;

import android.util.Log;

public class RunnerThread extends Thread implements Runnable {
    private static String TAG = "RunnerThread";
    boolean useJni;

    public RunnerThread(boolean useJni) {
        this.useJni = useJni;
    }


    public void setUseJni(boolean useJni) {
        this.useJni = useJni;
    }

    public void run() {
        /*Log.d(TAG, "This is Thread "+this.getId());
        double[] nums = new double[1000];
        for (int i=0; i<nums.length; i++)
            nums[i] = Math.random();

        //bubble sort array
        for (int i=0; i<nums.length; i++)
            for (int j=1; j<nums.length-i; j++)
                if (nums[j]<nums[j-1]) {
                    //Log.d(TAG, "swapping "+j+":"+nums[j]+"and "+(j-1)+":"+nums[j-1]);
                    double temp = nums[j];
                    nums[j] = nums[j-1];
                    nums[j-1] = temp;
                }

        //verify
        for (int i=1; i<nums.length; i++)
            if (nums[i]<nums[i-1]) Log.d(TAG, "Error "+nums[i-1]+">"+nums[i]);*/
        double cookie = Math.random();
        if (!useJni) {
            double c2 = nativeRun(100000, null, cookie);
            if (c2 != cookie) Log.d(TAG, "ERROR: wrong cookie returned "+c2+"!="+cookie);
            else Log.d(TAG, "Correct cookie returned: "+cookie);
        } else {
            int[] nums = new int[100000];
            for (int i=0; i<nums.length; i++)
                nums[i] = (int)(1000.0*Math.random());
            double c2 = nativeRun(100000, nums, cookie);
            if (c2 != cookie) Log.d(TAG, "ERROR: wrong cookie returned "+c2+"!="+cookie);
            else  Log.d(TAG, "Correct cookie returned: "+cookie);
        }
    }

    public native double nativeRun(int length, int[] data, double cookie);

}
