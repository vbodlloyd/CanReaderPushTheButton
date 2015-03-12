package com.naio.canreaderpushthebutton.canframeclasses;

import java.util.List;

import net.sourceforge.juint.UInt8;

import com.naio.canreaderpushthebutton.R;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Display the data of the Brain ( yet only the cpu temperature )
 * 
 * @author bodereau
 * 
 */
public class BrainCanFrame extends CanFrame {
	private UInt8 temperature;

	public BrainCanFrame(int id, int dlc, List<Integer> data, Double time) {
		super(id, dlc, data);
		this.type = "BRAIN";
	}

	public BrainCanFrame() {
		this.type = "BRAIN";
	}

	public BrainCanFrame setParams(int id, int dlc, List<Integer> data) {
		super.setParams(id, dlc, data);
		return this;
	}

	public UInt8 getTemperature() {
		synchronized (lock) {
			return temperature;
		}
	}


	public void save_datas() {
		synchronized (lock) {

			if (idMess == null) {
				return;
			}
			switch (idMess) {
			case "1110":
				save_data_temperature();
				break;
			}
		}
	}

	/**
	 * 
	 */
	private void save_data_temperature() {
		temperature = new UInt8(getData().get(0));
	}
}
