package com.corcow.hyunwoo

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*

class MainViewModel : ViewModel() {

    val message = MutableLiveData<String>("Coroutine Practices")

    /**
     * 1. Coroutine Basics
     * [launch]는 코루틴 빌더. 이를 이용하여 코드를 [CoroutineScope] 내에서 실행한다.
     * 다음 예제는 [GlobalScope] 에서 실행된다.
     * [GlobalScope]는 수명시간이 application process에 의존하므로 application이 종료되면 같이 끝나기 때문에
     * 종료 직전에 sleep을 걸고 기다려야 launch 내부 동작을 실행할 수 있다.
     * - (안드로이드의 경우는 액티비티가 종료되어도 application process가 종료되지 않으므로 동작한다)
     */
    fun practice1() {
        GlobalScope.launch {          // launch new coroutine in background and continue
            delay(1000L)     // non-blocking delay for 1 second
            Log.d(TAG, " World!")
        }

        Log.d(TAG, " Hello,")
        Thread.sleep(2000L)     // blocking main thread for 2 seconds to keep JVM alive
    }


    /**
     * non-blocking 인 [delay] 와 blocking [Thread.sleep] 을 혼합해서 사용하면
     * 어떤 부분이 blocking 인지 아닌지 혼동이 올 수 있다.
     * 메인 스레드는 [runBlocking]을 만나면 내부 코드가 완료될때까지 블락된다.
     */
    fun practice1_a() = runBlocking {   // start "practice1" coroutine
        GlobalScope.launch {
            delay(1000L)
            Log.d(TAG, " World!")
        }

        Log.d(TAG, " Hello,")     // "practice1" coroutine continues here immediately
        delay(2000L)          // delaying for 2 seconds to keep JVM alive
    }

    fun practice1_b() {
        runBlocking {
            launch {
                delay(1000L)
                Log.d(TAG, " World!")
            }
            Log.d(TAG, " Hello,")
            delay(2000L)
        }

        Log.d(TAG, "End function")
    }


    /**
     * 2. Waiting for a Job
     * 다른 작업을 기다리기 위해 [delay]를 사용하는 것은 별로이므로 [Job.join]을 사용. (join 역시 non-blocking)
     * [launch]의 리턴값으로 [Job]이 반환된다. 이는 스케줄 관리나 상태 등을 확인할 때 사용.
     */
    fun practice2() = runBlocking {
    // sampleStart
        val job = GlobalScope.launch {  // launch a new coroutine and keep a reference to its Job
            delay(1000L)
            Log.d(TAG, " World!")
        }

        Log.d(TAG, " Hello,")
        job.join()      // wait until child coroutine completes
    // sampleEnd
    }


    /**
     * 3. Structured Concurrency
     * [GlobalScope.launch]는 top-level 코루틴을 만듬.
     * 즉, [GlobalScope]는 프로세스가 살아 있는 한 계속 유지되기 때문에 여기에 계속해서 launch를 하는 작업은 메모리/관리면에서 비효율적.
     * [GlobalScope.launch] 만 사용한다면 이를 관리하기 위한 [Job]의 레퍼런스를 모두 가지고 있으면서 join 을 수동으로 관리해야한다.
     *
     * 이 문제를 해결하기 위해 Structured Concurrency 사용.
     * 특정한 [CoroutineScope]를 만들고 그 안에서 새로운 coroutine builder로 코루틴을 시작하면
     * 생성된 코루틴 scope이, 코드블럭의 코루틴 scope에 더해진다.
     * 따라서 외부 scope은 내부의 코루틴들이 종료되기 전까진 종료되지 않으므로 join 없는 간단한 코드를 만들 수 있다.
     */
    fun practice3() {
        runBlocking {
            val jobs = List(10) {
                launch {
                    delay(1000L)
                    Log.d(TAG, "Hello")     /** @issue? : 10개 리스트만큼 로그가 찍히지 않는다. **/
                }
            }
            // join 할경우 "End runBlock" 이 나중에 찍힘.
            // join 안하면 "End runBlock" 이 먼저 찍힘.
            jobs.forEach { it.join() }
            Log.d(TAG, "End runBlock")
        }
        Log.d(TAG, "End function")
    }


    /**
     * 4. Scope Builder
     * [coroutineScope] 빌더를 이용하여 내부에 또다른 scope을 생성함.
     * 모든 코루틴 block은 내부(자식) 코루틴이 모두 완료할때까지 대기한다.
     * 따라서 "Coroutine scope is over #4" 는 coroutineScope이 끝날 때까지 기다렸다가 찍힘.
     */
    fun practice4() = runBlocking {
        launch {
            delay(200L)
            Log.d(TAG, "Task from runBlocking #2")
        }

        coroutineScope {    // 새로운 CoroutineScope 생성
            launch {
                delay(500L)
                Log.d(TAG, "Task from nested launch #3")
            }

            delay(100L)
            // This line will be printed until before nested launch
            Log.d(TAG, "Task from coroutine scope #1")
        }

        // This line is not printed until nested launch completes
        Log.d(TAG, "Coroutine scope is over #4")
    }


    /**
     * 5. Extract function refactoring
     * 코루틴에서 사용하는 코드를 외부 함수로 빼내려면 [suspend] 키워드를 사용.
     * suspend function은
     *  함수 내부에서 코루틴 api를 사용할 수 있다.
     *  coroutine scope 에서만 사용할 수 있다. 일반 함수에서 호출 시 컴파일 에러 발생.
     *
     */
    fun practice5() = runBlocking {
        launch { printWorld() }
        Log.d(TAG, "Hello, ")
    }
    suspend fun printWorld() {
        delay(1000L)
        Log.d(TAG, "World!")
    }


    /**
     * 6. Coroutines are light-weight
     * 다음 코드 수행 시 100K의 코루틴이 정상적으로 수행됨.
     * --> 이걸 스레드로 바꾸면 OOM 나고 죽는다.
     */
    fun practice6() = runBlocking {
        // launch a lot of coroutines
        repeat(100_000) {
            launch {
                delay(1000L)
                Log.d(TAG, ".")
            }
        }
    }


    /**
     * 7. Global coroutines are like daemon threads
     * 아래 예제는 Android 예제는 아님. (안드로이드 상에서는 프로세스가 죽을 때 까지 출력)
     *
     * [GlobalScope]에서 수행하는 코루틴은 daemon thread 처럼 프로세스가 kill 될 경우 함께 멈춘다.
     * (부모 코루틴 스코프와는 별도로 동작한다.)
     *
     * [runBlocking]은 일반적의으로 내부에서 발생한 모든 자식 코루틴의 동작을 보장하지만,
     * 내부에서 [GlobalScope] 로 launch한 경우에는 [runBlocking]과는 다른 scope을 갖는다.
     *
     * 따라서 [runBlocking]은 1.3초만 대기하고 종료되고, main 함수가 종료되어 application process 역시 종료되므로
     * runBlocking 내부에서 선언한 GlobalScope 은 동작이 정지된다.
     */
    fun main() = runBlocking {
        GlobalScope.launch {
            repeat(1000) { i ->
                Log.d(TAG, "I'm sleeping $i...")
                delay(500L)
            }
        }
        delay(1300L)    // just quit after delay (no child scope)
    }


    companion object {
        const val TAG = "QWEQWE"
    }

}