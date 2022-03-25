package com.example.happyplaces

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.activity_add_happy_places.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*


class AddHappyPlaces : AppCompatActivity(),View.OnClickListener {
    private lateinit var mFusedLocationClient:FusedLocationProviderClient
    private var mHappyPlaceDetails:PlaceModel?=null
    private var savedImage:Uri?=null
    private var mLatitude:Double=0.0
    private var mLongitude:Double=0.0
    companion object{
    private const val CAMERA_REQUEST_CODE=1
    private const val GALLERY=2
    private const val IMAGE_DIRECTORY="HappyPlacesPhoto"
    private const val PLACE_AUTOCOMPLETEREQUESTCODE=3}
    private var cal=Calendar.getInstance()
    private lateinit var dateSetListner:DatePickerDialog.OnDateSetListener
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_happy_places)
        mFusedLocationClient=LocationServices.getFusedLocationProviderClient(this)
        setSupportActionBar(tool_bar_addplace)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if(intent.hasExtra(MainActivity.EXTRA_DETAIL))
        {
            mHappyPlaceDetails=intent.getParcelableExtra(MainActivity.EXTRA_DETAIL)
        }
        if(mHappyPlaceDetails !=null)
        {
            supportActionBar?.title="Edit Happy Place"
            et_title.setText(mHappyPlaceDetails!!.title)
            et_date.setText(mHappyPlaceDetails!!.date)
            et_description.setText(mHappyPlaceDetails!!.description)
            et_location.setText(mHappyPlaceDetails!!.location)
            mLatitude=mHappyPlaceDetails!!.latitude
            mLongitude=mHappyPlaceDetails!!.longitude
            savedImage= Uri.parse(mHappyPlaceDetails!!.image)
            iv_place_image.setImageURI(savedImage)
            btn_save.text="UPDATE"
        }
        tool_bar_addplace.setNavigationOnClickListener {
            onBackPressed()
        }
        if(!Places.isInitialized())
        {
            Places.initialize(this, resources.getString(R.string.google_api_key))
        }
        dateSetListner=DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            setDate()
        }
        setDate()
        et_date.setOnClickListener(this)
        tv_add_image.setOnClickListener(this)
        btn_save.setOnClickListener(this)
        et_location.setOnClickListener(this)
        tv_select_current_location.setOnClickListener(this)
    }
    fun setDate(){
        val format="dd.MM.yyyy"
        val sdf=SimpleDateFormat(format, Locale.getDefault())
        et_date.setText(sdf.format(cal.time).toString())
    }
    @SuppressLint("MissingPermission")
    private fun requestNewLocation()
    {
        var mLocationRequest=LocationRequest()
        mLocationRequest.priority=LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval=1000
        mLocationRequest.numUpdates=1
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallBack, Looper.myLooper())

    }
    private val mLocationCallBack=object : LocationCallback()
    {
        override fun onLocationResult(locationResult: LocationResult?) {
            val mLastLocation:Location=locationResult!!.lastLocation
            mLatitude=mLastLocation.latitude
            mLongitude=mLastLocation.longitude

        }
    }
    fun isLocationEnabled():Boolean{
        val locationManger:LocationManager=getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManger.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManger.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode==Activity.RESULT_OK){
            if(requestCode== CAMERA_REQUEST_CODE){
                val thumbNail:Bitmap=data!!.extras!!.get("data") as Bitmap
               savedImage=  storeImageOnPhone(thumbNail)
                iv_place_image.setImageBitmap(thumbNail)
            }else if(requestCode== GALLERY)
            {
                if(data!=null)
                {
                    val contentURI=data.data
                    try {
                        val selectedImage=MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                        savedImage= storeImageOnPhone(selectedImage)
                        iv_place_image.setImageBitmap(selectedImage)
                    }catch (e: IOException)
                    {
                        Toast.makeText(this, "Faiiled ", Toast.LENGTH_SHORT).show()
                    }
                }
            }else if(requestCode== PLACE_AUTOCOMPLETEREQUESTCODE)
            {
                val place:Place=Autocomplete.getPlaceFromIntent(data!!)
                et_location.setText(place.address)
                mLatitude=place.latLng!!.latitude
                mLongitude=place.latLng!!.longitude
            }
        }
    }

    override fun onClick(v: View?) {
        when(v!!.id)
        {
            R.id.et_date -> {
                DatePickerDialog(this, dateSetListner, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()

            }
            R.id.tv_add_image -> {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Select Action")
                val pictureDialogItems =
                        arrayOf("Select photo from gallery ", "Capture photo from camera")
                builder.setItems(pictureDialogItems) { dialog, which ->
                    when (which) {
                        0 -> {
                            choosePhotoFromGallery()
                        }
                        1 -> {
                            chooseImageFromCamera()
                        }
                    }
                }
                builder.show()
            }
            R.id.btn_save -> {
                when {
                    et_title.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "Please enter title", Toast.LENGTH_SHORT).show()
                    }
                    et_description.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "Please enter description", Toast.LENGTH_SHORT).show()
                    }

                    savedImage == null -> {
                        Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
                    }
                    else
                    -> {
                        val happyPlaces = PlaceModel(
                                if (mHappyPlaceDetails == null) 0 else
                                    mHappyPlaceDetails!!.id, et_title.text.toString(), savedImage.toString(),
                                et_description.text.toString(), et_date.text.toString(),
                                et_location.text.toString(), mLatitude, mLongitude
                        )
                        val dbHandle = DatabaseHandler(this)
                        if (mHappyPlaceDetails == null) {
                            val addHappyPlace = dbHandle.addHappyPlace(happyPlaces)
                            if (addHappyPlace > 0) {
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                        } else {
                            val updateHappyPlace = dbHandle.updateHappyPlace(happyPlaces)
                            if (updateHappyPlace > 0) {
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                        }

                    }
                }

            }
            R.id.et_location -> {
                try {
                    val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)
                    val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(this)
                    startActivityForResult(intent, PLACE_AUTOCOMPLETEREQUESTCODE)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            R.id.tv_select_current_location -> {
                if (!isLocationEnabled()) {
                    Toast.makeText(this, "Location is turned off ", Toast.LENGTH_SHORT).show()
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                } else {
                    Dexter.withActivity(this).withPermissions(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                            .withListener(object : MultiplePermissionsListener {
                                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                                    if (report!!.areAllPermissionsGranted()) {
                                        requestNewLocation()
                                        val address=getCompleteAddressString(mLatitude,mLongitude)
                                        et_location.setText(address)
                                    }
                                }

                                override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
                                    showRationalDialog()
                                }

                            }).onSameThread().check()
                }
            }
        }
    }
    fun chooseImageFromCamera()
    {
        Dexter.withContext(this).withPermission(android.Manifest.permission.CAMERA).withListener(
                object : PermissionListener {
                    override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(intent, CAMERA_REQUEST_CODE)
                    }

                    override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                        Toast.makeText(this@AddHappyPlaces, "You rejected the permission", Toast.LENGTH_SHORT).show()
                    }

                    override fun onPermissionRationaleShouldBeShown(
                            p0: PermissionRequest?,
                            p1: PermissionToken?
                    ) {
                        showRationalDialog()
                    }

                }).onSameThread().check()
    }
    fun choosePhotoFromGallery(){
        Dexter.withActivity(this).withPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE).withListener(object :
                MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()) {
                    val intentGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(intentGallery, GALLERY)
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
            ) {
                showRationalDialog()
            }
        }).onSameThread().check()
    }
    fun storeImageOnPhone(bitmap: Bitmap):Uri
    {
        val wrapper=ContextWrapper(applicationContext)
        var file=wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file= File(file, "${UUID.randomUUID()}.Jpg")
        try {
            val stream:OutputStream=FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        }catch (e: IOException)
        {
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)
    }
    fun showRationalDialog()
    {
        AlertDialog.Builder(this).setMessage("It looks like you have turned off permissions required for this feature .It can be enabled under the Application's Settings")
            .setPositiveButton("Go to settings"){ _, _->
                try {
                    val intent=Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri= Uri.fromParts("package", packageName, null)
                    intent.data=uri
                    startActivity(intent)
                }catch (e: ActivityNotFoundException)
                {
                    e.printStackTrace()
                }
            }.setNegativeButton("Close"){ dialog, _
            ->
                dialog.dismiss()
            }.show()
    }
    private fun getCompleteAddressString(LATITUDE: Double, LONGITUDE: Double): String? {
        var strAdd = ""
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses: List<Address>? = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1)
            if (addresses != null) {
                val returnedAddress: Address = addresses[0]
                val strReturnedAddress = StringBuilder("")
                for (i in 0..returnedAddress.getMaxAddressLineIndex()) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n")
                }
                strAdd = strReturnedAddress.toString()
                Log.w("My Current loction address", strReturnedAddress.toString())
            } else {
                Log.w("My Current loction address", "No Address returned!")
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            Log.w("My Current loction address", "Canont get Address!")
        }
        return strAdd
    }

}