package com.example.happyplaces

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_map.*

class MapActivity : AppCompatActivity(),OnMapReadyCallback {
    private var mPlaceModel:PlaceModel?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        setSupportActionBar(tool_bar_Map_Activity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        tool_bar_Map_Activity.setNavigationOnClickListener {
            onBackPressed()
        }
        if(intent.hasExtra(MainActivity.EXTRA_DETAIL))
        {
            mPlaceModel=intent.getParcelableExtra(MainActivity.EXTRA_DETAIL)
        }
        if(mPlaceModel!=null)
        {
            supportActionBar!!.title=mPlaceModel!!.title
            val supportMapFragment:SupportMapFragment
            =supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            supportMapFragment.getMapAsync(this)
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        val position=LatLng(mPlaceModel!!.latitude,mPlaceModel!!.longitude)
        googleMap!!.addMarker(MarkerOptions().position(position).title(mPlaceModel!!.title))
        var newLatLngZoom=CameraUpdateFactory.newLatLngZoom(position,10f)
        googleMap.animateCamera(newLatLngZoom)
    }
}