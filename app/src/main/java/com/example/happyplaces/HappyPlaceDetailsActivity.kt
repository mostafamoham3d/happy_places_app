package com.example.happyplaces

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_happy_place_details.*

class HappyPlaceDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        var placeModel:PlaceModel?=null
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_happy_place_details)
        setSupportActionBar(tool_bar_place_detail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        tool_bar_place_detail.setNavigationOnClickListener {
            onBackPressed()
        }
        if(intent.hasExtra(MainActivity.EXTRA_DETAIL))
        {
            placeModel=intent.getParcelableExtra(MainActivity.EXTRA_DETAIL)
        }
        if(placeModel!=null)
        {
            supportActionBar!!.title=placeModel.title
            iv_place_image.setImageURI(Uri.parse(placeModel.image))
            tv_description.text=placeModel.description
            tv_location.text=placeModel.location

            btn_view_on_map.setOnClickListener {
                val intent=Intent(this,MapActivity::class.java)
                intent.putExtra(MainActivity.EXTRA_DETAIL,placeModel)
                startActivity(intent)
            }
        }

    }
}