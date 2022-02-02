package com.cosylab.acs.maci.test;

import java.io.File;
import java.util.Properties;

import com.cosylab.util.FileHelper;

import junit.framework.TestCase;

import org.prevayler.Command;
import org.prevayler.Prevayler;
import org.prevayler.PrevalentSystem;
import org.prevayler.implementation.SnapshotPrevayler;

import java.io.Serializable;

public class PrevaylerSerializationTest extends TestCase {

	private PrevaylerTest test;
	private String recoveryLocation;
	private SnapshotPrevayler prevayler;
	
	public PrevaylerSerializationTest(String arg0) {
		super(arg0);
	}

	protected void setUp() throws Exception {
		super.setUp();
		try {
		this.recoveryLocation = FileHelper.getTempFileName(null, "PrevaylerTest_Recovery");
                this.prevayler = new SnapshotPrevayler(new PrevaylerTest(), recoveryLocation);
		this.test = (PrevaylerTest)this.prevayler.system();
		this.test.setValue(1);
		this.prevayler.takeSnapshot();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public PrevalentSystem deserialize(PrevalentSystem system) {
		SnapshotPrevayler prevayler = null;
		try {
			prevayler = new SnapshotPrevayler(system, recoveryLocation);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		return prevayler.system();
	}

	public void testSerializationBehavior() throws Exception {
		assertEquals(1, this.test.getValue());
                PrevaylerTest ntest = new PrevaylerTest();
		assertEquals(0, ntest.getValue());
		ntest = (PrevaylerTest) deserialize(new PrevaylerTest());
		assertEquals(1, ntest.getValue());
		this.test.setValue(5);
		ntest = (PrevaylerTest) deserialize(new PrevaylerTest());
		assertEquals(1, ntest.getValue());
		this.prevayler.takeSnapshot();
		ntest = (PrevaylerTest) deserialize(new PrevaylerTest());
		assertEquals(5, ntest.getValue());
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(PrevaylerSerializationTest.class);
		System.exit(0);
	}
}
