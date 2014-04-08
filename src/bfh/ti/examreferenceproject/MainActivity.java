/*
 ***************************************************************************
 * \brief   Embedded-Android (BTE5484)
 * \file    MainActivity.java
 * \version 1.0
 * \date    08.04.2014
 * \author  kasen1
 *
 * \remark  Last Modifications:
 * \remark  V1.0, stolc2, 01.04.2014
 * 			V1.1, kasen1, 08.04.2014 Exam
 ***************************************************************************
 */
package bfh.ti.examreferenceproject;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnInitListener {

	private ADC adc;
	private TextView viewADCValue;

	private Context context;

	private TextToSpeech tts;
	private String tts_text = "Aktueller Füllstand: ";

	private SysfsFileGPIO led1;
	// private SysfsFileGPIO led2;
	// private SysfsFileGPIO led3;
	private SysfsFileGPIO led4;

	private SysfsFileGPIO button1;
	// private SysfsFileGPIO button2;
	// private SysfsFileGPIO button3;
	// private SysfsFileGPIO button4;

	private int adcValue;
	private boolean speakEnabled = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// context is used for the Toast message outside the Activity
		// environment
		context = getApplicationContext();

		// set up UI elements
		viewADCValue = (TextView) findViewById(R.id.myTextView);

		// set up text2speech
		tts = new TextToSpeech(this, this);

		// set up ADC
		adc = new ADC();

		// set up LEDs
		led1 = new SysfsFileGPIO(SysfsFileGPIO.LED_L1);
		// led2 = new SysfsFileGPIO(SysfsFileGPIO.LED_L2);
		// led3 = new SysfsFileGPIO(SysfsFileGPIO.LED_L3);
		led4 = new SysfsFileGPIO(SysfsFileGPIO.LED_L4);

		led1.set_direction_out();
		// led2.set_direction_out();
		// led3.set_direction_out();
		led4.set_direction_out();

		// set up buttons
		button1 = new SysfsFileGPIO(SysfsFileGPIO.BUTTON_T1);
		// button2 = new SysfsFileGPIO(SysfsFileGPIO.BUTTON_T2);
		// button3 = new SysfsFileGPIO(SysfsFileGPIO.BUTTON_T3);
		// button4 = new SysfsFileGPIO(SysfsFileGPIO.BUTTON_T4);

		button1.set_direction_in();
		// button2.set_direction_in();
		// button3.set_direction_in();
		// button4.set_direction_in();

		// set up ADC handling timer, delay 0ms and repeat in 50ms (20Hz)
		Timer adcTimer = new Timer();
		ADCTimerTask adcTimerTask = new ADCTimerTask();
		adcTimer.schedule(adcTimerTask, 0, 50);

		// set up button handling timer, delay 0ms and repeat in 50ms (20Hz)
		Timer buttonTimer = new Timer();
		ButtonTimerTask buttonTimerTask = new ButtonTimerTask();
		buttonTimer.schedule(buttonTimerTask, 0, 50);

		// set up LED handling timer, delay 0ms and repeat in 100ms
		Timer ledTimer = new Timer();
		LEDTimerTask ledTimerTask = new LEDTimerTask();
		ledTimer.schedule(ledTimerTask, 0, 100);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	// startup code for the text2speech engine
	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			//tts.setSpeechRate(1.5F);
			int result = tts.setLanguage(Locale.GERMAN);
			if (result != TextToSpeech.LANG_MISSING_DATA
					&& result != TextToSpeech.LANG_NOT_SUPPORTED) {
				speakEnabled = true;
			}
		}
	}

	// ADC handle timer
	class ADCTimerTask extends TimerTask {
		@Override
		public void run() {
			// Read analog value and display it on the screen
			adcValue = adc.read_adc(ADC.ADC_IN4);

			runOnUiThread(new Runnable() {
				public void run() {
					// update textView in a UI-Thread
					viewADCValue.setText("Füllstand: " + adcValue + "mm");
				}
			});
		}
		// led4 = new SysfsFileGPIO(SysfsFileGPIO.LED_L4);
	}

	// button handle timer
	class ButtonTimerTask extends TimerTask {
		private int oldButton1Value = 1; // for edge detection

		public void run() {
			// Read button values and react accordingly
			// Warning: BUTTONS AND LEDS ARE ACTIVE-LOW
			led1.write_value(button1.read_value());

			if (button1.read_value() == 0 && oldButton1Value == 1
					&& speakEnabled) {
				// Tell adc Value
				tts.speak(tts_text + adcValue + " Millimeter", TextToSpeech.QUEUE_FLUSH, null);

				// place Toast. --> WARNING: inside a timer thread,
				// the Toast has to be launched in a UI-Thread!!
				runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(context, (CharSequence) "(speaking)",
								Toast.LENGTH_LONG).show();
					}
				});
			}
			oldButton1Value = button1.read_value();
		}
	}

	// LED handle timer
	class LEDTimerTask extends TimerTask {
		@Override
		public void run() {
			led4.write_value(led4.read_value());
		}
	}

	// clean up
	public void onDestroy() {
		// Don't forget to shutdown!
		if (tts != null) {
			tts.stop();
			tts.shutdown();
		}
		super.onDestroy();
	}
}
