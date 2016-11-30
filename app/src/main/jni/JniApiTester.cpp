#include <jni.h>
#include <android/log.h>
#include <string.h>
#include <time.h>
#include <stdlib.h>
#include <unistd.h>
#include <pthread.h>

#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOG_TAG "JNIApiTester"

extern "C" {

jmethodID someMethodID;
jfieldID someFieldID;

jint nativeMethod_A(JNIEnv* env, jobject thiz) {
    return 42;
}

jint nativeMethod_B(JNIEnv* env, jobject thiz, jint a) {
    return a-42;
}

jint nativeMethod_C(JNIEnv* env, jobject thiz, jint x, jdouble y) {
    return 42;
}

jdouble nativeMethod_D(JNIEnv* env, jobject thiz, jdouble x, jdouble y) {
    return 0.0;
}

jstring nativeMethod_E(JNIEnv* env, jobject thiz) {
    const char* str = "ABC_E";
    return env->NewStringUTF(str);
}

jstring nativeMethod_F(JNIEnv* env, jobject thiz, jobject t) {
    const char* str = "ABC_F";
    return env->NewStringUTF(str);
}

static JNINativeMethod methods [] = {
        { "A", "()I",   (void *)&nativeMethod_A},
        { "B", "(I)I",  (void *)&nativeMethod_B},
        { "C", "(ID)I", (void *)&nativeMethod_C},
        { "D", "(DD)D", (void *)&nativeMethod_D},
        { "E", "()Ljava/lang/String;", (void *)&nativeMethod_E},
        { "F", "(Lcom/itsec/jniapitester/TestObject;)Ljava/lang/String;", (void *)&nativeMethod_F}
};

timespec timespec_diff(timespec start, timespec end, JNIEnv *env)
{
	timespec temp;
	if ((end.tv_nsec-start.tv_nsec)<0) {
		temp.tv_sec = end.tv_sec-start.tv_sec-1;
		temp.tv_nsec = 1000000000L+end.tv_nsec-start.tv_nsec;
	} else {
		temp.tv_sec = end.tv_sec-start.tv_sec;
		temp.tv_nsec = end.tv_nsec-start.tv_nsec;
	}

    return temp;
}

void addTime(JNIEnv* env, jobject accTime, timespec toAdd) {
    jclass timeclass = env->GetObjectClass(accTime);
    jmethodID addMethod = env->GetMethodID(timeclass, "add", "(IJ)V");
    jint sec = (jint)toAdd.tv_sec;
    jlong nsec = (jlong)toAdd.tv_nsec;
    env->CallVoidMethod(accTime, addMethod, sec, nsec);
    env->DeleteLocalRef(timeclass);
}

  jint Java_com_itsec_jniapitester_ApiTestRunner_doSomething(JNIEnv* env, jobject thiz, jint id, jint wait) {
    LOGD("THREAD_%d: Start to sleep ... ", (int)id);
    sleep((int)wait);
    
    // TODO: do some JNI calls here
    
    LOGD("THREAD_%d: ... wake up and die.", (int)id);

    return id;
  }

  jlongArray Java_com_itsec_jniapitester_ApiTestRunner_testLongArguments(JNIEnv* env, jobject thiz, jlong a, jlongArray la, jlong b) {
    LOGD("testLongArguments:");
    LOGD("\ta = 0x%08lx", (long)a);
    LOGD("\tb = 0x%08lx", (long)b);

    jboolean isCopy;
    jlong *larray = env->GetLongArrayElements(la, &isCopy);
    for(int i = 0; i < 2; i++) {
      LOGD("\t %d: %lld", i, larray[i]);
    }
    env->ReleaseLongArrayElements(la, larray, 0);
    
    return la;
  }

/* jboolean
Java_com_itsec_jniapitester_ApiTestRunner_testGetStringUTFChars(JNIEnv* env, jobject thiz, jstring s) {
	jboolean b = JNI_TRUE;
	const char* chars = env->GetStringUTFChars(s, &b);
	const char* expected = "someString";
	jboolean result = 0;
	if (strcmp(chars, expected) == 0) {
		LOGD("GetStringUTFChars: SUCCESS");
		result = JNI_TRUE;
	} else {
		LOGD("GetStringUTFChars: FAIL (%s != %s)", chars, expected);
		result = JNI_FALSE;
	}
	env->ReleaseStringUTFChars(s, chars);
	return result;
}*/

  void Java_com_itsec_jniapitester_ApiTestRunner_testNewGlobalRef(JNIEnv* env, jobject thiz, jobjectArray jobjArray, jint size, jlong iterations, jobject time) {
    timespec tstart, tstop, tdiff;
    jobject jobj;
    jobject jglobalobj;
    srand(1);

    for(int i = 0; i < (int)iterations; i++) {
      jint index = (jint)rand() % size;
      jobj = env->GetObjectArrayElement(jobjArray, index);
      clock_gettime(CLOCK_MONOTONIC, &tstart);
      jglobalobj = env->NewGlobalRef(jobj);
      clock_gettime(CLOCK_MONOTONIC, &tstop);
      tdiff = timespec_diff(tstart, tstop, env);
      addTime(env, time, tdiff);
      env->DeleteGlobalRef(jglobalobj);
      env->DeleteLocalRef(jobj);
    }
  }

  void Java_com_itsec_jniapitester_ApiTestRunner_testDeleteGlobalRef(JNIEnv* env, jobject thiz, jobjectArray jobjArray, jint size, jlong iterations, jobject time) {
    timespec tstart, tstop, tdiff;
    jobject jobj;
    jobject jglobalobj;
    srand(1);

    for(int i = 0; i < (int)iterations; i++) {
      jint index = (jint)rand() % size;
      jobj = env->GetObjectArrayElement(jobjArray, index);
      jglobalobj = env->NewGlobalRef(jobj);
      clock_gettime(CLOCK_MONOTONIC, &tstart);
      env->DeleteGlobalRef(jglobalobj);
      clock_gettime(CLOCK_MONOTONIC, &tstop);
      tdiff = timespec_diff(tstart, tstop, env);
      addTime(env, time, tdiff);
      env->DeleteLocalRef(jobj);
    }
  }

  void Java_com_itsec_jniapitester_ApiTestRunner_testDeleteLocalRef(JNIEnv* env, jobject thiz, jobjectArray jobjArray, jint size, jlong iterations, jobject time) {
    timespec tstart, tstop, tdiff;
    jobject jobj;
    jobject jglobalobj;
    srand(1);

    for(int i = 0; i < (int)iterations; i++) {
      jint index = (jint)rand() % size;
      jobj = env->GetObjectArrayElement(jobjArray, index);
      clock_gettime(CLOCK_MONOTONIC, &tstart);
      env->DeleteLocalRef(jobj);
      clock_gettime(CLOCK_MONOTONIC, &tstop);
      tdiff = timespec_diff(tstart, tstop, env);
      addTime(env, time, tdiff);
    }
  }

  void Java_com_itsec_jniapitester_ApiTestRunner_testIsSameObject(JNIEnv* env, jobject thiz, jobjectArray jobjArray, jint size, jlong iterations, jobject time) {
    timespec tstart, tstop, tdiff;
    jobject jobj1, jobj2;
    jint index1, index2;
    jboolean res;
    srand(1);
    for(int i = 0; i < (int)iterations; i++) {
      index1 = (jint)rand() % size;
      index2 = (jint)rand() % size;

      jobj1 = env->GetObjectArrayElement(jobjArray, index1);
      jobj2 = env->GetObjectArrayElement(jobjArray, index2);
      clock_gettime(CLOCK_MONOTONIC, &tstart);
      res = env->IsSameObject(jobj1, jobj2);
      clock_gettime(CLOCK_MONOTONIC, &tstop);
      tdiff = timespec_diff(tstart, tstop, env);
      addTime(env, time, tdiff);
      env->DeleteLocalRef(jobj1);
      env->DeleteLocalRef(jobj2);
    }
  }

  void Java_com_itsec_jniapitester_ApiTestRunner_testNewLocalRef(JNIEnv* env, jobject thiz, jobjectArray jobjArray, jint size, jlong iterations, jobject time) {
    timespec tstart, tstop, tdiff;
    jobject jobj;
    jobject jlocalobj;
    srand(1);

    for(int i = 0; i < (int)iterations; i++) {
      jint index = (jint)rand() % size;
      jobj = env->GetObjectArrayElement(jobjArray, index);
      clock_gettime(CLOCK_MONOTONIC, &tstart);
      jlocalobj = env->NewLocalRef(jobj);
      clock_gettime(CLOCK_MONOTONIC, &tstop);
      tdiff = timespec_diff(tstart, tstop, env);
      addTime(env, time, tdiff);
      env->DeleteLocalRef(jlocalobj);
      env->DeleteLocalRef(jobj);
    }
  }

  void Java_com_itsec_jniapitester_ApiTestRunner_testEnsureLocalCapacity(JNIEnv* env, jobject thiz, jobjectArray jobjArray, jint size, jlong iterations, jobject time) {
    timespec tstart, tstop, tdiff;
    // Not sure a performance test makes sense - leave out
  }

  void Java_com_itsec_jniapitester_ApiTestRunner_testNewWeakGlobalRef(JNIEnv* env, jobject thiz, jobjectArray jobjArray, jint size, jlong iterations, jobject time) {
    timespec tstart, tstop, tdiff;
    jobject jobj;
    jobject jglobalobj;
    srand(1);

    for(int i = 0; i < (int)iterations; i++) {
      jint index = (jint)rand() % size;
      jobj = env->GetObjectArrayElement(jobjArray, index);
      clock_gettime(CLOCK_MONOTONIC, &tstart);
      jglobalobj = env->NewWeakGlobalRef(jobj);
      clock_gettime(CLOCK_MONOTONIC, &tstop);
      tdiff = timespec_diff(tstart, tstop, env);
      addTime(env, time, tdiff);
      env->DeleteWeakGlobalRef(jglobalobj);
      env->DeleteLocalRef(jobj);
    }
  }

  void Java_com_itsec_jniapitester_ApiTestRunner_testDeleteWeakGlobalRef(JNIEnv* env, jobject thiz, jobjectArray jobjArray, jint size, jlong iterations, jobject time) {
    timespec tstart, tstop, tdiff;
    jobject jobj;
    jobject jglobalobj;
    srand(1);

    for(int i = 0; i < (int)iterations; i++) {
      jint index = (jint)rand() % size;
      jobj = env->GetObjectArrayElement(jobjArray, index);
      jglobalobj = env->NewWeakGlobalRef(jobj);
      clock_gettime(CLOCK_MONOTONIC, &tstart);
      env->DeleteWeakGlobalRef(jglobalobj);
      clock_gettime(CLOCK_MONOTONIC, &tstop);
      tdiff = timespec_diff(tstart, tstop, env);
      addTime(env, time, tdiff);
      env->DeleteLocalRef(jobj);
    }
  }

void Java_com_itsec_jniapitester_ApiTestRunner_testNewString(JNIEnv* env, jobject thiz, jlong iterations, jobject time) {
    timespec tstart, tstop, tdiff;
    int maxLength = 2048;
    jchar cString[maxLength+2];
    srand(1);
    for(int i = 0; i < (int)iterations; i++) {
        int length = (rand() + 40) % maxLength;
        for (int l = 0; l < length; l++)
            cString[l] = 1 + (jchar)(rand() % 0x70);
        cString[length] = 0;
        cString[length + 1] = 0;

        // jstring refs[100];
        jstring ref;
        clock_gettime(CLOCK_MONOTONIC, &tstart);
        // for(int c = 0; c < 100; c++)
        ref = env->NewString(cString, length);
        clock_gettime(CLOCK_MONOTONIC, &tstop);
        tdiff = timespec_diff(tstart, tstop, env);
        // for(int c = 0; c < 100; c++)
        env->DeleteLocalRef(ref);
        addTime(env, time, tdiff);
    }
}

void
Java_com_itsec_jniapitester_ApiTestRunner_testNewStringUTF(JNIEnv* env, jobject thiz, jlong iterations, jobject time) {
    timespec tstart, tstop, tdiff;
    int maxLength = 2048;
    char cString[maxLength+2];
    srand(1);
    for(int i = 0; i < (int)iterations; i++) {
        int length = (rand() + 40) % maxLength;
        for (int l = 0; l < length; l++)
          cString[l] = 1 + (char)(rand() % 0x70);
        cString[length] = 0;
        cString[length+1] = 0;
        // jstring refs[100];
        jstring ref;
        clock_gettime(CLOCK_MONOTONIC, &tstart);
        // for(int c = 0; c < 100; c++)
        ref = env->NewStringUTF(cString);
        clock_gettime(CLOCK_MONOTONIC, &tstop);
        tdiff = timespec_diff(tstart, tstop, env);
        // for(int c = 0; c < 100; c++)
        env->DeleteLocalRef(ref);
        addTime(env, time, tdiff);
    }
}

void
Java_com_itsec_jniapitester_ApiTestRunner_testGetStringLength(JNIEnv* env, jobject thiz, jlong iterations, jobject time) {
    timespec tstart, tstop, tdiff;
    int maxLength = 2048;
    jchar cString[maxLength+2];
    srand(1);
    for(int i = 0; i < (int)iterations; i++) {
        int length = (rand() + 40) % maxLength;
        for (int l = 0; l < length; l++)
            cString[l] = 1 + (jchar)(rand() % 0x70);
        cString[length] = 0;
        cString[length + 1] = 0;
        jstring ref = env->NewString(cString, length);
        timespec s;
        clock_gettime(CLOCK_MONOTONIC, &tstart);
        jint l = env->GetStringLength(ref);
        clock_gettime(CLOCK_MONOTONIC, &tstop);
        tdiff = timespec_diff(tstart, tstop, env);
        env->DeleteLocalRef(ref);
        addTime(env, time, tdiff);
    }
}

void
Java_com_itsec_jniapitester_ApiTestRunner_testGetStringRegion(JNIEnv* env, jobject thiz, jlong iterations, jobject time) {
    timespec tstart, tstop, tdiff;
    int maxLength = 2048;
    jchar cString[maxLength+2];
    jchar region[maxLength+2];
    srand(1);
    for(int i = 0; i < (int)iterations; i++) {
        int length = 2048;
        for (int l = 0; l < length; l++)
            cString[l] = 1 + (jchar)(rand() % 0x70);
        cString[length] = 0;
        cString[length + 1] = 0;

        int from = 40 + rand() % 1024;
        int to = from + 40 + (rand() % (2048 - from - 40));
        int len = to - from;

        jstring ref = env->NewString(cString, length);
        timespec s;
        clock_gettime(CLOCK_MONOTONIC, &tstart);
        env->GetStringRegion(ref, from, len, region);
        clock_gettime(CLOCK_MONOTONIC, &tstop);
        tdiff = timespec_diff(tstart, tstop, env);
        env->DeleteLocalRef(ref);
        addTime(env, time, tdiff);
    }
}

void
Java_com_itsec_jniapitester_ApiTestRunner_testGetStringUTFRegion(JNIEnv* env, jobject thiz, jlong iterations, jobject time) {
    timespec tstart, tstop, tdiff;
    int maxLength = 2048;
    char cString[maxLength+2];
    char region[maxLength+2];
    srand(1);
    for(int i = 0; i < (int)iterations; i++) {
        int length = 2048;
        for (int l = 0; l < length; l++)
          cString[l] = 1 + (char)(rand() % 0x70);
        cString[length] = 0;
        cString[length + 1] = 0;

        int from = 40 + rand() % 1024;
        int to = from + 40 + (rand() % (length - from - 40));
        int len = to - from;

        // LOGD("GetStringUTFRegion: from %d to %d, len: %d", from, to, len);

        jstring ref = env->NewStringUTF(cString);
        timespec s;
        clock_gettime(CLOCK_MONOTONIC, &tstart);
        env->GetStringUTFRegion(ref, from, len, region);
        clock_gettime(CLOCK_MONOTONIC, &tstop);
        tdiff = timespec_diff(tstart, tstop, env);
        env->DeleteLocalRef(ref);
        addTime(env, time, tdiff);
    }
}

void
Java_com_itsec_jniapitester_ApiTestRunner_testGetStringUTFLength(JNIEnv* env, jobject thiz, jlong iterations, jobject time) {
    timespec tstart, tstop, tdiff;
    int maxLength = 2048;
    char cString[maxLength+2];
    srand(1);
    for(int i = 0; i < (int)iterations; i++) {
        int length = (rand() + 40) % maxLength;
        for (int l = 0; l < length; l++)
          cString[l] = 1 + (unsigned char)((rand() % 0x70));
        cString[length] = 0;
        cString[length+1] = 0;

        jstring ref = env->NewStringUTF(cString);
        clock_gettime(CLOCK_MONOTONIC, &tstart);
        // for(int c = 0; c < 100; c++)
            jint l = env->GetStringUTFLength(ref);
        clock_gettime(CLOCK_MONOTONIC, &tstop);
        tdiff = timespec_diff(tstart, tstop, env);
        env->DeleteLocalRef(ref);
        addTime(env, time, tdiff);
    }
}

void
Java_com_itsec_jniapitester_ApiTestRunner_testGetStringChars(JNIEnv* env, jobject thiz, jlong iterations, jobject time) {
    timespec tstart, tstop, tdiff;
    int maxLength = 2048;
    jchar cString[maxLength+2];
    srand(1);
    for(int i = 0; i < (int)iterations; i++) {
        int length = (rand() + 40) % maxLength;
        for (int l = 0; l < length; l++)
            cString[l] = 1 + ((jchar) rand() % 0x70);
        cString[length] = 0;
        cString[length + 1] = 0;
        // const jchar* refs[100];
        const jchar* refs;
        jboolean isCopy;
        jstring ref = env->NewString(cString, length);
        clock_gettime(CLOCK_MONOTONIC, &tstart);
        // for(int c = 0; c < 100; c++)
        refs = env->GetStringChars(ref, &isCopy);
        clock_gettime(CLOCK_MONOTONIC, &tstop);
        tdiff = timespec_diff(tstart, tstop, env);
        addTime(env, time, tdiff);
        // for(int c = 0; c < 100; c++)
        env->ReleaseStringChars(ref, refs);
        env->DeleteLocalRef(ref);
    }
}

void
Java_com_itsec_jniapitester_ApiTestRunner_testGetStringCritical(JNIEnv* env, jobject thiz, jlong iterations, jobject time) {
    timespec tstart, tstop, tdiff;
    int maxLength = 2048;
    jchar cString[maxLength+2];
    srand(1);
    for(int i = 0; i < (int)iterations; i++) {
        int length = (rand() + 40) % maxLength;
        for (int l = 0; l < length; l++)
          cString[l] = 1 + (jchar)(rand() % 0x70);
        cString[length] = 0;
        cString[length+1] = 0;
        const jchar* refs;
        jboolean isCopy;
        jstring ref = env->NewString(cString, length);
        clock_gettime(CLOCK_MONOTONIC, &tstart);
        refs = env->GetStringCritical(ref, &isCopy);
        clock_gettime(CLOCK_MONOTONIC, &tstop);
        env->ReleaseStringCritical(ref, refs);
        tdiff = timespec_diff(tstart, tstop, env);
        addTime(env, time, tdiff);
        env->DeleteLocalRef(ref);
    }
}

void
Java_com_itsec_jniapitester_ApiTestRunner_testReleaseStringCritical(JNIEnv* env, jobject thiz, jlong iterations, jobject time) {
    timespec tstart, tstop, tdiff;
    int maxLength = 2048;
    jchar cString[maxLength+2];
    srand(1);
    for(int i = 0; i < (int)iterations; i++) {
        int length = (rand() + 40) % maxLength;
        for (int l = 0; l < length; l++)
          cString[l] = 1+(jchar)(rand() % 0x70);
        cString[length] = 0;
        cString[length + 1] = 0;
        const jchar* refs;
        jboolean isCopy;
        jstring ref = env->NewString(cString, length);
        refs = env->GetStringCritical(ref, &isCopy);
        clock_gettime(CLOCK_MONOTONIC, &tstart);
        env->ReleaseStringCritical(ref, refs);
        clock_gettime(CLOCK_MONOTONIC, &tstop);
        tdiff = timespec_diff(tstart, tstop, env);
        addTime(env, time, tdiff);
        env->DeleteLocalRef(ref);
    }
}

void
Java_com_itsec_jniapitester_ApiTestRunner_testGetStringUTFChars(JNIEnv* env, jobject thiz, jlong iterations, jobject time) {
    timespec tstart, tstop, tdiff;
    int maxLength = 2048;
    char cString[maxLength+2];
    srand(1);
    for(int i = 0; i < (int)iterations; i++) {
        int length = (rand() + 40) % maxLength;
        for (int l = 0; l < length; l++)
          cString[l] = 1 + (jchar)(rand() % 0x70);
        cString[length] = 0;
        cString[length + 1] = 0;
        // const char* refs[100];
        const char* refs;
        jboolean isCopy;
        jstring ref = env->NewStringUTF(cString);
        clock_gettime(CLOCK_MONOTONIC, &tstart);
        // for(int c = 0; c < 100; c++)
        refs = env->GetStringUTFChars(ref, &isCopy);
        clock_gettime(CLOCK_MONOTONIC, &tstop);
        tdiff = timespec_diff(tstart, tstop, env);
        // for(int c = 0; c < 100; c++)
        env->ReleaseStringUTFChars(ref, refs);
        env->DeleteLocalRef(ref);
        addTime(env, time, tdiff);
    }
}

void
Java_com_itsec_jniapitester_ApiTestRunner_testReleaseStringChars(JNIEnv* env, jobject thiz, jlong iterations, jobject time) {
    timespec tstart, tstop, tdiff;
    int maxLength = 2048;
    jchar cString[maxLength+2];
    srand(1);
    for(int i = 0; i < (int)iterations; i++) {
        int length = (rand() + 40) % maxLength;
        for (int l = 0; l < length; l++)
          cString[l] = 1 + (jchar)(rand() % 0x70);
        cString[length] = 0;
        cString[length + 1] = 0;
        // const jchar* refs[100];
        const jchar* refs;
        jboolean isCopy;
        jstring ref = env->NewString(cString, length);
        // for(int c = 0; c < 100; c++)
        refs = env->GetStringChars(ref, &isCopy);
        clock_gettime(CLOCK_MONOTONIC, &tstart);
        // for(int c = 0; c < 100; c++)
        env->ReleaseStringChars(ref, refs);
        clock_gettime(CLOCK_MONOTONIC, &tstop);
        tdiff = timespec_diff(tstart, tstop, env);
        env->DeleteLocalRef(ref);
        addTime(env, time, tdiff);
    }
}

void
Java_com_itsec_jniapitester_ApiTestRunner_testReleaseStringUTFChars(JNIEnv* env, jobject thiz, jlong iterations, jobject time) {
    timespec tstart, tstop, tdiff;
    int maxLength = 2048;
    char cString[maxLength+2];
    srand(1);
    for(int i = 0; i < (int)iterations; i++) {
        int length = (rand() + 40) % maxLength;
        for (int l = 0; l < length; l++)
          cString[l] = 1 + (char)(rand() % 0x70);
        cString[length] = 0;
        cString[length + 1] = 0;
        const char* refs;
        jboolean isCopy;
        jstring ref = env->NewStringUTF(cString);
        // for(int c = 0; c < 100; c++)
        refs = env->GetStringUTFChars(ref, &isCopy);
        clock_gettime(CLOCK_MONOTONIC, &tstart);
        // for(int c = 0; c < 100; c++)
        env->ReleaseStringUTFChars(ref, refs);
        clock_gettime(CLOCK_MONOTONIC, &tstop);
        tdiff = timespec_diff(tstart, tstop, env);
        env->DeleteLocalRef(ref);
        addTime(env, time, tdiff);
    }
}

jboolean
Java_com_itsec_jniapitester_ApiTestRunner_testGetVersion(JNIEnv* env, jobject thiz) {
	jint result = env->GetVersion();
	if (result == JNI_VERSION_1_6) {
		LOGD("GetVersion: SUCCESS");
		return JNI_TRUE;
	} else
		LOGD("GetVersion: FAIL (%d != %d)", result, JNI_VERSION_1_6);
	return JNI_FALSE;
}

  /* jclass
     Java_com_itsec_jniapitester_ApiTestRunner_testFindClass(JNIEnv* env, jobject thiz) {
     jclass result = env->FindClass("android/app/Activity");
     LOGD("android.app.Activity=%08x", (int)result);
     return result;
     }

     jclass
     Java_com_itsec_jniapitester_ApiTestRunner_testGetSuperclass(JNIEnv* env, jobject thiz, jclass clazz) {
     jclass result = env->GetSuperclass(clazz);
     LOGD("class %08x -> superclass %08x", (int)clazz, (int)result);
     return result;
     }

     jboolean
     Java_com_itsec_jniapitester_ApiTestRunner_testIsAssignableFrom(JNIEnv* env, jobject thiz, jclass clazz1, jclass clazz2) {
     jboolean result = env->IsAssignableFrom(clazz1, clazz2);
     LOGD("%08x assignable from %08x: %08x", (int)clazz1, (int)clazz2, (int)result);
     return result;
     } */

void
Java_com_itsec_jniapitester_ApiTestRunner_testAllocObject(JNIEnv* env, jobject thiz, jobjectArray jobjArray, jint size, jlong iterations, jobject time) {
  timespec tstart, tstop, tdiff;
  jobject result;
  jobject jobj;
  jclass jcls;
  jint index;
  srand(1);
  for(jlong i = 0; i < iterations; i++) {
    index = (jint)rand() % size;
    jobj = env->GetObjectArrayElement(jobjArray, index);
    jcls = env->GetObjectClass(jobj);
    clock_gettime(CLOCK_MONOTONIC, &tstart);
    result = env->AllocObject(jcls);
    clock_gettime(CLOCK_MONOTONIC, &tstop);
    tdiff = timespec_diff(tstart, tstop, env);
    addTime(env, time, tdiff);
    env->DeleteLocalRef(result);
    env->DeleteLocalRef(jobj);
    env->DeleteLocalRef(jcls);
  }
}

void
Java_com_itsec_jniapitester_ApiTestRunner_testNewObject(JNIEnv* env, jobject thiz, jobjectArray jobjArray, jint size, jlong iterations, jobject time) {
  timespec tstart, tstop, tdiff;
  jobject result;
  jclass jcls;
  jmethodID jmId;
  jobject jobj;
  jint index;
  srand(1);
  for(jlong i = 0; i < iterations; i++) {
    index = (jint)rand() % ((int) size);
    jobj = env->GetObjectArrayElement(jobjArray, index);
    jcls = env->GetObjectClass(jobj);
    jmId = env->GetMethodID(jcls, "<init>", "()V");
    clock_gettime(CLOCK_MONOTONIC, &tstart);
    result = env->NewObject(jcls, jmId);
    clock_gettime(CLOCK_MONOTONIC, &tstop);
    tdiff = timespec_diff(tstart, tstop, env);
    addTime(env, time, tdiff);
    env->DeleteLocalRef(result);
    env->DeleteLocalRef(jobj);
    env->DeleteLocalRef(jcls);
  }
}

jthrowable
Java_com_itsec_jniapitester_ApiTestRunner_testExceptionOccurred_old(JNIEnv* env, jobject thiz, jclass jc) {
	jint e = env->ThrowNew(jc, "cheeky exception");
	if (e!=0) LOGE("Unexpected result of throwing exception: %d", e);
	jthrowable jt = env->ExceptionOccurred();
	env->ExceptionDescribe();
	env->ExceptionClear();
	env->ExceptionDescribe();
	return jt;
}

jboolean
Java_com_itsec_jniapitester_ApiTestRunner_testExceptionCheck_old(JNIEnv* env, jobject thiz, jclass jc) {
	jint e = env->ThrowNew(jc, "check me if you can");
	return env->ExceptionCheck();
}

jshort
Java_com_itsec_jniapitester_ApiTestRunner_testCallStaticShortMethod(JNIEnv* env, jobject thiz, jclass jc) {
	someMethodID = env->GetStaticMethodID(jc, "staticNum", "(S)S");
	jshort param = 498;
	jshort result = env->CallStaticShortMethodA(jc, someMethodID, (jvalue*)&param);
	return result;
}


jint
Java_com_itsec_jniapitester_ApiTestRunner_testGetFieldID(JNIEnv* env, jobject thiz, jobject jobj) {
	jclass jc = env->GetObjectClass(jobj);
	someFieldID = env->GetFieldID(jc, "myField", "I");
	if (someFieldID == 0) return 0;
	jint secret = env->GetIntField(jobj, someFieldID);
	if (secret == 43) return 3;
	else return 0;
}

jint
Java_com_itsec_jniapitester_ApiTestRunner_testFromReflectedField(JNIEnv* env, jobject thiz, jobject jobj, jobject jfield) {
	someFieldID = env->FromReflectedField(jfield);
	if (someFieldID == 0) return 0;
	jint secret = env->GetIntField(jobj, someFieldID);
	if (secret == 43) return 1;
	else return 0;
}

  /* jint
Java_com_itsec_jniapitester_ApiTestRunner_testGetObjectField(JNIEnv* env, jobject thiz, jobject jobj, jobject result) {
	jclass jc = env->GetObjectClass(jobj);
	someFieldID = env->GetFieldID(jc, "inception", "Lcom/itsec/jniapitester/TestObject;");
	if (someFieldID == 0) return 0;
	jobject inception = env->GetObjectField(jobj, someFieldID);
	if (result == inception) return 1;
	LOGD("FAIL GetObjectField: %08x != %08x", (int)result, (int)inception);
	return 0;
    }*/

void
Java_com_itsec_jniapitester_ApiTestRunner_testGetJNIEnv(JNIEnv* env, jobject thiz, jlong iterations, jobject time) {
    JavaVM *jvm;
    void *env2;
    timespec tstart, tstop, tdiff;
    jint result = env->GetJavaVM(&jvm);
    for(jlong c = 0; c < iterations; c++) {
        clock_gettime(CLOCK_MONOTONIC, &tstart);
        result = jvm->GetEnv(&env2, JNI_VERSION_1_2);
        clock_gettime(CLOCK_MONOTONIC, &tstop);
        if (result != JNI_OK)
            LOGD("testGetJNIEnv has a problem");
        tdiff = timespec_diff(tstart, tstop, env);
        addTime(env, time, tdiff);
    }
}

void
Java_com_itsec_jniapitester_ApiTestRunner_testGetDestroyJVM(JNIEnv* env, jobject thiz, jlong iterations, jobject time) {
    JavaVM *jvm;
    timespec tstart, tstop, tdiff;
    for(jlong c = 0; c < iterations; c++) {
        jint result = env->GetJavaVM(&jvm);
        clock_gettime(CLOCK_MONOTONIC, &tstart);
        clock_gettime(CLOCK_MONOTONIC, &tstop);
        tdiff = timespec_diff(tstart, tstop, env);
        addTime(env, time, tdiff);
    }
}

void
Java_com_itsec_jniapitester_ApiTestRunner_testGetClass(JNIEnv* env, jobject thiz, jobjectArray jobjArray, jint size, jlong iterations, jobject time) {
  JavaVM *jvm;
    timespec tstart, tstop, tdiff;
    jobject jobj;
    srand(1);
    LOGD(">>>>>>> APITESTRUNNER::TESTGETCLASS");
    for(long i = 0; i < (long)iterations; i++) {
        jint index = (jint)rand() % size;
        LOGD(">>>>>>> ENTER NEW ITERATION");
        jobj = env->GetObjectArrayElement(jobjArray, index);
        jclass refs;
        clock_gettime(CLOCK_MONOTONIC, &tstart);
        refs = env->GetObjectClass(jobj);
        clock_gettime(CLOCK_MONOTONIC, &tstop);
        env->DeleteLocalRef(refs);
        tdiff = timespec_diff(tstart, tstop, env);
        addTime(env, time, tdiff);
        env->DeleteLocalRef(jobj);
        LOGD(">>>>>>> LEAVE ITERATION");
    }
    LOGD("<<<<<< APITESTRUNNER::TESTGETCLASS");
}

void
Java_com_itsec_jniapitester_ApiTestRunner_testExceptionOccurred(JNIEnv* env, jobject thiz, jobjectArray jobjArray, jint size, jlong iterations, jobject time) {
    JavaVM *jvm;
    timespec tstart, tstop, tdiff;
    jobject jobj;
    jclass jc;
    jthrowable occurred;
    jmethodID id;
    srand(1);
    for(long i = 0; i < (long)iterations; i++) {
        jint index = (jint)rand() % size;
        jobj = env->GetObjectArrayElement(jobjArray, index);
        jc = env->GetObjectClass(jobj);
        id = env->GetMethodID(jc, "callThrowException", "()V");
        env->CallVoidMethod(jobj, id);
        clock_gettime(CLOCK_MONOTONIC, &tstart);
        occurred = env->ExceptionOccurred();
        clock_gettime(CLOCK_MONOTONIC, &tstop);
        tdiff = timespec_diff(tstart, tstop, env);
        env->ExceptionClear();
        addTime(env, time, tdiff);
        env->DeleteLocalRef(occurred);
        env->DeleteLocalRef(jc);
        env->DeleteLocalRef(jobj);
    }
}

void
Java_com_itsec_jniapitester_ApiTestRunner_testExceptionClear(JNIEnv* env, jobject thiz, jobjectArray jobjArray, jint size, jlong iterations, jobject time) {
    JavaVM *jvm;
    timespec tstart, tstop, tdiff;
    jobject jobj;
    jclass jc;
    jthrowable occurred;
    jmethodID id;
    srand(1);
    for(long i = 0; i < (long)iterations; i++) {
        int index = rand() % ((int) size);
        jobj = env->GetObjectArrayElement(jobjArray, index);
        jc = env->GetObjectClass(jobj);
        id = env->GetMethodID(jc, "callThrowException", "()V");
        env->CallVoidMethod(jobj, id);
        clock_gettime(CLOCK_MONOTONIC, &tstart);
        env->ExceptionClear();
        clock_gettime(CLOCK_MONOTONIC, &tstop);
        tdiff = timespec_diff(tstart, tstop, env);
        addTime(env, time, tdiff);
        env->DeleteLocalRef(jc);
        env->DeleteLocalRef(jobj);
    }
}

void
Java_com_itsec_jniapitester_ApiTestRunner_testExceptionDescribe(JNIEnv* env, jobject thiz, jobjectArray jobjArray, jint size, jlong iterations, jobject time) {
    JavaVM *jvm;
    timespec tstart, tstop, tdiff;
    jobject jobj;
    jclass jc;
    jthrowable occurred;
    jmethodID id;
    srand(1);
    for(long i = 0; i < (long)iterations; i++) {
        int index = rand() % ((int) size);
        jobj = env->GetObjectArrayElement(jobjArray, index);
        jc = env->GetObjectClass(jobj);
        id = env->GetMethodID(jc, "callThrowException", "()V");
        env->CallVoidMethod(jobj, id);
        clock_gettime(CLOCK_MONOTONIC, &tstart);
        env->ExceptionDescribe();
        clock_gettime(CLOCK_MONOTONIC, &tstop);
        env->ExceptionClear();
        tdiff = timespec_diff(tstart, tstop, env);
        addTime(env, time, tdiff);
        env->DeleteLocalRef(jc);
        env->DeleteLocalRef(jobj);
    }
}

void
Java_com_itsec_jniapitester_ApiTestRunner_testExceptionCheck(JNIEnv* env, jobject thiz, jobjectArray jobjArray, jint size, jlong iterations, jobject time) {
    JavaVM *jvm;
    timespec tstart, tstop, tdiff;
    jobject jobj;
    jclass jc;
    jboolean exists;
    jmethodID id;
    srand(1);
    for(long i = 0; i < (long)iterations; i++) {
        int index = rand() % ((int) size);
        jobj = env->GetObjectArrayElement(jobjArray, index);
        jc = env->GetObjectClass(jobj);
        id = env->GetMethodID(jc, "callThrowException", "()V");
        env->CallVoidMethod(jobj, id);
        clock_gettime(CLOCK_MONOTONIC, &tstart);
        exists = env->ExceptionCheck();
        clock_gettime(CLOCK_MONOTONIC, &tstop);
        env->ExceptionClear();
        tdiff = timespec_diff(tstart, tstop, env);
        addTime(env, time, tdiff);
        env->DeleteLocalRef(jc);
        env->DeleteLocalRef(jobj);
    }
}

void
Java_com_itsec_jniapitester_ApiTestRunner_testThrow(JNIEnv* env, jobject thiz, jobjectArray jobjArray, jint size, jlong iterations, jobject time) {
    JavaVM *jvm;
    timespec tstart, tstop, tdiff;
    jobject jobj;
    jclass jc;
    jthrowable occurred;
    jmethodID id;
    srand(1);
    for(long i = 0; i < (long)iterations; i++) {
        int index = rand() % ((int) size);
        jobj = env->GetObjectArrayElement(jobjArray, index);
        jc = env->GetObjectClass(jobj);
        id = env->GetMethodID(jc, "callThrowException", "()V");
        env->CallVoidMethod(jobj, id);
        occurred = env->ExceptionOccurred();
        env->ExceptionClear();
        clock_gettime(CLOCK_MONOTONIC, &tstart);
        env->Throw(occurred);
        clock_gettime(CLOCK_MONOTONIC, &tstop);
        env->ExceptionClear();
        tdiff = timespec_diff(tstart, tstop, env);
        addTime(env, time, tdiff);
        env->DeleteLocalRef(occurred);
        env->DeleteLocalRef(jc);
        env->DeleteLocalRef(jobj);
    }
}

void
Java_com_itsec_jniapitester_ApiTestRunner_testThrowNew(JNIEnv* env, jobject thiz, jobjectArray jobjArray, jint size, jlong iterations, jobject time) {
    JavaVM *jvm;
    timespec tstart, tstop, tdiff;
    jobject jobj;
    jclass jc;
    jthrowable occurred;
    jmethodID id;
    jint r;
    srand(1);
    for(long i = 0; i < (long)iterations; i++) {
        int index = rand() % ((int) size);
        jc = env->FindClass("com/itsec/jniapitester/TestException");
        clock_gettime(CLOCK_MONOTONIC, &tstart);
        r = env->ThrowNew(jc, "Exception generated by testThrowNew in ApiTestRunner");
        clock_gettime(CLOCK_MONOTONIC, &tstop);
        env->ExceptionClear();
        tdiff = timespec_diff(tstart, tstop, env);
        addTime(env, time, tdiff);
        env->DeleteLocalRef(jc);
    }
}

void
Java_com_itsec_jniapitester_ApiTestRunner_testGetMethodID(JNIEnv* env, jobject thiz, jobjectArray jobjArray, jint size, jlong iterations, jobject time) {
    timespec tstart, tstop, tdiff;
    jclass jc;
    jobject jobj;
    srand(1);
    for(long i = 0; i < (long)iterations; i++) {
        int index = rand() % ((int) size);
        jobj = env->GetObjectArrayElement(jobjArray, index);
        jc = env->GetObjectClass(jobj);
        int m = rand() % 5;
        jmethodID id;
        switch(m) {
            case 0:
                clock_gettime(CLOCK_MONOTONIC, &tstart);
                // for(int c = 0; c < 100; c++)
                    id = env->GetMethodID(jc, "callSomethingVoid", "(I)V");
                clock_gettime(CLOCK_MONOTONIC, &tstop);
                break;
            case 1:
                clock_gettime(CLOCK_MONOTONIC, &tstart);
                // // for(int c = 0; c < 100; c++)
                    id = env->GetMethodID(jc, "callSomethingByte", "()B");
                clock_gettime(CLOCK_MONOTONIC, &tstop);
                break;
            case 2:
                clock_gettime(CLOCK_MONOTONIC, &tstart);
                // for(int c = 0; c < 100; c++)
                    id = env->GetMethodID(jc, "callSomethingInt", "()I");
                clock_gettime(CLOCK_MONOTONIC, &tstop);
                break;
            case 3:
                clock_gettime(CLOCK_MONOTONIC, &tstart);
                // for(int c = 0; c < 100; c++)
                    id = env->GetMethodID(jc, "callSomethingBoolean", "(ID)Z");
                clock_gettime(CLOCK_MONOTONIC, &tstop);
                break;
            case 4:
                clock_gettime(CLOCK_MONOTONIC, &tstart);
                // for(int c = 0; c < 100; c++)
                    id = env->GetMethodID(jc, "callSomethingObject", "()Ljava/lang/Object;");
                clock_gettime(CLOCK_MONOTONIC, &tstop);
                break;
        }
        tdiff = timespec_diff(tstart, tstop, env);
        addTime(env, time, tdiff);
        env->DeleteLocalRef(jobj);
        env->DeleteLocalRef(jc);
    }
}

void
Java_com_itsec_jniapitester_ApiTestRunner_testGetStaticMethodID(JNIEnv* env, jobject thiz, jobjectArray jobjArray, jint size, jlong iterations, jobject time) {
    timespec tstart, tstop, tdiff;
    jclass jc;
    jobject jobj;
    srand(1);
    for(long i = 0; i < (long)iterations; i++) {
        int index = rand() % ((int) size);
        jobj = env->GetObjectArrayElement(jobjArray, index);
        jc = env->GetObjectClass(jobj);
        int m = rand() % 5;
        jmethodID id;
        switch(m) {
            case 0:
                clock_gettime(CLOCK_MONOTONIC, &tstart);
                id = env->GetStaticMethodID(jc, "callStaticSomethingVoid", "(I)V");
                clock_gettime(CLOCK_MONOTONIC, &tstop);
                break;
            case 1:
                clock_gettime(CLOCK_MONOTONIC, &tstart);
                // // for(int c = 0; c < 100; c++)
                    id = env->GetStaticMethodID(jc, "callStaticSomethingByte", "()B");
                clock_gettime(CLOCK_MONOTONIC, &tstop);
                break;
            case 2:
                clock_gettime(CLOCK_MONOTONIC, &tstart);
                // for(int c = 0; c < 100; c++)
                    id = env->GetStaticMethodID(jc, "callStaticSomethingInt", "()I");
                clock_gettime(CLOCK_MONOTONIC, &tstop);
                break;
            case 3:
                clock_gettime(CLOCK_MONOTONIC, &tstart);
                // for(int c = 0; c < 100; c++)
                    id = env->GetStaticMethodID(jc, "callStaticSomethingBoolean", "(ID)Z");
                clock_gettime(CLOCK_MONOTONIC, &tstop);
                break;
            case 4:
                clock_gettime(CLOCK_MONOTONIC, &tstart);
                // for(int c = 0; c < 100; c++)
                    id = env->GetStaticMethodID(jc, "callStaticSomethingObject", "()Ljava/lang/Object;");
                clock_gettime(CLOCK_MONOTONIC, &tstop);
                break;
        }
        tdiff = timespec_diff(tstart, tstop, env);
        addTime(env, time, tdiff);
        env->DeleteLocalRef(jobj);
        env->DeleteLocalRef(jc);
    }
}

void
Java_com_itsec_jniapitester_ApiTestRunner_testCallMethod(JNIEnv* env, jobject thiz, jobjectArray jobjArray, jint size, jlong iterations, jobject time) {
    timespec tstart, tstop, tdiff;
    jclass jc;
    jobject jobj;
    srand(1);
    for(long i = 0; i < (long)iterations; i++) {
        int index = rand() % ((int) size);
        jobj = env->GetObjectArrayElement(jobjArray, index);
        jc = env->GetObjectClass(jobj);
        int m = rand() % 5;
        jmethodID id;
        switch(m) {
            case 0:
                id = env->GetMethodID(jc, "callSomethingVoid", "(I)V");
                clock_gettime(CLOCK_MONOTONIC, &tstart);
                // for(int c = 0; c < 100; c++)
                    env->CallVoidMethod(jobj, id);
                clock_gettime(CLOCK_MONOTONIC, &tstop);
                break;
            case 1:
                id = env->GetMethodID(jc, "callSomethingByte", "()B");
                clock_gettime(CLOCK_MONOTONIC, &tstart);
                // for(int c = 0; c < 100; c++)
                    env->CallByteMethod(jobj, id);
                clock_gettime(CLOCK_MONOTONIC, &tstop);
                break;
            case 2:
                id = env->GetMethodID(jc, "callSomethingInt", "()I");
                clock_gettime(CLOCK_MONOTONIC, &tstart);
                // for(int c = 0; c < 100; c++)
                    env->CallIntMethod(jobj, id);
                clock_gettime(CLOCK_MONOTONIC, &tstop);
                break;
            case 3:
                id = env->GetMethodID(jc, "callSomethingBoolean", "(ID)Z");
                clock_gettime(CLOCK_MONOTONIC, &tstart);
                // for(int c = 0; c < 100; c++)
                    env->CallBooleanMethod(jobj, id, 0, 0.1);
                clock_gettime(CLOCK_MONOTONIC, &tstop);
                break;
            case 4:
                id = env->GetMethodID(jc, "callSomethingObject", "()Ljava/lang/Object;");
                jobject refs;
                clock_gettime(CLOCK_MONOTONIC, &tstart);
                // for(int c = 0; c < 100; c++)
                    refs = env->CallObjectMethod(jobj, id);
                clock_gettime(CLOCK_MONOTONIC, &tstop);
                // for(int c = 0; c < 100; c++)
                    env->DeleteLocalRef(refs);
                break;
        }
        tdiff = timespec_diff(tstart, tstop, env);
        addTime(env, time, tdiff);
        env->DeleteLocalRef(jobj);
        env->DeleteLocalRef(jc);
    }
}

void
Java_com_itsec_jniapitester_ApiTestRunner_testCallStaticMethod(JNIEnv* env, jobject thiz, jobjectArray jobjArray, jint size, jlong iterations, jobject time) {
    timespec tstart, tstop, tdiff;
    jclass jc;
    jobject jobj;
    srand(1);
    for(long i = 0; i < (long)iterations; i++) {
        int index = rand() % ((int) size);
        jobj = env->GetObjectArrayElement(jobjArray, index);
        jc = env->GetObjectClass(jobj);
        int m = rand() % 5;
        jmethodID id;
        switch(m) {
            case 0:
                id = env->GetStaticMethodID(jc, "callStaticSomethingVoid", "(I)V");
                clock_gettime(CLOCK_MONOTONIC, &tstart);
                env->CallStaticVoidMethod(jc, id);
                clock_gettime(CLOCK_MONOTONIC, &tstop);
                break;
            case 1:
                id = env->GetStaticMethodID(jc, "callStaticSomethingByte", "()B");
                clock_gettime(CLOCK_MONOTONIC, &tstart);
                env->CallStaticByteMethod(jc, id);
                clock_gettime(CLOCK_MONOTONIC, &tstop);
                break;
            case 2:
                id = env->GetStaticMethodID(jc, "callStaticSomethingInt", "()I");
                clock_gettime(CLOCK_MONOTONIC, &tstart);
                env->CallStaticIntMethod(jc, id);
                clock_gettime(CLOCK_MONOTONIC, &tstop);
                break;
            case 3:
                id = env->GetStaticMethodID(jc, "callStaticSomethingBoolean", "(ID)Z");
                clock_gettime(CLOCK_MONOTONIC, &tstart);
                env->CallStaticBooleanMethod(jc, id, 0, 0.1);
                clock_gettime(CLOCK_MONOTONIC, &tstop);
                break;
            case 4:
                id = env->GetStaticMethodID(jc, "callStaticSomethingObject", "()Ljava/lang/Object;");
                jobject refs;
                clock_gettime(CLOCK_MONOTONIC, &tstart);
                refs = env->CallStaticObjectMethod(jc, id);
                clock_gettime(CLOCK_MONOTONIC, &tstop);
                env->DeleteLocalRef(refs);
                break;
        }
        tdiff = timespec_diff(tstart, tstop, env);
        addTime(env, time, tdiff);
        env->DeleteLocalRef(jobj);
        env->DeleteLocalRef(jc);
    }
}

void
Java_com_itsec_jniapitester_ApiTestRunner_testCallNonvirtualMethod(JNIEnv* env, jobject thiz, jobjectArray jobjArray, jint size, jlong iterations, jobject time) {
    timespec tstart, tstop, tdiff;
    jclass jc;
    jobject jobj;
    srand(1);
    for(long i = 0; i < (long)iterations; i++) {
        int index = rand() % ((int) size);
        jobj = env->GetObjectArrayElement(jobjArray, index);
        jc = env->FindClass("com/itsec/jniapitester/TestObject");
        int m = rand() % 5;
        jmethodID id;
        switch(m) {
            case 0:
                id = env->GetMethodID(jc, "callSomethingVoid", "(I)V");
                clock_gettime(CLOCK_MONOTONIC, &tstart);
                // for(int c = 0; c < 100; c++)
                    env->CallNonvirtualVoidMethod(jobj, jc, id);
                clock_gettime(CLOCK_MONOTONIC, &tstop);
                break;
            case 1:
                id = env->GetMethodID(jc, "callSomethingByte", "()B");
                clock_gettime(CLOCK_MONOTONIC, &tstart);
                // for(int c = 0; c < 100; c++)
                    env->CallNonvirtualByteMethod(jobj, jc, id);
                clock_gettime(CLOCK_MONOTONIC, &tstop);
                break;
            case 2:
                id = env->GetMethodID(jc, "callSomethingInt", "()I");
                clock_gettime(CLOCK_MONOTONIC, &tstart);
                // for(int c = 0; c < 100; c++)
                    env->CallNonvirtualIntMethod(jobj, jc, id);
                clock_gettime(CLOCK_MONOTONIC, &tstop);
                break;
            case 3:
                id = env->GetMethodID(jc, "callSomethingBoolean", "(ID)Z");
                clock_gettime(CLOCK_MONOTONIC, &tstart);
                // for(int c = 0; c < 100; c++)
                    env->CallNonvirtualBooleanMethod(jobj, jc, id, 0, 0.1);
                clock_gettime(CLOCK_MONOTONIC, &tstop);
                break;
            case 4:
                id = env->GetMethodID(jc, "callSomethingObject", "()Ljava/lang/Object;");
                jobject refs;
                clock_gettime(CLOCK_MONOTONIC, &tstart);
                // for(int c = 0; c < 100; c++)
                    refs = env->CallNonvirtualObjectMethod(jobj, jc, id);
                clock_gettime(CLOCK_MONOTONIC, &tstop);
                // for(int c = 0; c < 100; c++)
                    env->DeleteLocalRef(refs);
                break;
        }
        tdiff = timespec_diff(tstart, tstop, env);
        addTime(env, time, tdiff);
        env->DeleteLocalRef(jobj);
        env->DeleteLocalRef(jc);
    }
}

void
Java_com_itsec_jniapitester_ApiTestRunner_testRegisterNatives(JNIEnv* env, jobject thiz, jobjectArray jobjArray, jint size, jlong iterations, jobject time) {
    timespec tstart, tstop, tdiff;
    jclass jc;
    jobject jobj;
    srand(1);
    for(int i = 0; i < (int)iterations; i++) {
        jint index = (jint)i % size;// rand() % ((int) size);
        jobj = env->GetObjectArrayElement(jobjArray, index);
        jc = env->GetObjectClass(jobj);
        clock_gettime(CLOCK_MONOTONIC, &tstart);
        // for(int c = 0; c < 100; c++)
            env->RegisterNatives(jc, methods, 6);
        clock_gettime(CLOCK_MONOTONIC, &tstop);
        tdiff = timespec_diff(tstart, tstop, env);
        addTime(env, time, tdiff);

        // TODO: This may be required but it generates output which influences runtime
        // env->UnregisterNatives(jc);

        env->DeleteLocalRef(jobj);
        env->DeleteLocalRef(jc);
    }
}

#define GET_TYPE_FIELD(_ctype, _jname, _field, _signature) \
    void Java_com_itsec_jniapitester_ApiTestRunner_testGet##_jname##Field(JNIEnv* env,                 \
                       jobject thiz, jobjectArray jobjArray, jint size, jlong iterations, jobject time) {  \
        jclass jc; \
        jobject jobj; \
        srand(1); \
        for(int i = 0; i < (int)iterations; i++) { \
            int index = rand() % size; \
            jobj = env->GetObjectArrayElement(jobjArray, index); \
            jc = env->GetObjectClass(jobj); \
            jfieldID someFieldID = env->GetFieldID(jc, _field, _signature);\
            _ctype fieldContent; \
            timespec tstart, tstop, tdiff; \
            clock_gettime( CLOCK_MONOTONIC , &tstart); \
            fieldContent = env->Get##_jname##Field(jobj, someFieldID); \
            clock_gettime( CLOCK_MONOTONIC , &tstop ) ; \
            tdiff = timespec_diff(tstart, tstop, env); \
            addTime(env, time, tdiff); \
            env->DeleteLocalRef(jobj); \
            env->DeleteLocalRef(jc); \
        } \
}
GET_TYPE_FIELD(jboolean, Boolean, "m_booleanField", "Z");
GET_TYPE_FIELD(jshort, Short, "m_shortField", "S");
GET_TYPE_FIELD(jbyte, Byte, "m_byteField", "B");
GET_TYPE_FIELD(jchar, Char, "m_charField", "C");
GET_TYPE_FIELD(jint, Int, "m_intField", "I");
GET_TYPE_FIELD(jfloat, Float, "m_floatField", "F");
GET_TYPE_FIELD(jlong, Long, "m_longField", "J");
GET_TYPE_FIELD(jdouble, Double, "m_doubleField", "D");

  void Java_com_itsec_jniapitester_ApiTestRunner_testGetObjectField(JNIEnv* env, jobject thiz, jobjectArray jobjArray, jint size, jlong iterations, jobject time) {
    jclass jc;
    jobject jobj;
    srand(1);
    for(int i = 0; i < (int)iterations; i++) {
      int index = rand() % size;
      jobj = env->GetObjectArrayElement(jobjArray, index);
      jc = env->GetObjectClass(jobj);
      jfieldID fieldID = env->GetFieldID(jc, "m_objectField", "Lcom/itsec/jniapitester/TestObject;");
      jobject fieldContent;
      timespec tstart, tstop, tdiff;
      clock_gettime( CLOCK_MONOTONIC , &tstart);
      fieldContent = env->GetObjectField(jobj, fieldID);
      clock_gettime( CLOCK_MONOTONIC , &tstop ) ;
      tdiff = timespec_diff(tstart, tstop, env);
      addTime(env, time, tdiff);
      env->DeleteLocalRef(fieldContent);
      env->DeleteLocalRef(jobj);
      env->DeleteLocalRef(jc);
    }
  }

  void Java_com_itsec_jniapitester_ApiTestRunner_testSetObjectField(JNIEnv* env, jobject thiz, jobjectArray jobjArray, jint size, jlong iterations, jobject time) {
    jclass jc;
    jobject jobj1, jobj2;
    srand(1);
    for(int i = 0; i < (int)iterations; i++) {
      jint index1 = (jint)rand() % size;
      jint index2 = (jint)rand() % size;
      jobj1 = env->GetObjectArrayElement(jobjArray, index1);
      jobj2 = env->GetObjectArrayElement(jobjArray, index2);
      jc = env->GetObjectClass(jobj1);
      jfieldID fieldID = env->GetFieldID(jc, "m_objectField", "Lcom/itsec/jniapitester/TestObject;");
      jobject fieldContent;
      timespec tstart, tstop, tdiff;
      clock_gettime( CLOCK_MONOTONIC , &tstart);
      env->SetObjectField(jobj1, fieldID, jobj2);
      clock_gettime( CLOCK_MONOTONIC , &tstop ) ;
      tdiff = timespec_diff(tstart, tstop, env);
      addTime(env, time, tdiff);
      env->DeleteLocalRef(jobj1);
      env->DeleteLocalRef(jobj2);
      env->DeleteLocalRef(jc);
    }
  }

#define SET_TYPE_FIELD(_ctype, _jname, _field, _signature, _value) \
    void Java_com_itsec_jniapitester_ApiTestRunner_testSet##_jname##Field(JNIEnv* env,                 \
                       jobject thiz, jobjectArray jobjArray, jint size, jlong iterations, jobject time) {  \
        jclass jc; \
        jobject jobj; \
        timespec tstart, tstop, tdiff; \
        srand(1); \
        for(int i = 0; i < (int)iterations; i++) { \
            jint index = rand() % size; \
            jobj = env->GetObjectArrayElement(jobjArray, index); \
            jc = env->GetObjectClass(jobj); \
            someFieldID = env->GetFieldID(jc, _field, _signature);\
            _ctype fieldContent = _value; \
            clock_gettime( CLOCK_MONOTONIC , &tstart); \
            env->Set##_jname##Field(jobj, someFieldID, fieldContent); \
            clock_gettime( CLOCK_MONOTONIC , &tstop ) ; \
            tdiff = timespec_diff(tstart, tstop, env); \
            addTime(env, time, tdiff); \
            env->DeleteLocalRef(jobj); \
            env->DeleteLocalRef(jc); \
        } \
}
SET_TYPE_FIELD(jboolean, Boolean, "m_booleanField", "Z", true);
SET_TYPE_FIELD(jshort, Short, "m_shortField", "S", 123);
SET_TYPE_FIELD(jbyte, Byte, "m_byteField", "B", 128);
SET_TYPE_FIELD(jchar, Char, "m_charField", "C", 'a');
SET_TYPE_FIELD(jint, Int, "m_intField", "I", 65000);
SET_TYPE_FIELD(jfloat, Float, "m_floatField", "F", 0.112);
SET_TYPE_FIELD(jlong, Long, "m_longField", "J", 0xdeadbeef);
SET_TYPE_FIELD(jdouble, Double, "m_doubleField", "D", 0.2345);

void
Java_com_itsec_jniapitester_ApiTestRunner_testCallSomethingByte(JNIEnv* env, jobject thiz, jclass jc) {
	someMethodID = env->GetMethodID(jc, "callSomethingByte", "()B");

}

  /* jboolean
Java_com_itsec_jniapitester_ApiTestRunner_testCallSomethingBoolean(JNIEnv* env, jobject thiz, jobject jobj, jclass jc, jint i, jdouble d) {
	LOGD("now calling env->GetMethodID");
	someMethodID = env->GetMethodID(jc, "callSomethingInt", "()I");
	if (someMethodID == 0)
		return JNI_FALSE;
	LOGD("now calling env->CallIntMethod(jobj=%08x)", (int)jobj);
	jint ii = env->CallIntMethod(jobj, someMethodID);
	if (ii == 86) {
		LOGD("CallIntMethod: SUCCESS");
	} else LOGD("CallIntMethod: FAIL (unexpected result %d!=86)", ii);
	va_list list;
	LOGD("now calling env->CallIntMethodV(jobj=%08x)", (int)jobj);
	ii = env->CallIntMethodV(jobj, someMethodID, list);
	if (ii == 86) {
		LOGD("CallIntMethodV: SUCCESS");
	} else LOGD("CallIntMethodV: FAIL (unexpected result %d!=86)", ii);
	jvalue* args = (jvalue*)malloc(2*sizeof(jvalue));
	LOGD("now calling env->CallIntMethodA(jobj=%08x)", (int)jobj);
	ii = env->CallIntMethodA(jobj, someMethodID, args);
	if (ii == 86) {
		LOGD("CallIntMethodA: SUCCESS");
	} else LOGD("CallIntMethodA: FAIL (unexpected result %d!=86)", ii);

	someMethodID = env->GetMethodID(jc, "callSomethingBoolean", "(ID)Z");
	if (someMethodID == 0)
		return JNI_FALSE;
	LOGD("now calling env->CallBooleanMethod");
	jboolean result = env->CallBooleanMethod(jobj, someMethodID, i, d);
	args[0].i=i;
	args[1].d=d;
	LOGD("now calling env->CallBooleanMethodA");
	result = env->CallBooleanMethodA(jobj, someMethodID, args);
	return JNI_TRUE;
}

jboolean
Java_com_itsec_jniapitester_ApiTestRunner_testCallNonvirtual(JNIEnv* env, jobject thiz, jobject jobj, jclass jc) {
	LOGD("now calling env->CallNonvirtualObjectMethodA()");
	jvalue* args = 0;
	someMethodID = env->GetMethodID(jc, "callSomethingObject", "()Ljava/lang/Object;");
	if (someMethodID == 0) return JNI_FALSE;
	jobject o = env->CallNonvirtualObjectMethodA(jobj, jc, someMethodID, args);
	return JNI_TRUE;
}

jboolean
Java_com_itsec_jniapitester_ApiTestRunner_testIsInstanceOf(JNIEnv* env, jobject thiz, jobject jobj, jclass jc) {
	return env->IsInstanceOf(jobj, jc);
}

jboolean Java_com_itsec_jniapitester_ApiTestRunner_testNewObject(JNIEnv* env, jobject thiz, jclass jc) {
	jmethodID methodID = env->GetMethodID(jc, "<init>", "(I)V");
	LOGD("constructor methodID=%08x", (int)methodID);
	jobject newOne = env->NewObject(jc, methodID, 49);
	return JNI_TRUE;
}

void Java_com_itsec_jniapitester_ApiTestRunner_testArrayCritical(JNIEnv* env, jobject thiz, jshortArray jsa) {
	jboolean b = JNI_TRUE;
	LOGD("calling GetPrimitiveArrayCritical with jarr=%08x boolean=%08x", (int)jsa, (int)(&b));
	jshort* shorts = (jshort*)(env->GetPrimitiveArrayCritical(jsa, &b));
	LOGD("received data=%08x", (int)shorts);
	LOGD("shorts[0]=%d, shorts[1]=%d", shorts[0], shorts[1]);
	shorts[1] = 333;
	env->ReleasePrimitiveArrayCritical(jsa, shorts, 2);
    } */

#define GET_TYPE_ARRAYELEMENTS(_ctype, _carrtype, _jname, _field, _signature) \
    void Java_com_itsec_jniapitester_ApiTestRunner_testGet##_jname##ArrayElements(JNIEnv* env,                 \
                       jobject thiz, jobjectArray jobjArray, jint size, jlong iterations, jobject time) {  \
      jclass jc;                                                        \
      jobject jobj;                                                     \
      srand(1);                                                         \
      for(int i = 0; i < (int)iterations; i++) {                        \
        int index = rand() % size;                                      \
        index = 1;                                                      \
        jobj = env->GetObjectArrayElement(jobjArray, index);            \
        jc = env->GetObjectClass(jobj);                                 \
        someFieldID = env->GetFieldID(jc, _field, _signature);          \
        _carrtype arrayRef = (_carrtype) env->GetObjectField(jobj, someFieldID); \
        jboolean isCopy = false;                                        \
        timespec tstart, tstop, tdiff;                                  \
        clock_gettime( CLOCK_MONOTONIC , &tstart);                      \
        _ctype* array = env->Get##_jname##ArrayElements(arrayRef, &isCopy); \
        clock_gettime( CLOCK_MONOTONIC , &tstop ) ;                     \
        tdiff = timespec_diff(tstart, tstop, env);                      \
        env->Release##_jname##ArrayElements(arrayRef, array, 0);        \
        addTime(env, time, tdiff);                                      \
        env->DeleteLocalRef(arrayRef);                                  \
        env->DeleteLocalRef(jobj);                                      \
        env->DeleteLocalRef(jc);                                        \
      }                                                                 \
    }
  GET_TYPE_ARRAYELEMENTS(jboolean, jbooleanArray, Boolean, "m_booleanArrayField", "[Z");
  GET_TYPE_ARRAYELEMENTS(jshort, jshortArray, Short, "m_shortArrayField", "[S");
  GET_TYPE_ARRAYELEMENTS(jbyte, jbyteArray, Byte, "m_byteArrayField", "[B");
  GET_TYPE_ARRAYELEMENTS(jchar, jcharArray, Char, "m_charArrayField", "[C");
  GET_TYPE_ARRAYELEMENTS(jint, jintArray, Int, "m_intArrayField", "[I");
  GET_TYPE_ARRAYELEMENTS(jfloat, jfloatArray, Float, "m_floatArrayField", "[F");
  GET_TYPE_ARRAYELEMENTS(jlong, jlongArray, Long, "m_longArrayField", "[J");
  GET_TYPE_ARRAYELEMENTS(jdouble, jdoubleArray, Double, "m_doubleArrayField", "[D");

// TODO: Check whether release types make a difference in performance (they should)
#define RELEASE_TYPE_ARRAYELEMENTS(_ctype, _carrtype, _jname, _field, _signature) \
    void Java_com_itsec_jniapitester_ApiTestRunner_testRelease##_jname##ArrayElements(JNIEnv* env,                 \
                       jobject thiz, jobjectArray jobjArray, jint size, jlong iterations, jobject time) {  \
        jclass jc; \
        jobject jobj; \
        srand(1); \
        for(int i = 0; i < (int)iterations; i++) { \
            int index = rand() % size; \
            jobj = env->GetObjectArrayElement(jobjArray, index); \
            jc = env->GetObjectClass(jobj); \
            someFieldID = env->GetFieldID(jc, _field, _signature);\
            _carrtype arrayRef = (_carrtype) env->GetObjectField(jobj, someFieldID); \
            jboolean isCopy = false; \
            timespec tstart, tstop, tdiff; \
            _ctype* array = env->Get##_jname##ArrayElements(arrayRef, &isCopy); \
            if(array == NULL) {                                         \
              LOGD("Cannot get array elements");                        \
              continue;                                                 \
            }                                                           \
            clock_gettime( CLOCK_MONOTONIC , &tstart); \
            env->Release##_jname##ArrayElements(arrayRef, array, 0); \
            clock_gettime( CLOCK_MONOTONIC , &tstop ) ; \
            tdiff = timespec_diff(tstart, tstop, env);  \
            addTime(env, time, tdiff); \
            env->DeleteLocalRef(arrayRef); \
            env->DeleteLocalRef(jobj); \
            env->DeleteLocalRef(jc); \
        } \
}
RELEASE_TYPE_ARRAYELEMENTS(jboolean, jbooleanArray, Boolean, "m_booleanArrayField", "[Z");
RELEASE_TYPE_ARRAYELEMENTS(jshort, jshortArray, Short, "m_shortArrayField", "[S");
RELEASE_TYPE_ARRAYELEMENTS(jbyte, jbyteArray, Byte, "m_byteArrayField", "[B");
RELEASE_TYPE_ARRAYELEMENTS(jchar, jcharArray, Char, "m_charArrayField", "[C");
RELEASE_TYPE_ARRAYELEMENTS(jint, jintArray, Int, "m_intArrayField", "[I");
RELEASE_TYPE_ARRAYELEMENTS(jfloat, jfloatArray, Float, "m_floatArrayField", "[F");
RELEASE_TYPE_ARRAYELEMENTS(jlong, jlongArray, Long, "m_longArrayField", "[J");
RELEASE_TYPE_ARRAYELEMENTS(jdouble, jdoubleArray, Double, "m_doubleArrayField", "[D");

#define GET_TYPE_ARRAYREGION(_ctype, _carrtype, _jname, _field, _signature) \
  void Java_com_itsec_jniapitester_ApiTestRunner_testGet##_jname##ArrayRegion(JNIEnv* env, \
        jobject thiz, jobjectArray jobjArray, jint size, jlong iterations, jobject time) { \
    jclass jc;                                                          \
    jobject jobj;                                                       \
    srand(1);                                                           \
    for(int i = 0; i < (int)iterations; i++) {                          \
      int index = rand() % size;                                        \
      jobj = env->GetObjectArrayElement(jobjArray, index);              \
      jc = env->GetObjectClass(jobj);                                   \
      someFieldID = env->GetFieldID(jc, _field, _signature);            \
      _carrtype arrayRef = (_carrtype) env->GetObjectField(jobj, someFieldID); \
      jboolean isCopy = false;                                          \
      timespec tstart, tstop, tdiff;                                    \
      jsize length = env->GetArrayLength(arrayRef);                     \
      jsize from = (jsize)rand() % ((length-1) / 2);                    \
      jsize tocopy = (jsize)rand() % (length - 1 - from);               \
      _ctype* ptr = (_ctype *)malloc(tocopy * sizeof(_ctype));          \
      clock_gettime( CLOCK_MONOTONIC , &tstart);                        \
      env->Get##_jname##ArrayRegion(arrayRef, from, tocopy, ptr);       \
      clock_gettime( CLOCK_MONOTONIC , &tstop ) ;                       \
      tdiff = timespec_diff(tstart, tstop, env);                        \
      free(ptr);                                                        \
      addTime(env, time, tdiff);                                        \
      env->DeleteLocalRef(arrayRef);                                    \
      env->DeleteLocalRef(jobj);                                        \
      env->DeleteLocalRef(jc);                                          \
    }                                                                   \
}
  GET_TYPE_ARRAYREGION(jboolean, jbooleanArray, Boolean, "m_booleanArrayField", "[Z");
  GET_TYPE_ARRAYREGION(jshort, jshortArray, Short, "m_shortArrayField", "[S");
  GET_TYPE_ARRAYREGION(jbyte, jbyteArray, Byte, "m_byteArrayField", "[B");
  GET_TYPE_ARRAYREGION(jchar, jcharArray, Char, "m_charArrayField", "[C");
  GET_TYPE_ARRAYREGION(jint, jintArray, Int, "m_intArrayField", "[I");
  GET_TYPE_ARRAYREGION(jfloat, jfloatArray, Float, "m_floatArrayField", "[F");
  GET_TYPE_ARRAYREGION(jlong, jlongArray, Long, "m_longArrayField", "[J");
  GET_TYPE_ARRAYREGION(jdouble, jdoubleArray, Double, "m_doubleArrayField", "[D");

#define SET_TYPE_ARRAYREGION(_ctype, _carrtype, _jname, _field, _signature) \
  void Java_com_itsec_jniapitester_ApiTestRunner_testSet##_jname##ArrayRegion(JNIEnv* env, \
        jobject thiz, jobjectArray jobjArray, jint size, jlong iterations, jobject time) { \
    jclass jc;                                                          \
    jobject jobj;                                                       \
    srand(1);                                                           \
    for(int i = 0; i < (int)iterations; i++) {                          \
      int index = rand() % size;                                        \
      jobj = env->GetObjectArrayElement(jobjArray, index);              \
      jc = env->GetObjectClass(jobj);                                   \
      someFieldID = env->GetFieldID(jc, _field, _signature);            \
      _carrtype arrayRef = (_carrtype) env->GetObjectField(jobj, someFieldID); \
      jboolean isCopy = false;                                          \
      timespec tstart, tstop, tdiff;                                    \
      jsize length = env->GetArrayLength(arrayRef);                     \
      jsize from = rand() % ((length-1) / 2);                           \
      jsize tocopy = rand() % (length - 1 - from);                      \
      _ctype* ptr = (_ctype *)malloc(tocopy * sizeof(_ctype));          \
      env->Get##_jname##ArrayRegion(arrayRef, from, tocopy, ptr);       \
      clock_gettime( CLOCK_MONOTONIC , &tstart);                        \
      env->Set##_jname##ArrayRegion(arrayRef, from, tocopy, ptr);       \
      clock_gettime( CLOCK_MONOTONIC , &tstop ) ;                       \
      tdiff = timespec_diff(tstart, tstop, env);                        \
      free(ptr);                                                        \
      addTime(env, time, tdiff);                                        \
      env->DeleteLocalRef(arrayRef);                                    \
      env->DeleteLocalRef(jobj);                                        \
      env->DeleteLocalRef(jc);                                          \
    }                                                                   \
}

SET_TYPE_ARRAYREGION(jboolean, jbooleanArray, Boolean, "m_booleanArrayField", "[Z");
SET_TYPE_ARRAYREGION(jshort, jshortArray, Short, "m_shortArrayField", "[S");
SET_TYPE_ARRAYREGION(jbyte, jbyteArray, Byte, "m_byteArrayField", "[B");
SET_TYPE_ARRAYREGION(jchar, jcharArray, Char, "m_charArrayField", "[C");
SET_TYPE_ARRAYREGION(jint, jintArray, Int, "m_intArrayField", "[I");
SET_TYPE_ARRAYREGION(jfloat, jfloatArray, Float, "m_floatArrayField", "[F");
SET_TYPE_ARRAYREGION(jlong, jlongArray, Long, "m_longArrayField", "[J");
SET_TYPE_ARRAYREGION(jdouble, jdoubleArray, Double, "m_doubleArrayField", "[D");

#define NEW_TYPE_ARRAY(_carrtype, _jname) \
  void Java_com_itsec_jniapitester_ApiTestRunner_testNew##_jname##Array(JNIEnv* env, \
        jobject thiz, jlong iterations, jobject time) { \
    jsize maxSize = 2048;                                               \
    timespec tstart, tstop, tdiff;                                      \
    srand(1);                                                           \
    for(int i = 0; i < (int)iterations; i++) {                          \
      jsize size = 512 + rand() % maxSize;                              \
      _carrtype arr;                                                    \
      clock_gettime( CLOCK_MONOTONIC , &tstart);                        \
      arr = env->New##_jname##Array(size);                              \
      clock_gettime( CLOCK_MONOTONIC , &tstop ) ;                       \
      tdiff = timespec_diff(tstart, tstop, env);                        \
      addTime(env, time, tdiff);                                        \
      env->DeleteLocalRef(arr);                                         \
    }                                                                   \
}

NEW_TYPE_ARRAY(jbooleanArray, Boolean);
NEW_TYPE_ARRAY(jshortArray, Short);
NEW_TYPE_ARRAY(jbyteArray, Byte);
NEW_TYPE_ARRAY(jcharArray, Char);
NEW_TYPE_ARRAY(jintArray, Int);
NEW_TYPE_ARRAY(jfloatArray, Float);
NEW_TYPE_ARRAY(jlongArray, Long);
NEW_TYPE_ARRAY(jdoubleArray, Double);

  void Java_com_itsec_jniapitester_ApiTestRunner_testNewObjectArray(JNIEnv* env,
                                 jobject thiz, jobjectArray jobjArray, jint size, jlong iterations, jobject time) {
    timespec tstart, tstop, tdiff;
    srand(1);
    jint maxSize = 4096;
    for(int i = 0; i < (int)iterations; i++) {
      jsize s = 512 + rand() % maxSize;
      jsize index = rand() % size;
      jobject jobj = env->GetObjectArrayElement(jobjArray, index);
      jclass cls = env->GetObjectClass(jobj);
      jobjectArray arr;
      clock_gettime(CLOCK_MONOTONIC, &tstart);
      arr = env->NewObjectArray(s, cls, jobj);
      clock_gettime(CLOCK_MONOTONIC, &tstop);
      tdiff = timespec_diff(tstart, tstop, env);
      addTime(env, time, tdiff);
      env->DeleteLocalRef(arr);
      env->DeleteLocalRef(jobj);
      env->DeleteLocalRef(cls);
    }
  }

  void Java_com_itsec_jniapitester_ApiTestRunner_testGetObjectArrayElement(JNIEnv* env, jobject thiz, jobjectArray jobjArray, jint size, jlong iterations, jobject time) {
      timespec tstart, tstop, tdiff;
      jobject jobj;
      srand(1);
      for (int i = 0; i < (int) iterations; i++) {
          jint index = (jint) rand() % size;
          clock_gettime(CLOCK_MONOTONIC, &tstart);
          jobj = env->GetObjectArrayElement(jobjArray, index);
          clock_gettime(CLOCK_MONOTONIC, &tstop);
          tdiff = timespec_diff(tstart, tstop, env);
          addTime(env, time, tdiff);
          env->DeleteLocalRef(jobj);
      }
  }

  void Java_com_itsec_jniapitester_ApiTestRunner_testSetObjectArrayElement(JNIEnv* env, jobject thiz, jobjectArray jobjArray, jint size, jlong iterations, jobject time) {
      timespec tstart, tstop, tdiff;
      jobject jobj;
      srand(1);
      for (jlong i = 0; i < iterations; i++) {
          jint from = (jint) rand() % size, to;
          do {
            to = (jint) rand() % size;
          } while(to == from);

          jobj = env->GetObjectArrayElement(jobjArray, from);
          clock_gettime(CLOCK_MONOTONIC, &tstart);
          env->SetObjectArrayElement(jobjArray, to, jobj);
          clock_gettime(CLOCK_MONOTONIC, &tstop);
          tdiff = timespec_diff(tstart, tstop, env);
          addTime(env, time, tdiff);
          env->DeleteLocalRef(jobj);
      }
  }


  void Java_com_itsec_jniapitester_ApiTestRunner_testCallWithVariableArgumentSize(JNIEnv* env, jobject thiz, jobjectArray jobjArray, jint size, jlong argSize, jlong iterations, jobject time) {
    timespec tstart, tstop, tdiff;
    jobject jobj;
    jclass cls;
    srand(1);
    jlongArray longArray = env->NewLongArray(argSize);
    jlong* jlongArr = (jlong*)malloc(((long)argSize) * sizeof(jlong));
    for (long i = 0; i < (long)argSize; i++) {
      jlongArr[i] = (jlong)rand();
    }
    env->SetLongArrayRegion(longArray, 0, argSize, jlongArr);
    
    for (jlong i = 0; i < iterations; i++) {
        jint index = (jint) rand() % size;

        jobj = env->GetObjectArrayElement(jobjArray, index);
        cls = env->GetObjectClass(jobj);
        jmethodID id = env->GetMethodID(cls, "callArray", "([JI)[J");

        clock_gettime(CLOCK_MONOTONIC, &tstart);
        jobject res = env->CallObjectMethod(jobj, id, longArray, (jint)0);
        clock_gettime(CLOCK_MONOTONIC, &tstop);

        tdiff = timespec_diff(tstart, tstop, env);
        addTime(env, time, tdiff);
        env->DeleteLocalRef(jobj);
        env->DeleteLocalRef(cls);
        env->DeleteLocalRef(res);
    }
    env->DeleteLocalRef(longArray);
    free(jlongArr);
  }

jdouble Java_com_itsec_jniapitester_RunnerThread_nativeRun(
        JNIEnv* env, jobject thiz, jint length, jintArray data, jdouble cookie) {
    pthread_t pt = pthread_self();
    LOGD("This is Thread %ld (%f)", pt, cookie);

    if (data == NULL) {
        LOGD("thread %ld: sorting native array of length %d", pt, (int)length);
        int nums[length];
        for (int i = 0; i < length; i++)
            nums[i] = rand();

        //bubble sort array
        for (int i = 0; i < length; i++) {
            if(i % 1000 == 0)
                LOGD("%ld: native (%d)", pt, i);

            for (int j = 1; j < length - i; j++) {
                if (nums[j] < nums[j - 1]) {
                    //Log.d(TAG, "swapping "+j+":"+nums[j]+"and "+(j-1)+":"+nums[j-1]);
                    double temp = nums[j];
                    nums[j] = nums[j - 1];
                    nums[j - 1] = temp;
                }
            }
        }

        //verify
        for (int i = 1; i < length; i++)
            if (nums[i] < nums[i - 1]) LOGD("Error %d>%d", nums[i - 1], nums[i]);
    } else {
        LOGD("This is Thread %ld: sorting java array", pt);
        int* nums = (int*)env->GetPrimitiveArrayCritical(data, 0);
        for (int i = 0; i < length; i++) {
            if(i % 1000 == 0) {
                LOGD("%ld: java (%d)", pt, i);
            }
            for (int j = 1; j < length - i; j++)
                if (nums[j] < nums[j - 1]) {
                    double temp = nums[j];
                    nums[j] = nums[j - 1];
                    nums[j - 1] = temp;
                }
        }

        //verify
        for (int i = 1; i < length; i++)
            if (nums[i] < nums[i - 1]) LOGD("Error %d>%d", nums[i - 1], nums[i]);
        env->ReleasePrimitiveArrayCritical(data, nums, 0);
        //free(nums);
    }
    LOGD("thread %ld (%f): finished sorting array", pt, cookie);
    return cookie;
}

void Java_com_itsec_jniapitester_ApiTestRunner_testCallWithVariableReturnSize(JNIEnv* env, jobject thiz, jobjectArray jobjArray, jint size, jint returnSize, jlong iterations, jobject time) {
    timespec tstart, tstop, tdiff;
    jobject jobj;
    jclass cls;
    srand(1);
    jlong dummy = 10;
    jlongArray longArray = env->NewLongArray(dummy);
    jlong* jlongArr = (jlong*)malloc(((long)dummy) * sizeof(jlong));
    for (long i = 0; i < (long)dummy; i++) {
        jlongArr[i] = (jlong)rand();
    }
    env->SetLongArrayRegion(longArray, 0, dummy, jlongArr);

    for (jlong i = 0; i < iterations; i++) {
        jint index = (jint) rand() % size;

        jobj = env->GetObjectArrayElement(jobjArray, index);
        cls = env->GetObjectClass(jobj);
        jmethodID id = env->GetMethodID(cls, "callArray", "([JI)[J");

        clock_gettime(CLOCK_MONOTONIC, &tstart);
        jobject res = env->CallObjectMethod(jobj, id, longArray, returnSize);
        clock_gettime(CLOCK_MONOTONIC, &tstop);

        tdiff = timespec_diff(tstart, tstop, env);
        addTime(env, time, tdiff);
        env->DeleteLocalRef(jobj);
        env->DeleteLocalRef(cls);
        env->DeleteLocalRef(res);
    }
    env->DeleteLocalRef(longArray);
    free(jlongArr);
  }

}