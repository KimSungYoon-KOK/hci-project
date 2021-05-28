package com.android.hciproject.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import androidx.appcompat.widget.SearchView
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
import com.naver.maps.map.*
import com.naver.maps.map.overlay.CircleOverlay
import com.naver.maps.map.overlay.InfoWindow
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.util.FusedLocationSource
import android.graphics.Color
import android.location.Location
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MainFragment : Fragment(), OnMapReadyCallback {

    private lateinit var binding: MainFragmentBinding
    private val viewModel: MainFragmentViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var naverMap: NaverMap
    private lateinit var mLocationSource: FusedLocationSource
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val PERMISSION_REQUEST_CODE = 100
    var overlay = CircleOverlay()
    private lateinit var postMarkers: ArrayList<Marker>

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
        init()
        requestPermission()
        initMapFragment()
        setOnClickListener()
        setSearchListener()
    }

    fun init() {
        postMarkers = ArrayList()
    }

    @SuppressLint("MissingPermission")
    private fun setFusedLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val temp = LatLng(
                        location.latitude,
                        location.longitude
                    )
                    sharedViewModel.latLng.postValue(temp)
                    makeOverlay(temp)
                }
            }
    }

    private fun setSearchListener() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    val location = LocationUtils.addressToLocation(query, requireContext())
                    if (location != null) {
//                        searchMarker.map = null
//                        searchMarker = Marker()
//                        searchMarker.iconTintColor = Color.BLUE
//                        searchMarker.position = location
//                        searchMarker.map = naverMap
//                        val infoWindow = InfoWindow()
//                        infoWindow.adapter =
//                            object : InfoWindow.DefaultTextAdapter(requireContext()) {
//                                override fun getText(infoWindow: InfoWindow): CharSequence {
//                                    return query.toString()
//                                }
//                            }
//                        infoWindow.open(searchMarker)
//
//                        naverMap.moveCamera(CameraUpdate.scrollAndZoomTo(location, 15.0))
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
        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {

                } else {
                    Snackbar.make(
                        binding.container,
                        getString(R.string.prompt_request_permission),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }

        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
            -> {
                // You can use the API that requires the permission.
            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionLauncher.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                requestPermissionLauncher.launch(
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            }
        }
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
            overlay.radius = 5000.0
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
        mLocationSource = FusedLocationSource(this, PERMISSION_REQUEST_CODE)
//        searchMarker = Marker()

        val fm = childFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map, it).commit()
            }
        mapFragment.getMapAsync(this)
    }

    @UiThread
    override fun onMapReady(p0: NaverMap) {
        naverMap = p0

        naverMap.apply {
            mapType = NaverMap.MapType.Navi
            locationSource = mLocationSource
            locationTrackingMode = LocationTrackingMode.Follow
        }

        setFusedLocationClient()
        setMapListener()
        makeMarker()
        setMapComponent()
    }

    private fun setMapListener() {
        naverMap.setOnMapClickListener { point, coord ->
            naverMap.moveCamera(CameraUpdate.scrollTo(coord))
            makeOverlay(coord)
        }
    }

    private fun makeOverlay(_latLng: LatLng) {
        overlay.center = _latLng
        overlay.radius = sharedViewModel.selectedOverlaySize.value!!       //1000m
        overlay.color = getColor(requireContext(), R.color.overlayColor)
        overlay.map = naverMap
    }

    private fun makeMarker() {
        for (post in sharedViewModel.postList.value!!) {
            val listener = Overlay.OnClickListener { overlay ->
                sharedViewModel.selectedPost.value = post
                val intent = Intent(requireContext(), PostDetailActivity::class.java)
                Log.d("MainFragment", post.toString())
                intent.putExtra("post", post)
                startActivity(intent)
                true
            }
            val infoWindow = InfoWindow()
            infoWindow.adapter = object : InfoWindow.DefaultTextAdapter(requireContext()) {
                override fun getText(infoWindow: InfoWindow): CharSequence {
                    return post.title
                }
            }
            val postMarker = Marker(LatLng(post.uploadLat, post.uploadLng))
            postMarker.onClickListener = listener
            postMarker.map = naverMap
            infoWindow.open(postMarker)

            postMarkers.add(postMarker)
        }
    }


}
