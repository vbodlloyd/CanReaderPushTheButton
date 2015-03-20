package com.naio.canreaderpushthebutton.canframeclasses;

import java.util.List;

import com.naio.canreaderpushthebutton.utils.BytesFunction;


/**
 * Display the data of the IMU
 * 
 * @author bodereau
 * 
 */
public class IMUCanFrame extends CanFrame {

	private Integer accelXMSB, accelXLSB, accelYLSB, accelYMSB, accelZLSB,
			accelZMSB;
	private Integer gyroXMSB, gyroXLSB, gyroYMSB, gyroYLSB, gyroZMSB, gyroZLSB;
	private Integer magnetoXMSB, magnetoXLSB, magnetoYMSB, magnetoYLSB,
			magnetoZMSB, magnetoZLSB, resMagnMSB, resMagnLSB;
	private Integer temperature;
	private Integer versionMaj, versionMin;
	private Integer adresse1, donnee1, deviceSEL1;
	private Integer adresse2, donnee2, deviceSEL2;
	private Integer adresse3, deviceSEL3;
	private Integer adresse4, donnee4, deviceSEL4;
	private Integer board, rev;
	static private int indexF, indexA, indexC;
	static private Double freqMagneto, freqAccel, freqGyro;
	static private Double time, timeAccel, timeGyro, timeMagneto;
	static private int indexDisplay, indexDisplay2;

	public IMUCanFrame(int id, int dlc, List<Integer> data, Double time) {
		super(id, dlc, data);
		init();
	}

	public IMUCanFrame() {
		init();
	}

	/**
	 * Put all the variables to 0.0
	 * 
	 */
	private void init() {
		this.type = "IMU";

		if (IMUCanFrame.timeMagneto == null) {
			IMUCanFrame.freqMagneto = 0.0;
			IMUCanFrame.freqAccel = 0.0;
			IMUCanFrame.freqGyro = 0.0;
			IMUCanFrame.indexF = 0;
			IMUCanFrame.indexA = 0;
			IMUCanFrame.indexC = 0;
			IMUCanFrame.indexDisplay = 0;
			IMUCanFrame.indexDisplay2 = 0;
			IMUCanFrame.timeMagneto = 0.0;
			IMUCanFrame.timeAccel = 0.0;
			IMUCanFrame.timeGyro = 0.0;
		}

	}

	public IMUCanFrame setParams(int id, int dlc, List<Integer> data,
			Double time) {
		super.setParams(id, dlc, data);
		IMUCanFrame.time = time;
		return this;
	}

	public void save_datas() {
		synchronized (lock) {
			if (idMess == null) {
				return;
			}
			switch (idMess) {
			case "0000":
				save_data_accel();

				break;
			case "0001":
				save_data_gyro();

				break;
			case "0010":
				save_data_magneto();

				break;
			case "0011":
				save_data_temperature();

				break;
			case "0100":
				save_data_version();

				break;
			case "1111":
				save_data_board();

				break;
			default:
				break;
			}
		}
	}

	/**
	 * 
	 */
	private void save_data_board() {
		board = getData().get(0);
		rev = getData().get(1);
	}

	/**
	 * 
	 */
	private void save_data_version() {
		versionMaj = getData().get(0);
		versionMin = getData().get(1);

	}

	/**
	 * 
	 */
	private void save_data_temperature() {
		temperature = BytesFunction.fromTwoComplement(getData().get(0), 8);
	}

	public double getTemperature() {
		synchronized (lock) {
			double tempeFinal = (double) temperature * 0.5 + 23;
			return tempeFinal;
		}
	}

	/**
	 * 
	 */
	private void save_data_magneto() {
		magnetoXMSB = getData().get(0);
		magnetoXLSB = getData().get(1);
		magnetoYMSB = getData().get(2);
		magnetoYLSB = getData().get(3);
		magnetoZMSB = getData().get(4);
		magnetoZLSB = getData().get(5);
		resMagnMSB = getData().get(6);
		resMagnLSB = getData().get(7);
		if (IMUCanFrame.indexF == 99) {
			IMUCanFrame.timeMagneto = IMUCanFrame.time;
			return;
		}
		IMUCanFrame.freqMagneto = IMUCanFrame.freqMagneto
				+ (IMUCanFrame.time - IMUCanFrame.timeMagneto) * 0.01;
		IMUCanFrame.timeMagneto = IMUCanFrame.time;
		IMUCanFrame.indexF += 1;
	}

	public double getGyroX() {
		synchronized (lock) {
			if (gyroXMSB == null) {
				return 0.0;
			}
			double factor = 1024 / 32.8;
			double valGyroX = BytesFunction.fromTwoComplement(gyroXMSB,
					gyroXLSB, 16, factor);
			return valGyroX;

		}
	}

	public double getGyroZ() {
		synchronized (lock) {
			double factor = 1024 / 32.8;
			double valGyroZ = BytesFunction.fromTwoComplement(gyroZMSB,
					gyroZLSB, 16, factor);
			return valGyroZ;

		}
	}

	public double getGyroY() {
		synchronized (lock) {
			double factor = 1024 / 32.8;
			double valGyroY = BytesFunction.fromTwoComplement(gyroYMSB,
					gyroYLSB, 16, factor);
			return valGyroY;

		}
	}

	/**
	 * 
	 */
	private void save_data_gyro() {
		gyroXMSB = getData().get(0);
		gyroXLSB = getData().get(1);
		gyroYMSB = getData().get(2);
		gyroYLSB = getData().get(3);
		gyroZMSB = getData().get(4);
		gyroZLSB = getData().get(5);
		if (IMUCanFrame.indexA == 99) {
			IMUCanFrame.timeGyro = IMUCanFrame.time;
			return;
		}
		IMUCanFrame.freqGyro = IMUCanFrame.freqGyro
				+ (IMUCanFrame.time - IMUCanFrame.timeGyro) * 0.01;
		IMUCanFrame.timeGyro = IMUCanFrame.time;
		IMUCanFrame.indexA += 1;
	}

	/**
	 * 
	 */
	private void save_data_accel() {
		accelXMSB = getData().get(0);
		accelXLSB = getData().get(1);
		accelYMSB = getData().get(2);
		accelYLSB = getData().get(3);
		accelZMSB = getData().get(4);
		accelZLSB = getData().get(5);
		if (IMUCanFrame.indexC == 99) {
			IMUCanFrame.timeAccel = IMUCanFrame.time;
			return;
		}
		IMUCanFrame.freqAccel = IMUCanFrame.freqAccel
				+ (IMUCanFrame.time - IMUCanFrame.timeAccel) * 0.01;
		IMUCanFrame.timeAccel = IMUCanFrame.time;
		IMUCanFrame.indexC += 1;
	}
	

	/**
	 * @return the accelXMSB
	 */
	public Integer getAccelXMSB() {
		return accelXMSB;
	}

	/**
	 * @return the accelXLSB
	 */
	public Integer getAccelXLSB() {
		return accelXLSB;
	}

	/**
	 * @return the gyroXMSB
	 */
	public Integer getGyroXMSB() {
		return gyroXMSB;
	}

	/**
	 * @return the gyroXLSB
	 */
	public Integer getGyroXLSB() {
		return gyroXLSB;
	}

	/**
	 * @return the magnetoXMSB
	 */
	public Integer getMagnetoXMSB() {
		return magnetoXMSB;
	}

	/**
	 * @return the magnetoXLSB
	 */
	public Integer getMagnetoXLSB() {
		return magnetoXLSB;
	}

	/**
	 * @return the accelYLSB
	 */
	public Integer getAccelYLSB() {
		return accelYLSB;
	}

	/**
	 * @return the accelYMSB
	 */
	public Integer getAccelYMSB() {
		return accelYMSB;
	}

	/**
	 * @return the accelZLSB
	 */
	public Integer getAccelZLSB() {
		return accelZLSB;
	}

	/**
	 * @return the accelZMSB
	 */
	public Integer getAccelZMSB() {
		return accelZMSB;
	}

	/**
	 * @return the gyroYMSB
	 */
	public Integer getGyroYMSB() {
		return gyroYMSB;
	}

	/**
	 * @return the gyroYLSB
	 */
	public Integer getGyroYLSB() {
		return gyroYLSB;
	}

	/**
	 * @return the gyroZMSB
	 */
	public Integer getGyroZMSB() {
		return gyroZMSB;
	}

	/**
	 * @return the gyroZLSB
	 */
	public Integer getGyroZLSB() {
		return gyroZLSB;
	}

	/**
	 * @return the magnetoYMSB
	 */
	public Integer getMagnetoYMSB() {
		return magnetoYMSB;
	}

	/**
	 * @return the magnetoYLSB
	 */
	public Integer getMagnetoYLSB() {
		return magnetoYLSB;
	}

	/**
	 * @return the magnetoZMSB
	 */
	public Integer getMagnetoZMSB() {
		return magnetoZMSB;
	}

	/**
	 * @return the magnetoZLSB
	 */
	public Integer getMagnetoZLSB() {
		return magnetoZLSB;
	}

	/**
	 * @return the resMagnMSB
	 */
	public Integer getResMagnMSB() {
		return resMagnMSB;
	}

	/**
	 * @return the resMagnLSB
	 */
	public Integer getResMagnLSB() {
		return resMagnLSB;
	}

	/**
	 * @return the versionMaj
	 */
	public Integer getVersionMaj() {
		return versionMaj;
	}

	/**
	 * @return the versionMin
	 */
	public Integer getVersionMin() {
		return versionMin;
	}

	/**
	 * @return the board
	 */
	public Integer getBoard() {
		return board;
	}

	/**
	 * @return the rev
	 */
	public Integer getRev() {
		return rev;
	}
}
