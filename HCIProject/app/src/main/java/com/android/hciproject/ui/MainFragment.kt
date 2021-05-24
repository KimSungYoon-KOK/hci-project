package com.android.hciproject.ui

import android.Manifest
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.findNavController
import com.android.hciproject.R
import com.android.hciproject.databinding.MainFragmentBinding
import com.android.hciproject.utils.LocationUtils
import com.android.hciproject.viewmodels.MainViewModel
import com.google.android.material.snackbar.Snackbar
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.*
import com.naver.maps.map.overlay.InfoWindow
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.util.FusedLocationSource

class MainFragment : Fragment(), OnMapReadyCallback {

    private var _binding: MainFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MainViewModel
    private lateinit var mapFragment: MapFragment
    private lateinit var naverMap: NaverMap
    lateinit var infoWindow: InfoWindow
    private lateinit var searchMarker: Marker
    private lateinit var mLocationSource: FusedLocationSource
    private val PERMISSION_REQUEST_CODE = 100
    private val PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

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
        requestPermission()
        testMap()
        initMapFragment()
        setOnClickListener()
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

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    val location = LocationUtils.addressToLocation(query, requireContext())
                    if (location != null) {
                        searchMarker.map = null
                        searchMarker = Marker()
                        searchMarker.position = location
                        searchMarker.map = naverMap
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

        binding.zoomLevel1Btn.setOnClickListener {
            naverMap.moveCamera(CameraUpdate.zoomTo(5.0))
        }

        binding.zoomLevel2Btn.setOnClickListener {
            naverMap.moveCamera(CameraUpdate.zoomTo(10.0))
        }

        binding.zoomLevel3Btn.setOnClickListener {
            naverMap.moveCamera(CameraUpdate.zoomTo(15.0))
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

        mLocationSource = FusedLocationSource(this, PERMISSION_REQUEST_CODE)
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


        infoWindow = InfoWindow()
        infoWindow.adapter = object : InfoWindow.DefaultTextAdapter(requireContext()) {
            override fun getText(infoWindow: InfoWindow): CharSequence {
                return "게시글 이름"
            }
        }
    }

    @UiThread
    override fun onMapReady(p0: NaverMap) {
        this.naverMap = p0
        val cameraPosition = CameraPosition(LatLng(37.54225941463205, 127.07629578159484), 8.0)
        val testMarker = Marker(LatLng(37.54225941463205, 127.07629578159484))
        val listener = Overlay.OnClickListener { overlay ->
            val marker = overlay as Marker
            if (marker.infoWindow == null) {
                // 현재 마커에 정보 창이 열려있지 않을 경우 엶
                infoWindow.open(marker)
                infoWindow.setOnClickListener {
                    findNavController().navigate(R.id.action_mainFragment_to_postDetailFragment)
                    true
                }
            } else {
                // 이미 현재 마커에 정보 창이 열려있을 경우 닫음
                infoWindow.close()
            }

            true


        }

        testMarker.onClickListener = listener

        testMarker.map = naverMap
        naverMap.apply {
            this.cameraPosition = cameraPosition
            mapType = NaverMap.MapType.Navi
//            locationSource = mLocationSource
        }

        setMapComponent()
    }
}