/*
 ***************************************************************************
 * \brief   Embedded-Android (BTE5484)
 *			Accessing the ADC (connected potentiometer)
 * \file    ADC.java
 * \version 1.1
 * \date    08.04.2014
 * \author  kasen1
 *
 * \remark  Last Modifications:
 * \remark  V1.0, stolc2, 01.04.2014
 * 			V1.1, kasen1, 08.04.2014 Exam
 ***************************************************************************
 */
package bfh.ti.examreferenceproject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/*
 * Reading a value from one of the eight ADC channels AIN0 ... AIN7
 */
public class ADC {
	public static final String ADC_IN0 = "in_voltage0_raw";
	public static final String ADC_IN1 = "in_voltage1_raw";
	public static final String ADC_IN2 = "in_voltage2_raw";
	public static final String ADC_IN3 = "in_voltage3_raw";
	public static final String ADC_IN4 = "in_voltage4_raw";	// is the potentiometer (LOGARITHMIC!!)
	public static final String ADC_IN5 = "in_voltage5_raw";
	public static final String ADC_IN6 = "in_voltage6_raw";
	public static final String ADC_IN7 = "in_voltage7_raw";

	public int read_adc(String adc_channel) {
		Process p;
		String[] shellCmd = {
				"/system/bin/sh",
				"-c",
				String.format("cat /sys/bus/iio/devices/iio\\:device0/"
						+ adc_channel) };
		try {
			p = Runtime.getRuntime().exec(shellCmd);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			return Integer.parseInt(reader.readLine());
		} catch (IOException e) {
			return -1;
		}
	}
}
