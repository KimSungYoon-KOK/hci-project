package com.android.hciproject.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.android.hciproject.R
import com.android.hciproject.databinding.WritePostFragmentBinding
import com.android.hciproject.viewmodels.SharedViewModel
import com.android.hciproject.viewmodels.WritePostViewModel
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.FileOutputStream
import java.util.*


class WritePostFragment : Fragment() {

    private var _binding: WritePostFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: WritePostViewModel
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private var photoUri: Uri? = null
    private var photoID: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = WritePostViewModel()
        _binding = WritePostFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.sharedViewModel = sharedViewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestPermission()
        setBtnOnClickListener()
    }

    private fun requestPermission() {
        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (!isGranted) {
                    Snackbar.make(
                        binding.container,
                        getString(R.string.prompt_request_permission_file),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }

        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) -> {
                // You can use the API that requires the permission.
            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionLauncher.launch(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            }
        }
    }

    private fun setBtnOnClickListener() {
        binding.takePictureBtn.setOnClickListener {
            takePicture()
        }

        binding.closeBtn.setOnClickListener {
            findNavController().navigate(R.id.action_writePostFragment_to_mainFragment)
        }

        binding.nextBtn.setOnClickListener {
//            if (saveBitmapFile()) {
            findNavController().navigate(R.id.action_writePostFragment_to_writeContentFragment)
//            }
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun takePicture() {
        Log.d("Intent", "takePicture")
//        val intent = Intent(ACTION_PICK).apply {
//            type = "image/*"
//        }
        getContent.launch("image/*")

        Log.d("Intent", "takePicture Complete")

        if (photoUri != null && photoID != null)  {
            //sharedViewModel.setWritingPostImageUri(photoUri!!)
            sharedViewModel.setWritingPostImageID(photoID!!)
        } else {
            Snackbar.make(
                binding.container,
                getString(R.string.prompt_fail_photoPick),
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    ////////////////////////////// Load Photo //////////////////////////////
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        Log.d("Intent", uri.toString())

    }
    private val requestActivity = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { activityResult ->
        Log.d("Intent", activityResult.resultCode.toString())
        if (activityResult.resultCode == AppCompatActivity.RESULT_OK && activityResult.data != null) {
            Log.d("Uploading_data", activityResult.data!!.data.toString())
            val selectedImage = activityResult.data!!.data
            val listOfAllImages: MutableList<String> = mutableListOf()
            val projection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                arrayOf(
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.DATE_TAKEN
                )
            } else {
                arrayOf(MediaStore.Images.Media._ID)
            }
            requireContext().contentResolver.query(
                selectedImage!!, projection, null, null, null
            )?.use { cursor ->
                val columnIndexId = cursor.getColumnIndexOrThrow(projection[0])
                val columnIndexName = cursor.getColumnIndexOrThrow(projection[1])
                val columnIndexDate = cursor.getColumnIndexOrThrow(projection[2])
                while (cursor.moveToNext()) {
                    val imageId = cursor.getLong(columnIndexId)
                    photoID = cursor.getString(columnIndexName)
                    val imageDate = Date(cursor.getLong(columnIndexDate))
                    listOfAllImages.add(imageId.toString())
                    photoUri = Uri.withAppendedPath(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        imageId.toString()
                    )
                    Log.d("Uploading_URI", "id: $imageId, Name: $photoID, Date: $imageDate")
                }
                cursor.close()
            }

            Log.d("Uploading_photoUri", photoUri.toString())
        }
    }

    private fun saveBitmapFile(): Boolean {
        if (photoUri == null || photoID == null) {
            Snackbar.make(
                binding.container,
                getString(R.string.prompt_do_photoPick),
                Snackbar.LENGTH_SHORT
            ).show()
            return false
        } else {
            //캐시 파일 생성
            val resolver = requireContext().contentResolver
            try {
                val inputStream = resolver.openInputStream(photoUri!!)
                val options = BitmapFactory.Options()
                options.inSampleSize = 4
                val imgBitmap = BitmapFactory.decodeStream(inputStream, null, options)
                inputStream?.close()
                saveBitmapToJpeg(imgBitmap!!, photoID!!)
                Log.d("Uploading", "Save Cache Success")
            } catch (e: Exception) {
                Log.e("Uploading", "Save Cache Fail")
            }
            return true
        }
    }

    private fun saveBitmapToJpeg(bitmap: Bitmap, imgName: String) {   // 선택한 이미지 내부 저장소에 저장
        val path = requireContext().getDir("tmp", Context.MODE_PRIVATE).path
        val tempFile = File(path, imgName) // 파일 경로와 이름 넣기
        try {
            tempFile.createNewFile() // 자동으로 빈 파일을 생성하기
            val out = FileOutputStream(tempFile) // 파일을 쓸 수 있는 스트림을 준비하기
            bitmap.compress(Bitmap.CompressFormat.PNG, 50, out) // compress 함수를 사용해 스트림에 비트맵을 저장하기
            out.close() // 스트림 닫아주기
            Log.d("Uploading", "Save Bitmap Success")
        } catch (e: java.lang.Exception) {
            Log.e("Uploading", "Save Bitmap Fail")
        }
    }
}
