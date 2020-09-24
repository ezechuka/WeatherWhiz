package com.javalon.weatherwhiz.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import com.javalon.weatherwhiz.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val navHostFrag = supportFragmentManager.fragments.first() as? NavHostFragment
        navHostFrag.let {
            val fragments = it?.childFragmentManager?.fragments
            fragments?.forEach {fragment ->
                fragment.onActivityResult(requestCode, resultCode, data)
            }
        }
    }
}