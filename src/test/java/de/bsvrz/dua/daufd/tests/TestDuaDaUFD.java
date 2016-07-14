/*
 * Copyright 2016 by Kappich Systemberatung Aachen
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

package de.bsvrz.dua.daufd.tests;

import com.google.common.collect.ImmutableList;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.tests.ColumnLayout;
import de.bsvrz.dua.tests.DuADataIdentification;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.List;

/**
 * TBD Dokumentation
 *
 * @author Kappich Systemberatung
 */
public class TestDuaDaUFD extends DuAUfdTestBase {

	private Aspect _aspSend;
	private Aspect _aspReceive;
	private SystemObject _ni;
	private SystemObject _wfd;
	private SystemObject _ns;
	private SystemObject _fbz;
	private SystemObject _lt;
	private SystemObject _rlf;
	private SystemObject _sw;
	private SystemObject _messstelle;
	private DataDescription _ddniSend;
	private DataDescription _ddnsSend;
	private DataDescription _ddwfdSend;
	private DataDescription _ddfbzSend;
	private DataDescription _ddltSend;
	private DataDescription _ddrlfSend;
	private DataDescription _ddswSend;
	private DataDescription _ddNiReceive;
	private DataDescription _ddWfdReceive;
	private DataDescription _ddNsReceive;
	private DataDescription _ddSwReceive;
	private ImmutableList<DuADataIdentification> _send;
	private ImmutableList<DuADataIdentification> _rec;
	private DataDescription _ddTptLtReceive;
	private DataDescription _ddTptFbtReceive;
	private SystemObject _fbt;
	private DataDescription _ddfbtSend;
	private ImmutableList<DuADataIdentification> _sendNoWfd;
	private ImmutableList<DuADataIdentification> _recNoWfd;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		_aspSend = _dataModel.getAspect("asp.messWertErsetzung");
		_aspReceive = _dataModel.getAspect("asp.messWertErsetzung");
		_ni = _dataModel.getObject("ufd.ni");
		_ns = _dataModel.getObject("ufd.na");
		_wfd = _dataModel.getObject("ufd.wfd");
		_fbz = _dataModel.getObject("ufd.fbz");
		_lt = _dataModel.getObject("ufd.lt");
		_fbt = _dataModel.getObject("ufd.fbt");
		_rlf = _dataModel.getObject("ufd.rlf");
		_sw = _dataModel.getObject("ufd.sw");
		_messstelle = _dataModel.getObject("ufdm.1");
		AttributeGroup atgni = _dataModel.getAttributeGroup("atg.ufds" + "NiederschlagsIntensität");
		AttributeGroup atgns = _dataModel.getAttributeGroup("atg.ufds" + "NiederschlagsArt");
		AttributeGroup atgwfd = _dataModel.getAttributeGroup("atg.ufds" + "WasserFilmDicke");
		AttributeGroup atgfbz = _dataModel.getAttributeGroup("atg.ufds" + "FahrBahnOberFlächenZustand");
		AttributeGroup atglt = _dataModel.getAttributeGroup("atg.ufds" + "LuftTemperatur");
		AttributeGroup atgfbt = _dataModel.getAttributeGroup("atg.ufds" + "FahrBahnOberFlächenTemperatur");
		AttributeGroup atgrlf = _dataModel.getAttributeGroup("atg.ufds" + "RelativeLuftFeuchte");
		AttributeGroup atgsw = _dataModel.getAttributeGroup("atg.ufds" + "SichtWeite");
		AttributeGroup atgniclass = _dataModel.getAttributeGroup("atg.ufdsStufeNiederschlagsIntensität");
		AttributeGroup atgwfdclass = _dataModel.getAttributeGroup("atg.ufdsStufeWasserFilmDicke");
		AttributeGroup atgnsclass = _dataModel.getAttributeGroup("atg.ufdmsNässeStufe");
		AttributeGroup atgswclass = _dataModel.getAttributeGroup("atg.ufdsStufeSichtWeite");
		AttributeGroup atgTptLt = _dataModel.getAttributeGroup("atg.ufdmsTaupunktTemperaturLuft");
		AttributeGroup atgTptFbt = _dataModel.getAttributeGroup("atg.ufdmsTaupunktTemperaturFahrBahn");
		Aspect aspclass = _dataModel.getAspect("asp.klassifizierung");
		Aspect aspAnalyse = _dataModel.getAspect("asp.analyse");
		_ddniSend = new DataDescription(atgni, _aspSend);
		_ddnsSend = new DataDescription(atgns, _aspSend);
		_ddwfdSend = new DataDescription(atgwfd, _aspSend);
		_ddfbzSend = new DataDescription(atgfbz, _aspSend);
		_ddltSend = new DataDescription(atglt, _aspSend);
		_ddfbtSend = new DataDescription(atgfbt, _aspSend);
		_ddrlfSend = new DataDescription(atgrlf, _aspSend);
		_ddswSend = new DataDescription(atgsw, _aspSend);
		fakeParamApp.publishParam(_ni.getPid(), "atg.ufdsKlassifizierungNiederschlagsIntensität",
		                          "{KlassifizierungNiederschlagsIntensität:[{von:'0',bis:'0.3'},{von:'0.2',bis:'1.2'},{von:'1.0',bis:'5.0'},{von:'4.0',bis:'12.0'},{von:'10.0',bis:'20.0'}]}"
		);
		fakeParamApp.publishParam(_ni.getPid(), "atg.ufdsAggregationNiederschlagsIntensität",
		                          "{b0:'0.08',fb:'0.25'}"
		);
		fakeParamApp.publishParam(_wfd.getPid(), "atg.ufdsKlassifizierungWasserFilmDicke",
		                          "{KlassifizierungWasserFilmDicke:[{von:'0',bis:'0.2'},{von:'0.1',bis:'0.5'},{von:'0.4',bis:'1.2'},{von:'1.0',bis:'10.0'}]}"
		);
		fakeParamApp.publishParam(_wfd.getPid(), "atg.ufdsAggregationWasserFilmDicke",
		                          "{b0:'0.08',fb:'0.25'}"
		);
		fakeParamApp.publishParam(_sw.getPid(), "atg.ufdsKlassifizierungSichtWeite",
		                          "{KlassifizierungSichtWeite:[{von:'10',bis:'60'},{von:'50',bis:'100'},{von:'80',bis:'150'},{von:'120',bis:'300'},{von:'250',bis:'500'},{von:'400',bis:'2000'}]}"
		);
		fakeParamApp.publishParam(_sw.getPid(), "atg.ufdsAggregationSichtWeite",
		                          "{b0:'0.08',fb:'0.25'}"
		);
		fakeParamApp.publishParam(_messstelle.getPid(), "atg.ufdmsAbtrockungsPhasen",
		                          "{ZeitNass4Nass3:'60s',ZeitNass3Nass2:'60s',ZeitNass2Nass1:'60s',ZeitNass1Trocken:'180s'}"
		);
		_ddNiReceive = new DataDescription(atgniclass, aspclass);
		_ddWfdReceive = new DataDescription(atgwfdclass, aspclass);
		_ddNsReceive = new DataDescription(atgnsclass, aspclass);
		_ddSwReceive = new DataDescription(atgswclass, aspclass);
		_ddTptLtReceive = new DataDescription(atgTptLt, aspAnalyse);
		_ddTptFbtReceive = new DataDescription(atgTptFbt, aspAnalyse);
		_send = ImmutableList.of(
				new DuADataIdentification(_ni, _ddniSend),
				new DuADataIdentification(_ns, _ddnsSend),
				new DuADataIdentification(_wfd, _ddwfdSend),
				new DuADataIdentification(_fbz, _ddfbzSend),
				new DuADataIdentification(_lt, _ddltSend),
				new DuADataIdentification(_fbt, _ddfbtSend),
				new DuADataIdentification(_rlf, _ddrlfSend),
				new DuADataIdentification(_sw, _ddswSend)
		);	
		_sendNoWfd = ImmutableList.of(
				new DuADataIdentification(_ni, _ddniSend),
				new DuADataIdentification(_ns, _ddnsSend),
				new DuADataIdentification(_fbz, _ddfbzSend),
				new DuADataIdentification(_lt, _ddltSend),
				new DuADataIdentification(_fbt, _ddfbtSend),
				new DuADataIdentification(_rlf, _ddrlfSend),
				new DuADataIdentification(_sw, _ddswSend)
		);
		_rec = ImmutableList.of(
				new DuADataIdentification(_ni, _ddNiReceive),
				new DuADataIdentification(_wfd, _ddWfdReceive),
				new DuADataIdentification(_messstelle, _ddNsReceive),
				new DuADataIdentification(_sw, _ddSwReceive),
				new DuADataIdentification(_messstelle, _ddTptLtReceive),
				new DuADataIdentification(_messstelle, _ddTptFbtReceive)
		);	
		_recNoWfd = ImmutableList.of(
				new DuADataIdentification(_ni, _ddNiReceive),
				new DuADataIdentification(_messstelle, _ddNsReceive),
				new DuADataIdentification(_sw, _ddSwReceive),
				new DuADataIdentification(_messstelle, _ddTptLtReceive),
				new DuADataIdentification(_messstelle, _ddTptFbtReceive)
		);
	}

	@Test
	public void testDaUfd1() throws Exception {
		startTestCase("DaUfd.csv", _send, _rec, new DaUfdLayout());
	}
	
	@Test
	public void testDaUfd2() throws Exception {
		startTestCase("DaUfd2.csv", _send, _rec, new DaUfdLayout(){
			@Override
			public Collection<String> getIgnored() {
				return ImmutableList.of("TaupunktTemperaturLuft","TaupunktTemperaturFahrBahn", "Stufe");
			}

			@Override
			public Collection<String> getIgnoredLines() {
				return ImmutableList.of("271");
			}
		});
	}	
	
	@Test
	public void testDaUfd2b() throws Exception {
		sendData(new ResultData(_wfd, _ddwfdSend, 60000, null));
		startTestCase("DaUfd2.csv", _sendNoWfd, _recNoWfd, new DaUfdLayout(){
			@Override
			public Collection<String> getIgnored() {
				return ImmutableList.of("TaupunktTemperaturLuft","TaupunktTemperaturFahrBahn", "Stufe");
			}

			@Override
			public Collection<String> getIgnoredLines() {
				return ImmutableList.of("271");
			}
		});
	}

	private class DaUfdLayout extends ColumnLayout {
		@Override
		public int getColumnCount(final boolean in) {
			return 1;
		}

		@Override
		public void setValues(final SystemObject testObject, final Data item, final List<String> row, final int realCol, final String type, final boolean in) {
			if (!in){
				if(testObject.equals(_ni) && realCol == 0){
					item.asUnscaledValue().set(Integer.parseInt(row.get(0)));
					if(item.asUnscaledValue().intValue() > 4){
						item.asUnscaledValue().set(-1);
					}
				}	
				if(testObject.equals(_wfd) && realCol == 1){
					item.asUnscaledValue().set(Integer.parseInt(row.get(1)));
					if(item.asUnscaledValue().intValue() > 3){
						item.asUnscaledValue().set(-1);
					}
				}	
				if(testObject.equals(_sw) && realCol == 3){
					item.asUnscaledValue().set(Integer.parseInt(row.get(3)));
					if(item.asUnscaledValue().intValue() > 5){
						item.asUnscaledValue().set(-1);
					}
				}		
				if(testObject.equals(_messstelle) && realCol == 2){
					item.asUnscaledValue().set(Integer.parseInt(row.get(2)));
				}	
				if(testObject.equals(_messstelle) && realCol == 4){
					double value = Double.parseDouble(row.get(4).replace(',','.'));
					if(value < -1000){
						item.asUnscaledValue().set(Integer.parseInt(row.get(4)));
					}
					else {
						item.asScaledValue().set(value);
					}
				}	
				if(testObject.equals(_messstelle) && realCol == 5){
					double value = Double.parseDouble(row.get(5).replace(',','.'));
					if(value < -1000){
						item.asUnscaledValue().set(Integer.parseInt(row.get(5)));
					}
					else {
						item.asScaledValue().set(value);
					}
				}
				return;
			}
			String pid = item.getAttributeType().getPid();
			switch(pid) {
				case "atl.ufdsNiederschlagsIntensität":
					set(item, row, realCol);
					break;
				case "atl.ufdsNiederschlagsArt":
					set(item, row, realCol);
					break;
				case "atl.ufdsWasserFilmDicke":
					set(item, row, realCol);
					break;
				case "atl.ufdsFahrBahnOberFlächenZustand":
					set(item, row, realCol);
					break;
				case "atl.ufdsLuftTemperatur":
					set(item, row, realCol);
					break;	
				case "atl.ufdsFahrBahnOberFlächenTemperatur":
					set(item, row, realCol);
					break;
				case "atl.ufdsRelativeLuftFeuchte":
					set(item, row, realCol);
					break;
				case "atl.ufdsSichtWeite":
					set(item, row, realCol);
					break;
				default:
					System.out.println("pid = " + pid);
			}
		}

		private void set(final Data item, final List<String> row, final int realCol) {
			try {
				item.getTextValue("Wert").setText(row.get(realCol));
			}
			catch(Exception e) {
				item.getUnscaledValue("Wert").set(Double.parseDouble(row.get(realCol)));
			}
			if(!item.isDefined()){
				item.getUnscaledValue("Wert").set(Double.parseDouble(row.get(realCol)));
			}
		}
	}
}
