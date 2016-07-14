/*
 * Copyright 2015 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.dua.daufd.tests.
 * 
 * de.bsvrz.dua.daufd.tests is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dua.daufd.tests is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.dua.daufd.tests.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.dua.daufd.tests.stufenaesse;

import com.google.common.collect.ImmutableTable;
import de.bsvrz.dua.daufd.stufenaesse.NaesseStufe;
import de.bsvrz.dua.daufd.stufenaesse.NaesseStufe.NS_Stufe;
import de.bsvrz.dua.daufd.stufewfd.WasserFilmDickeStufe.WFD_Stufe;
import de.bsvrz.dua.daufd.vew.FBZ_Klasse;
import de.bsvrz.dua.daufd.vew.VerwaltungAufbereitungUFD;
import de.bsvrz.dua.tests.DuATestBase;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IBearbeitungsKnoten;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Comparator;
import java.util.Map;

import static de.bsvrz.dua.daufd.stufeni.NiederschlagIntensitaetStufe.NI_Stufe;

/**
 * TBD Dokumentation
 *
 * @author Kappich Systemberatung
 */
public class TestDaUfdNaesseStufenArgument extends DuATestBase {

	private MyVerwaltungAufbereitungUFD _daUfd;

	@Override
	protected String[] getConfigurationAreas() {
		return new String[]{"kb.duaTestUfd"};
	}
	
	@Test
	public void testDuaUfdWithStufen1() throws Exception {
		ImmutableTable<WFD_Stufe, NI_Stufe, NS_Stufe> table = startTest("stufen.txt", FBZ_Klasse.Regen, "konfigurationNS");
		Assert.assertEquals("{WFD_STUFE0={NI_STUFE0=NS_TROCKEN, NI_STUFE1=NS_TROCKEN, NI_STUFE2=NS_NASS1, NI_STUFE3=NS_NASS2, NI_STUFE4=NS_NASS2, NI_WERT_NV=NS_TROCKEN}, WFD_STUFE1={NI_STUFE0=NS_NASS1, NI_STUFE1=NS_NASS1, NI_STUFE2=NS_NASS2, NI_STUFE3=NS_NASS3, NI_STUFE4=NS_NASS4, NI_WERT_NV=NS_NASS1}, WFD_STUFE2={NI_STUFE0=NS_NASS2, NI_STUFE1=NS_NASS2, NI_STUFE2=NS_NASS2, NI_STUFE3=NS_NASS3, NI_STUFE4=NS_NASS4, NI_WERT_NV=NS_NASS2}, WFD_STUFE3={NI_STUFE0=NS_NASS2, NI_STUFE1=NS_NASS2, NI_STUFE2=NS_NASS3, NI_STUFE3=NS_NASS3, NI_STUFE4=NS_NASS4, NI_WERT_NV=NS_NASS3}, WFD_WERT_NV={NI_STUFE0=NS_TROCKEN, NI_STUFE1=NS_TROCKEN, NI_STUFE2=NS_NASS1, NI_STUFE3=NS_NASS2, NI_STUFE4=NS_NASS3, NI_WERT_NV=NS_WERT_NE}}"
		,table.toString());
	}	
	@Test
	public void testDuaUfdWithStufen2() throws Exception {
		ImmutableTable<WFD_Stufe, NI_Stufe, NS_Stufe> table = startTest("stufen2.txt", FBZ_Klasse.Regen, "konfigurationNS");
		Assert.assertEquals("{WFD_STUFE0={NI_STUFE0=NS_TROCKEN, NI_STUFE1=NS_TROCKEN, NI_STUFE2=NS_TROCKEN, NI_STUFE3=NS_TROCKEN, NI_STUFE4=NS_TROCKEN, NI_WERT_NV=NS_TROCKEN}, WFD_STUFE1={NI_STUFE0=NS_TROCKEN, NI_STUFE1=NS_TROCKEN, NI_STUFE2=NS_TROCKEN, NI_STUFE3=NS_TROCKEN, NI_STUFE4=NS_TROCKEN, NI_WERT_NV=NS_TROCKEN}, WFD_STUFE2={NI_STUFE0=NS_TROCKEN, NI_STUFE1=NS_TROCKEN, NI_STUFE2=NS_TROCKEN, NI_STUFE3=NS_TROCKEN, NI_STUFE4=NS_TROCKEN, NI_WERT_NV=NS_TROCKEN}, WFD_STUFE3={NI_STUFE0=NS_TROCKEN, NI_STUFE1=NS_TROCKEN, NI_STUFE2=NS_TROCKEN, NI_STUFE3=NS_TROCKEN, NI_STUFE4=NS_TROCKEN, NI_WERT_NV=NS_TROCKEN}, WFD_WERT_NV={NI_STUFE0=NS_TROCKEN, NI_STUFE1=NS_TROCKEN, NI_STUFE2=NS_TROCKEN, NI_STUFE3=NS_TROCKEN, NI_STUFE4=NS_TROCKEN, NI_WERT_NV=NS_TROCKEN}}"
		,table.toString());
	}
	@Test
	public void testDuaUfdWithStufen3() throws Exception {
		ImmutableTable<WFD_Stufe, NI_Stufe, NS_Stufe> table = startTest("stufen3.txt", FBZ_Klasse.Regen, "konfigurationNSRegen");
		Assert.assertEquals("{WFD_STUFE0={NI_STUFE0=NS_WERT_NE, NI_STUFE1=NS_WERT_NE, NI_STUFE2=NS_WERT_NE, NI_STUFE3=NS_WERT_NE, NI_STUFE4=NS_WERT_NE, NI_WERT_NV=NS_WERT_NE}, WFD_STUFE1={NI_STUFE0=NS_WERT_NE, NI_STUFE1=NS_WERT_NE, NI_STUFE2=NS_WERT_NE, NI_STUFE3=NS_WERT_NE, NI_STUFE4=NS_WERT_NE, NI_WERT_NV=NS_WERT_NE}, WFD_STUFE2={NI_STUFE0=NS_WERT_NE, NI_STUFE1=NS_WERT_NE, NI_STUFE2=NS_WERT_NE, NI_STUFE3=NS_WERT_NE, NI_STUFE4=NS_WERT_NE, NI_WERT_NV=NS_WERT_NE}, WFD_STUFE3={NI_STUFE0=NS_WERT_NE, NI_STUFE1=NS_WERT_NE, NI_STUFE2=NS_WERT_NE, NI_STUFE3=NS_WERT_NE, NI_STUFE4=NS_WERT_NE, NI_WERT_NV=NS_WERT_NE}, WFD_WERT_NV={NI_STUFE0=NS_WERT_NE, NI_STUFE1=NS_WERT_NE, NI_STUFE2=NS_WERT_NE, NI_STUFE3=NS_WERT_NE, NI_STUFE4=NS_WERT_NE, NI_WERT_NV=NS_WERT_NE}}"
		,table.toString());
	}	
	
	@Test
	public void testDuaUfdWithStufen3b() throws Exception {
		ImmutableTable<WFD_Stufe, NI_Stufe, NS_Stufe> table = startTest("stufen3.txt", FBZ_Klasse.Schnee, "konfigurationNSSchnee");
		Assert.assertEquals("{WFD_STUFE0={NI_STUFE0=NS_WERT_NE, NI_STUFE1=NS_WERT_NE, NI_STUFE2=NS_WERT_NE, NI_STUFE3=NS_WERT_NE, NI_STUFE4=NS_WERT_NE, NI_WERT_NV=NS_WERT_NE}, WFD_STUFE1={NI_STUFE0=NS_WERT_NE, NI_STUFE1=NS_WERT_NE, NI_STUFE2=NS_WERT_NE, NI_STUFE3=NS_WERT_NE, NI_STUFE4=NS_WERT_NE, NI_WERT_NV=NS_WERT_NE}, WFD_STUFE2={NI_STUFE0=NS_WERT_NE, NI_STUFE1=NS_WERT_NE, NI_STUFE2=NS_WERT_NE, NI_STUFE3=NS_WERT_NE, NI_STUFE4=NS_WERT_NE, NI_WERT_NV=NS_WERT_NE}, WFD_STUFE3={NI_STUFE0=NS_WERT_NE, NI_STUFE1=NS_WERT_NE, NI_STUFE2=NS_WERT_NE, NI_STUFE3=NS_WERT_NE, NI_STUFE4=NS_WERT_NE, NI_WERT_NV=NS_WERT_NE}, WFD_WERT_NV={NI_STUFE0=NS_WERT_NE, NI_STUFE1=NS_WERT_NE, NI_STUFE2=NS_WERT_NE, NI_STUFE3=NS_WERT_NE, NI_STUFE4=NS_WERT_NE, NI_WERT_NV=NS_WERT_NE}}"
		,table.toString());
	}	
	
	@Test
	public void testDuaUfdWithStufen3c() throws Exception {
		ImmutableTable<WFD_Stufe, NI_Stufe, NS_Stufe> table = startTest("stufen3.txt", FBZ_Klasse.Platzregen, "konfigurationNSPlatzregen");
		Assert.assertEquals("{WFD_STUFE0={NI_STUFE0=NS_WERT_NE, NI_STUFE1=NS_WERT_NE, NI_STUFE2=NS_WERT_NE, NI_STUFE3=NS_WERT_NE, NI_STUFE4=NS_WERT_NE, NI_WERT_NV=NS_WERT_NE}, WFD_STUFE1={NI_STUFE0=NS_WERT_NE, NI_STUFE1=NS_WERT_NE, NI_STUFE2=NS_WERT_NE, NI_STUFE3=NS_WERT_NE, NI_STUFE4=NS_WERT_NE, NI_WERT_NV=NS_WERT_NE}, WFD_STUFE2={NI_STUFE0=NS_WERT_NE, NI_STUFE1=NS_WERT_NE, NI_STUFE2=NS_WERT_NE, NI_STUFE3=NS_WERT_NE, NI_STUFE4=NS_WERT_NE, NI_WERT_NV=NS_WERT_NE}, WFD_STUFE3={NI_STUFE0=NS_WERT_NE, NI_STUFE1=NS_WERT_NE, NI_STUFE2=NS_WERT_NE, NI_STUFE3=NS_WERT_NE, NI_STUFE4=NS_WERT_NE, NI_WERT_NV=NS_WERT_NE}, WFD_WERT_NV={NI_STUFE0=NS_WERT_NE, NI_STUFE1=NS_WERT_NE, NI_STUFE2=NS_WERT_NE, NI_STUFE3=NS_WERT_NE, NI_STUFE4=NS_WERT_NE, NI_WERT_NV=NS_WERT_NE}}"
		,table.toString());
	}	
	
	@Test
	public void testDuaUfdWithStufen3d() throws Exception {
		ImmutableTable<WFD_Stufe, NI_Stufe, NS_Stufe> table = startTest("stufen3.txt", FBZ_Klasse.Glaette, "konfigurationNSGlaette");
		Assert.assertEquals("{WFD_STUFE0={NI_STUFE0=NS_WERT_NE, NI_STUFE1=NS_WERT_NE, NI_STUFE2=NS_WERT_NE, NI_STUFE3=NS_WERT_NE, NI_STUFE4=NS_WERT_NE, NI_WERT_NV=NS_WERT_NE}, WFD_STUFE1={NI_STUFE0=NS_WERT_NE, NI_STUFE1=NS_WERT_NE, NI_STUFE2=NS_WERT_NE, NI_STUFE3=NS_WERT_NE, NI_STUFE4=NS_WERT_NE, NI_WERT_NV=NS_WERT_NE}, WFD_STUFE2={NI_STUFE0=NS_WERT_NE, NI_STUFE1=NS_WERT_NE, NI_STUFE2=NS_WERT_NE, NI_STUFE3=NS_WERT_NE, NI_STUFE4=NS_WERT_NE, NI_WERT_NV=NS_WERT_NE}, WFD_STUFE3={NI_STUFE0=NS_WERT_NE, NI_STUFE1=NS_WERT_NE, NI_STUFE2=NS_WERT_NE, NI_STUFE3=NS_WERT_NE, NI_STUFE4=NS_WERT_NE, NI_WERT_NV=NS_WERT_NE}, WFD_WERT_NV={NI_STUFE0=NS_WERT_NE, NI_STUFE1=NS_WERT_NE, NI_STUFE2=NS_WERT_NE, NI_STUFE3=NS_WERT_NE, NI_STUFE4=NS_WERT_NE, NI_WERT_NV=NS_WERT_NE}}"
		,table.toString());
	}	
	
	
	@Test
	public void testDuaUfdWithoutStufen() throws Exception {
		ImmutableTable<WFD_Stufe, NI_Stufe, NS_Stufe> table = startTest(null, FBZ_Klasse.Regen, "konfigurationNS");
		Assert.assertEquals("{WFD_STUFE0={NI_STUFE0=NS_TROCKEN, NI_STUFE1=NS_TROCKEN, NI_STUFE2=NS_NASS1, NI_STUFE3=NS_NASS2, NI_STUFE4=NS_NASS2, NI_WERT_NV=NS_TROCKEN}, WFD_STUFE1={NI_STUFE0=NS_NASS1, NI_STUFE1=NS_NASS1, NI_STUFE2=NS_NASS2, NI_STUFE3=NS_NASS3, NI_STUFE4=NS_NASS4, NI_WERT_NV=NS_NASS1}, WFD_STUFE2={NI_STUFE0=NS_NASS2, NI_STUFE1=NS_NASS2, NI_STUFE2=NS_NASS2, NI_STUFE3=NS_NASS3, NI_STUFE4=NS_NASS4, NI_WERT_NV=NS_NASS2}, WFD_STUFE3={NI_STUFE0=NS_NASS2, NI_STUFE1=NS_NASS2, NI_STUFE2=NS_NASS3, NI_STUFE3=NS_NASS3, NI_STUFE4=NS_NASS4, NI_WERT_NV=NS_NASS3}, WFD_WERT_NV={NI_STUFE0=NS_TROCKEN, NI_STUFE1=NS_TROCKEN, NI_STUFE2=NS_NASS1, NI_STUFE3=NS_NASS2, NI_STUFE4=NS_NASS3, NI_WERT_NV=NS_WERT_NE}}"
		,table.toString());
	}
	@Test
	public void testDuaUfdWithoutStufen2() throws Exception {
		ImmutableTable<WFD_Stufe, NI_Stufe, NS_Stufe> table = startTest("stufen3.txt", FBZ_Klasse.Regen, "konfigurationNSSchnee");
		Assert.assertEquals("{WFD_STUFE0={NI_STUFE0=NS_TROCKEN, NI_STUFE1=NS_TROCKEN, NI_STUFE2=NS_NASS1, NI_STUFE3=NS_NASS2, NI_STUFE4=NS_NASS2, NI_WERT_NV=NS_TROCKEN}, WFD_STUFE1={NI_STUFE0=NS_NASS1, NI_STUFE1=NS_NASS1, NI_STUFE2=NS_NASS2, NI_STUFE3=NS_NASS3, NI_STUFE4=NS_NASS4, NI_WERT_NV=NS_NASS1}, WFD_STUFE2={NI_STUFE0=NS_NASS2, NI_STUFE1=NS_NASS2, NI_STUFE2=NS_NASS2, NI_STUFE3=NS_NASS3, NI_STUFE4=NS_NASS4, NI_WERT_NV=NS_NASS2}, WFD_STUFE3={NI_STUFE0=NS_NASS2, NI_STUFE1=NS_NASS2, NI_STUFE2=NS_NASS3, NI_STUFE3=NS_NASS3, NI_STUFE4=NS_NASS4, NI_WERT_NV=NS_NASS3}, WFD_WERT_NV={NI_STUFE0=NS_TROCKEN, NI_STUFE1=NS_TROCKEN, NI_STUFE2=NS_NASS1, NI_STUFE3=NS_NASS2, NI_STUFE4=NS_NASS3, NI_WERT_NV=NS_WERT_NE}}"
		,table.toString());
	}

	@After
	public void tearDown() throws Exception {
		_daUfd.getVerbindung().disconnect(false, "");
		super.tearDown();
	}

	private ImmutableTable<WFD_Stufe, NI_Stufe, NS_Stufe> startTest(final String fileName, final FBZ_Klasse klasse, final String arg) throws Exception {
		_daUfd = new MyVerwaltungAufbereitungUFD();
		String[] strings;
		if(fileName == null){
			strings = new String[]{"-KonfigurationsBereichsPid=kb.duaTestUfd"};
		}
		else {
			strings = new String[]{"-KonfigurationsBereichsPid=kb.duaTestUfd", "-" + arg + "=" + getFilePath(fileName)};
		}
		_daUfd.parseArguments(new ArgumentList(strings));
		_daUfd.initialize(testEnvironment.startDavConnection(null));
		ImmutableTable.Builder<WFD_Stufe, NI_Stufe, NS_Stufe> tableBuilder = ImmutableTable.builder();
		Map<FBZ_Klasse, Map<WFD_Stufe, Map<NI_Stufe, NS_Stufe>>> hashTable = NaesseStufe.getTabellenWFDNIzumNS();
		tableBuilder.orderColumnsBy(Comparator.naturalOrder());
		tableBuilder.orderRowsBy(Comparator.naturalOrder());
		for(Map.Entry<WFD_Stufe, Map<NI_Stufe, NS_Stufe>> entry : hashTable.get(klasse).entrySet()) {
			for(Map.Entry<NI_Stufe, NS_Stufe> entry2 : entry.getValue().entrySet()) {
				tableBuilder.put(entry.getKey(), entry2.getKey(), entry2.getValue());
			}
		}
		return tableBuilder.build();
	}

	private File getFilePath(final String fileName) {
		testEnvironment.copyResources("de/bsvrz/dua/daufd/tests/files", "files");
		return new File(testEnvironment.getTemporaryDirectory(), "files/" + fileName);
	}

	private static class MyVerwaltungAufbereitungUFD extends VerwaltungAufbereitungUFD {
		public IBearbeitungsKnoten getKnoten(){
			return ersterKnoten;
		}
	}
}
