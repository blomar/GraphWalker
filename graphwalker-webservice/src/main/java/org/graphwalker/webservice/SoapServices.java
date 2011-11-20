/*
 * #%L
 * GraphWalker Core
 * %%
 * Copyright (C) 2011 GraphWalker
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

package org.graphwalker.webservice;

import java.io.File;
import java.util.Arrays;
import java.util.Vector;

import javax.jws.WebService;

import org.apache.log4j.Logger;
import org.graphwalker.core.ModelBasedTesting;
import org.graphwalker.core.Util;
import org.graphwalker.core.exceptions.InvalidDataException;

@WebService
/**
 * <p>SoapServices class.</p>
 */
public class SoapServices {

	private static Logger logger = Util.setupLogger(SoapServices.class);
	private Vector<String> stepPair = new Vector<String>();
	public String xmlFile = "";
	private boolean hardStop = false;
	public ModelBasedTesting mbt;

	/**
	 * <p>Constructor for SoapServices.</p>
	 */
	public SoapServices() {
	}

	/**
	 * <p>Constructor for SoapServices.</p>
	 *
	 * @param mbt a {@link org.graphwalker.core.ModelBasedTesting} object.
	 */
	public SoapServices(ModelBasedTesting mbt) {
		this.mbt = mbt;
		Reset();
	}

	/**
	 * <p>SetCurrentVertex.</p>
	 *
	 * @param newVertex a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public boolean SetCurrentVertex(String newVertex) {
		logger.debug("SOAP service SetCurrentVertex recieving: " + newVertex);
		boolean value = mbt.setCurrentVertex(newVertex);
		logger.debug("SOAP service SetCurrentVertex returning: " + value);
		return value;
	}

	/**
	 * <p>GetDataValue.</p>
	 *
	 * @param data a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String GetDataValue(String data) {
		logger.debug("SOAP service getDataValue recieving: " + data);
		String value = "";
		try {
			value = mbt.getDataValue(data);
		} catch (InvalidDataException e) {
			logger.error(e);
		}
		logger.debug("SOAP service getDataValue returning: " + value);
		return value;
	}

	/**
	 * <p>ExecAction.</p>
	 *
	 * @param action a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String ExecAction(String action) {
		logger.debug("SOAP service ExecAction recieving: " + action);
		String value = "";
		try {
			value = mbt.execAction(action);
		} catch (InvalidDataException e) {
			logger.error(e);
		} catch (Exception e) {
			logger.error(e);
		}
		logger.debug("SOAP service ExecAction returning: " + value);
		return value;
	}

	/**
	 * <p>PassRequirement.</p>
	 *
	 * @param pass a {@link java.lang.String} object.
	 */
	public void PassRequirement(String pass) {
		logger.debug("SOAP service PassRequirement recieving: " + pass);
		if ("TRUE".equalsIgnoreCase(pass)) {
			mbt.passRequirement(true);
		} else if ("FALSE".equalsIgnoreCase(pass)) {
			mbt.passRequirement(false);
		} else {
			logger.error("SOAP service PassRequirement dont know how to handle: " + pass + "\nOnly the strings true or false are permitted");
		}
	}

	/**
	 * <p>GetNextStep.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String GetNextStep() {
		logger.debug("SOAP service getNextStep");
		try {
			String value = "";

			if (!mbt.hasNextStep() && (stepPair.size() == 0)) {
				return value;
			}

			if (stepPair.size() == 0) {
				try {
					stepPair = new Vector<String>(Arrays.asList(mbt.getNextStep()));
				} catch (Exception e) {
					hardStop = true;
					return "";
				}
			}

			value = stepPair.remove(0);
			value = value.replaceAll("/.*$", "");
			String addInfo = "";

			if (stepPair.size() == 1) {
				mbt.logExecution(mbt.getMachine().getLastEdge(), addInfo);
				if (mbt.isUseStatisticsManager()) {
					mbt.getStatisticsManager().addProgress(mbt.getMachine().getLastEdge());
				}
			} else {
				mbt.logExecution(mbt.getMachine().getCurrentVertex(), addInfo);
				if (mbt.isUseStatisticsManager()) {
					mbt.getStatisticsManager().addProgress(mbt.getMachine().getCurrentVertex());
				}
			}
			return value;
		} finally {
			if (mbt.isUseGUI()) {
                mbt.getGui().setButtons();
                mbt.getGui().updateLayout();
			}
		}
	}

	/**
	 * <p>HasNextStep.</p>
	 *
	 * @return a boolean.
	 */
	public boolean HasNextStep() {
		logger.debug("SOAP service hasNextStep");
		boolean value = false;
		if (hardStop) {
			value = false;
		} else {
			value = stepPair.size() != 0 || mbt.hasNextStep();
		}
		logger.debug("SOAP service hasNextStep returning: " + value);
		if (!value) {
			logger.info(mbt.getStatisticsString());
		}
		return value;
	}

	/**
	 * <p>Reload.</p>
	 *
	 * @return a boolean.
	 */
	public boolean Reload() {
		logger.debug("SOAP service reload");
		boolean retValue = true;
		boolean useGui = mbt.isUseGUI();
		try {
			if (!this.xmlFile.isEmpty()) {
				mbt = Util.loadMbtAsWSFromXml(Util.getFile(this.xmlFile));
				//if (useGui) {
				//	mbt.setUseGUI();
				//}
			}
		} catch (Exception e) {
			Util.logStackTraceToError(e);
			retValue = false;
		} finally {
			if (mbt.isUseGUI()) {
				//mbt.getGui().setMbt(mbt);
				mbt.getGui().setButtons();
				mbt.getGui().updateLayout();
			}
		}
		Reset();
		logger.debug("SOAP service reload returning: " + retValue);
		return retValue;
	}

	/**
	 * <p>Load.</p>
	 *
	 * @param xmlFile a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public boolean Load(String xmlFile) {
		logger.debug("SOAP service load recieving: " + xmlFile);
		if (xmlFile == null) {
			logger.error("Web service 'Load' needs a valid xml file name.");
			logger.debug("SOAP service load returning: false");
			return false;
		}
		if (!new File(xmlFile).canRead()) {
			logger.error("Web service 'Load' needs a readable xml file name. Check the file: '" + xmlFile + "'");
			logger.debug("SOAP service load returning: false");
			return false;
		}
		this.xmlFile = xmlFile;
		boolean retValue = true;
		boolean useGui = mbt.isUseGUI();
		try {
			mbt = Util.loadMbtAsWSFromXml(Util.getFile(this.xmlFile));
			//if (useGui) {
			//	mbt.setUseGUI();
			//}
		} catch (Exception e) {
			Util.logStackTraceToError(e);
			retValue = false;
		} finally {
			if (mbt.isUseGUI()) {
				//mbt.getGui().setMbt(mbt);
				mbt.getGui().setButtons();
				mbt.getGui().updateLayout();
			}
		}
		Reset();
		logger.debug("SOAP service load returning: " + retValue);
		return retValue;
	}

	/**
	 * <p>GetStatistics.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String GetStatistics() {
		logger.debug("SOAP service getStatistics");
		return mbt.getStatisticsVerbose();
	}

	private void Reset() {
		hardStop = false;
		stepPair.clear();
	}
}
