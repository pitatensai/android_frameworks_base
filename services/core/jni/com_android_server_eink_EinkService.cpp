/*
 * Copyright (C) 2008 The Android Open Source Project
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

#define LOG_TAG "com_android_server_eink_EinkService"

#define LOG_NDEBUG 0
#define DEBUG_EINK 1

#include <stdlib.h>
#include <stdio.h>
#include <math.h>
#include <ctype.h>
#include <pthread.h>
#include <errno.h>
#include <sys/types.h>
#include <dirent.h>
#include <grp.h>
#include <inttypes.h>
#include <pwd.h>
#include <time.h>
#include <poll.h>
#include <fcntl.h>

#include <sys/stat.h>
#include <sys/syscall.h>

#include <string.h>
#include <unistd.h>
#include <assert.h>
#include <hardware/hardware.h>
#include <android_runtime/Log.h>
#include <nativehelper/JNIHelp.h>
#include "jni.h"
#include <sched.h>
#include <utils/Log.h>
#include <logwrap/logwrap.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <errno.h>
#include <unistd.h>
#include <signal.h>
#include <cutils/properties.h>
// ----------------------------------------------------------------------------

namespace android {

	const char *args[1];
	static pid_t pid;
/*
 * Field/method IDs and class object references.
 *
 * You should not need to store the JNIEnv pointer in here.  It is
 * thread-specific and will be passed back in on every call.
 */
static struct {
    jclass      platformLibraryClass;
} gCachedState;

// ----------------------------------------------------------------------------

/*
 * Helper function to throw an arbitrary exception.
 *
 * Takes the exception class name, a format string, and one optional integer
 * argument (useful for including an error code, perhaps from errno).
 */
static void throwException(JNIEnv* env, const char* ex, const char* fmt,
                           int data)
{

    if (jclass cls = env->FindClass(ex)) {
        if (fmt != NULL) {
            char msg[1000];
            // snprintf(msg, sizeof(msg), fmt, data);
            env->ThrowNew(cls, msg);
        } else {
            env->ThrowNew(cls, NULL);
        }

        /*
         * This is usually not necessary -- local references are released
         * automatically when the native code returns to the VM.  It's
         * required if the code doesn't actually return, e.g. it's sitting
         * in a native event loop.
         */
        env->DeleteLocalRef(cls);
    }
}

int system(const char * cmdstring)
{

    int status;

    if(cmdstring == NULL){      
         return (1);
    }

    if((pid = fork())<0){
            status = -1;
    }
    else if(pid == 0){
	        execl("/bin/sh", "sh", "-c", cmdstring, (char *)0);
	        exit(127);
        }
    else{
           while(waitpid(pid, &status, 0) < 0){
                if(errno != EINTR){
                    status = -1;
                    break;
                }
            }
        }
    return status;
}

jint com_android_server_eink_EinkService_init(JNIEnv *env, jclass clazz)
{
    int result=0;
    ALOGV("com_android_server_eink_EinkService_init");
	/*args[0] = "/system/bin/eink-drawpath";
	result = logwrap_fork_execvp(1, (char **)args, NULL, false,1, false, NULL);
	if (result) {
          errno = EIO;
          ALOGE("uibc sink start fail (unknown exit code %d)", result);
      }*/
      //system("/system/bin/eink-drawpath");
 
      property_set("ctl.start","einkdrawpath");
    return result;
}

jint com_android_server_eink_EinkService_kill(JNIEnv *env, jclass clazz)
{
    int result=0;
    ALOGV("com_android_server_eink_EinkService_kill");
    property_set("ctl.stop","einkdrawpath");
    //kill(pid, SIGKILL);
    return result;
}

// ----------------------------------------------------------------------------

/*
 * Array of methods.
 *
 * Each entry has three fields: the name of the method, the method
 * signature, and a pointer to the native implementation.
 */
/****

Android JNI 使用的数据结构JNINativeMethod详解Java&Android 2009-09-06 20:42:56 阅读234 评论0 字号：大中小
Andoird 中使用了一种不同传统Java JNI的方式来定义其native的函数。其中很重要的区别是Andorid使用了一种Java 和 C 函数的映射表数组，并在其中描述了函数的参数和返回值。这个数组的类型是JNINativeMethod，定义如下：


typedef struct {
const char* name;
const char* signature;
void* fnPtr;
} JNINativeMethod;

第一个变量name是Java中函数的名字。

第二个变量signature，用字符串是描述了函数的参数和返回值

第三个变量fnPtr是函数指针，指向C函数。


其中比较难以理解的是第二个参数，例如

"()V"

"(II)V"

"(Ljava/lang/String;Ljava/lang/String;)V"


实际上这些字符是与函数的参数类型一一对应的。

"()" 中的字符表示参数，后面的则代表返回值。例如"()V" 就表示void Func();

"(II)V" 表示 void Func(int, int);


具体的每一个字符的对应关系如下


字符 Java类型 C类型

V		void			void
Z		 jboolean	  boolean
I		  jint				int
J		 jlong			  long
D		jdouble 	  double
F		jfloat			  float
B		jbyte			 byte
C		jchar			char
S		jshort			short


数组则以"["开始，用两个字符表示


[I 	  jintArray 	 int[]
[F 	jfloatArray    float[]
[B 	jbyteArray	  byte[]
[C    jcharArray	 char[]
[S    jshortArray	 short[]
[D    jdoubleArray double[]
[J 	jlongArray	   long[]
[Z    jbooleanArray boolean[]


上面的都是基本类型。如果Java函数的参数是class，则以"L"开头，以";"结尾中间是用"/" 隔开的包及类名。而其对应的C函数名的参数则为jobject. 一个例外是String类，其对应的类为jstring


Ljava/lang/String; String jstring
Ljava/net/Socket; Socket jobject


如果JAVA函数位于一个嵌入类，则用$作为类名间的分隔符。

例如 "(Ljava/lang/String;Landroid/os/FileUtils$FileStatus;)Z"
****/

static const JNINativeMethod gMethods[] = {
    /* name,                        signature,      funcPtr */
    { "init_native","()I",(void *)com_android_server_eink_EinkService_init  },
    { "kill_native","()I",(void *)com_android_server_eink_EinkService_kill  },
};

/*
 * Do some (slow-ish) lookups now and save the results.
 *
 * Returns 0 on success.
 */
static int cacheIds(JNIEnv* env, jclass clazz)
{
    /*
     * Save the class in case we want to use it later.  Because this is a
     * reference to the Class object, we need to convert it to a JNI global
     * reference.
     */
    gCachedState.platformLibraryClass = (jclass) env->NewGlobalRef(clazz);      // .! : sample 中的实现, 就 HDMI 的实现, 不需要回调 Java.
    if (clazz == NULL) {
        ALOGE("Can't create new global ref\n");
        return -1;
    }

    return 0;
}

/*
 * Explicitly register all methods for our class.
 *
 * While we're at it, cache some class references and method/field IDs.
 *
 * Returns 0 on success.
 */
int register_android_server_EinkService(JNIEnv* env)
{
    static const char* const kClassName = "com/android/server/eink/EinkService";
    jclass clazz;

    /* look up the class */
    clazz = env->FindClass(kClassName);
    if (clazz == NULL) {
        ALOGE("Can't find class %s\n", kClassName);
        return -1;
    }
    ALOGI("have find class %s\n", kClassName);

    /* register all the methods */
    if (env->RegisterNatives(clazz, gMethods,
                             sizeof(gMethods) / sizeof(gMethods[0])) != JNI_OK) {
        ALOGE("Failed registering methods for %s\n", kClassName);
        return -1;
    }
    ALOGI("registering methods for %s\n", kClassName);

    /* fill out the rest of the ID cache */ // .! : 在 eink 实现中, 没有必要 cache Java 类 or field 的 ID.
    return cacheIds(env, clazz);
}
};
