package com.example.imageselect

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageSwitcher
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class MainActivity : AppCompatActivity() {
    private var images : ArrayList<Uri?>? =null
    private val GALLERY_CODE = 22
    private var position = 0
    lateinit var imageSwitcher : ImageSwitcher
    lateinit var fileUri: Uri
    lateinit var storage: FirebaseStorage
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //*find id from xml *//
        imageSwitcher = findViewById(R.id.imageSwitcher)
        val previousBtn : Button = findViewById(R.id.previousBtn)
        val nextBtn : Button = findViewById(R.id.nextBtn)
        val chooseBtn : Button = findViewById(R.id.chooseImageBtn)

        //* init images
        images = ArrayList()

        imageSwitcher.setFactory { ImageView(applicationContext ) }
        chooseBtn.setOnClickListener {
            pickImageIntent()
        }
        nextBtn.setOnClickListener {
            if (position< images!!.size-1){
                position++
                imageSwitcher.setImageURI(images!![position])
            }
            else{
                Toast.makeText(this@MainActivity, "No More Images", Toast.LENGTH_SHORT).show()
            }
        }
        previousBtn.setOnClickListener {
            if (position>0){
                position--
                imageSwitcher.setImageURI(images!![position])
            }
            else{
                Toast.makeText(this@MainActivity, "No More Images", Toast.LENGTH_SHORT).show()
            }

        }
        val nextActivity : TextView = findViewById(R.id.nextActivity)
        nextActivity.setOnClickListener {
            startActivity(Intent(this,View_Page::class.java))
        }
    }

    private fun pickImageIntent(){
        val intent = Intent()
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true)
        intent.action=Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent,"Pick image"),GALLERY_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_CODE){
            if (resultCode == Activity.RESULT_OK){
                if (data!!.clipData !=null){
                    val count = data.clipData!!.itemCount
                    for (i in 0 until count){
                        val imageUri = data.clipData!!.getItemAt(i).uri
                        images!!.add(imageUri)
                        position = 0
                    }
                    imageSwitcher.setImageURI(images!![0])
                    uploadImagesToFirebase()
                }
                else{
                    val imageUri = data.data
                    imageSwitcher.setImageURI(imageUri)
                }
            }
        }
    }
    // Inside your MainActivity
// Add a function to upload images to Firebase Storage
    private fun uploadImagesToFirebase() {
        if (images.isNullOrEmpty()) {
            Toast.makeText(this@MainActivity, "No images to upload", Toast.LENGTH_SHORT).show()
            return
        }

        val storageRef = FirebaseStorage.getInstance().reference

        for (i in images!!.indices){
            val imageUri = images!![i]
            val imageName = "image_$i.jpg" // Change the name as needed

            val imageRef = storageRef.child("imagesCollection/$imageName")
            val firestore = FirebaseFirestore.getInstance()
            imageUri?.let {
                imageRef.putFile(it)
                    .addOnSuccessListener { uploadTask ->
                        // Image uploaded successfully, get download URL
                        uploadTask.storage.downloadUrl
                            .addOnSuccessListener { uri ->
                                // Image download URL obtained
                                val imageUrl = uri.toString()
                                val randomId = UUID.randomUUID().toString()
                                // Store the image URL in Firestore
                                val data =ImageModel(
                                    imageUrl,randomId
                                )
                                // Replace "imagesCollection" with your Firestore collection name
                                firestore.collection("imagesCollection").document(randomId)
                                    .set(data)
                                    .addOnSuccessListener {
                                        Toast.makeText(this@MainActivity, "Image $i URL added to Firestore", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(this@MainActivity, "Error adding image URL to Firestore: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        // Image uploaded successfully
                        Toast.makeText(this@MainActivity, "Image $i uploaded", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        // Error uploading image
                        Toast.makeText(this@MainActivity, "Error uploading image: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

}