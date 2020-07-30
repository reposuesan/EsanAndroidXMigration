package com.example.vuforiamod.international.activities

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.example.vuforiamod.R
import kotlinx.android.synthetic.main.activity_main_international.*

import com.example.vuforiamod.international.fragments.*


/**
 * Created by killerypa on 6/01/2019.
 * yahyrparedesarteaga@gmail.com
 */

class InternationalMainActivity : AppCompatActivity() {

    private var count = 0;
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_register -> {
                // message.setText(R.string.title_register)
                val registerFrag = InternationalRegisterFragment()
                openFragment(registerFrag)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_week_international -> {
                // message.setText(R.string.title_week_intenational)
                val registerFrag = InternationalParticipationFragment()
                openFragment(registerFrag)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_course -> {
                // message.setText(R.string.title_course)
                val registerFrag = InternationalCourseFragment()
                openFragment(registerFrag)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_schedule -> {
                // message.setText(R.string.title_schedule)
                val registerFrag = InternationalScheduleFragment()
                openFragment(registerFrag)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_more -> {
                //  message.setText(R.string.title_more)
                val registerFrag = InternationalMoreFragment()
                openFragment(registerFrag)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    private fun openFragment(fragment: androidx.fragment.app.Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(null)
        //getSupportFragmentManager().popBackStack();
        transaction.commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_main_international)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        val indexItem = 0
        navigation.menu.getItem(indexItem).isChecked = true
        mOnNavigationItemSelectedListener.onNavigationItemSelected(navigation.menu.getItem(indexItem))
    }

    @Override
    override fun onBackPressed() {
        val selectedItemId = navigation.selectedItemId

        if (count <= 1) {
            count++
            if (count == 1)
                Toast.makeText(this, getString(R.string.pulse_una_vez_mas_para_salir), Toast.LENGTH_SHORT).show()
            else if (count == 2) {
                try {
                    val loginIntent = Intent(this, Class.forName("pe.edu.esan.appostgrado.view.login.LoginActivity"))
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(loginIntent)
                    finish() //System.exit(0);
                } catch (e: ClassNotFoundException){
                    e.printStackTrace()
                }
                /*val loginIntent = Intent(this@InternationalMainActivity, LoginActivity::class.java)
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(loginIntent)
                finish() //System.exit(0);*/
            }
        } else {
            count = 0
            /*
            when (seletedItemId) {
                R.id.navigation_register -> {
                    navigation.menu.getItem(0).isChecked = true
                }
                R.id.navigation_week_international -> {
                    navigation.menu.getItem(1).isChecked = true
                }
                R.id.navigation_schedule -> {
                    navigation.menu.getItem(2).isChecked = true
                }
                R.id.navigation_course -> {
                    navigation.menu.getItem(3).isChecked = true
                }
                R.id.navigation_more -> {
                    navigation.menu.getItem(4).isChecked = true
                }
            }*/
            //super.onBackPressed();
        }
    }
}
