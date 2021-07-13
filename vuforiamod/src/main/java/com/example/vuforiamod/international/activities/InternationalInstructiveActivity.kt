package com.example.vuforiamod.international.activities

import android.os.Bundle
import androidx.constraintlayout.widget.Placeholder
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.appcompat.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.vuforiamod.R
import kotlinx.android.synthetic.main.activity_international_instructive.*
import kotlinx.android.synthetic.main.fragment_international_instructive.view.*


class InternationalInstructiveActivity : AppCompatActivity() {

    //private var mSectionsPagerAdapter: SectionsPagerAdapter? = null
    private val listImages = ArrayList<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_international_instructive)

        //mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        generateList()
        setViewPager()
    }

    private fun generateList(){
        listImages.add(R.drawable.instructivo_1_2020_1)
        listImages.add(R.drawable.instructivo_2_2020_1)
        listImages.add(R.drawable.instructivo_3_2020_1)
        listImages.add(R.drawable.instructivo_4_2020_1)
        listImages.add(R.drawable.instructivo_5_2020_1)
        listImages.add(R.drawable.instructivo_6_2020_1)
    }

    private fun setViewPager(){
        val mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)
        //mSectionsPagerAdapter.addFragment(PlaceholderFragment(), listImages[0])
        //mSectionsPagerAdapter.addFragment(PlaceholderFragment(), listImages[1])
        //mSectionsPagerAdapter.addFragment(PlaceholderFragment(), listImages[2])
        container.adapter = mSectionsPagerAdapter
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    internal inner class SectionsPagerAdapter(fm: androidx.fragment.app.FragmentManager) : androidx.fragment.app.FragmentPagerAdapter(fm) {
        //private val mFragmentList = java.util.ArrayList<Fragment>()

        override fun getItem(position: Int): androidx.fragment.app.Fragment {
            return PlaceholderFragment.newInstance(listImages.get(position))
            //return mFragmentList[position]
        }

        override fun getCount(): Int {
            return listImages.size
        }

        //fun addFragment(fragment: Fragment, resource: Int) {
            //mFragmentList.add(fragment)
            //mFragmentTitleList.add(title)
        //}

    }

    class PlaceholderFragment : androidx.fragment.app.Fragment() {

        override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {

            val rootView = inflater.inflate(R.layout.fragment_international_instructive, container, false)

            rootView.imgInstructive.setImageResource(requireArguments().getInt(ARG_SECTION_NUMBER))
            //rootView.imgInstructive.setImageResource(R.drawable.instructivo_uno)
            //val imgView = rootView.findViewById<ImageView>(R.id.imgInstructive)
            //imgView.setImageResource(R.drawable.instructivo_uno)

            rootView.img_intenational_close.setOnClickListener {
                //startActivity(Intent().setClass(getActivity()!!, InternationalMainActivity::class.java))
                requireActivity().onBackPressed()
            }

            return rootView
        }

        companion object {
            var ARG_SECTION_NUMBER = "item_position"
            fun newInstance(sectionNumber: Int): PlaceholderFragment {
                val fragment = PlaceholderFragment()
                val args = Bundle()
                args.putInt(ARG_SECTION_NUMBER, sectionNumber)
                fragment.arguments = args
                return fragment
            }
        }

    }


}
