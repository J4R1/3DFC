package com.example.a3dfc

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.speech.RecognizerIntent
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.activity_main.*
import android.widget.TextView
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ViewRenderable
import kotlinx.android.synthetic.main.name_animal.*
import java.lang.Exception
import java.net.URL
import java.util.*
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.preference.PreferenceManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_main.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus
import org.osmdroid.views.overlay.OverlayItem


class MainActivity : AppCompatActivity(), SensorEventListener, LocationListener {

    var arrayView: Array<View>? = null
    private var arFragment: ArFragment? = null
    private var selected: Int = 1 //Default value
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var bearRendereable: ModelRenderable
    private lateinit var catRendereable: ModelRenderable
    private lateinit var dogRendereable: ModelRenderable
    private lateinit var cowRendereable: ModelRenderable
    private lateinit var elephantRendereable: ModelRenderable
    private lateinit var ferretRendereable: ModelRenderable
    private lateinit var hippopotamusRendereable: ModelRenderable
    private lateinit var horseRendereable: ModelRenderable
    private lateinit var koala_bearRendereable: ModelRenderable
    private lateinit var lionRendereable: ModelRenderable
    private lateinit var reindeerRendereable: ModelRenderable
    private lateinit var wolverineRendereable: ModelRenderable

    private lateinit var animalName: ViewRenderable

    ///* Sensors ------------
    private lateinit var sensorManager: SensorManager
    private lateinit var mSensorManager: SensorManager
    private var mSensors: Sensor? = null
    //*/ end of sensors ----------

    //lateinit var textToSpeech: TextToSpeech
    private val REQUEST_CODE_SPEECH_INPUT = 100
    private val mHandler: Handler = object :
        Handler(Looper.getMainLooper()) {
        override fun handleMessage(inputMessage: Message) {
            if (inputMessage.what == 0) {
                val msg = inputMessage.obj.toString().take(30)
                textviewfetch.text = msg //inputMessage.obj.toString()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        arFragment = supportFragmentManager.findFragmentById(R.id.sceneform_ux_fragment) as ArFragment

        setArrayView()
        setClickListener()
        setupModel()

        arFragment?.setOnTapArPlaneListener { hitResult, _, _ ->

            val anchor = hitResult.createAnchor()
            val anchorNode = AnchorNode(anchor)
            anchorNode.setParent(arFragment?.arSceneView?.scene)

            createModel(anchorNode, selected)

        }

        //Check internet connection
        if (isNetworkAvailable()) {
            val myRunnable = Conn(mHandler)
            val myThread = Thread(myRunnable)
            myThread.start()
        }

        //Location
        val ctx = applicationContext
        //important! set your user agent to prevent getting banned from the osm servers
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        if ((Build.VERSION.SDK_INT >= 26 &&
                    ContextCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION), 0)
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation.addOnSuccessListener { location : Location? ->
                // Got last known location. In some rare situations this can be null.
                val lat = location?.latitude.toString()
                val long = location?.longitude.toString()

                //val lokaatio = GeoPoint(lat.toDouble(), long.toDouble())

            }
            fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this)
            val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60 * 1000, 50f, this)
            createLocationRequest()
        } else {
            Log.d("--ELSE--", "------------------Else-----------------------------------------")
        }


        // Sensors--
        this.sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val deviceSensors: List<Sensor> = mSensorManager.getSensorList(Sensor.TYPE_ALL)
        Log.v("Total sensors",""+deviceSensors.size)
        deviceSensors.forEach {
            Log.v("Sensor name", "" + it)
        }

        if (sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
            // You can register listener to get data and use them.
            Log.v("-----SUCCESS-----","Gyroscope success")
        } else {
            Log.v("-----FAILURE-----","No sensor found")
        }

        if (mSensors == null) {
            mSensors = if (sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
                sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
            } else {
                Log.v("-----FAILURE-----", "No sensor found")
                null
            }
        }
        //End of sensors--

        //Speeh to text command
        circle_button.setOnClickListener {
            SpeechFunction()
        }

        //Text to speech button
        speak_button.setOnClickListener {
            // TextToSpeechFunction()
            Toast.makeText(applicationContext, "Press the button below and name a color.\nFor example: 'RED'", Toast.LENGTH_LONG).show()
        }
    }
    fun createLocationRequest() {
        Log.d("--GG--", "createLocationRequest() started" )

        val locationRequest = LocationRequest.create()?.apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val locRequest = locationRequest.toString()
        Log.d("--HH--", locRequest)
    }

    override fun onLocationChanged(p0: Location?) {
        //new location react...
        Log.d("GEOLOCATION", "new latitude: ${p0?.latitude} and longitude: ${p0?.longitude}")
    }
    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?)
    {}
    override fun onProviderEnabled(p0: String?) {}
    override fun onProviderDisabled(p0: String?) {}
    override fun onSensorChanged(p0: SensorEvent?) {
        /*
        Log.v("----onSensorChanged----","p0!!.values[0]")
        //Sensor change value
        val data = p0!!.values[0]
        val data2 = p0.values[1]
        val data3 = p0.values[2]

        val x = "%.4f".format(data)
        val y = "%.4f".format(data2)
        val z = "%.4f".format(data3)

        val r = 1
        val g = 1
        val b = 1

        //val red = r*data
        //val green = g*data2
        val blue = b*data3
        sensor.text = ("r: $x   g: $y   b: $z")

        if (blue > 3) {
            direction.text = "left"
        } else if (blue < -3) {
            direction.text = "right"
        }

        if (p0.sensor.type == Sensor.TYPE_GYROSCOPE) {
            Log.v("----Sensor data----","$data")
            Log.v("----Sensor data----","$data2")
            Log.v("----Sensor data----","$data3")
        } else {
            Log.v("-----FAILURE-----","No sensor data")
            Toast.makeText(this, "#####FAIL #####", Toast.LENGTH_SHORT).show()
        }*/
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        Log.v("---onAccuracyChanged---","onAccuracyChanged Started!!!!!]")
    }

    override fun onResume() {
        super.onResume()
        mSensorManager.registerListener(this, mSensors, 999999998,999999999)//SensorManager.SENSOR_DELAY_NORMAL
        //        Register the sensor on resume of the activity

    }

    override fun onPause() {
        super.onPause()
        Log.v("-----onPause-----","unregisterListener")
        sensorManager.unregisterListener(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?){
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == Activity.RESULT_OK || null != data) {
                val res: ArrayList<String> = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                textview.text = res[0]
                GlobalModel.speechText = res[0].toLowerCase(Locale.US)
            }
        }

        //Set action based on speech
        if (GlobalModel.speechText == "red") {
            main.setBackgroundColor(Color.RED)
        } else if (GlobalModel.speechText == "green") {
            main.setBackgroundColor(Color.GREEN)
        } else if (GlobalModel.speechText == "blue") {
            main.setBackgroundColor(Color.BLUE)
        } else if (GlobalModel.speechText == "black") {
            main.setBackgroundColor(Color.BLACK)
        } else if (GlobalModel.speechText == "white") {
            main.setBackgroundColor(Color.WHITE)
        } else if (GlobalModel.speechText == "next") {
            Toast.makeText(applicationContext,"Next...", Toast.LENGTH_LONG).show()
        } else if (GlobalModel.speechText == "previous") {
            Toast.makeText(applicationContext,"Previous...", Toast.LENGTH_LONG).show()
        }
    }

    private fun SpeechFunction() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US) //Locale.getDefault()
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Say something...")
        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT)
        } catch (exp: ActivityNotFoundException)
        {
            Toast.makeText(applicationContext,"Speech Not Supported...", Toast.LENGTH_LONG).show()
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = this.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        return cm.activeNetworkInfo?.isConnected ?: false
    }


    //----- old below

    private fun setupModel() {
        ModelRenderable.builder()
            .setSource(this, R.raw.bear)
            .build()
            .thenAccept { renderable -> bearRendereable = renderable }
            .exceptionally {
                Toast.makeText(this, "Load failed ${it.message}", Toast.LENGTH_SHORT).show()
                null
            }

        ModelRenderable.builder()
            .setSource(this, R.raw.dog)
            .build()
            .thenAccept { renderable -> dogRendereable = renderable }
            .exceptionally {
                Toast.makeText(this, "Load failed ${it.message}", Toast.LENGTH_SHORT).show()
                null
            }

        ModelRenderable.builder()
            .setSource(this, R.raw.cat)
            .build()
            .thenAccept { renderable -> catRendereable = renderable }
            .exceptionally {
                Toast.makeText(this, "Load failed ${it.message}", Toast.LENGTH_SHORT).show()
                null
            }

        ModelRenderable.builder()
            .setSource(this, R.raw.cow)
            .build()
            .thenAccept { renderable -> cowRendereable = renderable }
            .exceptionally {
                Toast.makeText(this, "Load failed ${it.message}", Toast.LENGTH_SHORT).show()
                null
            }

        ModelRenderable.builder()
            .setSource(this, R.raw.elephant)
            .build()
            .thenAccept { renderable -> elephantRendereable = renderable }
            .exceptionally {
                Toast.makeText(this, "Load failed ${it.message}", Toast.LENGTH_SHORT).show()
                null
            }

        ModelRenderable.builder()
            .setSource(this, R.raw.ferret)
            .build()
            .thenAccept { renderable -> ferretRendereable = renderable }
            .exceptionally {
                Toast.makeText(this, "Load failed ${it.message}", Toast.LENGTH_SHORT).show()
                null
            }

        ModelRenderable.builder()
            .setSource(this, R.raw.hippopotamus)
            .build()
            .thenAccept { renderable -> hippopotamusRendereable = renderable }
            .exceptionally {
                Toast.makeText(this, "Load failed ${it.message}", Toast.LENGTH_SHORT).show()
                null
            }

        ModelRenderable.builder()
            .setSource(this, R.raw.horse)
            .build()
            .thenAccept { renderable -> horseRendereable = renderable }
            .exceptionally {
                Toast.makeText(this, "Load failed ${it.message}", Toast.LENGTH_SHORT).show()
                null
            }

        ModelRenderable.builder()
            .setSource(this, R.raw.koala_bear)
            .build()
            .thenAccept { renderable -> koala_bearRendereable = renderable }
            .exceptionally {
                Toast.makeText(this, "Load failed ${it.message}", Toast.LENGTH_SHORT).show()
                null
            }

        ModelRenderable.builder()
            .setSource(this, R.raw.lion)
            .build()
            .thenAccept { renderable -> lionRendereable = renderable }
            .exceptionally {
                Toast.makeText(this, "Load failed ${it.message}", Toast.LENGTH_SHORT).show()
                null
            }

        ModelRenderable.builder()
            .setSource(this, R.raw.reindeer)
            .build()
            .thenAccept { renderable -> reindeerRendereable = renderable }
            .exceptionally {
                Toast.makeText(this, "Load failed ${it.message}", Toast.LENGTH_SHORT).show()
                null
            }

        ModelRenderable.builder()
            .setSource(this, R.raw.wolverine)
            .build()
            .thenAccept { renderable -> wolverineRendereable = renderable }
            .exceptionally {
                Toast.makeText(this, "Load failed ${it.message}", Toast.LENGTH_SHORT).show()
                null
            }


        ViewRenderable.builder()
            .setView(this, R.layout.name_animal)
            .build()
            .thenAccept { renderable -> animalName = renderable }



    }

    private fun createModel(anchorNode: AnchorNode, selected: Int) {
        val renderableNode = TransformableNode(arFragment?.transformationSystem)
        renderableNode.setParent(anchorNode)
        when (selected) {
            1 -> {
                renderableNode.renderable = bearRendereable
                renderableNode.select()
                //testi(anchorNode, renderableNode, "Bear")

                val nameView = TransformableNode(arFragment?.transformationSystem)
                nameView.localPosition = Vector3(0f, renderableNode.localPosition.y + 0.5f, 0f)
                nameView.setParent(anchorNode)
                nameView.renderable = animalName
                nameView.select()
            }
            2 ->{
                renderableNode.renderable = catRendereable
                renderableNode.select()
            }
            3 ->{
                renderableNode.renderable = dogRendereable
                renderableNode.select()
            }
            4 ->{
                renderableNode.renderable = cowRendereable
                renderableNode.select()
            }
            5 ->{
                renderableNode.renderable = elephantRendereable
                renderableNode.select()
            }
            6 ->{
                renderableNode.renderable = ferretRendereable
                renderableNode.select()
            }
            7 ->{
                renderableNode.renderable = ferretRendereable
                renderableNode.select()
            }
            8 ->{
                renderableNode.renderable = ferretRendereable
                renderableNode.select()
            }
            9 ->{
                renderableNode.renderable = ferretRendereable
                renderableNode.select()
            }
            10 ->{
                renderableNode.renderable = ferretRendereable
                renderableNode.select()
            }
            11 ->{
                renderableNode.renderable = reindeerRendereable
                renderableNode.select()
            }
            12 ->{
                renderableNode.renderable = wolverineRendereable
                renderableNode.select()
            }
        }
    }


    private fun setArrayView() {
        arrayView = arrayOf(
            bear
            , cat
            , dog
            , cow
            , elephant
            , ferret
            , hippopotamus
            , horse
            , koala_bear
            , lion
            , reindeer
            , wolverine
        )
    }

    private fun setClickListener() {
        for (view in arrayView!!) {
            view.setOnClickListener { thisView ->
                setBackground(thisView.id)
                when (thisView.id) {
                    R.id.bear ->            selected = 1
                    R.id.cat ->             selected = 2
                    R.id.dog ->             selected = 3
                    R.id.cow ->             selected = 4
                    R.id.elephant ->        selected = 5
                    R.id.ferret ->          selected = 6
                    R.id.hippopotamus ->    selected = 7
                    R.id.horse ->           selected = 8
                    R.id.koala_bear ->      selected = 9
                    R.id.lion ->            selected = 10
                    R.id.reindeer ->        selected = 11
                    R.id.wolverine ->       selected = 12
                }
            }
        }
    }

    private fun setBackground(id: Int) {
        for (view in arrayView!!) {
            if (view.id == id) {
                view.setBackgroundColor(android.graphics.Color.parseColor("#80333639"))
            } else {
                view.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            }
        }
    }
}
// End of MainAvtivity

object GlobalModel {
    var position = 0 // for wiki texts
    var speechText = "" // for speech to text placeholder
    var bear_txt = ""
    var cat_txt = ""
    var cow_txt = ""
    var dog_txt = ""

    var bear_rotation = 180f
    var cat_rotation = 180f
    var cow_rotation = 180f
    var dog_rotation = 180f
}

class Conn(mHand: Handler?) : Runnable {
    val myHandler = mHand
    override fun run() {
        try {
            val res = URL("https://developer.android.com/").readText()
            Log.d("res", res)
            val msg = myHandler?.obtainMessage()
            msg?.what = 0
            msg?.obj = res
            myHandler?.sendMessage(msg)
        } catch (e: Exception) {
            Log.d("Error", e.toString())
        }
    }
}