/*
 * Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.8 Datenaufbereitung UFD
 * Copyright (C) 2007-2015 BitCtrl Systems GmbH
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * Contact Information:<br>
 * BitCtrl Systems GmbH<br>
 * Weißenfelser Straße 67<br>
 * 04229 Leipzig<br>
 * Phone: +49 341-490670<br>
 * mailto: info@bitctrl.de
 */
package de.bsvrz.dua.daufd.hysterese;

import org.junit.Assert;

import org.junit.Test;

/**
 * Tests des Moduls Hysterese
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class HystereseTest {
	
	/**
	 * Test der Hysterese mit <code>long</code>-Werten
	 */
	@Test
	public void testGetStufeInteger()
	throws Exception{
		Hysterese hysterese = new Hysterese();
		
		hysterese.initialisiere(new long[]{ 0,  5, 15, 20, 25},
								new long[]{10, 15, 35, 35, 35});

		Assert.assertEquals(hysterese.getStufe(0), 0);
		Assert.assertEquals(hysterese.getStufe(-1), -1);
		Assert.assertEquals(hysterese.getStufe(35), -1);
		Assert.assertEquals(hysterese.getStufe(36), -1);
		Assert.assertEquals(hysterese.getStufe(34), 4);
		
		Assert.assertEquals(hysterese.getStufe(-1), -1);
		Assert.assertEquals(hysterese.getStufe(6), 0);
		Assert.assertEquals(hysterese.getStufe(9), 0);
		Assert.assertEquals(hysterese.getStufe(0), 0);
		Assert.assertEquals(hysterese.getStufe(10), 1);
		
		Assert.assertEquals(hysterese.getStufe(5), 1);
		Assert.assertEquals(hysterese.getStufe(4), 0);
		Assert.assertEquals(hysterese.getStufe(10), 1);
		Assert.assertEquals(hysterese.getStufe(5), 1);
		Assert.assertEquals(hysterese.getStufe(14), 1);
		Assert.assertEquals(hysterese.getStufe(15), 2);
		
		Assert.assertEquals(hysterese.getStufe(4), 0);
		Assert.assertEquals(hysterese.getStufe(31), 4);
		Assert.assertEquals(hysterese.getStufe(25), 4);
		Assert.assertEquals(hysterese.getStufe(19), 2);
		
		Assert.assertEquals(hysterese.getStufe(4), 0);
		Assert.assertEquals(hysterese.getStufe(29), 4);
		
		Assert.assertEquals(hysterese.getStufe(4), 0);
		Assert.assertEquals(hysterese.getStufe(25), 2);
		Assert.assertEquals(hysterese.getStufe(4), 0);
		Assert.assertEquals(hysterese.getStufe(28), 3);
		Assert.assertEquals(hysterese.getStufe(4), 0);
		Assert.assertEquals(hysterese.getStufe(30), 4);

		Assert.assertEquals(hysterese.getStufe(14), 1);
		Assert.assertEquals(hysterese.getStufe(30), 4);
		Assert.assertEquals(hysterese.getStufe(14), 1);
		Assert.assertEquals(hysterese.getStufe(28), 3);
		Assert.assertEquals(hysterese.getStufe(21), 3);
		Assert.assertEquals(hysterese.getStufe(30), 3);		
		Assert.assertEquals(hysterese.getStufe(14), 1);
		Assert.assertEquals(hysterese.getStufe(30), 4);
		
	}

	
	/**
	 * Test der Hysterese mit <code>double</code>-Werten
	 */
	@Test
	public void testGetStufeDouble()
	throws Exception{
		Hysterese hysterese = new Hysterese();
		
		hysterese.initialisiere(new double[]{0.0, 0.5},
								new double[]{1.0, 1.5});

		Assert.assertEquals(hysterese.getStufe(0), 0);
		Assert.assertEquals(hysterese.getStufe(-1), -1);
		Assert.assertEquals(hysterese.getStufe(2), -1);
		Assert.assertEquals(hysterese.getStufe(-0.00001), -1);
		Assert.assertEquals(hysterese.getStufe(1.5000), -1);
		
		Assert.assertEquals(hysterese.getStufe(1), 1);
		Assert.assertEquals(hysterese.getStufe(0.5), 1);
		Assert.assertEquals(hysterese.getStufe(0.4999), 0);
		Assert.assertEquals(hysterese.getStufe(0.0), 0);
		Assert.assertEquals(hysterese.getStufe(0.9999), 0);		
		Assert.assertEquals(hysterese.getStufe(1.0), 1);
		Assert.assertEquals(hysterese.getStufe(0.9999), 1);
		
		Assert.assertEquals(hysterese.getStufe(1.5), -1);
		Assert.assertEquals(hysterese.getStufe(0.95), 1);
		Assert.assertEquals(hysterese.getStufe(0.99), 1);
		
		Assert.assertEquals(hysterese.getStufe(1.6), -1);
		Assert.assertEquals(hysterese.getStufe(0.6), 0);
		Assert.assertEquals(hysterese.getStufe(0.4), 0);
		Assert.assertEquals(hysterese.getStufe(1.001), 1);
		Assert.assertEquals(hysterese.getStufe(0.49), 0);
		
	}

}
