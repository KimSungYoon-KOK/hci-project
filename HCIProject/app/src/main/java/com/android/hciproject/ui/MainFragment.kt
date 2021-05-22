package com.android.hciproject.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import androidx.navigation.fragment.findNavController
import com.android.hciproject.R
import com.android.hciproject.databinding.MainFragmentBinding
import com.android.hciproject.viewmodels.MainViewModel
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback

class MainFragment : Fragment(), OnMapReadyCallback {

    private var _binding: MainFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = MainViewModel()
        _binding = MainFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initMapFragment()
        testMap()
        setClickListener()

    }

    private fun setClickListener() {
        binding.showPostListBtn.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_postListFragment)
        }

        binding.writePostBtn.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_writePostFragment)
        }
    }

    private fun initMapFragment() {
        val fm = parentFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map, it).commit()
            }

        mapFragment.getMapAsync(this)
    }

    private fun testMap() {
        val bounds = LatLngBounds.Builder()
            .include(LatLng(37.5640984, 126.9712268))
            .include(LatLng(37.5651279, 126.9767904))
            .include(LatLng(37.5625365, 126.9832241))
            .include(LatLng(37.5585305, 126.9809297))
            .include(LatLng(37.5590777, 126.974617))
            .build()
    }

    @UiThread
    override fun onMapReady(p0: NaverMap) {
        p0.apply {
            mapType = NaverMap.MapType.Navi
//            isNightModeEnabled = true
        }
    }


}