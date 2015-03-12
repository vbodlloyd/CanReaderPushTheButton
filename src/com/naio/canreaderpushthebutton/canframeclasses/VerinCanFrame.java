/**
 * Created the 16 janv. 2015 at 14:12:16
 * by bodereau
 * 
 */
package com.naio.canreaderpushthebutton.canframeclasses;

import java.util.List;

import com.naio.canreaderpushthebutton.utils.BytesFunction;

import android.util.Log;

/**
 * Display the data of the verin, the ODO and of the tension
 * 
 * @author bodereau
 * 
 */
public class VerinCanFrame extends CanFrame {

	private Integer requetePosition;
	private Integer retourPosition, etatVerin;
	private Integer commandeMoteur;
	private Integer lectureODO;
	private Integer sortieCapteur, activationUSB;
	private Integer versionMaj, versionMin;
	private Integer t12vLSB, t12vMSB, t33vLSB, t33vMSB, t5vLSB, t5vMSB,
			flagSortie;
	private Integer t24vLSB, t24vMSB, pileLSB, pileMSB;
	// global variables for the counter of the ODO
	static private Integer cptAvg, cptArg, cptAvd, cptArd;
	private static boolean state_ard;
	private static boolean state_avd;
	private static boolean state_arg;
	private static boolean state_avg;

	public VerinCanFrame(int id, int dlc, List<Integer> data) {
		super(id, dlc, data);
		init();
	}

	public VerinCanFrame() {
		init();
	}

	private void init() {
		this.type = "Verin";

		VerinCanFrame.resetCpt();

	}

	public VerinCanFrame setParams(int id, int dlc, List<Integer> data) {
		super.setParams(id, dlc, data);
		return this;
	}

	public void save_datas() {
		synchronized (lock) {

			if (idMess == null) {
				return;
			}
			switch (idMess) {
			case "0000":
				save_data_requete();
				break;
			case "0001":
				save_data_retour();
				break;
			case "0010":
				save_data_commande();
				break;
			case "0011":
				save_data_capteur();
				break;
			case "0100":
				save_data_sortie();
				break;
			case "0101":
				save_data_version();
				break;
			case "0110":
				save_data_tension_12v();
				break;
			case "0111":
				save_data_tension_principale();
				break;
			default:
				break;
			}
		}
	}

	private void save_data_tension_12v() {
		t12vLSB = getData().get(0);
		t12vMSB = getData().get(1);
		t33vLSB = getData().get(2);
		t33vMSB = getData().get(3);
		t5vLSB = getData().get(4);
		t5vMSB = getData().get(5);
		flagSortie = getData().get(6);
	}

	public double getTension24v() {
		synchronized (lock) {
			if (t24vMSB == null) {
				return 0.0;
			}
			return BytesFunction.fromTwoComplement(t24vMSB, t24vLSB, 16, 1000);
		}
	}

	public double getTensionPile() {
		synchronized (lock) {
			return BytesFunction.fromTwoComplement(pileMSB, pileLSB, 16, 1000);
		}
	}

	private void save_data_tension_principale() {
		t24vLSB = getData().get(0);
		t24vMSB = getData().get(1);
		pileLSB = getData().get(2);
		pileMSB = getData().get(3);
	}

	public double getTension12v() {
		synchronized (lock) {
			return BytesFunction.fromTwoComplement(t12vMSB, t12vLSB, 16, 1000);
		}
	}

	public double getTension33v() {
		synchronized (lock) {
			return BytesFunction.fromTwoComplement(t33vMSB, t33vLSB, 16, 1000);
		}
	}

	public double getTension5v() {
		synchronized (lock) {
			return BytesFunction.fromTwoComplement(t5vMSB, t5vLSB, 16, 1000);
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
	 * 
	 */
	private void save_data_sortie() {
		sortieCapteur = getData().get(0);
		activationUSB = getData().get(1);

	}

	public String getOdo() {
		synchronized (lock) {
			String text = Integer.toBinaryString(lectureODO);
			String[] data = BytesFunction.fillWithZeroTheBinaryString(text)
					.split("(?<!^)");
			String write = "";
			write += "ARD:" + data[7];
			write += " AVD:" + data[6];
			write += " ARG:" + data[5];
			write += " AVG:" + data[4];
			if (data[7].contains("1") && VerinCanFrame.state_ard) {
				VerinCanFrame.cptArd += 1;
				VerinCanFrame.state_ard = false;
			}
			if (data[6].contains("1") && VerinCanFrame.state_avd) {
				VerinCanFrame.cptAvd += 1;
				VerinCanFrame.state_avd = false;
			}
			if (data[5].contains("1") && VerinCanFrame.state_arg) {
				VerinCanFrame.cptArg += 1;
				VerinCanFrame.state_arg = false;
			}
			if (data[4].contains("1") && VerinCanFrame.state_avg) {
				VerinCanFrame.cptAvg += 1;
				VerinCanFrame.state_avg = false;
			}
			if (data[7].contains("0") && !VerinCanFrame.state_ard) {
				VerinCanFrame.state_ard = true;
			}
			if (data[6].contains("0") && !VerinCanFrame.state_avd) {
				VerinCanFrame.state_avd = true;
			}
			if (data[5].contains("0") && !VerinCanFrame.state_arg) {
				VerinCanFrame.state_arg = true;
			}
			if (data[4].contains("0") && !VerinCanFrame.state_avg) {
				VerinCanFrame.state_avg = true;
			}
			return write;

		}
	}

	/**
	 * 
	 */
	private void save_data_capteur() {
		lectureODO = getData().get(0);
	}

	/**
	 * 
	 */
	private void save_data_commande() {
		commandeMoteur = getData().get(0);
	}

	/**
	 * 
	 */
	private void save_data_retour() {
		retourPosition = getData().get(0);
		etatVerin = getData().get(1);
	}

	public Integer getRetourPosition() {
		synchronized (lock) {
			return retourPosition;

		}
	}

	public String getEtatVerin() {
		synchronized (lock) {
			String text = Integer.toBinaryString(etatVerin);
			String write = "";
			Log.e("etatVerin", BytesFunction.fillWithZeroTheBinaryString(text)
					.subSequence(5, 8).toString());
			switch (BytesFunction.fillWithZeroTheBinaryString(text)
					.subSequence(6, 8).toString()) {
			case "11":
				write += "dep. en cours, erreur driver";
				break;
			case "10":
				write += "erreur driver";
				break;
			case "01":
				write += "dep. en cours";
				break;
			case "00":
				write += "no data";
				break;

			}
			return write;
		}
	}

	/**
	 * 
	 */
	private void save_data_requete() {
		requetePosition = getData().get(0);
	}

	/**
	 * Put all the global variables to 0
	 */
	public static void resetCpt() {

		VerinCanFrame.cptArd = 0;
		VerinCanFrame.cptAvg = 0;
		VerinCanFrame.cptArg = 0;
		VerinCanFrame.cptAvd = 0;
		VerinCanFrame.cptArd = 0;
		VerinCanFrame.cptAvg = 0;
		VerinCanFrame.cptArg = 0;
		VerinCanFrame.cptAvd = 0;
		VerinCanFrame.state_ard = true;
		VerinCanFrame.state_avg = true;
		VerinCanFrame.state_arg = true;
		VerinCanFrame.state_avd = true;

	}
}