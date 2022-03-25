package com.example.happyplaces

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHandler(context: Context):SQLiteOpenHelper(context, DATABASE_NAME,null,
    DATABASE_VERSION) {
    companion object{
        const val DATABASE_VERSION=1
        const val DATABASE_NAME="HappyPlacesDatabase"
        const val TABLE_HAPPY_PLACE="HappyPlacesTable"
        private const val KEY_ID="_id"
        private const val KEY_TITLE="title"
        private const val KEY_IMAGE="image"
        private const val KEY_DESCRIPTION="description"
        private const val KEY_DATE="date"
        private const val KEY_LOCATION="location"
        private const val KEY_LATITUDE="latitude"
        private const val KEY_LONGITUDE="longitude"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_TABLE=("CREATE TABLE " + TABLE_HAPPY_PLACE + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_TITLE + " TEXT,"
                + KEY_IMAGE + " TEXT,"
                + KEY_DESCRIPTION + " TEXT,"
                + KEY_DATE + " TEXT,"
                + KEY_LOCATION + " TEXT,"
                + KEY_LATITUDE + " TEXT,"
                + KEY_LONGITUDE + " TEXT)")
        db?.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_HAPPY_PLACE")
        onCreate(db)
    }
    fun addHappyPlace(placeModel: PlaceModel):Long
    {
        val db=this.writableDatabase
        val contentValues=ContentValues()
        contentValues.put(KEY_TITLE,placeModel.title)
        contentValues.put(KEY_IMAGE,placeModel.image)
        contentValues.put(KEY_DESCRIPTION,placeModel.description)
        contentValues.put(KEY_DATE,placeModel.date)
        contentValues.put(KEY_LOCATION,placeModel.location)
        contentValues.put(KEY_LATITUDE,placeModel.latitude)
        contentValues.put(KEY_LONGITUDE,placeModel.longitude)
        val success=db.insert(TABLE_HAPPY_PLACE,null,contentValues)
        db.close()
        return success
    }
    fun updateHappyPlace(placeModel: PlaceModel):Int
    {
        val db=this.writableDatabase
        val contentValues=ContentValues()
        contentValues.put(KEY_TITLE,placeModel.title)
        contentValues.put(KEY_IMAGE,placeModel.image)
        contentValues.put(KEY_DESCRIPTION,placeModel.description)
        contentValues.put(KEY_DATE,placeModel.date)
        contentValues.put(KEY_LOCATION,placeModel.location)
        contentValues.put(KEY_LATITUDE,placeModel.latitude)
        contentValues.put(KEY_LONGITUDE,placeModel.longitude)
        val success=db.update(TABLE_HAPPY_PLACE,contentValues, KEY_ID+"="+placeModel.id,null)
        db.close()
        return success
    }
    fun deleteHappyPlace(placeModel: PlaceModel):Int
    {
        val db=this.writableDatabase
        val contentValues=ContentValues()
        contentValues.put(KEY_ID,placeModel.id)
        val success=db.delete(TABLE_HAPPY_PLACE, KEY_ID+"="+placeModel.id,null)
        db.close()
        return success
    }
    fun viewHappyPlaces():ArrayList<PlaceModel>
    {
        val happyList:ArrayList<PlaceModel> = ArrayList<PlaceModel>()
        val selectQuery="SELECT * FROM $TABLE_HAPPY_PLACE"
        val db=this.readableDatabase
        var cursor:Cursor?=null
        try {
            cursor=db.rawQuery(selectQuery,null)
        }catch (e:SQLException)
        {
            db.execSQL(selectQuery)
            return ArrayList()
        }
        var id:Int
        var title:String
        var image:String
        var description:String
        var date:String
        var location:String
        var latitude:Double
        var longitude:Double
        if(cursor.moveToFirst())
        {
            do {
                id=cursor.getInt(cursor.getColumnIndex(KEY_ID))
                title=cursor.getString(cursor.getColumnIndex(KEY_TITLE))
                image=cursor.getString(cursor.getColumnIndex(KEY_IMAGE))
                description=cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION))
                date=cursor.getString(cursor.getColumnIndex(KEY_DATE))
                location=cursor.getString(cursor.getColumnIndex(KEY_LOCATION))
                latitude=cursor.getDouble(cursor.getColumnIndex(KEY_LATITUDE))
                longitude=cursor.getDouble(cursor.getColumnIndex(KEY_LONGITUDE))
                val happyModel=PlaceModel(id,title,image,description, date, location, latitude, longitude)
                happyList.add(happyModel)
            }while (cursor.moveToNext())
        }
        return happyList
    }

}