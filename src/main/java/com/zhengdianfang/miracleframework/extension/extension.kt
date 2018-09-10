package com.zhengdianfang.miracleframework.extension

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

fun <T> LiveData<T>.getValueBlocking() : T? {
    var value: T? = null
    val countDownLatch = CountDownLatch(1)
    val observer = Observer<T> {
        value = it
        countDownLatch.countDown()
    }
    observeForever(observer)
    countDownLatch.await(2, TimeUnit.SECONDS)
    return value
}
