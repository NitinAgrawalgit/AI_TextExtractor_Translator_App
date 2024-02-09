package com.example.aitextextractor

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.FragmentTransaction

class OnboardingActivity : AppCompatActivity() {

    private var frag1 = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)


        supportFragmentManager
            .beginTransaction()
            .replace(R.id.splash_container, SplashFragment1.newInstance())
            .commitNow()

        runLoop()
    }

    private fun runLoop(){
        Handler(Looper.getMainLooper()).postDelayed({
            if(frag1){
                frag1 = false
                supportFragmentManager
                    .beginTransaction()
                    .setCustomAnimations(R.anim.enter_right_to_left, R.anim.exit_right_to_left, R.anim.enter_left_to_right, R.anim.exit_left_to_right)
                    .replace(R.id.splash_container, SplashFragment2.newInstance())
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit()

                runLoop()
            }else {
                val intent = Intent(this, UploadImgActivity::class.java)
                startActivity(intent)
            }

        }, 2000)
    }
}