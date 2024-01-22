package kr.co.htap.onboarding

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 *
 * @author 송원선
 *
 */
class OnboardingPagerAdapter(activity: FragmentActivity):FragmentStateAdapter(activity) {
    private val numPages = 3

    override fun getItemCount(): Int = numPages

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> OnboardingPage1Fragment()
            1 -> OnboardingPage2Fragment()
            2 -> OnboardingPage3Fragment()
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
}