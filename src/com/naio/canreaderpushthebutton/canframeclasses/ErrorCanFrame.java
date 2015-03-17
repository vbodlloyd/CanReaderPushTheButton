package com.naio.canreaderpushthebutton.canframeclasses;

import java.util.List;

public class ErrorCanFrame extends CanFrame {
	private String error;
	private String complementError;

	public ErrorCanFrame(int id, int dlc, List<Integer> data, Double time) {
		super(id, dlc, data);
		this.type = "ERROR";
		error = "no error";
		complementError = " ";
	}

	public ErrorCanFrame() {
		this.type = "ERROR";
		error = "no error";
		complementError = " ";
	}

	public ErrorCanFrame setParams(int id, int dlc, List<Integer> data) {
		super.setParams(id, dlc, data);
		return this;
	}


	public void setError(String frame) {
		synchronized (lock) {
			error = frame;
		}
	}
	
	public String getError(){
		synchronized (lock) {
			return error ;
		}
	}

	public void setComplementError(String frame) {
		synchronized (lock) {
			complementError = frame;
		}
	}

	public String getComplementError() {
		synchronized (lock) {
			return complementError;
		}
	}
}
