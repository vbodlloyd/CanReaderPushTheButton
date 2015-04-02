package com.naio.canreaderpushthebutton.activities;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.Process;
import java.util.ArrayList;
import java.util.List;

import com.naio.canreaderpushthebutton.R;
import com.naio.canreaderpushthebutton.canframeclasses.BrainCanFrame;
import com.naio.canreaderpushthebutton.canframeclasses.GSMCanFrame;
import com.naio.canreaderpushthebutton.canframeclasses.IHMCanFrame;
import com.naio.canreaderpushthebutton.canframeclasses.IMUCanFrame;
import com.naio.canreaderpushthebutton.canframeclasses.VerinCanFrame;
import com.naio.canreaderpushthebutton.parser.CanParser;
import com.naio.canreaderpushthebutton.threads.CanDumpThread;
import com.naio.canreaderpushthebutton.threads.CanParserThread;
import com.naio.canreaderpushthebutton.threads.CanSendThread;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import android.os.Handler;

import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 
 * MainActivity have a button, if you push it, the protocol test will be
 * launched
 * 
 * @author bodereau
 */
public class MainActivity extends FragmentActivity {

	private CanDumpThread canDumpThread;
	private CanSendThread canSendThread;
	private final Object lock = new Object();
	private int indexDebug;
	private IMUCanFrame imuCanFrame;
	private BrainCanFrame brainCanFrame;
	private IHMCanFrame ihmCanFrame;
	private VerinCanFrame verinCanFrame;
	private GSMCanFrame gsmCanFrame;
	private static final int MILLISECONDS_RUNNABLE = 1000;

	// 1 * MILLISECONDS_RUNNABLE for re send the keep control message
	private static final int KEEP_CONTROL_CAN_LOOP = 1;
	// message for keeping the hand over the Pascal's code ( only the '69' is
	// important )
	private static final String KEEP_CONTROL_CAN_LOOP_MESSAGE = "69.55.21.23.25.12.11.FF";
	private static final Integer BEGINNING = 0;
	private static final Integer TEST_GSM = 1;
	private static final Integer TEST_GPS = 2;
	private static final Integer TEST_VERIN = 3;
	private static final Integer TEST_IHM = 4;
	private static final Integer TEST_IMU = 5;
	private static final Integer TEST_BRAIN = 6;
	private static final Integer TEST_ODO = 7;
	private static final Integer FIN = 20;
	private static Integer stateIn = 0;
	private static boolean clickOnScreen = false;

	/**
	 * @return the lock
	 */
	public Object getLock() {
		return lock;
	}

	Handler handler = new Handler();
	Boolean reading = false;
	CanParser canParser = new CanParser();
	Runnable runnable = new Runnable() {
		public void run() {
			test_protocol_loop();
		}
	};
	private CanParserThread canParserThread;
	private Integer state;
	private boolean is_test_gsm_good;
	private boolean is_test_verin_good;
	private boolean is_test_tension_good;
	private Integer pos2;
	private Integer pos1;
	private Integer pos3;
	protected boolean is_test_son_good;
	private Button go_button;
	private String memoryKeyboardState;
	private boolean is_test_ihm_good;
	private boolean is_test_imu_good;
	private boolean is_test_gps_good;
	private boolean is_test_brain_good;
	private int cptStateGps;
	private boolean is_test_odo_good;
	private int cptStateOnClick;
	private LinearLayout layout_result;
	private Bitmap check;
	private Bitmap cross;
	private ImageView im_brain;
	private ImageView im_gps;
	private ImageView im_gsm;
	private ImageView im_ihm;
	private ImageView im_imu;
	private ImageView im_odo;
	private ImageView im_son;
	private ImageView im_tension;
	private ImageView im_verin;
	private TextView reset_button;
	private TextView go_text;
	private TextView test_text;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main_activity);

		canDumpThread = new CanDumpThread();
		canParserThread = new CanParserThread(canDumpThread, this);
		reading = false;
		cptStateGps = 0;
		indexDebug = 0;
		canParser = new CanParser();

		// text/button that will be changed ( visible or invisible ) in the
		// protocol test
		layout_result = (LinearLayout) findViewById(R.id.layout_result);
		go_button = (Button) findViewById(R.id.button_connect_main_activity);
		go_text = (TextView) findViewById(R.id.txt_appuyer);
		test_text = (TextView) findViewById(R.id.txt_test);
		reset_button = (TextView) findViewById(R.id.button_reset_main_activity);
		reset_button.setVisibility(View.GONE);
		test_text.setVisibility(View.GONE);
		layout_result.setVisibility(View.GONE);

		// check and cross image changed to bitmap
		check = BitmapFactory.decodeResource(this.getResources(),
				R.drawable.vcheck);
		cross = BitmapFactory.decodeResource(this.getResources(),
				R.drawable.cross2);

		// check image for all the module
		im_brain = (ImageView) findViewById(R.id.im_brain);
		im_gps = (ImageView) findViewById(R.id.im_gps);
		im_gsm = (ImageView) findViewById(R.id.im_gsm);
		im_ihm = (ImageView) findViewById(R.id.im_ihm);
		im_imu = (ImageView) findViewById(R.id.im_imu);
		im_odo = (ImageView) findViewById(R.id.im_odo);
		im_son = (ImageView) findViewById(R.id.im_son);
		im_tension = (ImageView) findViewById(R.id.im_tension);
		im_verin = (ImageView) findViewById(R.id.im_verin);
		state = 0;
		executeCommand("su -c mount -o rw,remount /");
		File file = new File("/sbin/candump");
		executeCommand("su -c mount -o ro,remount /");
		if (!file.exists()) {
			executeCommand("su -c mount -o rw,remount /");
			executeCommand("su -c cp /storage/sdcard0/candump2 /sbin/candump");
			executeCommand("su -c cp /storage/sdcard0/cansend2 /sbin/cansend");
			executeCommand("su -c chmod 775 /sbin/candump");
			executeCommand("su -c chmod 775 /sbin/cansend");
			executeCommand("su -c insmod /storage/sdcard0/drive/can.ko");
			executeCommand("su -c insmod /storage/sdcard0/drive/can-dev.ko");
			executeCommand("su -c insmod /storage/sdcard0/drive/can-raw.ko");
			executeCommand("su -c insmod /storage/sdcard0/drive/can-bcm.ko");
			executeCommand("su -c insmod /storage/sdcard0/drive/pcan.ko");
			executeCommand("su -c insmod /storage/sdcard0/drive/vcan.ko");
			executeCommand("su -c insmod /storage/sdcard0/drive/peak_usb.ko");
			executeCommand("su -c rmmod pcan");
			executeCommand("su -c mount -o ro,remount /");

			new AlertDialog.Builder(this)
					.setTitle("Information")
					.setMessage(
							"Vous pouvez brancher dès à présent l'interface can usb, si elle est déjà branché, rebranchez la seulement si c'est la première fois que vous lancez l'application depuis le démarrage de la tablette.\n"
									+ "Et assurez vous bien que le robot soit allumé ( il affiche 'mode : binage ' ).")
					.setPositiveButton(android.R.string.yes,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
								}
							}).setIcon(android.R.drawable.ic_dialog_info)
					.show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			final Dialog dialog = new Dialog(this);

			dialog.setContentView(R.layout.info_dialog);
			dialog.setTitle(getString(R.string.info_title));

			Button dialogButton = (Button) dialog
					.findViewById(R.id.dialogButtonOK);
			dialogButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});

			dialog.show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void button_go_clicked(View v) {
		if (button_connect_clicked(v).contains("can0")) {
			Toast.makeText(
					this,
					"L'interface CAN n'est pas branchée ou n'est pas allumée",
					Toast.LENGTH_LONG).show();
			try {
				Thread.sleep(400);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}
		button_read_clicked(v);
	}

	/**
	 * Action performed by the READ button. Run the runnable in 1 ms ( that will
	 * be run in a loop ) This runnable going to read the FIFO filled by a
	 * candump
	 * 
	 * @param v
	 */
	public void button_read_clicked(View v) {
		if (!reading) {
			// the sleep here is for avoid the user to press the button
			// multi-time before it changes its state
			try {
				Thread.sleep(400);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			canDumpThread = new CanDumpThread();
			canParserThread = new CanParserThread(canDumpThread);
			canDumpThread
					.setCmd("su -c /sbin/candump -tz -e can0,0:0,#FFFFFFFF");
			canDumpThread.start();
			canParserThread.start();
			cansend("00F", KEEP_CONTROL_CAN_LOOP_MESSAGE);
			handler.postDelayed(runnable, 1);
			reading = true;
			return;
		}
		reading = false;
		canDumpThread.quit();
		canParserThread.setStop(false);
		canDumpThread.interrupt();
		canParserThread.interrupt();
		go_button.setVisibility(View.VISIBLE);
		go_text.setVisibility(View.VISIBLE);
		test_text.setVisibility(View.GONE);
		layout_result.setVisibility(View.GONE);
		reset_button.setVisibility(View.GONE);
		state = BEGINNING;
		stateIn = 0;
		cptStateGps = 0;
		cptStateOnClick = 0;
		handler.removeCallbacks(runnable);
		// the sleep here is just because there is a sleep when the user press
		// the READ button, so do the STOP.
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onPause() {
		// we stop the application when onPause because an other app using the
		// can could be running
		super.onPause();
		if (reading) {
			reading = false;
			canDumpThread.quit();
			canParserThread.setStop(false);
			canDumpThread.interrupt();
			canParserThread.interrupt();
			canSendThread.interrupt();
			handler.removeCallbacks(runnable);
		}
	}

	@Override
	public void onBackPressed() {
		// When back is pressed, we stop all the thread and hope it's really the
		// case
		super.onBackPressed();
		if (reading) {
			reading = false;
			canDumpThread.quit();
			canParserThread.setStop(false);
			canDumpThread.interrupt();
			canParserThread.interrupt();
			canSendThread.interrupt();
			handler.removeCallbacks(runnable);
		}
	}

	/**
	 * Action performed by the CONNECT button. Mount the can interface with a
	 * bitrate of 1000K
	 * 
	 * @param v
	 */
	public String button_connect_clicked(View v) {
		return executeCommand("su -c ip link set can0 up type can bitrate 1000000");
	}

	public void disconnect_can() {
		executeCommand("su -c ip link set can0 down");
	}

	/**
	 * Function call by the runnable, it read the FIFO of CanDumpThread with the
	 * get100Poll function ( which extract 100 values dumped or less if there is
	 * not 100 values ) and parse the data with CanParser to finally call the
	 * action method of the CanFrame class instantiate by the CanParser.
	 */
	private void test_protocol_loop() {
		if (state == BEGINNING) {
			init_protocol();
		} else if (state == TEST_GSM) {
			test_gsm();
		} else if (state == TEST_VERIN) {
			test_verin();
		} else if (state == TEST_IHM) {
			test_ihm();
		} else if (state == TEST_IMU) {
			test_imu();
		} else if (state == TEST_GPS) {
			test_gps();
		} else if (state == TEST_BRAIN) {
			test_brain();
		} else if (state == TEST_ODO) {
			test_odo();
		} else if (state == FIN) {
			display_result();
			handler.removeCallbacks(runnable);
			state = BEGINNING;
			return;
		}

		if (!canParserThread.getCanParser().getErrorcanframe().getError()
				.contentEquals("no error")) {
			button_read_clicked(null);
			disconnect_can();
			Toast.makeText(
					this,
					"can error : \n"
							+ canParserThread.getCanParser().getErrorcanframe()
									.getComplementError(), Toast.LENGTH_LONG)
					.show();
			return;
		}
		keep_control_of_can();
		if (state == TEST_ODO) {
			handler.postDelayed(runnable, 100);
		} else
			handler.postDelayed(runnable, MILLISECONDS_RUNNABLE);
	}

	private void test_brain() {
		// we test if the temperature in the brainCanFrame exists to know if the
		// module works
		test_text.setText("Test Brain");
		if (brainCanFrame.getTemperature() != null) {
			is_test_brain_good = true;
		} else {
			is_test_brain_good = false;
		}
		state = TEST_ODO;
		stateIn = 0;
	}

	private void test_gps() {
		// we test if the gps return a value which contains a "word" we know (
		// GPGLL, GP... ) to know if the module works
		switch (stateIn) {
		case 0:
			test_text.setText("Test GPS");
			String text = "";
			List<Integer> data = new ArrayList<Integer>();
			data.addAll(canParserThread.getCanParser().getGpscanframe()
					.getGpsData());
			for (int a : data) {
				text += (char) a;
			}
			String[] gps = text.split(",");

			if (gps[0].contains("GLL")) {
				is_test_gps_good = true;
				stateIn++;
			} else if (gps[0].contains("GSV")) {
				is_test_gps_good = true;
				stateIn++;
			} else {
				cptStateGps++;
				stateIn = 0;
			}
			if (cptStateGps > 10) {
				stateIn++;
				is_test_gps_good = false;
			}
			break;
		case 1:
			state = TEST_BRAIN;
			stateIn = 0;
			cptStateGps = 0;
			break;
		}
	}

	private void test_imu() {
		// we test if the imu return a value of the gyro superior to 1 or
		// inferior to -1 to know if the module works
		switch (stateIn) {
		case 0:
			test_text
					.setText("Vous avez 10 secondes pour faire bouger la centrale inertielle");
			stateIn++;
			break;
		case 1:
		case 2:
		case 3:
		case 4:
		case 5:
		case 6:
		case 7:
		case 8:
		case 9:
			if (imuCanFrame.getGyroX() >= 1 || imuCanFrame.getGyroX() <= -1) {
				is_test_imu_good = true;
				stateIn = 0;
				state = TEST_GPS;
				break;
			}
			stateIn++;
			break;

		case 10:
			if (imuCanFrame.getGyroX() >= 1 || imuCanFrame.getGyroX() <= -1) {
				is_test_imu_good = true;
			} else {
				is_test_imu_good = false;
			}
			stateIn = 0;
			state = TEST_GPS;
			break;
		}
	}

	private void test_odo() {
		// we test if the counter of step of the odo of the front wheel is the
		// same ( +- 3) as the rear wheel, when the counter is above 15
		switch (stateIn) {
		case 0:
			VerinCanFrame.resetCpt();
			test_text
					.setText("Vous avez 30 secondes pour faire avancer/reculer le robot sur 3 metres");
			stateIn++;
			break;
		default:
			if (stateIn > 300) {
				if (verinCanFrame.getOdom())
					is_test_odo_good = true;
				else
					is_test_odo_good = false;
				state = FIN;
				stateIn = 0;
				break;
			} else {
				if (verinCanFrame.getOdom()) {
					is_test_odo_good = true;
					state = FIN;
					stateIn = 0;
				}
				stateIn++;
				break;
			}
		}
	}

	private void test_ihm() {
		// we make a sound with the IHM and ask for the user to answer if he
		// hear the sound.
		// And we ask him to maintain a button pushed until test is passed.
		switch (stateIn) {
		case 0:
			test_text.setText("Test IHM");
			cansend("383", "02.10.10.10.02.32");
			stateIn++;
			cptStateOnClick = 0;
			break;
		case 1:
			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case DialogInterface.BUTTON_POSITIVE:
						is_test_son_good = true;
						break;

					case DialogInterface.BUTTON_NEGATIVE:
						is_test_son_good = false;
						break;
					}
					clickOnScreen = true;
				}
			};

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Avez vous entendu un son (*bip* *bip* *bip* )?")
					.setPositiveButton("Oui", dialogClickListener)
					.setNegativeButton("Non", dialogClickListener).show();

			stateIn = 20;
			break;
		case 20:
			cptStateOnClick++;
			if (clickOnScreen)
				stateIn = 2;
			if (cptStateOnClick >= 10) {
				cptStateOnClick = 0;
				stateIn = 1;
			}
			break;
		case 2:
			test_text
					.setText("Vous avez 10 secondes pour maintenir appuyé un bouton de l'ihm");
			memoryKeyboardState = ihmCanFrame.getDataKeyboard();
			stateIn++;
			break;
		case 3:
		case 4:
		case 5:
		case 6:
		case 7:
		case 8:
		case 9:
		case 10:
		case 11:
		case 12:
			if (!ihmCanFrame.getDataKeyboard().contains(memoryKeyboardState)) {
				is_test_ihm_good = true;
				stateIn = 0;
				state = TEST_IMU;
				break;
			}
			stateIn++;
			break;

		case 13:
			if (!ihmCanFrame.getDataKeyboard().contains(memoryKeyboardState)) {
				is_test_ihm_good = true;
			} else {
				is_test_ihm_good = false;
			}
			stateIn = 0;
			state = TEST_IMU;
			break;
		}
	}

	private void test_verin() {
		// we make move the actuator 2 times and compare the position low with
		// the position high
		switch (stateIn) {
		case 0:
			test_text.setText("Test Verin");
			cansend("400", "32");
			stateIn++;
			break;
		case 1:
			stateIn++;
			break;
		case 2:
			retour_position_verin(null);
			pos1 = verinCanFrame.getRetourPosition();
			cansend("400", "00");
			stateIn++;
			break;
		case 3:
			stateIn++;
			break;
		case 4:
			retour_position_verin(null);
			pos2 = verinCanFrame.getRetourPosition();
			cansend("400", "32");
			stateIn++;
			break;
		case 5:
			stateIn++;
			break;
		case 6:
			retour_position_verin(null);
			pos3 = verinCanFrame.getRetourPosition();
			if (pos1 != pos2 && pos2 != pos3) {
				is_test_verin_good = true;
			} else
				is_test_verin_good = false;
			cansend("400", "00");
			cansend("406", "R");
			cansend("407", "R");
			stateIn++;
			break;
		case 7:

			if (verinCanFrame.getTension24v() > 20) {
				is_test_tension_good = true;
			} else
				is_test_tension_good = false;
			state = TEST_IHM;
			stateIn = 0;
			break;
		}
	}

	private void test_gsm() {
		// we send AT+CPIN? to the GSM, if the answer is READY or SIM PIN, the
		// test passed.
		switch (stateIn) {
		case 0:
			test_text.setText("Test GSM");
			statut_gsm(null);
			stateIn++;
			break;
		case 1:
			stateIn++;
			break;
		case 2:
			if (gsmCanFrame.getGsmData().contains("READY")
					|| gsmCanFrame.getGsmData().contains("SIM")
					|| gsmCanFrame.getGsmData().contains("ERROR")) {
				is_test_gsm_good = true;
			} else
				is_test_gsm_good = false;
			state = TEST_VERIN;
			stateIn = 0;
			break;
		}
	}

	private void init_protocol() {
		// initialisation of the variables
		test_text.setText("Initialisation des tests");
		go_button.setVisibility(View.GONE);
		go_text.setVisibility(View.GONE);
		test_text.setVisibility(View.VISIBLE);
		imuCanFrame = canParserThread.getCanParser().getImucanframe();
		gsmCanFrame = canParserThread.getCanParser().getGsmcanframe();
		verinCanFrame = canParserThread.getCanParser().getVerincanframe();
		ihmCanFrame = canParserThread.getCanParser().getIhmcanframe();
		brainCanFrame = canParserThread.getCanParser().getBraincanframe();
		state = TEST_GSM;
		clickOnScreen = false;
		cptStateGps = 0;
		stateIn = 0;
	}

	private void display_result() {
		// we display the result putting bitmaps in the corresponding imageView
		test_text.setVisibility(View.GONE);
		layout_result.setVisibility(View.VISIBLE);
		reset_button.setVisibility(View.VISIBLE);
		if (is_test_brain_good)
			im_brain.setImageBitmap(check);
		else
			im_brain.setImageBitmap(cross);

		if (is_test_gps_good)
			im_gps.setImageBitmap(check);
		else
			im_gps.setImageBitmap(cross);

		if (is_test_gsm_good)
			im_gsm.setImageBitmap(check);
		else
			im_gsm.setImageBitmap(cross);

		if (is_test_ihm_good)
			im_ihm.setImageBitmap(check);
		else
			im_ihm.setImageBitmap(cross);

		if (is_test_imu_good)
			im_imu.setImageBitmap(check);
		else
			im_imu.setImageBitmap(cross);

		if (is_test_son_good)
			im_son.setImageBitmap(check);
		else
			im_son.setImageBitmap(cross);

		if (is_test_tension_good)
			im_tension.setImageBitmap(check);
		else
			im_tension.setImageBitmap(cross);

		if (is_test_verin_good)
			im_verin.setImageBitmap(check);
		else
			im_verin.setImageBitmap(cross);

		if (is_test_odo_good)
			im_odo.setImageBitmap(check);
		else
			im_odo.setImageBitmap(cross);
	}

	/**
	 * send a message on the can which disable other programs to send something
	 * on the can
	 */
	private void keep_control_of_can() {
		indexDebug++;
		if (indexDebug == KEEP_CONTROL_CAN_LOOP) {
			cansend("00F", KEEP_CONTROL_CAN_LOOP_MESSAGE);
			indexDebug = 0;
		}
	}

	/**
	 * Execute a command in a shell
	 * 
	 * @param command
	 * 
	 * @return
	 */
	private String executeCommand(String command) {
		// Only use by the CONNECT button
		StringBuffer output = new StringBuffer();
		Process p;
		String line = "";
		String answer = "";
		try {
			p = Runtime.getRuntime().exec(command);

			BufferedReader stdError = new BufferedReader(new InputStreamReader(
					p.getErrorStream()));

			String s = null;

			// read any errors from the attempted command
			while ((s = stdError.readLine()) != null) {
				answer += s;
			}
			p.waitFor();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return answer;
	}

	/**
	 * Check if access is granted to the sim card ( response : READY )
	 * 
	 * @param v
	 * 
	 * 
	 */
	public void statut_gsm(View v) {
		cansend_gsm("AT+CPIN?\r");
	}

	public void retour_position_verin(View v) {
		cansend("401", "R");
	}

	/**
	 * Function specific for sending GSM trame on the can ( all char one by one
	 * )
	 * 
	 * @param command
	 */
	private void cansend_gsm(String command) {
		canParser.setGsmcanframe(new GSMCanFrame());
		if (canSendThread != null) {
			canSendThread = null;
		}
		canSendThread = new CanSendThread();
		canSendThread.addStringCommandForGSM("281", command);
		canSendThread.start();
	}

	/**
	 * Function for send on the can a command ( with the id )
	 * 
	 * @param id
	 * @param command
	 */
	private void cansend(String id, String command) {
		if (canSendThread != null) {
			canSendThread = null;
		}
		canSendThread = new CanSendThread();
		canSendThread.addStringCommand(id, command);
		canSendThread.start();
	}
}
