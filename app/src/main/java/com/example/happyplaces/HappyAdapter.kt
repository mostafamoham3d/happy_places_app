package com.example.happyplaces

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_row.view.*

class HappyAdapter(val context: Context,val items:ArrayList<PlaceModel>):RecyclerView.Adapter<HappyAdapter.Viewholder>() {
private var onClickListner:OnClickListner?=null
    class Viewholder(view:View):RecyclerView.ViewHolder(view)
    {
        val ivImage=view.ivImageItem
        val tvTitle=view.tvTitle
        val tvDescription=view.tvDescription
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        return Viewholder(LayoutInflater.from(context).inflate(R.layout.item_row,parent,false))
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        val item=items.get(position)
        if(holder is Viewholder) {
            holder.tvTitle.text = item.title
            holder.tvDescription.text = item.description
            holder.ivImage.setImageURI(Uri.parse(item.image))
            holder.itemView.setOnClickListener {
                if(onClickListner!=null)
                {
                    onClickListner!!.onClick(position,item)
                }
            }
        }
        }

    override fun getItemCount(): Int {
        return items.size
    }
    fun setOnClickListner(onClickListner:OnClickListner)
    {
        this.onClickListner=onClickListner
    }
    interface OnClickListner{
        fun onClick(postion:Int,model:PlaceModel)
    }
    fun notifyEditItem(activity:Activity,position: Int,requestCode:Int)
    {
        val intent =Intent(context,AddHappyPlaces::class.java)
        intent.putExtra(MainActivity.EXTRA_DETAIL,items[position])
        activity.startActivityForResult(intent,requestCode)
        notifyItemChanged(position)
    }
    fun notifyDeletItem(position: Int,)
    {
        val dbHandler=DatabaseHandler(context)
        val isDeleted=dbHandler.deleteHappyPlace(items[position])
        if(isDeleted>0)
        {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}