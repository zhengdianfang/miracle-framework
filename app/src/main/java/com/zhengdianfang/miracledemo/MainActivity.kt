package com.zhengdianfang.miracledemo

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import com.zhengdianfang.miracleframework.BaseActivity
import com.zhengdianfang.miracleframework.permission.MiraclePermissions

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadRootFragment(android.R.id.content, FirstFragment.newInstance())
        MiraclePermissions(this)
                .request(Manifest.permission.CAMERA)
                .subscribe { granted ->
                    Toast.makeText(this, granted.toString(), Toast.LENGTH_SHORT).show()
                }
    }
}
