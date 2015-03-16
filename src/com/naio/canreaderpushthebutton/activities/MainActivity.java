package com.naio.canreaderpushthebutton.activities;

import java.io.File;
import java.lang.Process;
import java.util.ArrayList;
import java.util.List;

import com.naio.canreaderpushthebutton.R;
import com.naio.canreaderpushthebutton.canframeclasses.BrainCanFrame;
import com.naio.canreaderpushthebutton.canframeclasses.GPSCanFrame;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * 
 * MainActivity call a function every 10 ms when the Read button is pressed,
 * this function read the FIFO filled by CanDumpThread and display it on the
 * screen. Also the button connect allow to mount the can interface.
 * 
 * @author bodereau
 */
public class MainActivity extends FragmentActivity {

	private CanDumpThread canDumpThread;
	private CanSendThread canSendThread;
	private final Object lock = new Object();
	private int indexDebug;
	private GPSCanFrame gpsCanframe;
	private IMUCanFrame imuCanFrame;
	private BrainCanFrame brainCanFrame;
	private IHMCanFrame ihmCanFrame;
	private VerinCanFrame verinCanFrame;
	private GSMCanFrame gsmCanFrame;
	private static final int MILLISECONDS_RUNNABLE = 1000;

	// 50 * MILLISECONDS_RUNNABLE for re send the keep control message
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
		layout_result = (LinearLayout) findViewById(R.id.layout_result);
		layout_result.setVisibility(View.GONE);
		go_button = (Button) findViewById(R.id.button_connect_main_activity);
		go_text = (TextView) findViewById(R.id.txt_appuyer);
		test_text = (TextView) findViewById(R.id.txt_test);
		reset_button = (TextView) findViewById(R.id.button_reset_main_activity);
		reset_button.setVisibility(View.GONE);
		test_text.setVisibility(View.GONE);
		check = BitmapFactory.decodeResource(this.getResources(),
				R.drawable.vcheck);
		cross = BitmapFactory.decodeResource(this.getResources(),
				R.drawable.cross2);
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
							"Vous pouvez brancher dès à présent l'interface can usb, si elle est déjà branché, rebranchez la.\n" +
							"Et assurez vous bien que le robot soit allumé ( il affiche 'mode : binage ' ) et que l'interface can soit allumée avant d'appuyer sur le bouton.")
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
			dialog.setTitle("INFO");

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
		button_connect_clicked(v);
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
			canDumpThread.setCmd("su -c /sbin/candump -tz can0");
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
		// go_button.setText("Appuyer sur ce bouton une fois vous êtes assuré d'avoir une manette connectée");
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
		onBackPressed();
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
	public void button_connect_clicked(View v) {
		executeCommand("su -c ip link set can0 up type can bitrate 1000000");
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
		keep_control_of_can();
		if(state == TEST_ODO){
			handler.postDelayed(runnable, 100);
		}else
			handler.postDelayed(runnable, MILLISECONDS_RUNNABLE);
	}

	private void test_brain() {

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
			Log.e("gps", text);
			String[] gps = text.split(",");
			/*
			 * if (gps.length <= 5) { cptStateGps++; if (cptStateGps > 5) {
			 * stateIn++; is_test_gps_good = false; } break; }
			 */
			if (gps[0].contains("GPGLL")) {
				is_test_gps_good = true;
				stateIn++;
			} else if (gps[0].contains("GP")) {
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
		switch (stateIn) {
		case 0:
			test_text
					.setText("Vous avez 10 secondes pour faire bouger la centrale inertielle");
			stateIn++;
			break;
		case 1:
			if (imuCanFrame.getGyroX() >= 1 || imuCanFrame.getGyroX() <= -1) {
				is_test_imu_good = true;
				stateIn = 0;
				state = TEST_GPS;
				break;
			}
			stateIn++;
			break;
		case 2:
			if (imuCanFrame.getGyroX() >= 1 || imuCanFrame.getGyroX() <= -1) {
				is_test_imu_good = true;
				stateIn = 0;
				state = TEST_GPS;
				break;
			}
			stateIn++;
			break;
		case 3:
			if (imuCanFrame.getGyroX() >= 1 || imuCanFrame.getGyroX() <= -1) {
				is_test_imu_good = true;
				stateIn = 0;
				state = TEST_GPS;
				break;
			}
			stateIn++;
			break;
		case 4:
			if (imuCanFrame.getGyroX() >= 1 || imuCanFrame.getGyroX() <= -1) {
				is_test_imu_good = true;
				stateIn = 0;
				state = TEST_GPS;
				break;
			}
			stateIn++;
			break;
		case 5:
			if (imuCanFrame.getGyroX() >= 1 || imuCanFrame.getGyroX() <= -1) {
				is_test_imu_good = true;
				stateIn = 0;
				state = TEST_GPS;
				break;
			}
			stateIn++;
			break;
		case 6:
			if (imuCanFrame.getGyroX() >= 1 || imuCanFrame.getGyroX() <= -1) {
				is_test_imu_good = true;
				stateIn = 0;
				state = TEST_GPS;
				break;
			}
			stateIn++;
			break;
		case 7:
			if (imuCanFrame.getGyroX() >= 1 || imuCanFrame.getGyroX() <= -1) {
				is_test_imu_good = true;
				stateIn = 0;
				state = TEST_GPS;
				break;
			}
			stateIn++;
			break;
		case 8:
			if (imuCanFrame.getGyroX() >= 1 || imuCanFrame.getGyroX() <= -1) {
				is_test_imu_good = true;
				stateIn = 0;
				state = TEST_GPS;
				break;
			}
			stateIn++;
			break;
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
		switch (stateIn) {
		case 0:
			VerinCanFrame.resetCpt();
			test_text
					.setText("Vous avez 30 secondes pour faire avancer/reculer le robot sur 2 metres");
			stateIn++;
			break;
		default:
			if(stateIn > 300){
				if (verinCanFrame.getOdom())
					is_test_odo_good = true;
				else
					is_test_odo_good = false;
				state = FIN;
				stateIn = 0;
				break;
			}else{
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
					.setText("Vous avez 10 secondes pour appuyer, en le maintenant, sur un bouton de l'ihm");
			memoryKeyboardState = ihmCanFrame.getDataKeyboard();
			stateIn++;
			break;
		case 3:
			if (!ihmCanFrame.getDataKeyboard().contains(memoryKeyboardState)) {
				is_test_ihm_good = true;
				stateIn = 0;
				state = TEST_IMU;
				break;
			}
			stateIn++;
			break;
		case 4:
			if (!ihmCanFrame.getDataKeyboard().contains(memoryKeyboardState)) {
				is_test_ihm_good = true;
				stateIn = 0;
				state = TEST_IMU;
				break;
			}
			stateIn++;
			break;
		case 5:
			if (!ihmCanFrame.getDataKeyboard().contains(memoryKeyboardState)) {
				is_test_ihm_good = true;
				stateIn = 0;
				state = TEST_IMU;
				break;
			}
			stateIn++;
			break;
		case 6:
			if (!ihmCanFrame.getDataKeyboard().contains(memoryKeyboardState)) {
				is_test_ihm_good = true;
				stateIn = 0;
				state = TEST_IMU;
				break;
			}
			stateIn++;
			break;
		case 7:
			if (!ihmCanFrame.getDataKeyboard().contains(memoryKeyboardState)) {
				is_test_ihm_good = true;
				stateIn = 0;
				state = TEST_IMU;
				break;
			}
			stateIn++;
			break;
		case 8:
			if (!ihmCanFrame.getDataKeyboard().contains(memoryKeyboardState)) {
				is_test_ihm_good = true;
				stateIn = 0;
				state = TEST_IMU;
				break;
			}
			stateIn++;
			break;
		case 9:
			if (!ihmCanFrame.getDataKeyboard().contains(memoryKeyboardState)) {
				is_test_ihm_good = true;
				stateIn = 0;
				state = TEST_IMU;
				break;
			}
			stateIn++;
			break;
		case 10:
			if (!ihmCanFrame.getDataKeyboard().contains(memoryKeyboardState)) {
				is_test_ihm_good = true;
				stateIn = 0;
				state = TEST_IMU;
				break;
			}
			stateIn++;
			break;
		case 11:
			if (!ihmCanFrame.getDataKeyboard().contains(memoryKeyboardState)) {
				is_test_ihm_good = true;
				stateIn = 0;
				state = TEST_IMU;
				break;
			}
			stateIn++;
			break;
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
			button_retour_position_clicked(null);
			pos1 = verinCanFrame.getRetourPosition();
			cansend("400", "00");
			stateIn++;
			break;
		case 3:
			stateIn++;
			break;
		case 4:
			button_retour_position_clicked(null);
			pos2 = verinCanFrame.getRetourPosition();
			cansend("400", "32");
			stateIn++;
			break;
		case 5:
			stateIn++;
			break;
		case 6:
			button_retour_position_clicked(null);
			pos3 = verinCanFrame.getRetourPosition();
			Log.e("posverin", pos1 + "--" + pos2 + "---" + pos3 + "---"
					+ (pos1 != pos2) + "---" + (pos2 != pos3) + "---"
					+ (false && false));
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
		switch (stateIn) {
		case 0:
			test_text.setText("Test GSM");
			button_statut_gsm_clicked(null);
			stateIn++;
			break;
		case 1:
			stateIn++;
			break;
		case 2:
			if (gsmCanFrame.getGsmData().contains("READY")
					|| gsmCanFrame.getGsmData().contains("PIN")) {
				is_test_gsm_good = true;
			} else
				is_test_gsm_good = false;
			state = TEST_VERIN;
			stateIn = 0;
			break;
		}
	}

	private void init_protocol() {
		test_text.setText("Initialisation des tests");
		go_button.setVisibility(View.GONE);
		go_text.setVisibility(View.GONE);
		test_text.setVisibility(View.VISIBLE);
		gpsCanframe = canParserThread.getCanParser().getGpscanframe();
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
		try {
			p = Runtime.getRuntime().exec(command);
			p.waitFor();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return output.toString();
	}

	public void button_send_gsm_clicked(View v) {
		// create a Dialog component
		final Dialog dialog = new Dialog(this);

		dialog.setContentView(R.layout.send_sms_dialog);
		dialog.setTitle("Send a sms");

		final EditText editNumero = (EditText) dialog
				.findViewById(R.id.edittext_numero);
		final EditText editMessage = (EditText) dialog
				.findViewById(R.id.edittext_message);

		Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
		dialogButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				cansend_gsm("AT+CMGS=\"" + editNumero.getText().toString()
						+ "\"\r" + editMessage.getText().toString() + "\u001A");
				dialog.dismiss();

			}
		});
		dialog.show();
	}

	/**
	 * Read the unread sms and only the unread ones
	 * 
	 * @param v
	 * 
	 * 
	 */
	public void button_receive_gsm_clicked(View v) {
		cansend_gsm("AT+CMGL=\"REC UNREAD\"\r");
	}

	/**
	 * Config the sim card in GSM mode ( default mode is IRA )
	 * 
	 * @param v
	 * 
	 * 
	 */
	public void button_config_gsm_clicked(View v) {
		cansend_gsm("AT+CSCS=\"GSM\"\r");
	}

	/**
	 * Open a dialog which allows the user to enter his own command
	 * 
	 * @param v
	 * 
	 * 
	 */
	public void button_custom_gsm_clicked(View v) {
		final Dialog dialog = new Dialog(this);

		dialog.setContentView(R.layout.custom_at_command_dialog);
		dialog.setTitle("Custom AT command");

		final EditText editCommand = (EditText) dialog
				.findViewById(R.id.edittext_numero);

		Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
		dialogButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String txt = editCommand.getText().toString();
				cansend_gsm("AT+" + txt + "\r");
				dialog.dismiss();

			}
		});
		dialog.show();
	}

	/**
	 * Config the sim card in txt mode ( default is PDU )
	 * 
	 * @param v
	 * 
	 * 
	 */
	public void button_config2_gsm_clicked(View v) {
		cansend_gsm("AT+CMGF=1\r");
	}

	/**
	 * Check if access is granted to the sim card ( response : READY )
	 * 
	 * @param v
	 * 
	 * 
	 */
	public void button_statut_gsm_clicked(View v) {
		cansend_gsm("AT+CPIN?\r");
	}

	/**
	 * Display a dialog asking for the PIN of the sim card
	 * 
	 * @param v
	 * 
	 */
	public void button_config_pin_clicked(View v) {
		// create a Dialog component
		final Dialog dialog = new Dialog(this);

		dialog.setContentView(R.layout.enter_pin_dialog);
		dialog.setTitle("Pin");

		final EditText pinCodeTextView = (EditText) dialog
				.findViewById(R.id.edittext_numero);

		Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
		dialogButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String pinCode = "";

				if (!pinCodeTextView.getText().toString().isEmpty()) {
					pinCode += pinCodeTextView.getText().toString();
				}
				cansend_gsm("AT+CPIN=\"" + pinCode + "\"\r");
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	/**
	 * Display a dialog to set active or not the LEDs and their colors
	 * 
	 * @param v
	 * 
	 * 
	 */
	public void button_etat_led_clicked(View v) {
		// create a Dialog component
		final Dialog dialog = new Dialog(this);

		dialog.setContentView(R.layout.config_led_dialog);
		dialog.setTitle("LED");

		final Spinner spinnerGauche = (Spinner) dialog
				.findViewById(R.id.spinner_led_gauche);
		final Spinner spinner2 = (Spinner) dialog
				.findViewById(R.id.spinner_led_2);
		final Spinner spinner3 = (Spinner) dialog
				.findViewById(R.id.spinner_led_3);
		final Spinner spinnerDroite = (Spinner) dialog
				.findViewById(R.id.spinner_led_droite);
		final Spinner spinnerCGauche = (Spinner) dialog
				.findViewById(R.id.spinner_led_gauche_couleur);
		final Spinner spinnerC2 = (Spinner) dialog
				.findViewById(R.id.spinner_led_2_couleur);
		final Spinner spinnerC3 = (Spinner) dialog
				.findViewById(R.id.spinner_led_3_couleur);
		final Spinner spinnerCDroite = (Spinner) dialog
				.findViewById(R.id.spinner_led_droite_couleur);

		Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
		dialogButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String binary_led = "0000"
						+ spinnerDroite.getSelectedItem().toString()
						+ spinner3.getSelectedItem().toString()
						+ spinner2.getSelectedItem().toString()
						+ spinnerGauche.getSelectedItem().toString();

				binary_led = String.format("%02X",
						Long.parseLong(binary_led, 2));
				String binary_colors = "0000"
						+ spinnerCDroite.getSelectedItemPosition()
						+ spinnerC3.getSelectedItemPosition()
						+ spinnerC2.getSelectedItemPosition()
						+ spinnerCGauche.getSelectedItemPosition();
				binary_colors = String.format("%02X",
						Long.parseLong(binary_colors, 2));

				canParser.setGsmcanframe(new GSMCanFrame());
				cansend("382", binary_led + binary_colors);
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	public void button_magneto_board_clicked(View v) {
		cansend("18F", "R");
	}

	public void button_magneto_version_clicked(View v) {
		cansend("184", "R");
	}

	public void button_magneto_temperature_clicked(View v) {
		cansend("183", "R");
	}

	public void button_retour_position_clicked(View v) {
		cansend("401", "R");
	}

	public void button_lecture_odo_clicked(View v) {
		VerinCanFrame.resetCpt();
	}

	public void button_version_verin_clicked(View v) {
		cansend("405", "R");
	}

	/**
	 * Display a dialog asking for the command in hexa to send to the IHM or the
	 * text to display on the LCD screen
	 * 
	 * @param v
	 * 
	 */
	public void button_envoie_affichage_clicked(View v) {
		// create a Dialog component
		final Dialog dialog = new Dialog(this);

		dialog.setContentView(R.layout.envoi_commande_ecran);
		dialog.setTitle("Send display command");

		final EditText hexa1 = (EditText) dialog.findViewById(R.id.hexa1);
		final EditText hexa2 = (EditText) dialog.findViewById(R.id.hexa2);
		final EditText hexa3 = (EditText) dialog.findViewById(R.id.hexa3);
		final EditText ecranText = (EditText) dialog
				.findViewById(R.id.commandecran);
		final EditText ecranText2 = (EditText) dialog
				.findViewById(R.id.commandecran2);

		Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
		dialogButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String dataHexa = "";
				if (ecranText.getText().toString().isEmpty()
						&& ecranText2.getText().toString().isEmpty()) {

					if (!hexa1.getText().toString().isEmpty()) {
						dataHexa += hexa1.getText().toString();
					}
					if (!hexa2.getText().toString().isEmpty()) {
						dataHexa += "." + hexa2.getText().toString();
					}
					if (!hexa3.getText().toString().isEmpty()) {
						dataHexa += "." + hexa3.getText().toString();
					}
					cansend("380", dataHexa);
					dialog.dismiss();
				}
				String txtToWrite = ecranText.getText().toString();
				String txtToWrite2 = ecranText2.getText().toString();
				dataHexa += "02";
				int index = 0;
				for (char c : txtToWrite.toCharArray()) {
					cansend("380",
							dataHexa + "." + String.format("%02x", (int) index)
									+ "." + String.format("%02x", (int) c));
					index++;
				}
				index = 40;
				for (char c : txtToWrite2.toCharArray()) {
					cansend("380",
							dataHexa + "." + String.format("%02x", (int) index)
									+ "." + String.format("%02x", (int) c));
					index++;
				}
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	public void button_ihm_status_clicked(View v) {
		cansend("384", "R");
	}

	public void button_version_ihm_clicked(View v) {
		cansend("386", "R");
	}

	public void button_board_ihm_clicked(View v) {
		cansend("38F", "R");
	}

	/**
	 * Display a dialog asking for the command to send to the motor/verin
	 * 
	 * @param v
	 * 
	 * 
	 */
	public void button_commande_moteur_clicked(View v) {
		// create a Dialog component
		final Dialog dialog = new Dialog(this);

		dialog.setContentView(R.layout.envoi_one_command);
		dialog.setTitle("Motor command");

		final EditText hexa1 = (EditText) dialog.findViewById(R.id.hexa1);

		Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
		dialogButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String dataHexa = "";
				if (!hexa1.getText().toString().isEmpty()) {
					dataHexa += hexa1.getText().toString();
				}
				cansend("402", dataHexa);
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	/**
	 * Display a dialog asking for the command to send to the LCD screen ( for
	 * changing the contrast )
	 * 
	 * @param v
	 * 
	 * 
	 */
	public void button_contraste_clicked(View v) {
		// create a Dialog component
		final Dialog dialog = new Dialog(this);

		dialog.setContentView(R.layout.envoi_one_command);
		dialog.setTitle("Enter the contrast in hexa ( 00 to 64)");

		final EditText hexa1 = (EditText) dialog.findViewById(R.id.hexa1);

		Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
		dialogButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String dataHexa = "";
				if (!hexa1.getText().toString().isEmpty()) {
					dataHexa += hexa1.getText().toString();
				}
				cansend("387", dataHexa);
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	/**
	 * Display a dialog asking for the command to send to the LCD screen ( for
	 * changing the backlight )
	 * 
	 * @param v
	 * 
	 * 
	 */
	public void button_backlight_clicked(View v) {
		// create a Dialog component
		final Dialog dialog = new Dialog(this);

		dialog.setContentView(R.layout.envoi_one_command);
		dialog.setTitle("Enter the backlight in hexa ( 00 to 64)");

		final EditText hexa1 = (EditText) dialog.findViewById(R.id.hexa1);

		Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
		dialogButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String dataHexa = "";
				if (!hexa1.getText().toString().isEmpty()) {
					dataHexa += hexa1.getText().toString();
				}
				cansend("388", dataHexa);
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	/**
	 * Display a dialog asking for the command to send verin/motor ( to move the
	 * verin at a position)
	 * 
	 * @param v
	 * 
	 * 
	 */
	public void button_requete_position_clicked(View v) {
		final Dialog dialog = new Dialog(this);

		dialog.setContentView(R.layout.envoi_one_command);
		dialog.setTitle("Req position");

		final EditText hexa1 = (EditText) dialog.findViewById(R.id.hexa1);

		Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
		dialogButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String dataHexa = "";
				if (!hexa1.getText().toString().isEmpty()) {
					dataHexa += hexa1.getText().toString();
				}
				cansend("400", dataHexa);
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	/**
	 * 
	 * Display a dialog which emulate the keyboard of the IHM.
	 * 
	 * @param v
	 * 
	 * 
	 * 
	 */
	public void button_etat_clavier_clicked(View v) {
		final Dialog dialog = new Dialog(this);

		dialog.setContentView(R.layout.clavier_ihm_dialog);
		dialog.setTitle("Clavier");

		Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
		dialogButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		Button valider = (Button) dialog.findViewById(R.id.valider);
		valider.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				cansend("381", "02");
			}
		});
		Button annuler = (Button) dialog.findViewById(R.id.annuler);
		annuler.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				cansend("381", "01");
			}
		});
		Button haut = (Button) dialog.findViewById(R.id.haut);
		haut.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				cansend("381", "04");
			}
		});
		Button bas = (Button) dialog.findViewById(R.id.bas);
		bas.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				cansend("381", "08");
			}
		});
		Button gauche = (Button) dialog.findViewById(R.id.gauche);
		gauche.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				cansend("381", "20");
			}
		});
		Button droit = (Button) dialog.findViewById(R.id.droite);
		droit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				cansend("381", "10");
			}
		});
		dialog.show();
	}

	public void button_tension_clicked(View v) {
		cansend("406", "R");
	}

	public void button_batterie_clicked(View v) {
		cansend("407", "R");
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
