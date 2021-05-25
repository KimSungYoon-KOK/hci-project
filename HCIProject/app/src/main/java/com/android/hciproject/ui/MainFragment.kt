package com.android.hciproject.ui

import android.Manifest
import android.icu.text.IDNA
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil.inflate
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.android.hciproject.R
import com.android.hciproject.databinding.MainFragmentBinding
import com.android.hciproject.utils.LocationUtils
import com.android.hciproject.viewmodels.MainFragmentViewModel
import com.android.hciproject.viewmodels.SharedViewModel
import com.google.android.material.snackbar.Snackbar
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.*
import com.naver.maps.map.overlay.CircleOverlay
import com.naver.maps.map.overlay.InfoWindow
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.util.FusedLocationSource
import android.graphics.Color
import androidx.core.content.ContextCompat.getColor

class MainFragment : Fragment(), OnMapReadyCallback {

    private lateinit var binding: MainFragmentBinding
    private val viewModel: MainFragmentViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var mapFragment: MapFragment
    private lateinit var naverMap: NaverMap
    private lateinit var searchMarker: Marker
    private lateinit var mLocationSource: FusedLocationSource
    private val PERMISSION_REQUEST_CODE = 100
    private val PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    var overlay = CircleOverlay()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = inflate(inflater, R.layout.main_fragment, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.sharedViewModel = sharedViewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestPermission()
        testMap()
        initMapFragment()
        setOnClickListener()
        setSearchListener()
    }

    private fun setSearchListener() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    val location = LocationUtils.addressToLocation(query, requireContext())
                    if (location != null) {
                        searchMarker.map = null
                        searchMarker = Marker()
                        searchMarker.iconTintColor = Color.BLUE
                        searchMarker.position = location
                        searchMarker.map = naverMap
                        val infoWindow = InfoWindow()
                        infoWindow.adapter =object : InfoWindow.DefaultTextAdapter(requireContext()) {
                            override fun getText(infoWindow: InfoWindow): CharSequence {
                                return query.toString()
                            }
                        }
                        infoWindow.open(searchMarker)

                        naverMap.moveCamera(CameraUpdate.scrollAndZoomTo(location, 15.0))
                    } else {
                        Snackbar.make(
                            binding.container,
                            getString(R.string.prompt_no_result),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }

                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

        })
    }


    private fun requestPermission() {
        ActivityCompat.requestPermissions(requireActivity(), PERMISSIONS, PERMISSION_REQUEST_CODE)

    }

    private fun setOnClickListener() {
        binding.showPostListBtn.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_postListFragment)
        }

        binding.writePostBtn.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_writePostFragment)
        }

        binding.zoomLevel1Btn.setOnClickListener {
            naverMap.moveCamera(CameraUpdate.zoomTo(7.0))
            overlay.radius = 50000.0
        }

        binding.zoomLevel2Btn.setOnClickListener {
            naverMap.moveCamera(CameraUpdate.zoomTo(12.0))
            overlay.radius = 4000.0
        }

        binding.zoomLevel3Btn.setOnClickListener {
            naverMap.moveCamera(CameraUpdate.zoomTo(14.0))
            overlay.radius = 1000.0
        }

    }

    private fun setMapComponent() {
        binding.locationBtn.map = naverMap
    }

    private fun initMapFragment() {
        val fm = parentFragmentManager
        mapFragment = fm.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map, it).commit()
            }
        mapFragment.getMapAsync(this)

        sharedViewModel.mLocationSource.value = FusedLocationSource(this, PERMISSION_REQUEST_CODE)
        searchMarker = Marker()
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
        this.naverMap = p0
        val cameraPosition = CameraPosition(LatLng(37.54225941463205, 127.07629578159484), 14.0)
        overlay.center = LatLng(37.54225941463205, 127.07629578159484)
        overlay.map = naverMap
        overlay.radius = 1000.0         //1000m
        overlay.color = getColor(requireContext(), R.color.overlayColor)

        makeMarker()
        Log.d("makeMarker",  sharedViewModel.postList.value?.size.toString())
        naverMap.apply {
            this.cameraPosition = cameraPosition
            mapType = NaverMap.MapType.Navi
            locationSource = sharedViewModel.mLocationSource.value
        }
        setMapComponent()

    }

    private fun makeMarker() {
        for (post in sharedViewModel.postList.value!!) {
            val postMarker = Marker(LatLng(post.uploadLat, post.uploadLng))
            val listener = Overlay.OnClickListener { overlay ->
                val marker = overlay as Marker
                val infoWindow = InfoWindow()
                infoWindow.adapter =object : InfoWindow.DefaultTextAdapter(requireContext()) {
                    override fun getText(infoWindow: InfoWindow): CharSequence {
                        return post.title
                    }
                }
                if (marker.infoWindow == null) {
                    // 현재 마커에 정보 창이 열려있지 않을 경우 엶
                    infoWindow.open(marker)
                    infoWindow.setOnClickListener {
                        sharedViewModel.selectedPost.value = post
                        findNavController().navigate(R.id.action_mainFragment_to_postDetailFragment)
                        true
                    }
                } else {
                    // 이미 현재 마커에 정보 창이 열려있을 경우 닫음
                    infoWindow.close()
                }

                true
            }
            postMarker.onClickListener = listener
            postMarker.map = naverMap
        }
    }
}
