/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.itsec.jniapitester;

import java.io.IOException;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.NumberPicker;
import java.lang.reflect.Field;
import java.util.ArrayList;

import android.graphics.Paint;
import android.widget.ProgressBar;
import android.widget.TextView;

public class JniApiTester extends Activity implements OnClickListener {
    private static final String TAG = "JNIApiTester";
    private static final String testString = "someString";

    private static final int tests = 15;
    private ResponseReceiver receiver;

    ProgressBar progressBar = null;
    ApiTestRunner runner = null;

    public class ResponseReceiver extends BroadcastReceiver {
        public static final String ACTION_RESP = "com.itsec.jniapitester.intent.action.PROGRESS";

        @Override
        public void onReceive(Context context, Intent intent) {
            long max = intent.getLongExtra(ApiTestRunner.PARAM_OUT_PROGRESS_TOTAL, 0);
            long status = intent.getLongExtra(ApiTestRunner.PARAM_OUT_PROGRESS, 0);
            boolean finish = intent.getBooleanExtra(ApiTestRunner.PARAM_OUT_DONE, false);
            if(finish) {
                Button startTest = (Button) findViewById(R.id.btn_starttest);
                startTest.setEnabled(true);
            }
            progressBar.setMax((int)max);
            progressBar.setProgress((int)status);
            progressBar.invalidate();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout);

        /* iterationsPicker = (NumberPicker)findViewById(R.id.input_repetitions);
        iterationsPicker.setEnabled(true);
        iterationsPicker.setMinValue(10);
        setNumberPickerTextColor(iterationsPicker, R.color.red);*/

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        Button startTest = (Button) findViewById(R.id.btn_starttest);
        startTest.setOnClickListener(this);

        IntentFilter filter = new IntentFilter(ResponseReceiver.ACTION_RESP);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new ResponseReceiver();
        registerReceiver(receiver, filter);
    }

    public void onPause() {
        unregisterReceiver(receiver);
        super.onPause();
    }

    public void onClick(View v) {
        if (v.getId() == R.id.btn_starttest) {
            Button startTest = (Button) findViewById(R.id.btn_starttest);
            startTest.setEnabled(false);

            /* EditText txtRepetitions = (EditText) findViewById(R.id.input_repetitions);
            String n = txtRepetitions.getText().toString();*/
            int n2 = 10; // Integer.parseInt(n);
            runTest(n2);
        }
    }

    private void updateProgress() {
        int p = progressBar.getProgress();
        progressBar.setProgress(p + 1);
        progressBar.invalidate();
    }

    private void resetProgress() {
        progressBar.setProgress(0);
        progressBar.invalidate();
    }

    private void runTest(int n) {

        EditText iterationsText = (EditText) findViewById(R.id.input_repetitions);
        long iterations = Long.parseLong(iterationsText.getText().toString());

        boolean toRun[] = new boolean[tests];

        toRun[0] = (((CheckBox) findViewById(R.id.input_getClassMethodIDsRefs)).isChecked());
        toRun[1] = (((CheckBox) findViewById(R.id.input_JavaVMandJniEnv)).isChecked());
        toRun[2] = (((CheckBox) findViewById(R.id.input_modString)).isChecked());
        toRun[3] = (((CheckBox) findViewById(R.id.input_regNativeMethod)).isChecked());
        toRun[4] = (((CheckBox) findViewById(R.id.input_modObjRef)).isChecked());
        toRun[5] = (((CheckBox) findViewById(R.id.input_modThread)).isChecked());
        toRun[6] = (((CheckBox) findViewById(R.id.input_callJavaMethod)).isChecked());
        toRun[7] = (((CheckBox) findViewById(R.id.input_modArray)).isChecked());
        toRun[8] = (((CheckBox) findViewById(R.id.input_modExceptions)).isChecked());
        toRun[9] = (((CheckBox) findViewById(R.id.input_createObjInst)).isChecked());
        toRun[10] = (((CheckBox) findViewById(R.id.input_modObjField)).isChecked());
        toRun[11] = (((CheckBox) findViewById(R.id.input_modDirectBuffers)).isChecked());
        toRun[12] = (((CheckBox) findViewById(R.id.input_varArgumentSize)).isChecked());
        toRun[13] = (((CheckBox) findViewById(R.id.input_varReturnSize)).isChecked());
        toRun[14] = (((CheckBox) findViewById(R.id.input_threadTest)).isChecked());

        int runs = 0;
        for (boolean selected : toRun) {
            if (selected)
                runs += iterations;
        }

        Intent msgIntent = new Intent(this, ApiTestRunner.class);
        msgIntent.putExtra(ApiTestRunner.PARAM_IN_ITERATIONS, iterations);
        msgIntent.putExtra(ApiTestRunner.PARAM_IN_TESTS, toRun);
        msgIntent.putExtra(ApiTestRunner.PARAM_IN_TESTSIZE, (long) tests);
        startService(msgIntent);
    }
}

