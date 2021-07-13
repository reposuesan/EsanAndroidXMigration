package com.example.vuforiamod.international.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.vuforiamod.R
import kotlinx.android.synthetic.main.fragment_international_image_scroll.view.*
import kotlinx.android.synthetic.main.fragment_international_more.view.*
import com.example.vuforiamod.international.activities.InternationalInstructiveActivity
import com.example.vuforiamod.international.activities.InternationalPiscoActivity
import kotlin.math.acos


class InternationalMoreFragment : androidx.fragment.app.Fragment() {

    /*private var mSectionsPagerAdapter: SectionsPagerImagesAdapter? = null*/
    /*private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager*/
    val listImages = ArrayList<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_international_more, container, false)

        generateList()

        val mSectionsPagerAdapter = SectionsPagerImagesAdapter(requireActivity().supportFragmentManager)
        view.vp_international_container.adapter = mSectionsPagerAdapter
        /*viewManager = LinearLayoutManager(activity!!, LinearLayoutManager.HORIZONTAL, false)
        viewAdapter = UniversitiesImagesAdapter(listImages)

        view.vp_international_container.apply {
                // use this setting to improve performance if you know that changes
                // in content do not change the layout size of the RecyclerView
                setHasFixedSize(true)

                // use a linear layout manager
                layoutManager = viewManager

                // specify an viewAdapter (see also next example)
                adapter = viewAdapter

        }*/

        view.tv_international_exit.setOnClickListener {
            try {
                val loginIntent = Intent(activity, Class.forName("pe.edu.esan.appostgrado.view.login.LoginActivity"))
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(loginIntent)
                requireActivity().finish()
            } catch (e: ClassNotFoundException){
                e.printStackTrace()
            }
            /*val loginIntent = Intent(activity, LoginActivity::class.java)
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(loginIntent)
            activity!!.finish()*/
        }

        view.item_pisco.setOnClickListener {
            startActivity(Intent(activity, InternationalPiscoActivity::class.java))
        }

        view.item_instructive.setOnClickListener {
            startActivity(Intent(activity, InternationalInstructiveActivity::class.java))
        }

        return view
    }

    /*override fun onStart() {
        /*setViewPager()*/
        super.onStart()
    }*/

    private fun generateList(){
        listImages.add(R.drawable.logo_u_ort)
        listImages.add(R.drawable.logo_u_espol)
        listImages.add(R.drawable.logo_udem)
        listImages.add(R.drawable.logo_u_vienna_economics_business)
        listImages.add(R.drawable.logo_u_laval)
        listImages.add(R.drawable.logo_u_dallas)
        listImages.add(R.drawable.logo_u_torcuato_di_tella)
        listImages.add(R.drawable.logo_u_northcarolina)
        listImages.add(R.drawable.logo_u_st_gallen)
    }

    /*private fun setViewPager(){
        val mSectionsPagerAdapter = SectionsPagerImagesAdapter(activity!!.supportFragmentManager)
        view!!.vp_international_container.adapter = mSectionsPagerAdapter
    }*/

    inner class SectionsPagerImagesAdapter(fm: androidx.fragment.app.FragmentManager) : androidx.fragment.app.FragmentStatePagerAdapter(fm) {

        override fun getItem(position: Int): androidx.fragment.app.Fragment {
            return ImageHolderFragment.newInstance(listImages.get(position))
        }

        override fun getCount(): Int {
            return listImages.size
        }
    }

    class ImageHolderFragment : androidx.fragment.app.Fragment() {

        override fun onCreateView( inflater: LayoutInflater,
                                   container: ViewGroup?,
                                   savedInstanceState: Bundle? ): View? {

            val rootView = inflater.inflate(R.layout.fragment_international_image_scroll, container, false)

            rootView.imgPhotoInternationalU.setImageResource(requireArguments().getInt(ARG_SECTION_NUMBER))

            return rootView
        }

        companion object {
            var ARG_SECTION_NUMBER = "item_position"
            fun newInstance(sectionNumber: Int): ImageHolderFragment {
                val fragment = ImageHolderFragment()
                val args = Bundle()
                args.putInt(ARG_SECTION_NUMBER, sectionNumber)
                fragment.arguments = args
                return fragment
            }
        }
    }

}
