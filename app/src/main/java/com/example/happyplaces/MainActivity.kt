package com.example.happyplaces

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fabAddHappyPlaces.setOnClickListener {
            startActivityForResult(Intent(this,AddHappyPlaces::class.java), SEND_RESULT)
        }
        getList()
    }
    fun getList():ArrayList<PlaceModel>
    {
        val dbHandler=DatabaseHandler(this)
        val getPlaceList:ArrayList<PlaceModel> = dbHandler.viewHappyPlaces()
        if (getPlaceList.size>0)
        {
            rv_happy_places.visibility=View.VISIBLE
            tv_no_record_available.visibility=View.GONE
            setUpRecyclerView(getPlaceList)
        }else
        {
            rv_happy_places.visibility=View.GONE
            tv_no_record_available.visibility=View.VISIBLE
        }
        return getPlaceList
    }

    fun setUpRecyclerView(happyList:ArrayList<PlaceModel>)
    {
        rv_happy_places.visibility= View.VISIBLE
        tv_no_record_available.visibility=View.GONE
        rv_happy_places.layoutManager=LinearLayoutManager(this)
        val placeAdapter=HappyAdapter(this,happyList)
        rv_happy_places.adapter=placeAdapter
        placeAdapter.setOnClickListner(object:HappyAdapter.OnClickListner{
            override fun onClick(postion: Int, model: PlaceModel) {
                val intent=Intent(this@MainActivity,HappyPlaceDetailsActivity::class.java)
                intent.putExtra(EXTRA_DETAIL,model)
                startActivity(intent)
            }
        })
        val editSwipeHandler=object :SwipeToEdit(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter=rv_happy_places.adapter as HappyAdapter
                adapter.notifyEditItem(this@MainActivity,viewHolder.adapterPosition, SEND_RESULT)
            }

        }
        val editItemTouchHelper=ItemTouchHelper(editSwipeHandler)
        editItemTouchHelper.attachToRecyclerView(rv_happy_places)

        val deleteSwipeHandler=object :SwipeToDelete(this)
        {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter=rv_happy_places.adapter as HappyAdapter
                adapter.notifyDeletItem(viewHolder.adapterPosition)
                getList()
            }
        }
        val deleteItemTouchHelper=ItemTouchHelper(deleteSwipeHandler)
        deleteItemTouchHelper.attachToRecyclerView(rv_happy_places)

    }
    companion object{
        var SEND_RESULT=1
        var EXTRA_DETAIL="name"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode== SEND_RESULT)
        {
            if (resultCode==Activity.RESULT_OK){
                getList()
            }else{
                Log.e("Activity","Canceled")
            }
        }
    }
}