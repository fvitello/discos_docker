package com.cosylab.acs.maci.test;

import org.prevayler.implementation.AbstractPrevalentSystem;

public class PrevaylerTest extends AbstractPrevalentSystem {
        private static final long serialVersionUID = 1000011L;
	private int value;
	public PrevaylerTest() {
		this.value = 0;
	}
	public int getValue() {
		return this.value;
	}
	public void setValue(int value) {
		this.value = value;
	}
}
