package com.example.datasamplingapp

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import kotlin.properties.Delegates
import java.util.Calendar
import java.io.File

class MainActivity : AppCompatActivity(), SensorEventListener {

    private var sensorManager : SensorManager by Delegates.notNull<SensorManager>()
    private var accelerations : ArrayList<FloatArray>? = null
    private var time: Calendar by Delegates.notNull<Calendar>()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        time = Calendar.getInstance() as Calendar

        val textNote = findViewById<EditText>(R.id.text_note) as EditText
        val startButton = findViewById<Button>(R.id.start_record) as Button
        val stopButton = findViewById<Button>(R.id.stop_record) as Button


        startButton.setOnClickListener { v ->
            enableSensor()
        }

        stopButton.setOnClickListener {v ->
            disableSensor()

        }
    }

    private fun enableSensor(){
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST)
    }

    private fun disableSensor(){
        sensorManager.unregisterListener(this)
    }

    override fun onResume() {
        super.onResume()
        enableSensor()
    }

    override fun onPause() {
        super.onPause()
        disableSensor()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        var data : FloatArray = floatArrayOf(0F,0F,0F,0F)
        if (event != null) {
            data[0] = (time.timeInMillis).toFloat()
            data[1] = event.values[0]
            data[2] = event.values[1]
            data[3] = event.values[2]
            when{
                (event.sensor.type == Sensor.TYPE_ACCELEROMETER) -> {
                    accelerations?.add(data)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    fun writeCSV(sensor: String, data: ArrayList<FloatArray>){
        var txt = "Time,X-value,Y-value,Z-value"
        File(applicationContext.filesDir, "SensorData").writer().use {
            it.write("")
        }
    }

}