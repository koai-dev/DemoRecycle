package com.koaidev.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.koaidev.myapplication.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapterVertical: DataAdapter
    private lateinit var adapterHorizontal: DataAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapterVertical = DataAdapter(false)
        adapterHorizontal = DataAdapter(true)
        binding.rcvHorizontal.adapter = adapterHorizontal
        binding.rcvVertical.adapter = adapterVertical

        val list = ArrayList<Data>()
        list.add(Data("Số 1"))
        list.add(Data("Số 2"))
        list.add(Data("Số 3"))
        list.add(Data("Số 4"))
        list.add(Data("Số 5"))
        list.add(Data("Số 6"))
        adapterVertical.submitList(list)
        adapterHorizontal.submitList(list)
    }
}