package com.example.imageselect

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ImageAdapter(val context: Context, private val listItem: ArrayList<ImageModel>, val onclickDelete: OnclickDelete) : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val imageView : ImageView = itemView.findViewById(R.id.recy_imageView)
        val morevert : ImageView = itemView.findViewById(R.id.recy_moreVert)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.item_image,parent,false)
        return ViewHolder(layout)
    }

    override fun getItemCount(): Int {
        return listItem.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        Glide.with(context)
            .load(listItem[position].imageUrl)
//            .placeholder(R.drawable.ic_launcher_background) // Add a placeholder image
//            .error(R.drawable.more_vert) // Add an error image
            .into(holder.imageView)
        holder.morevert.setOnClickListener {
            val popupMenu = PopupMenu(context,holder.morevert)
            popupMenu.menuInflater.inflate(R.menu.menu_item,popupMenu.menu)
            popupMenu.show()
            popupMenu.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.delete->{
                        val documentId = listItem[position].documentId

                        // Invoke the deleteImage method through the callback interface
                        if (documentId != null) {
                            onclickDelete.onDelete(documentId, position)
                        }

                        Toast.makeText(context, "Delete Successfully!!! $documentId", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.update->{
                        val documentId = listItem[position].documentId

                        // Invoke the deleteImage method through the callback interface
                        if (documentId != null) {
                            onclickDelete.onUpdate(documentId, position)
                        }

                        Toast.makeText(context, "Delete Successfully!!! $documentId", Toast.LENGTH_SHORT).show()
                        true
                    }
                }
                true
            }

        }
    }
}