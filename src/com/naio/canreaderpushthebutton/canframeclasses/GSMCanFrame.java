package com.naio.canreaderpushthebutton.canframeclasses;

import java.util.ArrayList;
import java.util.List;

import com.naio.canreaderpushthebutton.R;

import android.support.v4.view.ViewPager;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Display the data of the GSM
 * 
 * @author bodereau
 */
public class GSMCanFrame extends CanFrame {

	private List<Integer> gsmData = new ArrayList<Integer>();

	public GSMCanFrame(int id, int dlc, List<Integer> data) {
		super(id, dlc, data);
		this.type = "GSM";
		gsmData = new ArrayList<Integer>();

	}

	public GSMCanFrame() {
		this.type = "GSM";
		gsmData = new ArrayList<Integer>();
	}
	
	public String getGsmData(){
		synchronized (lock) {
			String text = "";
			for (int i : gsmData) {
				text += (char) i;
			}
			gsmData.clear();
			return text;
		}
	}

	public GSMCanFrame setParams(int id, int dlc, List<Integer> data) {
		super.setParams(id, dlc, data);
		// 641 is the id where messages are sent. We only need to read the 640
		if (id == 641)
			return this;
		synchronized (lock) {
			//we all the data that we read because the response is sent char by char
			gsmData.add(getData().get(0));
			return this;
		}
	}

}
