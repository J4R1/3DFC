package com.example.a3dfc

import java.math.BigDecimal
import java.math.RoundingMode
import android.app.Activity
import android.app.AlertDialog
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
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ViewRenderable
import java.lang.Exception
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.os.Build
import android.preference.PreferenceManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration

class MainActivity : AppCompatActivity(), SensorEventListener, TextToSpeech.OnInitListener,
    LocationListener {
    var arrayView: Array<View>? = null
    private var arFragment: ArFragment? = null
    var selected: Int = 0 //Default value
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Ar object placeholders
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

    // Ar TextView objects for info
    private lateinit var animalName: ViewRenderable
    private lateinit var animalName2: ViewRenderable
    private lateinit var animalName3: ViewRenderable
    private lateinit var animalName4: ViewRenderable
    private lateinit var animalName5: ViewRenderable
    private lateinit var animalName6: ViewRenderable
    private lateinit var animalName7: ViewRenderable
    private lateinit var animalName8: ViewRenderable
    private lateinit var locationList: ViewRenderable

    // Sensors setup
    private lateinit var sensorManager: SensorManager
    private lateinit var mSensorManager: SensorManager
    private var mSensors: Sensor? = null

    // Name of the file which contains the visited places
    internal val FILENAME = "locationChecklist.txt"

    // Display the current list of visited places
    fun readChecklist() {
        // Prevents crashing if file is empty
        openFileOutput(FILENAME, Context.MODE_APPEND).use { it.write("".toByteArray()) }
        GlobalModel.checklist_text = openFileInput(FILENAME)?.bufferedReader().use {
            it?.readText() ?: getString(R.string.cl_error)
        }
        // TODO show the file
        Log.d("debug", "reads checklist here!")
    }

    // Write to visited places list after validation and add a timestamp
    fun writeChecklist(lon: Double, lat: Double) {
        val name = when {
            lon in 26.51..26.99 && lat in 60.00..61.00 -> "Location1"
            lon in 26.00..26.50 && lat in 60.00..61.00 -> "Location2"
            lon in 25.51..25.99 && lat in 60.00..61.00 -> "Location3"
            lon in 25.00..25.50 && lat in 60.00..61.00 -> "Location4"
            lon in 24.51..24.99 && lat in 60.00..61.00 -> "Myyrmäki"
            lon in 24.00..24.50 && lat in 60.00..61.00 -> "Location6"
            lon in 23.51..23.99 && lat in 60.00..61.00 -> "Location7"
            lon in 23.00..23.50 && lat in 60.00..61.00 -> "Location8"
            lon in 22.51..22.99 && lat in 60.00..61.00 -> "Location9"
            lon in 22.00..22.50 && lat in 60.00..61.00 -> "Location10"
            lon in 21.51..21.99 && lat in 60.00..61.00 -> "Location11"
            lon in 21.00..21.50 && lat in 60.00..61.00 -> "Location12"
            else -> return
        }
        val format = DateTimeFormatter.ofPattern("dd-MM-yyy")
        val date = LocalDate.now().format(format)
        Log.d("debug", "Date is $date")
        openFileOutput(FILENAME, Context.MODE_APPEND).use {
            it.write("You visited $name on $date\n".toByteArray())
        }
        Log.d("debug", "writes checklist here!")
    }

    // Initialize the text to speech engine
    var tts: TextToSpeech? = null
    private val REQUEST_CODE_SPEECH_INPUT = 100

    // Message handler for info from web
    private val mHandler: Handler = object :
        Handler(Looper.getMainLooper()) {
        override fun handleMessage(inputMessage: Message) {
            if (inputMessage.what == 0) {
                val msg = inputMessage.obj.toString()
                GlobalModel.bear_txt = msg
            }
            if (inputMessage.what == 1) {
                val msg = inputMessage.obj.toString()
                GlobalModel.cat_txt = msg
            }
            if (inputMessage.what == 2) {
                val msg = inputMessage.obj.toString()
                GlobalModel.cow_txt = msg
            }
            if (inputMessage.what == 3) {
                val msg = inputMessage.obj.toString()
                GlobalModel.dog_txt = msg
            }
            if (inputMessage.what == 4) {
                val msg = inputMessage.obj.toString()
                GlobalModel.elephant_txt = msg
            }
            if (inputMessage.what == 5) {
                val msg = inputMessage.obj.toString()
                GlobalModel.ferret_txt = msg
            }
            if (inputMessage.what == 6) {
                val msg = inputMessage.obj.toString()
                GlobalModel.hippopotamus_txt = msg
            }
            if (inputMessage.what == 7) {
                val msg = inputMessage.obj.toString()
                GlobalModel.horse_txt = msg
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

    // On startup
    override fun onCreate(savedInstanceState: Bundle?) {
        tts = TextToSpeech(this, this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Phone location and permission
        getLocation()

        // Initial run of previous location checklist
        readChecklist()

        //Check internet connection(REQUIRED)
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

        // Add onClickListeners for the speech recognition
        speak_button.setOnClickListener { SpeechFunction() }
        checklist_button.setOnClickListener { readChecklist() }

        // DialogBox Intro
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setCancelable(false)
        dialogBuilder.setMessage(R.string.intro)
            .setPositiveButton(R.string.intro_ok, DialogInterface.OnClickListener { dialog, id -> })
            .show()

        // Startup vibration
        vibrate(this)

        // Sensor setup
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

        // Text to speech button setup
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

        // Sets all buttons invisible
        setButtonsInvisible()
    }

    // Phone location and permission
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
                // Null check for location
                if (long != null && lat != null) {
                    val decimalLat = BigDecimal(lat).setScale(2, RoundingMode.HALF_EVEN).toDouble()
                    GlobalModel.latitude = decimalLat
                    val decimalLong =
                        BigDecimal(long).setScale(2, RoundingMode.HALF_EVEN).toDouble()
                    GlobalModel.longitude = decimalLong
                    //Writes location to memory
                    writeChecklist(GlobalModel.longitude, GlobalModel.latitude) //lon lat
                } else {
                    Toast.makeText(this, "Failed to get location", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            Toast.makeText(this, "Failed to get location", Toast.LENGTH_LONG).show()
            Log.d("--debug--", "Location error")
        }
    }

    // Vibrates phone
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

    // Location change
    override fun onLocationChanged(p0: Location?) {}

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}
    override fun onProviderEnabled(p0: String?) {}
    override fun onProviderDisabled(p0: String?) {}

    // Sets buttons invisible at start
    fun setButtonsInvisible() {
        // Buttons
        right_button.isClickable = false
        right_button.isVisible = false
        left_button.isClickable = false
        left_button.isVisible = false
        voice_button.isVisible = false
        voice_button.isClickable = false
        speak_button.isVisible = false
        speak_button.isClickable = false
        checklist_button.isVisible = false
        checklist_button.isClickable = false
        checklist_button.isVisible = false
        // imageViews
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

    // Stops Text to speech
    override fun onDestroy() {
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroy()
    }

    // Sensor data
    override fun onSensorChanged(p0: SensorEvent?) {}

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}
    override fun onResume() {
        mSensorManager.registerListener(
            this,
            mSensors,
            999999998,
            999999999
        )
        super.onResume()
    }

    override fun onPause() {
        Log.v("-----onPause-----", "unregisterListener")
        sensorManager.unregisterListener(this)
        super.onPause()
    }

    // Googles Speech to text function
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == Activity.RESULT_OK || null != data) {
                // !! required
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
        // Warns user about AR reset
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setCancelable(false)
        dialogBuilder.setMessage(R.string.voice_error).setNegativeButton(
            R.string.voice_ok,
            DialogInterface.OnClickListener { dialog, id -> })
            .setPositiveButton(
                R.string.voice_anyway,
                DialogInterface.OnClickListener { dialog, id -> doTheSpeech() })
            .show()
    }

    // Checks internet
    private fun isNetworkAvailable(): Boolean {
        val cm = this.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        return cm.activeNetworkInfo?.isConnected ?: false
    }

    // Setup for all AR objects
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

        val inflater6: LayoutInflater = LayoutInflater.from(applicationContext)
        val view6 = inflater6.inflate(R.layout.name_animal5, root_layout, false)
        val textView6: TextView = view6?.findViewById(R.id.nameAnimal5) as TextView
        textView6.text = GlobalModel.elephant_txt

        ViewRenderable.builder()
            .setView(this, view6)
            .build()
            .thenAccept { renderable -> animalName5 = renderable }

        val inflater7: LayoutInflater = LayoutInflater.from(applicationContext)
        val view7 = inflater7.inflate(R.layout.name_animal6, root_layout, false)
        val textView7: TextView = view7?.findViewById(R.id.nameAnimal6) as TextView
        textView7.text = GlobalModel.ferret_txt

        ViewRenderable.builder()
            .setView(this, view7)
            .build()
            .thenAccept { renderable -> animalName6 = renderable }

        val inflater8: LayoutInflater = LayoutInflater.from(applicationContext)
        val view8 = inflater8.inflate(R.layout.name_animal7, root_layout, false)
        val textView8: TextView = view8?.findViewById(R.id.nameAnimal7) as TextView
        textView8.text = GlobalModel.hippopotamus_txt

        ViewRenderable.builder()
            .setView(this, view8)
            .build()
            .thenAccept { renderable -> animalName7 = renderable }

        val inflater9: LayoutInflater = LayoutInflater.from(applicationContext)
        val view9 = inflater9.inflate(R.layout.name_animal8, root_layout, false)
        val textView9: TextView = view9?.findViewById(R.id.nameAnimal8) as TextView
        textView9.text = GlobalModel.dog_txt

        ViewRenderable.builder()
            .setView(this, view9)
            .build()
            .thenAccept { renderable -> animalName8 = renderable }

        val inflater5: LayoutInflater = LayoutInflater.from(applicationContext)
        val view5 = inflater5.inflate(R.layout.location_list, root_layout, false)
        val textView5: TextView = view5?.findViewById(R.id.checklist_text) as TextView
        if (GlobalModel.checklist_text.length <= 5) {
            textView5.text = "You haven't visited any sites"
        } else {
            textView5.text = GlobalModel.checklist_text
        }

        ViewRenderable.builder()
            .setView(this, view5)
            .build()
            .thenAccept { renderable -> locationList = renderable }
    }

    // Rotates AR objects left
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
        } else if (animal == 5) {
            GlobalModel.elephant_rotation = GlobalModel.elephant_rotation - 20
        } else if (animal == 6) {
            GlobalModel.ferret_rotation = GlobalModel.ferret_rotation - 20
        } else if (animal == 7) {
            GlobalModel.hippopotamus_rotation = GlobalModel.hippopotamus_rotation - 20
        } else if (animal == 8) {
            GlobalModel.horse_rotation = GlobalModel.horse_rotation - 20
        }
    }

    // Rotates AR objects right
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
        } else if (animal == 5) {
            GlobalModel.elephant_rotation = GlobalModel.elephant_rotation + 20
        } else if (animal == 6) {
            GlobalModel.ferret_rotation = GlobalModel.ferret_rotation + 20
        } else if (animal == 7) {
            GlobalModel.hippopotamus_rotation = GlobalModel.hippopotamus_rotation + 20
        } else if (animal == 8) {
            GlobalModel.horse_rotation = GlobalModel.horse_rotation + 20
        }
    }

    // Sets invisible AR objects visible
    fun setVisible(node1: TransformableNode) {
        if (node1.isEnabled == false) {
            node1.isEnabled = !node1.isEnabled
        }
    }

    // Sets visible AR objects invisible
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
        val nameViewElephant = TransformableNode(arFragment?.transformationSystem)
        val nameViewFerret = TransformableNode(arFragment?.transformationSystem)
        val nameViewHippopotamus = TransformableNode(arFragment?.transformationSystem)
        val nameViewHorse = TransformableNode(arFragment?.transformationSystem)
        val locationNode = TransformableNode(arFragment?.transformationSystem)
        val renderableNode = TransformableNode(arFragment?.transformationSystem)
        renderableNode.localPosition = Vector3(0f, 0f, 0f)
        renderableNode.localRotation =
            Quaternion.axisAngle(Vector3(0f, 1f, 0f), GlobalModel.bear_rotation)
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
        val renderableNodeElephant = TransformableNode(arFragment?.transformationSystem)
        renderableNodeElephant.localPosition = Vector3(0f, 0f, 0f)
        renderableNodeElephant.localRotation =
            Quaternion.axisAngle(Vector3(0f, 1f, 0f), GlobalModel.elephant_rotation)
        renderableNodeElephant.setParent(anchorNode)
        val renderableNodeFerret = TransformableNode(arFragment?.transformationSystem)
        renderableNodeFerret.localPosition = Vector3(0f, 0f, 0f)
        renderableNodeFerret.localRotation =
            Quaternion.axisAngle(Vector3(0f, 1f, 0f), GlobalModel.ferret_rotation)
        renderableNodeFerret.setParent(anchorNode)
        val renderableNodeHippopotamus = TransformableNode(arFragment?.transformationSystem)
        renderableNodeHippopotamus.localPosition = Vector3(0f, 0f, 0f)
        renderableNodeHippopotamus.localRotation =
            Quaternion.axisAngle(Vector3(0f, 1f, 0f), GlobalModel.hippopotamus_rotation)
        renderableNodeHippopotamus.setParent(anchorNode)
        val renderableNodeHorse = TransformableNode(arFragment?.transformationSystem)
        renderableNodeHorse.localPosition = Vector3(0f, 0f, 0f)
        renderableNodeHorse.localRotation =
            Quaternion.axisAngle(Vector3(0f, 1f, 0f), GlobalModel.horse_rotation)
        renderableNodeHorse.setParent(anchorNode)
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
                renderableNodeElephant.renderable = elephantRendereable
                renderableNodeElephant.select()
                renderableNodeFerret.renderable = ferretRendereable
                renderableNodeFerret.select()
                renderableNodeHippopotamus.renderable = hippopotamusRendereable
                renderableNodeHippopotamus.select()
                renderableNodeHorse.renderable = horseRendereable
                renderableNodeHorse.select()

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

                nameViewElephant.localPosition =
                    Vector3(0f, renderableNode.localPosition.y + 1.5f, 0f)
                nameViewElephant.setParent(anchorNode)
                nameViewElephant.renderable = animalName5
                nameViewElephant.select()

                nameViewFerret.localPosition =
                    Vector3(0f, renderableNode.localPosition.y + 1.0f, 0f)
                nameViewFerret.setParent(anchorNode)
                nameViewFerret.renderable = animalName6
                nameViewFerret.select()

                nameViewHippopotamus.localPosition =
                    Vector3(0f, renderableNode.localPosition.y + 1.0f, 0f)
                nameViewHippopotamus.setParent(anchorNode)
                nameViewHippopotamus.renderable = animalName7
                nameViewHippopotamus.select()

                nameViewHorse.localPosition =
                    Vector3(0f, renderableNode.localPosition.y + 1.0f, 0f)
                nameViewHorse.setParent(anchorNode)
                nameViewHorse.renderable = animalName8
                nameViewHorse.select()

                locationNode.localPosition =
                    Vector3(1.0f, 0f, 0f)
                locationNode.setParent(anchorNode)
                locationNode.renderable = locationList
                locationNode.select()

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

                renderableNodeElephant.scaleController.isEnabled = false
                renderableNodeElephant.rotationController.isEnabled = false
                renderableNodeElephant.translationController.isEnabled = false

                renderableNodeFerret.scaleController.isEnabled = false
                renderableNodeFerret.rotationController.isEnabled = false
                renderableNodeFerret.translationController.isEnabled = false

                renderableNodeHippopotamus.scaleController.isEnabled = false
                renderableNodeHippopotamus.rotationController.isEnabled = false
                renderableNodeHippopotamus.translationController.isEnabled = false

                renderableNodeHorse.scaleController.isEnabled = false
                renderableNodeHorse.rotationController.isEnabled = false
                renderableNodeHorse.translationController.isEnabled = false

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

                nameViewElephant.scaleController.isEnabled = false
                nameViewElephant.rotationController.isEnabled = false
                nameViewElephant.translationController.isEnabled = false

                nameViewFerret.scaleController.isEnabled = false
                nameViewFerret.rotationController.isEnabled = false
                nameViewFerret.translationController.isEnabled = false

                nameViewHippopotamus.scaleController.isEnabled = false
                nameViewHippopotamus.rotationController.isEnabled = false
                nameViewHippopotamus.translationController.isEnabled = false

                nameViewHorse.scaleController.isEnabled = false
                nameViewHorse.rotationController.isEnabled = false
                nameViewHorse.translationController.isEnabled = false

                locationNode.scaleController.isEnabled = false
                locationNode.rotationController.isEnabled = false
                locationNode.translationController.isEnabled = false

                setInvisible(renderableNode, renderableNodeCat)
                setInvisible(nameViewCow, renderableNodeCow)
                setInvisible(nameViewCat, nameViewBear)
                setInvisible(nameViewElephant, nameViewFerret)
                setInvisible(nameViewHippopotamus, nameViewHorse)
                setInvisible(nameViewDog, renderableNodeDog)
                setInvisible(renderableNodeElephant, renderableNodeFerret)
                setInvisible(renderableNodeHippopotamus, renderableNodeHorse)

                right_button.isClickable = true
                right_button.isVisible = true
                left_button.isClickable = true
                left_button.isVisible = true
                voice_button.isVisible = true
                voice_button.isClickable = true
                speak_button.isVisible = true
                speak_button.isClickable = true
                checklist_button.isVisible = false
                checklist_button.isClickable = false

                bear.isVisible = true
                bear.isClickable = true

                cat.isVisible = true
                cat.isClickable = true

                cow.isVisible = true
                cow.isClickable = true

                dog.isVisible = true
                dog.isClickable = true

                elephant.isVisible = true
                elephant.isClickable = true

                ferret.isVisible = true
                ferret.isClickable = true

                hippopotamus.isVisible = true
                hippopotamus.isClickable = true

                horse.isVisible = true
                horse.isClickable = true
            }
        }
        bear.setOnClickListener {
            this.selected = 1
            setInvisible(renderableNodeCat, renderableNodeCow)
            setInvisible(nameViewCat, nameViewCow)
            setInvisible(renderableNodeDog, nameViewDog)
            setInvisible(renderableNodeElephant, renderableNodeFerret)
            setInvisible(renderableNodeHippopotamus, renderableNodeHorse)
            setInvisible(nameViewElephant, nameViewFerret)
            setInvisible(nameViewHippopotamus, nameViewHorse)
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
            setInvisible(renderableNodeElephant, renderableNodeFerret)
            setInvisible(renderableNodeHippopotamus, renderableNodeHorse)
            setInvisible(nameViewElephant, nameViewFerret)
            setInvisible(nameViewHippopotamus, nameViewHorse)
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
            setInvisible(renderableNodeElephant, renderableNodeFerret)
            setInvisible(renderableNodeHippopotamus, renderableNodeHorse)
            setInvisible(nameViewElephant, nameViewFerret)
            setInvisible(nameViewHippopotamus, nameViewHorse)
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
            this.selected = 3
            setInvisible(renderableNodeCat, renderableNode)
            setInvisible(nameViewBear, nameViewCat)
            setInvisible(renderableNodeCow, nameViewCow)
            setInvisible(renderableNodeElephant, renderableNodeFerret)
            setInvisible(renderableNodeHippopotamus, renderableNodeHorse)
            setInvisible(nameViewElephant, nameViewFerret)
            setInvisible(nameViewHippopotamus, nameViewHorse)
            setVisible(renderableNodeDog)
            setVisible(nameViewDog)
            right_button.setOnClickListener {
                rotateRight(renderableNodeDog, GlobalModel.dog_rotation, 4)
            }
            left_button.setOnClickListener {
                rotateLeft(renderableNodeDog, GlobalModel.dog_rotation, 4)
            }
        }

        elephant.setOnClickListener {
            this.selected = 5
            setInvisible(renderableNodeCat, renderableNode)
            setInvisible(nameViewBear, nameViewCat)
            setInvisible(renderableNodeCow, nameViewCow)
            setInvisible(renderableNodeDog, nameViewDog)
            setInvisible(renderableNodeHippopotamus, renderableNodeHorse)
            setInvisible(nameViewDog, nameViewFerret)
            setInvisible(nameViewHippopotamus, nameViewHorse)
            setInvisible(renderableNode, renderableNodeFerret)
            setVisible(renderableNodeElephant)
            setVisible(nameViewElephant)
            right_button.setOnClickListener {
                rotateRight(renderableNodeElephant, GlobalModel.elephant_rotation, 5)
            }
            left_button.setOnClickListener {
                rotateLeft(renderableNodeElephant, GlobalModel.elephant_rotation, 5)
            }
        }

        ferret.setOnClickListener {
            this.selected = 6
            setInvisible(renderableNodeCat, renderableNode)
            setInvisible(nameViewBear, nameViewCat)
            setInvisible(renderableNodeCow, nameViewCow)
            setInvisible(renderableNodeElephant, renderableNode)
            setInvisible(renderableNodeHippopotamus, renderableNodeHorse)
            setInvisible(nameViewElephant, nameViewDog)
            setInvisible(nameViewHippopotamus, nameViewHorse)
            setVisible(renderableNodeFerret)
            setVisible(nameViewFerret)
            right_button.setOnClickListener {
                rotateRight(renderableNodeFerret, GlobalModel.ferret_rotation, 6)
            }
            left_button.setOnClickListener {
                rotateLeft(renderableNodeFerret, GlobalModel.ferret_rotation, 6)
            }
        }

        hippopotamus.setOnClickListener {
            this.selected = 7
            setInvisible(renderableNodeCat, renderableNode)
            setInvisible(nameViewBear, nameViewCat)
            setInvisible(renderableNodeCow, nameViewCow)
            setInvisible(renderableNodeElephant, renderableNodeFerret)
            setInvisible(nameViewElephant, nameViewFerret)
            setInvisible(nameViewCat, nameViewHorse)
            setInvisible(renderableNode, renderableNodeHorse)
            setVisible(renderableNodeHippopotamus)
            setVisible(nameViewHippopotamus)
            right_button.setOnClickListener {
                rotateRight(renderableNodeHippopotamus, GlobalModel.hippopotamus_rotation, 7)
            }
            left_button.setOnClickListener {
                rotateLeft(renderableNodeHippopotamus, GlobalModel.hippopotamus_rotation, 7)
            }
        }

        horse.setOnClickListener {
            this.selected = 8
            setInvisible(renderableNodeCat, renderableNode)
            setInvisible(nameViewBear, nameViewCat)
            setInvisible(renderableNodeCow, nameViewCow)
            setInvisible(renderableNodeElephant, renderableNodeFerret)
            setInvisible(renderableNodeHippopotamus, renderableNodeHorse)
            setInvisible(nameViewElephant, nameViewFerret)
            setInvisible(nameViewHippopotamus, nameViewElephant)
            setVisible(renderableNodeHorse)
            setVisible(nameViewHorse)
            right_button.setOnClickListener {
                rotateRight(renderableNodeHorse, GlobalModel.horse_rotation, 8)
            }
            left_button.setOnClickListener {
                rotateLeft(renderableNodeHorse, GlobalModel.horse_rotation, 8)
            }
        }
    }

    // imageView array
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

    // imageView setup
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

    // imageView selection indication setup
    private fun setBackground(id: Int) {
        for (view in arrayView!!) {
            if (view.id == id) {
                view.setBackgroundResource(R.drawable.animal_button)
            } else {
                view.setBackgroundColor(Color.TRANSPARENT)
            }
        }
    }

    // Text to speech setup
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

// Fetches data from web
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
            Log.d("res", resultCat)
            val msg2 = myHandler?.obtainMessage()
            msg2?.what = 1
            msg2?.obj = resultCat
            myHandler?.sendMessage(msg2)

            val resultCow = URL("https://users.metropolia.fi/~jaripie/cow").readText()
            Log.d("res", resultCow)
            val msg3 = myHandler?.obtainMessage()
            msg3?.what = 2
            msg3?.obj = resultCow
            myHandler?.sendMessage(msg3)

            val resultDog = URL("https://users.metropolia.fi/~jaripie/dog").readText()
            Log.d("res", resultDog)
            val msg4 = myHandler?.obtainMessage()
            msg4?.what = 3
            msg4?.obj = resultDog
            myHandler?.sendMessage(msg4)

            val resultElephant = URL("https://users.metropolia.fi/~jaripie/elephant").readText()
            Log.d("res", resultElephant)
            val msg5 = myHandler?.obtainMessage()
            msg5?.what = 4
            msg5?.obj = resultElephant
            myHandler?.sendMessage(msg5)

            val resultFerret = URL("https://users.metropolia.fi/~jaripie/lion").readText()
            Log.d("res", resultFerret)
            val msg6 = myHandler?.obtainMessage()
            msg6?.what = 5
            msg6?.obj = resultFerret
            myHandler?.sendMessage(msg6)

            val resultHippopotamus =
                URL("https://users.metropolia.fi/~jaripie/wolverine").readText()
            Log.d("res", resultHippopotamus)
            val msg7 = myHandler?.obtainMessage()
            msg7?.what = 6
            msg7?.obj = resultHippopotamus
            myHandler?.sendMessage(msg7)

            val resultHorse = URL("https://users.metropolia.fi/~jaripie/reindeer").readText()
            Log.d("res", resultHorse)
            val msg8 = myHandler?.obtainMessage()
            msg8?.what = 7
            msg8?.obj = resultHorse
            myHandler?.sendMessage(msg8)

        } catch (e: Exception) {
            Log.d("Error", e.toString())
        }
    }
}

// Global objects usable anywhere
object GlobalModel {
    // For wiki texts
    var position = 0
    // For speech to text placeholder
    var speechText = ""
    // For animal wiki info
    var bear_txt = ""
    var cat_txt = ""
    var cow_txt = ""
    var dog_txt = ""
    var elephant_txt = ""
    var ferret_txt = ""
    var hippopotamus_txt = ""
    var horse_txt = ""
    // Default AR object rotation
    var bear_rotation = 180f
    var cat_rotation = 180f
    var cow_rotation = 180f
    var dog_rotation = 180f
    var elephant_rotation = 180f
    var ferret_rotation = 180f
    var hippopotamus_rotation = 180f
    var horse_rotation = 180f
    // Default location
    var latitude = 0.00
    var longitude = 0.00
    // Default visited location list
    var checklist_text = "No visited locations"
}
