/**
 * Created the 16 janv. 2015 at 13:50:39
 * by bodereau
 * 
 */
package com.naio.canreaderpushthebutton.canframeclasses;

import java.util.List;

import com.naio.canreaderpushthebutton.utils.BytesFunction;

/**
 * Display the data of the IHM
 * 
 * @author bodereau
 * 
 */
public class IHMCanFrame extends CanFrame {

	private Integer etatClavier;
	private Integer etatLed, couleurLed;
	private Integer contraste;
	private Integer backlight;
	private Integer board, rev;
	private Integer delaiLong;
	private Integer delaiRepet;
	private Integer versionMin;
	private Integer versionMaj;
	private Integer status;

	public IHMCanFrame(int id, int dlc, List<Integer> data) {
		super(id, dlc, data);
		this.type = "IHM";

	}

	public IHMCanFrame() {
		this.type = "IHM";
	}

	public IHMCanFrame setParams(int id, int dlc, List<Integer> data) {
		super.setParams(id, dlc, data);
		return this;
	}

	public void save_datas() {
		synchronized (lock) {

			if (idMess == null) {
				return;
			}
			switch (idMess) {
			case "0001":
				save_data_clavier();

				break;
			case "0010":
				save_data_led();
				break;
			case "0100":
				save_data_status();
				break;
			case "0101":
				save_data_delai();
				break;
			case "0110":
				save_data_version();
				break;
			case "0111":
				save_data_contraste();
				break;
			case "1000":
				save_data_backlight();
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

	public Integer getBoard() {
		synchronized (lock) {
			return board;
		}
	}

	public Integer getRev() {
		synchronized (lock) {
			return rev;
		}
	}


	/**
	 * 
	 */
	private void save_data_backlight() {
		backlight = getData().get(0);
	}

	public Integer getBacklight() {
		synchronized (lock) {
			return backlight;
		}
	}


	/**
	 * 
	 */
	private void save_data_contraste() {
		contraste = getData().get(0);
	}

	public Integer getConstrast() {
		synchronized (lock) {
			return contraste;
		}
	}

	/**
	 * 
	 */
	private void save_data_version() {
		versionMaj = getData().get(0);
		versionMin = getData().get(1);
	}

	/**
	 * @param rl
	 */

	/**
	 * 
	 */
	private void save_data_delai() {
		delaiLong = getData().get(0);
		delaiRepet = getData().get(1);

	}


	/**
	 * 
	 */
	private void save_data_status() {
		status = getData().get(0);

	}

	/**
	 * 
	 */
	private void save_data_led() {
		etatLed = getData().get(0);
		couleurLed = getData().get(1);
	}

	public String getDataLed() {
		synchronized (lock) {
			String text = Integer.toBinaryString(etatLed);
			String text2 = Integer.toBinaryString(couleurLed);
		
		text = BytesFunction.fillWithZeroTheBinaryString(text);
		text2 = BytesFunction.fillWithZeroTheBinaryString(text2);
		String[] data = text.split("(?<!^)");
		String[] data2 = text2.split("(?<!^)");
		String c1 = "";
		String c2 = "";
		String c3 = "";
		String c4 = "";
		if (data2[7].contains("0"))
			c1 = "Rouge";
		else
			c1 = "Verte";
		if (data2[6].contains("0"))
			c2 = "Rouge";
		else
			c2 = "Verte";
		if (data2[5].contains("0"))
			c3 = "Rouge";
		else
			c3 = "Verte";
		if (data2[4].contains("1"))
			c4 = "Verte";
		else
			c4 = "Rouge";
		String write = "";
		write += "Gauche:" + data[7] + "," + c1;
		write += "  ;Led 2:" + data[6] + "," + c2;
		write += "\nLed 3:" + data[5] + "," + c3;
		write += " ;Droite:" + data[4] + "," + c4;
		return write;
		}
	}

	/**
	 * 
	 */
	private void save_data_clavier() {
		etatClavier = getData().get(0);
	}

	public String getDataKeyboard() {
		synchronized (lock) {
			if(etatClavier ==null){
				etatClavier = 0;
			}
			String text = Integer.toBinaryString(etatClavier);
		
		text = BytesFunction.fillWithZeroTheBinaryString(text);
		String[] data = text.split("(?<!^)");

		String keyboardState = "";
		keyboardState += "valide:" + data[6];
		keyboardState += " annuler:" + data[7];
		keyboardState += " droite:" + data[3];
		keyboardState += " gauche:" + data[2];
		keyboardState += " haut:" + data[5];
		keyboardState += " bas:" + data[4];
		return keyboardState;
		}
	}
}
