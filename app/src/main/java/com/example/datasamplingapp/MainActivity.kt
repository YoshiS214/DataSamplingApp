package com.example.datasamplingapp

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity(), SensorEventListener {

    private var sensorManager : SensorManager by Delegates.notNull<SensorManager>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        val textNote = findViewById<EditText>(R.id.text_note) as EditText
        val startButton = findViewById<Button>(R.id.start_record) as Button
        val stopButton = findViewById<Button>(R.id.stop_record) as Button

        startButton.setOnClickListener { v ->
            enableSensor()
        }

        stopButton.setOnClickListener {

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

    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

}