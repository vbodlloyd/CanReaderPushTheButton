package com.naio.canreaderpushthebutton.canframeclasses;

import java.util.List;

import android.widget.RelativeLayout;
/**
 * TODO when ready
 * 
 * @author bodereau
 *
 */
public class MotorCanFrame extends CanFrame {

	protected Integer consigne, sensRotation;
	protected Integer courantMax, tension, rampeDemarrage, rampeFreinage;
	protected Integer couple, vitesse, statut;
	protected Integer temperature;
	protected Integer versionMaj, versionMin;
	protected Integer nbImpOdom, odomStatut;
	protected Integer partMSB, partLSB;

	public MotorCanFrame(int id, int dlc, List<Integer> data) {
		super(id, dlc, data);
		this.type = "MOTOR";
	}

	public MotorCanFrame() {
		this.type = "MOTOR";
	}

	public MotorCanFrame setParams(int id, int dlc, List<Integer> data) {
		super.setParams(id, dlc, data);
		return this;
	}


	private void display_data_part() {
		partMSB = getData().get(0);
		partLSB = getData().get(1);
	}

	private void display_data_odom() {
		nbImpOdom = getData().get(0);
		odomStatut = getData().get(1);
		

	}

	private void display_data_version() {
		versionMaj = getData().get(0);
		versionMin = getData().get(1);

	}

	private void display_data_temperature() {
		temperature = getData().get(0);

	}

	private void display_data_couple() {
		couple = getData().get(0);
		vitesse = getData().get(1);
		statut = getData().get(2);

	}

	private void display_data_courant() {
		courantMax = getData().get(0);
		tension = getData().get(1);
		rampeDemarrage = getData().get(2);
		rampeFreinage = getData().get(3);

	}

	private void display_data_consigne() {
		consigne = getData().get(0);
		sensRotation = getData().get(1);

	}
}
