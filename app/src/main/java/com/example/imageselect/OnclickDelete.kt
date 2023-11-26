package com.example.imageselect

interface OnclickDelete {
    fun onDelete(documentId: String, position: Int)
    fun onUpdate(documentId: String, position: Int)
}