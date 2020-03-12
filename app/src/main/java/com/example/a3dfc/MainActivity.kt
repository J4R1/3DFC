package com.example.a3dfc
import java.math.BigDecimal
import java.math.RoundingMode
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.ConnectivityManager
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.activity_main.*
import android.widget.TextView
import androidx.core.view.isVisible
import com.google.ar.core.Session
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ViewRenderable
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
import org.osmdroid.config.Configuration


class MainActivity : AppCompatActivity(), SensorEventListener, TextToSpeech.OnInitListener, LocationListener {

    var arrayView: Array<View>? = null
    private var arFragment: ArFragment? = null
    var selected: Int = 0 //Default value

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var bearRendereable: ModelRenderable
    private lateinit var catRendereable: ModelRenderable
    private lateinit var cowRendereable: ModelRenderable
    private lateinit var dogRendereable: ModelRenderable
    private lateinit var elephantRendereable: ModelRenderable
    private lateinit var ferretRendereable: ModelRenderable
    private lateinit var hippopotamusRendereable: ModelRenderable
    private lateinit var horseRendereable: ModelRenderable
    private lateinit var koala_bearRendereable: ModelRenderable
    private lateinit var lionRendereable: ModelRenderable
    private lateinit var reindeerRendereable: ModelRenderable
    private lateinit var wolverineRendereable: ModelRenderable
    private lateinit var animalName: ViewRenderable
    private lateinit var animalName2: ViewRenderable
    private lateinit var animalName3: ViewRenderable
    private lateinit var animalName4: ViewRenderable


    ///* Sensors ------------
    private lateinit var sensorManager: SensorManager
    private lateinit var mSensorManager: SensorManager
    private var mSensors: Sensor? = null
    //*/ end of sensors ----------

    // Initialize the text to speech enginge
    var tts: TextToSpeech? = null
    private val REQUEST_CODE_SPEECH_INPUT = 100

    private val mHandler: Handler = object :
        Handler(Looper.getMainLooper()) {
        override fun handleMessage(inputMessage: Message) {
            if (inputMessage.what == 0) {
                val msg = inputMessage.obj.toString() //.take(30)
                //textviewfetch.text = msg //inputMessage.obj.toString()
                GlobalModel.bear_txt = msg
            }
            if (inputMessage.what == 1) {
                val msg = inputMessage.obj.toString() //.take(30)
                GlobalModel.cat_txt = msg
            }
            if (inputMessage.what == 2) {
                val msg = inputMessage.obj.toString() //.take(30)
                GlobalModel.cow_txt = msg
            }
            if (inputMessage.what == 3) {
                val msg = inputMessage.obj.toString() //.take(30)
                GlobalModel.dog_txt = msg
            }

            //Setup AR
            arFragment =
                supportFragmentManager.findFragmentById(R.id.sceneform_ux_fragment) as ArFragment
            setArrayView()
            setClickListener()
            setupModel()
            arFragment?.setOnTapArPlaneListener { hitResult, _, _ ->

                val anchor = hitResult.createAnchor()
                val anchorNode = AnchorNode(anchor)
                anchorNode.setParent(arFragment?.arSceneView?.scene)

                createModel(anchorNode, selected)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        tts = TextToSpeech(this, this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Check internet connection
        if (isNetworkAvailable()) {
            val myRunnable = Conn(mHandler)
            val myThread = Thread(myRunnable)
            myThread.start()
        } else {
            Toast.makeText(
                applicationContext,
                "This app requires INTERNET, please try again",
                Toast.LENGTH_LONG
            ).show()
        }


        speak_button.setOnClickListener { SpeechFunction() }

        // DialogBox Intro --
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setCancelable(false)
        dialogBuilder.setMessage(R.string.intro)
            .setPositiveButton(R.string.intro_ok, DialogInterface.OnClickListener { dialog, id -> })
            .show()


        getLocation()
        vibrate(this)
        // Sensors--
        this.sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val deviceSensors: List<Sensor> = mSensorManager.getSensorList(Sensor.TYPE_ALL)
        Log.v("Total sensors", "" + deviceSensors.size)
        deviceSensors.forEach {
            Log.v("Sensor name", "" + it)
        }

        if (sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
            // You can register listener to get data and use them.
            Log.v("-----SUCCESS-----", "Gyroscope success")
        } else {
            Log.v("-----FAILURE-----", "No sensor found")
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

        voice_button.setOnClickListener {

            if (selected == 1) {
                val text = GlobalModel.bear_txt
                tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
            } else if (selected == 2) {
                val text = GlobalModel.cat_txt
                tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
            } else {
                val text = "Place a animal model"
                tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")

            }
        }
        setButtonsInvisible()
    }

    @Suppress("DEPRECATION")
    fun getLocation() {
        val ctx = applicationContext
        //important! set your user agent to prevent getting banned from the osm servers
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        //inflate layout after loading (to make sure that app can write to cache)

        if ((Build.VERSION.SDK_INT >= 26 &&
                    ContextCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ) !=
                    PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION),
                0
            )
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                // Got last known location. In some rare situations this can be null.

                val lat = location?.latitude
                val long = location?.longitude
                if (lat != null) {
                    val decimal = BigDecimal(lat).setScale(2, RoundingMode.HALF_EVEN).toDouble()
                    GlobalModel.latitude = decimal
                    Toast.makeText(this, "decimal: $decimal", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Location Latitude fail", Toast.LENGTH_SHORT).show()
                }
                if (long != null) {
                    val decimal = BigDecimal(long).setScale(2, RoundingMode.HALF_EVEN).toDouble()
                    GlobalModel.longitude = decimal
                    Toast.makeText(this, "decimal: $decimal", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Location Longitude fail", Toast.LENGTH_SHORT).show()
                }
                Toast.makeText(
                    this,
                    "Position: ${GlobalModel.latitude}, ${GlobalModel.longitude}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Log.d("--debug--", "Location error")
        }
    }

    private fun vibrate(context: Context) {
        if (Build.VERSION.SDK_INT >= 26) {
            (context.getSystemService(VIBRATOR_SERVICE) as Vibrator).vibrate(
                VibrationEffect.createOneShot(
                    150,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            (context.getSystemService(VIBRATOR_SERVICE) as Vibrator).vibrate(150)
        }
    }

    fun createLocationRequest() {
        Log.d("--GG--", "createLocationRequest() started")

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
        }

        override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}
        override fun onProviderEnabled(p0: String?) {}
        override fun onProviderDisabled(p0: String?) {}


        fun setButtonsInvisible() {
            right_button.isClickable = false
            right_button.isVisible = false
            left_button.isClickable = false
            left_button.isVisible = false
            voice_button.isVisible = false
            voice_button.isClickable = false
            speak_button.isVisible = false
            speak_button.isClickable = false

            bear.isVisible = false
            bear.isClickable = false

            cat.isVisible = false
            cat.isClickable = false

            cow.isVisible = false
            cow.isClickable = false

            dog.isVisible = false
            dog.isClickable = false

            elephant.isVisible = false
            elephant.isClickable = false

            ferret.isVisible = false
            ferret.isClickable = false

            hippopotamus.isVisible = false
            hippopotamus.isClickable = false

            horse.isVisible = false
            horse.isClickable = false

            koala_bear.isVisible = false
            koala_bear.isClickable = false

            lion.isVisible = false
            lion.isClickable = false

            reindeer.isVisible = false
            reindeer.isClickable = false

            wolverine.isVisible = false
            wolverine.isClickable = false
        }

        override fun onDestroy() {
            if (tts != null) {
                tts!!.stop()
                tts!!.shutdown()
            }
            super.onDestroy()
        }

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
            Log.v("---onAccuracyChanged---", "onAccuracyChanged Started!!!!!]")
        }

        override fun onResume() {
            mSensorManager.registerListener(
                this,
                mSensors,
                999999998,
                999999999
            )//SensorManager.SENSOR_DELAY_NORMAL
            //        Register the sensor on resume of the activity
            super.onResume()
        }

        override fun onPause() {
            Log.v("-----onPause-----", "unregisterListener")
            sensorManager.unregisterListener(this)
            super.onPause()
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
                if (resultCode == Activity.RESULT_OK || null != data) {
                    val res: ArrayList<String> =
                        data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    textview.text = res[0]
                    GlobalModel.speechText = res[0].toLowerCase(Locale.US)
                }
            }

            // Set action based on speech TODO toasts are placeholders for functions
            when (GlobalModel.speechText) {
                "commands", "help" -> tts!!.speak(
                    "Available commands are next, previous and read",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    ""
                )
                "next" -> Toast.makeText(
                    applicationContext,
                    "Next animal",
                    Toast.LENGTH_SHORT
                ).show()
                "previous" -> Toast.makeText(
                    applicationContext,
                    "Previous animal",
                    Toast.LENGTH_SHORT
                ).show()
                "read" -> voice_button.performClick()
                else -> tts!!.speak("Please repeat that again", TextToSpeech.QUEUE_FLUSH, null, "")

            }
        }

        private fun SpeechFunction() {
            fun doTheSpeech() {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US) //Locale.getDefault()
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something...")
                try {
                    startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT)
                } catch (exp: ActivityNotFoundException) {
                    Toast.makeText(applicationContext, "Speech Not Supported...", Toast.LENGTH_LONG)
                        .show()
                }
            }

            val dialogBuilder = AlertDialog.Builder(this)
            dialogBuilder.setCancelable(false)
            dialogBuilder.setMessage(R.string.voice_error).setNegativeButton(
                R.string.intro_ok,
                DialogInterface.OnClickListener { dialog, id -> })
                .setPositiveButton(
                    R.string.voice_anyway,
                    DialogInterface.OnClickListener { dialog, id -> doTheSpeech() })
                .show()
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

            val inflater: LayoutInflater = LayoutInflater.from(applicationContext)
            val view = inflater.inflate(R.layout.name_animal, root_layout, false)
            val textView: TextView = view?.findViewById(R.id.nameAnimal) as TextView
            textView.text = GlobalModel.bear_txt

            ViewRenderable.builder()
                .setView(this, view)
                .build()
                .thenAccept { renderable -> animalName = renderable }

            val inflater2: LayoutInflater = LayoutInflater.from(applicationContext)
            val view2 = inflater2.inflate(R.layout.name_animal2, root_layout, false)
            val textView2: TextView = view2?.findViewById(R.id.nameAnimal2) as TextView
            textView2.text = GlobalModel.cat_txt

            ViewRenderable.builder()
                .setView(this, view2)
                .build()
                .thenAccept { renderable -> animalName2 = renderable }

            val inflater3: LayoutInflater = LayoutInflater.from(applicationContext)
            val view3 = inflater3.inflate(R.layout.name_animal3, root_layout, false)
            val textView3: TextView = view3?.findViewById(R.id.nameAnimal3) as TextView
            textView3.text = GlobalModel.cow_txt

            ViewRenderable.builder()
                .setView(this, view3)
                .build()
                .thenAccept { renderable -> animalName3 = renderable }

            val inflater4: LayoutInflater = LayoutInflater.from(applicationContext)
            val view4 = inflater4.inflate(R.layout.name_animal4, root_layout, false)
            val textView4: TextView = view4?.findViewById(R.id.nameAnimal4) as TextView
            textView4.text = GlobalModel.dog_txt

            ViewRenderable.builder()
                .setView(this, view4)
                .build()
                .thenAccept { renderable -> animalName4 = renderable }


        }

        fun rotateLeft(node: TransformableNode, value: Float, animal: Int) {
            node.apply {
                localRotation = Quaternion.axisAngle(
                    Vector3(0.0f, 1.0f, 0.0f), value - 20
                )
            }
            if (animal == 1) {
                GlobalModel.bear_rotation = GlobalModel.bear_rotation - 20
            } else if (animal == 2) {
                GlobalModel.cat_rotation = GlobalModel.cat_rotation - 20
            } else if (animal == 3) {
                GlobalModel.cow_rotation = GlobalModel.cow_rotation - 20
            } else if (animal == 4) {
                GlobalModel.dog_rotation = GlobalModel.dog_rotation - 20
            }
        }

        fun rotateRight(node: TransformableNode, value: Float, animal: Int) {
            node.apply {
                localRotation = Quaternion.axisAngle(
                    Vector3(0.0f, 1.0f, 0.0f), value + 20
                )
            }
            if (animal == 1) {
                GlobalModel.bear_rotation = GlobalModel.bear_rotation + 20
            } else if (animal == 2) {
                GlobalModel.cat_rotation = GlobalModel.cat_rotation + 20
            } else if (animal == 3) {
                GlobalModel.cow_rotation = GlobalModel.cow_rotation + 20
            } else if (animal == 4) {
                GlobalModel.dog_rotation = GlobalModel.dog_rotation + 20
            }
        }

        fun setVisible(node1: TransformableNode) {
            if (node1.isEnabled == false) {
                node1.isEnabled = !node1.isEnabled
            }
            //node1.isEnabled = node1.isEnabled
        }

        fun setInvisible(node1: TransformableNode, node2: TransformableNode) {
            if (node1.isEnabled == true) {
                node1.isEnabled = !node1.isEnabled
            }
            if (node2.isEnabled == true) {
                node2.isEnabled = !node2.isEnabled
            }

        }

        // Loads the models and makes the UI visisble
        private fun createModel(anchorNode: AnchorNode, selected: Int) {
            val nameViewBear = TransformableNode(arFragment?.transformationSystem)
            val nameViewCat = TransformableNode(arFragment?.transformationSystem)
            val nameViewCow = TransformableNode(arFragment?.transformationSystem)
            val nameViewDog = TransformableNode(arFragment?.transformationSystem)
            val renderableNode = TransformableNode(arFragment?.transformationSystem)
            renderableNode.localPosition = Vector3(0f, 0f, 0f)
            renderableNode.localRotation =
                Quaternion.axisAngle(Vector3(0f, 1f, 0f), GlobalModel.bear_rotation)
            //renderableNode.scaleController.minScale  = 100f
            //renderableNode.scaleController.maxScale = 200f
            renderableNode.setParent(anchorNode)
            val renderableNodeCat = TransformableNode(arFragment?.transformationSystem)
            renderableNodeCat.localPosition = Vector3(0f, 0f, 0f)
            renderableNodeCat.localRotation =
                Quaternion.axisAngle(Vector3(0f, 1f, 0f), GlobalModel.cat_rotation)
            renderableNodeCat.setParent(anchorNode)
            val renderableNodeCow = TransformableNode(arFragment?.transformationSystem)
            renderableNodeCow.localPosition = Vector3(0f, 0f, 0f)
            renderableNodeCow.localRotation =
                Quaternion.axisAngle(Vector3(0f, 1f, 0f), GlobalModel.cow_rotation)
            renderableNodeCow.setParent(anchorNode)
            val renderableNodeDog = TransformableNode(arFragment?.transformationSystem)
            renderableNodeDog.localPosition = Vector3(0f, 0f, 0f)
            renderableNodeDog.localRotation =
                Quaternion.axisAngle(Vector3(0f, 1f, 0f), GlobalModel.dog_rotation)
            renderableNodeDog.setParent(anchorNode)
            right_button.setOnClickListener {
                Toast.makeText(
                    applicationContext,
                    "Select an animal model before rotation",
                    Toast.LENGTH_SHORT
                ).show()
            }
            left_button.setOnClickListener {
                Toast.makeText(
                    applicationContext,
                    "Select an animal model before rotation",
                    Toast.LENGTH_SHORT
                ).show()
            }
            when (selected) {
                0 -> {
                    renderableNode.renderable = bearRendereable
                    renderableNode.select()
                    renderableNodeCat.renderable = catRendereable
                    renderableNodeCat.select()
                    renderableNodeCow.renderable = cowRendereable
                    renderableNodeCow.select()
                    renderableNodeDog.renderable = dogRendereable
                    renderableNodeDog.select()

                    nameViewBear.localPosition =
                        Vector3(0f, renderableNode.localPosition.y + 1.0f, 0f)
                    nameViewBear.setParent(anchorNode)
                    nameViewBear.renderable = animalName
                    nameViewBear.select()

                    nameViewCat.localPosition =
                        Vector3(0f, renderableNode.localPosition.y + 1.0f, 0f)
                    nameViewCat.setParent(anchorNode)
                    nameViewCat.renderable = animalName2
                    nameViewCat.select()

                    nameViewCow.localPosition =
                        Vector3(0f, renderableNode.localPosition.y + 1.0f, 0f)
                    nameViewCow.setParent(anchorNode)
                    nameViewCow.renderable = animalName3
                    nameViewCow.select()

                    nameViewDog.localPosition =
                        Vector3(0f, renderableNode.localPosition.y + 1.0f, 0f)
                    nameViewDog.setParent(anchorNode)
                    nameViewDog.renderable = animalName4
                    nameViewDog.select()

                    renderableNode.scaleController.isEnabled = false
                    renderableNode.rotationController.isEnabled = false
                    renderableNode.translationController.isEnabled = false

                    renderableNodeCat.scaleController.isEnabled = false
                    renderableNodeCat.rotationController.isEnabled = false
                    renderableNodeCat.translationController.isEnabled = false

                    renderableNodeCow.scaleController.isEnabled = false
                    renderableNodeCow.rotationController.isEnabled = false
                    renderableNodeCow.translationController.isEnabled = false

                    renderableNodeDog.scaleController.isEnabled = false
                    renderableNodeDog.rotationController.isEnabled = false
                    renderableNodeDog.translationController.isEnabled = false

                    nameViewBear.scaleController.isEnabled = false
                    nameViewBear.rotationController.isEnabled = false
                    nameViewBear.translationController.isEnabled = false

                    nameViewCat.scaleController.isEnabled = false
                    nameViewCat.rotationController.isEnabled = false
                    nameViewCat.translationController.isEnabled = false

                    nameViewCow.scaleController.isEnabled = false
                    nameViewCow.rotationController.isEnabled = false
                    nameViewCow.translationController.isEnabled = false

                    nameViewDog.scaleController.isEnabled = false
                    nameViewDog.rotationController.isEnabled = false
                    nameViewDog.translationController.isEnabled = false

                    setInvisible(renderableNode, renderableNodeCat)
                    setInvisible(nameViewCow, renderableNodeCow)
                    setInvisible(nameViewCat, nameViewBear)
                    setInvisible(nameViewDog, renderableNodeDog)
                    right_button.isClickable = true
                    right_button.isVisible = true
                    left_button.isClickable = true
                    left_button.isVisible = true
                    voice_button.isVisible = true
                    voice_button.isClickable = true
                    speak_button.isVisible = true
                    speak_button.isClickable = true

                    bear.isVisible = true
                    bear.isClickable = true

                    cat.isVisible = true
                    cat.isClickable = true

                    cow.isVisible = true
                    cow.isClickable = true

                    dog.isVisible = true
                    dog.isClickable = true
                }
                1 -> {
                }
                2 -> {
                }
                3 -> {
                    /*
                renderableNode.renderable = cowRendereable
                renderableNode.select()*/
                }
                4 -> {
                    /*
                renderableNode.renderable = dogRendereable
                renderableNode.select()
                */
                }
                5 -> {
                    renderableNode.renderable = elephantRendereable
                    renderableNode.select()
                }
                6 -> {
                    renderableNode.renderable = ferretRendereable
                    renderableNode.select()
                }
                7 -> {
                    renderableNode.renderable = ferretRendereable
                    renderableNode.select()
                }
                8 -> {
                    renderableNode.renderable = ferretRendereable
                    renderableNode.select()
                }
                9 -> {
                    renderableNode.renderable = ferretRendereable
                    renderableNode.select()
                }
                10 -> {
                    renderableNode.renderable = ferretRendereable
                    renderableNode.select()
                }
                11 -> {
                    renderableNode.renderable = reindeerRendereable
                    renderableNode.select()
                }
                12 -> {
                    renderableNode.renderable = wolverineRendereable
                    renderableNode.select()
                }
            }
            bear.setOnClickListener {
                this.selected = 1
                setInvisible(renderableNodeCat, renderableNodeCow)
                setInvisible(nameViewCat, nameViewCow)
                setInvisible(renderableNodeDog, nameViewDog)
                setVisible(renderableNode)
                setVisible(nameViewBear)
                right_button.setOnClickListener {
                    rotateRight(renderableNode, GlobalModel.bear_rotation, 1)
                }
                left_button.setOnClickListener {
                    rotateLeft(renderableNode, GlobalModel.bear_rotation, 1)
                }

            }
            cat.setOnClickListener {
                this.selected = 2
                setInvisible(renderableNode, renderableNodeCow)
                setInvisible(nameViewBear, nameViewCow)
                setInvisible(renderableNodeDog, nameViewDog)
                setVisible(renderableNodeCat)
                setVisible(nameViewCat)
                right_button.setOnClickListener {
                    rotateRight(renderableNodeCat, GlobalModel.cat_rotation, 2)
                }
                left_button.setOnClickListener {
                    rotateLeft(renderableNodeCat, GlobalModel.cat_rotation, 2)
                }
            }
            cow.setOnClickListener {
                this.selected = 4
                setInvisible(renderableNodeCat, renderableNode)
                setInvisible(nameViewBear, nameViewCat)
                setInvisible(renderableNodeDog, nameViewDog)
                setVisible(renderableNodeCow)
                setVisible(nameViewCow)
                right_button.setOnClickListener {
                    rotateRight(renderableNodeCow, GlobalModel.cow_rotation, 3)
                }
                left_button.setOnClickListener {
                    rotateLeft(renderableNodeCow, GlobalModel.cow_rotation, 3)
                }
            }
            dog.setOnClickListener {
                this.selected = 4
                setInvisible(renderableNodeCat, renderableNode)
                setInvisible(nameViewBear, nameViewCat)
                setInvisible(renderableNodeCow, nameViewCow)
                setVisible(renderableNodeDog)
                setVisible(nameViewDog)
                right_button.setOnClickListener {
                    rotateRight(renderableNodeDog, GlobalModel.dog_rotation, 4)
                }
                left_button.setOnClickListener {
                    rotateLeft(renderableNodeDog, GlobalModel.dog_rotation, 4)
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
                        R.id.bear -> selected = 1
                        R.id.cat -> selected = 2
                        R.id.dog -> selected = 3
                        R.id.cow -> selected = 4
                        R.id.elephant -> selected = 5
                        R.id.ferret -> selected = 6
                        R.id.hippopotamus -> selected = 7
                        R.id.horse -> selected = 8
                        R.id.koala_bear -> selected = 9
                        R.id.lion -> selected = 10
                        R.id.reindeer -> selected = 11
                        R.id.wolverine -> selected = 12
                    }
                    Log.d("selected", "My animal is: $selected")
                }
            }
        }

        private fun setBackground(id: Int) {
            for (view in arrayView!!) {
                if (view.id == id) {
                    view.setBackgroundResource(R.drawable.animal_button)
                } else {
                    view.setBackgroundColor(Color.TRANSPARENT)
                }
            }
        }

        override fun onInit(status: Int) {
            if (status == TextToSpeech.SUCCESS) {
                val result = tts!!.setLanguage(Locale.US)

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.d("asdf", "Lang error")
                } else {
                    voice_button.isEnabled = true
                }
            } else {
                Log.d("asdf", "Text to Speech init fail")
            }
        }
    }
// End of MainActivity

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

    var latitude = 0.00
    var longitude = 0.00
}

class Conn(mHand: Handler?) : Runnable {
    val myHandler = mHand
    override fun run() {
        try {
            val resultBear = URL("https://users.metropolia.fi/~jaripie/karhu").readText()
            Log.d("res", resultBear)
            val msg = myHandler?.obtainMessage()
            msg?.what = 0
            msg?.obj = resultBear
            myHandler?.sendMessage(msg)

            val resultCat = URL("https://users.metropolia.fi/~jaripie/cat").readText()
            Log.d("res", resultBear)
            val msg2 = myHandler?.obtainMessage()
            msg2?.what = 1
            msg2?.obj = resultCat
            myHandler?.sendMessage(msg2)

            val resultCow = URL("https://users.metropolia.fi/~jaripie/cow").readText()
            Log.d("res", resultBear)
            val msg3 = myHandler?.obtainMessage()
            msg3?.what = 2
            msg3?.obj = resultCow
            myHandler?.sendMessage(msg3)

            val resultDog = URL("https://users.metropolia.fi/~jaripie/dog").readText()
            Log.d("res", resultBear)
            val msg4 = myHandler?.obtainMessage()
            msg4?.what = 3
            msg4?.obj = resultDog
            myHandler?.sendMessage(msg4)

        } catch (e: Exception) {
            Log.d("Error", e.toString())
        }
    }
}