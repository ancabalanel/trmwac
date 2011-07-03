package mwac;

import mwac.msgs.MData;


/**
 * This class holds information about the data that needs to be sent
 * 
 * @author Anca
 * @see MData
 */
public class DataInfo {
	private MData mdata;
	private boolean wasSent;

	public DataInfo(MData mdata) {
		this.setMdata(mdata);
		setWasSent(false);
	}

	public void setWasSent() {
		wasSent = true;
	}

	public void setMdata(MData mdata) {
		this.mdata = mdata;
	}

	public MData getMdata() {
		return mdata;
	}

	public void setWasSent(boolean wasSent) {
		this.wasSent = wasSent;
	}

	public boolean isWasSent() {
		return wasSent;
	}
}