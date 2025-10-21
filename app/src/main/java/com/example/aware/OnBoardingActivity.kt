package com.example.aware

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.example.aware.utils.OnboardingViewPagerAdapter
import com.example.aware.utils.getStatusBarHeight

class OnBoardingActivity : AppCompatActivity() {

    private lateinit var slideViewPager: ViewPager
    private lateinit var dotsLinearLayout: LinearLayout
    private lateinit var continueBtn: Button
    private lateinit var skipBtn: Button

    private lateinit var dots: Array<TextView?>
    private var onboardingViewPagerAdapter: OnboardingViewPagerAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_on_boarding)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //        add padding-top to the main layout
        val mainLayout = findViewById<View>(R.id.main)
        mainLayout.setPadding(0, getStatusBarHeight(this), 0, 0)

        continueBtn = findViewById<Button>(R.id.btn_continue)
        skipBtn = findViewById<Button>(R.id.btn_skip)


        continueBtn.setOnClickListener(View.OnClickListener {
            if (getItem(0) < 2) slideViewPager.setCurrentItem(getItem(1), true)
            else {
                val i = Intent(
                    this,
                    DashboardActivity::class.java
                )
                startActivity(i)
                finish()
            }
        })

        skipBtn.setOnClickListener(View.OnClickListener {
            val i = Intent(
                this,
                DashboardActivity::class.java
            )
            startActivity(i)
            finish()
        })

        slideViewPager = findViewById<View>(R.id.slideViewPager) as ViewPager
        dotsLinearLayout = findViewById<View>(R.id.indicator_layout) as LinearLayout

        onboardingViewPagerAdapter = OnboardingViewPagerAdapter(this)

        slideViewPager.adapter = onboardingViewPagerAdapter

        setUpIndicator(0)
        slideViewPager.addOnPageChangeListener(viewListener)
    }

    fun setUpIndicator(position: Int) {
        dots = arrayOfNulls(3)
        dotsLinearLayout.removeAllViews()

        for (i in dots.indices) {
            dots[i] = TextView(this)
            dots[i]!!.text = Html.fromHtml("&#8226")
            dots[i]!!.textSize = 35f
            dots[i]!!
                .setTextColor(resources.getColor(R.color.inactive, applicationContext.theme))
            dotsLinearLayout.addView(dots[i])
        }

        dots[position]!!
            .setTextColor(resources.getColor(R.color.active, applicationContext.theme))
    }

    private var viewListener: OnPageChangeListener = object : OnPageChangeListener {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
        }

        override fun onPageSelected(position: Int) {
            setUpIndicator(position)
        }

        override fun onPageScrollStateChanged(state: Int) {
        }
    }

    private fun getItem(i: Int): Int {
        return slideViewPager.currentItem + i
    }
}