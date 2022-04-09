package com.sangjin

import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.sangjin.multi_image_search.R
import java.io.ByteArrayOutputStream

class runPhotoAlbums : AppCompatActivity() {

    var mStorageRef: StorageReference? = null
    var mAuth: FirebaseAuth? = null

    private val imageList = mutableListOf<Uri>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_run_photo_albums)

        mStorageRef = FirebaseStorage.getInstance().reference
        mAuth = FirebaseAuth.getInstance()

        getImageUri()

    }

    private fun getImageUri() {
        val size = intent.getIntExtra("imageListSize", 0)
        for (i in 0..size) {
            intent.getStringExtra("image$i")?.let { imageList.add(Uri.parse(it)) }
        }
    }

    //구글 이미지 검색 링크의 리스트를 반환한다.
    private fun searchImage(imgPath: String): List<Uri> {
        val size = imageList.size
        var resultUriList = mutableListOf<Uri>()
        var base_url = "https://www.google.com/searchbyimage?image_url="

        for (i in 0..size) {
            var uri = Uri.parse(base_url + imgPath)
            resultUriList.add(i, uri)
            Log.d(TAG, "runPhotoAlbums - result URI - ${uri}")
        }
        return resultUriList
    }


}