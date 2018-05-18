package com.zhengdianfang.miracledemo

import android.os.Bundle
import com.zhengdianfang.miracleframework.BaseActivity

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadRootFragment(android.R.id.content, FirstFragment.newInstance())
    }
}
