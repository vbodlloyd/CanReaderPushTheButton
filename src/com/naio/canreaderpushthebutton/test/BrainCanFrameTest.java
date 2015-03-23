package com.naio.canreaderpushthebutton.test;

import net.sourceforge.juint.UInt8;

import com.naio.canreaderpushthebutton.activities.MainActivity;
import com.naio.canreaderpushthebutton.parser.CanParser;

import android.test.ActivityInstrumentationTestCase2;

public class BrainCanFrameTest extends ActivityInstrumentationTestCase2<MainActivity> {
	
	
	private CanParser canParser;
	private final String TRAME_TEMPERATURE = "(1215.1251) can0 00E [1] 08";
	
	@SuppressWarnings("deprecation")
	public BrainCanFrameTest() {
		super("com.naio.canreader.activities",MainActivity.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		canParser = new CanParser();
	}
	
	public void testParseTemperature(){
		canParser.parseOneFrame(TRAME_TEMPERATURE);
		assertEquals(canParser.getBraincanframe().getTemperature(), new UInt8(8));
	}

}
