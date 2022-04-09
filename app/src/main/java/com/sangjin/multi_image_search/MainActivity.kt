package com.sangjin.multi_image_search

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.sangjin.runPhotoAlbums
import java.io.ByteArrayOutputStream


class MainActivity : AppCompatActivity() {

    var mStorageRef: StorageReference? = null
    var mAuth: FirebaseAuth? = null

    val addButton: Button by lazy {
        findViewById(R.id.addButton)
    }
    val runButton: Button by lazy {
        findViewById(R.id.runButton)
    }

    var list = ArrayList<Uri>()
    var resultUriList = mutableListOf<Uri>()
    val adpater = imageAdapter(list, this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mStorageRef = FirebaseStorage.getInstance().reference
        mAuth = FirebaseAuth.getInstance()

        initaddbutton()
        initRunButton()

        val recyclerView: RecyclerView = findViewById(R.id.imageRecyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adpater


    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = mAuth!!.getCurrentUser()
        mAuth!!.signInAnonymously().addOnSuccessListener(this, OnSuccessListener<AuthResult>() {

            fun onSuccess(authResult : AuthResult) {
                // do your stuff
            }
        })
            .addOnFailureListener(this, OnFailureListener() {

                fun onFailure( exception: Exception) {
                    Log.e("TAG", "signInAnonymously:FAILURE", exception);
                }
            });
    }

    private fun initaddbutton() {
        addButton.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                )
                        == PackageManager.PERMISSION_GRANTED -> {
                    getImage()
                }
                shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                    alertDialog()
                }
                else -> requestPermissions(
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    1000
                )
            }
        }
    }

    private fun getImage() {
        var intent = Intent(Intent.ACTION_PICK).apply {
            data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            action = Intent.ACTION_GET_CONTENT
        }
        startActivityForResult(intent, 2000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            return
        }
        when (requestCode) {
            2000 -> {
                data?.let {
                    if (it.clipData != null) { //사진이 여러장 선택되었을때
                        val count = data.clipData!!.itemCount
                        for (i in 0 until count) {
                            val imageuri = data.clipData!!.getItemAt(i).uri
                            list.add(imageuri)
                            uploadImage(imageuri, "image$count")
                        }
                    } else { //사진이 한장만 선택 되었을때
                        data?.data?.let { uri ->
                            val imageUri: Uri? = data?.data
                            if (imageUri != null) {
                                list.add(imageUri)
                                uploadImage(imageUri, "image")
                            }
                        }
                    }
                }
            }
            else -> {
                Toast.makeText(this, "이미지를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
        adpater.notifyDataSetChanged()
    }

    private fun uploadImage(uri: Uri, imgName: String) {
        var bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri)
        var byteStream: ByteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteStream)

        var riversRef: StorageReference = mStorageRef!!.child("image/$imgName")
        riversRef.putFile(uri)
            .addOnProgressListener { taskSnapshot: UploadTask.TaskSnapshot ->
                Toast.makeText(applicationContext, "Updating", Toast.LENGTH_LONG).show()
            }
            .addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot ->
                mStorageRef!!.child("image/$imgName").downloadUrl.addOnSuccessListener {
                    searchImage(it.toString())
                    Log.d(TAG, "MainActivity - uploadImage(1) $it")
                    Log.d(TAG, "MainActivity - uploadImage(2) ${mStorageRef!!.child("image/$imgName").downloadUrl}")
                }.addOnFailureListener {
                    Toast.makeText(applicationContext, "Download Failed", Toast.LENGTH_LONG).show()
                }
                // Get a URL to the uploaded content
            }
            .addOnFailureListener {
                Toast.makeText(applicationContext, "Failed", Toast.LENGTH_LONG).show()
            }
    }

    private fun searchImage(imgPath: String) {
        val size = list.size
        var base_url = "https://www.google.com/searchbyimage?image_url="
        for (i in 0..size) {
            var uri = Uri.parse(base_url + imgPath)
            resultUriList.add(i, uri)
        }
    }



    private fun initRunButton() {
        runButton.setOnClickListener {
            val intent = Intent(this, runPhotoAlbums::class.java)
            list.forEachIndexed { index, uri ->
                intent.putExtra("image$index", uri.toString())
            }
            intent.putExtra("imageListSize", list.size)
            startActivity(intent)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            1000 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getImage()
                } else {
                    Toast.makeText(this, "권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {}
        }
    }

//    // Calls the server to securely obtain an unguessable download Url
//    private fun getUrlAsync(date: String) {
//        // Points to the root reference
//        val storageRef = FirebaseStorage.getInstance().reference
//        val dateRef = storageRef.child("/$date.csv")
//        dateRef.downloadUrl.addOnSuccessListener {
//            //do something with downloadurl
//        }
//    }

    private fun alertDialog() {
        AlertDialog.Builder(this).apply {
            this.setTitle("권한 요청")
                .setMessage("사진을 가져오기 위한 권한이 필요합니다.")
                .setPositiveButton("확인") { _, _ ->
                    requestPermissions(
                        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                        1000
                    )
                }
                .setNegativeButton("취소") { _, _ -> }
                .create()
                .show()
        }
    }
}