package com.example.azkar.services

import android.view.View

interface OnItemTouchListener {
    fun onItemClick(view: View?, position: Int)
}