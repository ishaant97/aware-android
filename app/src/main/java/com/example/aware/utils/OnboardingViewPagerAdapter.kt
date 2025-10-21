package com.example.aware.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.example.aware.R

class OnboardingViewPagerAdapter(var context: Context) : PagerAdapter() {

    private var images: IntArray = intArrayOf(
        R.drawable.img_onboarding_screen_1_temp,
        R.drawable.img_onboarding_screen_2,
        R.drawable.img_onboarding_screen_3,
    )

    private var headings: IntArray = intArrayOf(
        R.string.onboarding_heading_1,
        R.string.onboarding_heading_2,
        R.string.onboarding_heading_3,
    )

   private var description: IntArray = intArrayOf(
        R.string.onboarding_description_1,
        R.string.onboarding_description_2,
        R.string.onboarding_description_3,
    )

    override fun getCount(): Int {
        return headings.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object` as LinearLayout
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val layoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = layoutInflater.inflate(R.layout.view_onboarding_slider, container, false)

        val slideTitleImage = view.findViewById<View>(R.id.title_image) as ImageView
        val slideHeading = view.findViewById<View>(R.id.text_heading) as TextView
        val slideDescription = view.findViewById<View>(R.id.text_description) as TextView

        slideTitleImage.setImageResource(images[position])
        slideHeading.setText(headings[position])
        slideDescription.setText(description[position])

        container.addView(view)

        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as LinearLayout)
    }
}