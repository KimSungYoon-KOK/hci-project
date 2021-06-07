package com.android.hciproject.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.UiThread
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.databinding.DataBindingUtil.inflate
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.amazonaws.amplify.generated.graphql.ListPostsQuery
import com.amazonaws.mobile.client.AWSMobileClient
import com.android.hciproject.ClientFactory
import com.android.hciproject.R
import com.android.hciproject.data.Post
import com.android.hciproject.databinding.MainFragmentBinding
import com.android.hciproject.utils.ColorUtils
import com.android.hciproject.utils.LocationUtils
import com.android.hciproject.utils.MyTimeUtils
import com.android.hciproject.viewmodels.MainFragmentViewModel
import com.android.hciproject.viewmodels.SharedViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.*
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import kotlin.math.abs

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
    private lateinit var infoWindow: InfoWindow
    private lateinit var polyLine: PolylineOverlay
    private val clientFactory = ClientFactory()

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
        initDB()
        init()
        requestPermission()
        initMapFragment()
        setOnClickListener()
        setSearchListener()
    }

    private fun observeLatLng() {
        sharedViewModel.latLng.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            fetchPostListFromLocation(it)
        })
    }

    private fun initDB() {
        clientFactory.init(requireContext())
        sharedViewModel.fetchLoginUserName(AWSMobileClient.getInstance().username)
        sharedViewModel.fetchDB(clientFactory)
    }

    private fun init() {
        postMarkers = ArrayList()
        infoWindow = InfoWindow()
        polyLine = PolylineOverlay()
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
                    sharedViewModel.fetchSelectedLatLng(temp)
                    fetchPostListFromLocation(temp)
                }
            }
    }

    private fun setSearchListener() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    searchPost(query)
                }
                hideKeyboard()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

        })
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchView.windowToken, 0)
    }

    private fun searchPost(query: String) {
        deleteMarker()
        for (post in sharedViewModel.postList.value!!) {
            if (post.title().contains(query) || (!post.content().isNullOrEmpty() && post.content()!!
                    .contains(query))
            ) {
                addMarker(post)
            }
        }
    }

    private fun requestPermission() {
        val requestPermissionLauncher = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (!isGranted) {
                    Snackbar.make(
                        binding.container,
                        getString(R.string.prompt_request_permission_locate),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }

        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ), ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ), ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) -> {
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
                requestPermissionLauncher.launch(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
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
            naverMap.moveCamera(CameraUpdate.zoomTo(8.0).animate(CameraAnimation.Fly))
            sharedViewModel.fetchSelectedOverlaySize(50000.0)
        }

        binding.zoomLevel2Btn.setOnClickListener {
            naverMap.moveCamera(CameraUpdate.zoomTo(11.0).animate(CameraAnimation.Fly))
            sharedViewModel.fetchSelectedOverlaySize(5000.0)
        }

        binding.zoomLevel3Btn.setOnClickListener {
            naverMap.moveCamera(CameraUpdate.zoomTo(14.0).animate(CameraAnimation.Fly))
            sharedViewModel.fetchSelectedOverlaySize(1000.0)
        }
    }

    private fun setMapComponent() {
        binding.locationBtn.map = naverMap
    }

    private fun initMapFragment() {
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

        mLocationSource = FusedLocationSource(this, PERMISSION_REQUEST_CODE)
        naverMap.apply {
            locationSource = mLocationSource
            mapType = NaverMap.MapType.Navi
            locationTrackingMode = LocationTrackingMode.Follow
            uiSettings.isZoomControlEnabled = false
        }

        setFusedLocationClient()
        setMapListener()
        setMapComponent()
        sharedViewModel.fetchAddressFromLocation(requireContext())
        observeLatLng()
        observeOverlay()
    }

    private fun observeOverlay() {
        sharedViewModel.selectedOverlaySize.observe(
            viewLifecycleOwner,
            {
                fetchPostListFromLocation(sharedViewModel.latLng.value!!)
                makeOverlay(sharedViewModel.latLng.value!!)
            })
    }

    private fun setMapListener() {
        naverMap.setOnMapClickListener { _, coord ->
            naverMap.moveCamera(CameraUpdate.scrollTo(coord).animate(CameraAnimation.Linear))
            sharedViewModel.fetchSelectedLatLng(coord)
        }
    }

    private fun fetchPostListFromLocation(selectedLatLng: LatLng) {
        if (!sharedViewModel.postList.value.isNullOrEmpty()) {
            deleteMarker()
            makeOverlay(selectedLatLng)

            val overlaySize = sharedViewModel.selectedOverlaySize.value!!
            for (post in sharedViewModel.postList.value!!) {
                val postLatLng =
                    LatLng(post.uploadLat()!!.toDouble(), post.uploadLng()!!.toDouble())
                if (abs(
                        LocationUtils.getDistance(
                            selectedLatLng,
                            postLatLng
                        )
                    ) < overlaySize && MyTimeUtils.getTimeDiff(MyTimeUtils.getTimeInMillis(post.createdAt())) <= 100
                ) {
                    addMarker(post)
                }
            }
        }
    }

    private fun makeOverlay(_latLng: LatLng) {
        overlay.center = _latLng
        overlay.radius = sharedViewModel.selectedOverlaySize.value!!       //1000m
        overlay.color = getColor(requireContext(), R.color.overlayColor)
        overlay.map = naverMap
    }

    private fun drawPolyLine(latLng: LatLng) {
        polyLine.coords = listOf(
            naverMap.locationOverlay.position,
            latLng
        )
        polyLine.width = 3
        polyLine.color = getColor(requireContext(), R.color.grayColor500)
        polyLine.setPattern(20, 10)
        polyLine.map = naverMap
    }

    private fun addMarker(post: ListPostsQuery.Item) {
        val listener = Overlay.OnClickListener { overlay ->
            val postLatLng =
                LatLng(post.uploadLat()!!.toDouble(), post.uploadLng()!!.toDouble())
            val marker = overlay as Marker
            infoWindow.adapter = object : InfoWindow.DefaultViewAdapter(requireContext()) {
                override fun getContentView(p0: InfoWindow): View {
                    val v = layoutInflater.inflate(
                        R.layout.infowindow_item,
                        binding.container,
                        false
                    )
                    v.findViewById<TextView>(R.id.title).text = post.title()
                    v.findViewById<TextView>(R.id.distance).text =
                        LocationUtils.distanceToText(
                            LocationUtils.getDistance(
                                naverMap.locationOverlay.position,
                                postLatLng
                            )
                        )
                    return v
                }

            }
            if (marker.infoWindow == null) {
                // 정보 창이 열려있지 않은 경우
                infoWindow.open(marker)
                drawPolyLine(postLatLng)
            } else {
                // 정보 창이 열려있는 경우
                //infoWindow.close()
                val intent = Intent(requireContext(), PostDetailActivity::class.java)
                Log.d("MainFragment", post.toString())
                val tempPost = Post(post)
                intent.putExtra("post", tempPost)
                intent.putExtra("username", sharedViewModel.loginUserName.value!!)
                startActivity(intent)
            }

            true
        }
        val postMarker =
            Marker(LatLng(post.uploadLat()!!.toDouble(), post.uploadLng()!!.toDouble()))
        postMarker.onClickListener = listener
        postMarker.icon = MarkerIcons.BLACK
        postMarker.iconTintColor = getColor(
            requireContext(),
            ColorUtils.getColorFromTime(MyTimeUtils.getTimeDiff(MyTimeUtils.getTimeInMillis(post.createdAt())))
        )
        postMarker.map = naverMap

        postMarkers.add(postMarker)
    }

    private fun deleteMarker() {
        for (m in postMarkers)
            m.map = null
        postMarkers.clear()
    }
}
