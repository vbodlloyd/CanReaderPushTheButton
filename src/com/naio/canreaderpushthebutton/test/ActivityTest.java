package com.naio.canreaderpushthebutton.test;

import com.naio.canreaderpushthebutton.activities.MainActivity;
import com.robotium.solo.Solo;

import android.test.ActivityInstrumentationTestCase2;

public class ActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {
		

	private MainActivity mActivity;

	public ActivityTest() {
		super(MainActivity.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		mActivity = getActivity();
	}

	public void testPreconditions() {
	    assertNotNull("mActivity is null", mActivity);
	}
	
	public void testButton(){
		Solo han = new Solo(getInstrumentation(),mActivity);
		han.clickOnButton("OK");
		getInstrumentation().waitForIdleSync();
		han.clickOnButton("Debuter le test");
		getInstrumentation().waitForIdleSync();
		assertTrue("Could not find the toast for debuter le test!", han.searchText("CAN"));	
	
	}
}
