package com.ehang.sos;

import java.util.List;

import com.ehang.location.LocationApplication;
import com.ehang.sos.R.string;

import android.R.integer;
import android.content.Context;
import android.os.Looper;
import android.os.Message;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.widget.Toast;

public class CellNum {
	TelephonyManager tm;
	GsmCellLocation gsmCell;
	CdmaCellLocation cdmaCell;
	String cid, lac;
	Context context;

	public CellNum(Context ct) {
		context = ct;
	}

	public void GetCellnum() {
		tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		List<CellInfo> mCellInfoValue = (List<CellInfo>) tm.getAllCellInfo();
		CellInfoGsm mCellInfoGsm;
		CellInfoLte mCellInfoLte;
		CellInfoCdma mCellInfoCdma;
		CellInfoWcdma mCellInfoWcdma;
		
	    if (tm.getVoiceNetworkType() == 17) {//tds simcard
			CellLocation mcell = tm.getCellLocation();
			if (mcell instanceof CdmaCellLocation) {
				cdmaCell = (CdmaCellLocation) mcell;
				lac = Integer.toString(cdmaCell.getNetworkId());
				cid = Integer.toString(cdmaCell.getBaseStationId());
			}else {
				gsmCell = (GsmCellLocation) mcell;
				lac = Integer.toString(gsmCell.getLac());
				cid = Integer.toString(gsmCell.getCid());
			}
		}else {
		    if(mCellInfoValue == null){
				Log.d("SOS","mCellInfoValue = null ");
				Toast.makeText(context, context.getString(R.string.errsim),Toast.LENGTH_SHORT).show();
				return;
		    }
	    	for(CellInfo c : mCellInfoValue){
				if(c instanceof CellInfoLte){
					mCellInfoLte = (CellInfoLte) c;
					if(mCellInfoLte.isRegistered()){
						CellIdentityLte mCellIdentityLte = mCellInfoLte.getCellIdentity();
						lac = Integer.toString(mCellIdentityLte.getTac());
						cid = Integer.toString(mCellIdentityLte.getCi());
					}
					break;
				}
				if(c instanceof CellInfoWcdma){
					mCellInfoWcdma = (CellInfoWcdma) c;
					if(mCellInfoWcdma.isRegistered()){
						CellIdentityWcdma mCellIdentityWcdma = mCellInfoWcdma.getCellIdentity();
						lac = Integer.toString(mCellIdentityWcdma.getLac());
						cid = Integer.toString(mCellIdentityWcdma.getCid());
					}
					break;
				}
				if(c instanceof CellInfoGsm){
					mCellInfoGsm = (CellInfoGsm) c;
					if(mCellInfoGsm.isRegistered()){
						CellIdentityGsm mCellIdentityGsm = mCellInfoGsm.getCellIdentity();
						lac = Integer.toString(mCellIdentityGsm.getLac());
						cid = Integer.toString(mCellIdentityGsm.getCid());
					}
					break;
				}
				if(c instanceof CellInfoCdma){
					mCellInfoCdma = (CellInfoCdma) c;
					if(mCellInfoCdma.isRegistered()){
						CellIdentityCdma mCellIdentityCdma = mCellInfoCdma.getCellIdentity();
						lac = Integer.toString(mCellIdentityCdma.getNetworkId());
						cid = Integer.toString(mCellIdentityCdma.getBasestationId());
					}
					break;
				}
	    	}
		}
	    
	    
		LocationApplication mAppli = (LocationApplication) context.getApplicationContext();
		mAppli.lac = lac;
		mAppli.cid = cid;

		Thread LooperThread = new Thread(new Runnable() {
			public void run() {
				Looper.prepare();
				Message message = new Message();
				message.what = 1;
				if (lac != null && cid != null) {
					new Setup_Mod(context).mHandler.sendMessage(message); // send cell msg
				}
				Looper.loop();
			}
		});
		LooperThread.start();

	}
	

}
