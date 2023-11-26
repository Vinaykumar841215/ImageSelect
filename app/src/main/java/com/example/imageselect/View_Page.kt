package com.example.imageselect

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class View_Page : AppCompatActivity(),OnclickDelete {
    lateinit var recyclerView: RecyclerView
    lateinit var firestore : FirebaseFirestore
    lateinit var listItem : ArrayList<ImageModel>
    lateinit var imageAdapter: ImageAdapter
    private var Imageposition:Int?=null

    private val PICK_IMAGE_REQUEST_CODE = 1001
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_page)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        firestore = FirebaseFirestore.getInstance()
        listItem= arrayListOf()
        imageAdapter = ImageAdapter(this,listItem,this)
        recyclerView.adapter = imageAdapter
        firestore.collection("imagesCollection").get()
            .addOnSuccessListener {
                val data = it.toObjects(ImageModel::class.java)
                listItem.addAll(data)
                imageAdapter.notifyDataSetChanged()

            }
            .addOnFailureListener {
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }

    }
    override fun onDelete(documentId: String, position: Int) {
        deleteImage(documentId, position)
    }

    override fun onUpdate(documentId: String, position: Int) {
        // Create an intent to pick an image from the gallery
        Imageposition =position
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImageUri: Uri? = data.data

            if (selectedImageUri != null) {
                // Here, you might want to update the image URL in your data model or upload the new image to Firebase
                listItem[Imageposition!!].documentId?.let {
                    updateImage(
                        it,
                        selectedImageUri,
                        Imageposition!!
                    )
                }
            } else {
                Toast.makeText(this, "Failed to retrieve selected image", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun updateImage(documentId: String, imageUri: Uri, position: Int) {
        // Upload the new image to Firebase Storage (assuming you're using Firebase)
        // You need to have Firebase Storage set up in your project
        val storageRef = FirebaseStorage.getInstance().reference.child("images/${UUID.randomUUID()}")
        storageRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                // Get the download URL for the uploaded image
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    // Update the image URL in your Firestore database or any other data structure
                    val imageUrl = downloadUri.toString()
                    listItem[position].imageUrl = imageUrl
                    imageAdapter.notifyItemChanged(position)

                    // Update the image URL in your Firestore database
                    updateImageUrlInFirestore(documentId, imageUrl)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Image upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun updateImageUrlInFirestore(documentId: String, imageUrl: String) {
        // Update the image URL in your Firestore document
        val firestore = FirebaseFirestore.getInstance()
        val documentReference = firestore.collection("imagesCollection").document(documentId)
        documentReference.update("imageUrl", imageUrl)
            .addOnSuccessListener {
                Toast.makeText(this, "Image URL updated in Firestore", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update image URL in Firestore: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteImage(documentId: String, position: Int) {
        firestore.collection("imagesCollection").document(documentId)
            .delete()
            .addOnSuccessListener {
                // Delete successful
                Toast.makeText(this, "Delete Successfully!!!", Toast.LENGTH_SHORT).show()
                // Remove the item from the dataset and update the adapter
                finish()
                startActivity(Intent(this,View_Page::class.java))
            }
            .addOnFailureListener { e ->
                // Handle the failure to delete
                Toast.makeText(this, "Delete Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


}