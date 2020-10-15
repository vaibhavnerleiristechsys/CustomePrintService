package com.example.customeprintservice.print

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.customeprintservice.R
import com.example.customeprintservice.jipp.PrintUtils
import kotlinx.android.synthetic.main.activity_bottom_navigation.*

class BottomNavigationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bottom_navigation)
        PrintUtils().setContextAndInitializeJMDNS(this@BottomNavigationActivity)
        val printReleaseFragment = PrintReleaseFragment()
        val printersFragment = PrintersFragment()
        val servicePortalFragment = ServicePortalFragment()

        setCurrentFragment(PrintReleaseFragment())

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.printRelease -> {
                    setCurrentFragment(printReleaseFragment)

                }
                R.id.printer -> {
                    setCurrentFragment(printersFragment)

                }
                R.id.servicePortal -> {
                    setCurrentFragment(servicePortalFragment)
                }
            }
            true
        }
    }

    private fun setCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragmentContainer, fragment)
            commit()
        }

}