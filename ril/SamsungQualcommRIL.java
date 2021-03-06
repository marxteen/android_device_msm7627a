/*
 * Copyright (C) 2006 The Android Open Source Project
 * Copyright (C) 2011, 2012 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.internal.telephony;

import java.util.ArrayList;
import java.util.Collections;
import java.lang.Runtime;
import java.io.IOException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.os.AsyncResult;
import android.os.Parcel;
import android.os.Registrant;
import android.os.SystemProperties;
import android.telephony.PhoneNumberUtils;
import android.telephony.SignalStrength;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import static com.android.internal.telephony.RILConstants.*;

import com.android.internal.telephony.CallForwardInfo;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.gsm.SmsBroadcastConfigInfo;
import com.android.internal.telephony.gsm.SuppServiceNotification;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.uicc.IccCardStatus;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.RILConstants;
import com.android.internal.telephony.SmsResponse;
import com.android.internal.telephony.cdma.CdmaCallWaitingNotification;
import com.android.internal.telephony.cdma.CdmaInformationRecords;
import com.android.internal.telephony.cdma.CdmaInformationRecords.CdmaSignalInfoRec;
import com.android.internal.telephony.cdma.SignalToneUtil;
import com.android.internal.telephony.dataconnection.DataCallResponse;
import com.android.internal.telephony.dataconnection.DcFailCause;

import android.telephony.Rlog;

public class SamsungQualcommRIL extends RIL implements CommandsInterface {

    private boolean mSignalbarCount = SystemProperties.getInt("ro.telephony.sends_barcount", 0) == 1 ? true : false;
    private boolean mIsSamsungCdma = SystemProperties.getBoolean("ro.ril.samsung_cdma", false);
    private Object mCatProCmdBuffer;

    public SamsungQualcommRIL(Context context, int preferredNetworkType,
            int cdmaSubscription, Integer instanceId) {
        this(context, preferredNetworkType, cdmaSubscription);
    }

    public SamsungQualcommRIL(Context context, int networkMode, int cdmaSubscription) {
        super(context, networkMode, cdmaSubscription);
    }

    // SAMSUNG SGS STATES
    static final int RIL_UNSOL_O2_HOME_ZONE_INFO = 11007;
    static final int RIL_UNSOL_DEVICE_READY_NOTI = 11008;
    static final int RIL_UNSOL_GPS_NOTI = 11009;
    static final int RIL_UNSOL_AM = 11010;
    static final int RIL_UNSOL_SAMSUNG_UNKNOWN_MAGIC_REQUEST = 11012;
    static final int RIL_UNSOL_SAMSUNG_UNKNOWN_MAGIC_REQUEST_2 = 11011;
    static final int RIL_UNSOL_HSDPA_STATE_CHANGED = 11016;
    static final int RIL_REQUEST_DIAL_EMERGENCY = 10016;

    static String
    requestToString(int request) {
        switch (request) {
            case RIL_REQUEST_DIAL_EMERGENCY: return "DIAL_EMERGENCY";
            default: return RIL.requestToString(request);
        }
    }

    @Override
    public void
    setRadioPower(boolean on, Message result) {
        boolean allow = SystemProperties.getBoolean("persist.ril.enable", true);
        if (!allow) {
            return;
        }

        RILRequest rr = RILRequest.obtain(RIL_REQUEST_RADIO_POWER, result);

        if (on) {
            rr.mParcel.writeInt(1);
            rr.mParcel.writeInt(1);
        } else {
            rr.mParcel.writeInt(2);
            rr.mParcel.writeInt(0);
            rr.mParcel.writeInt(0);
        }

        if (RILJ_LOGD) riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    @Override
    protected RILRequest
    processSolicited (Parcel p) {
        int serial, error;

        serial = p.readInt();
        error = p.readInt();

        Rlog.d(RILJ_LOG_TAG, "Serial: " + serial);
        Rlog.d(RILJ_LOG_TAG, "Error: " + error);

        RILRequest rr;

        rr = findAndRemoveRequestFromList(serial);

        if (rr == null) {
            Rlog.w(RILJ_LOG_TAG, "Unexpected solicited response! sn: "
                    + serial + " error: " + error);
            return null;
        }

        Object ret = null;

        if (error == 0 || p.dataAvail() > 0) {
            // either command succeeds or command fails but with data payload
            try {switch (rr.mRequest) {
            /*
            cat libs/telephony/ril_commands.h \
            | egrep "^ *{RIL_" \
            | sed -re 's/\{([^,]+),[^,]+,([^}]+).+/case \1: ret = \2(p); break;/'
             */
            case RIL_REQUEST_GET_SIM_STATUS: ret =  responseIccCardStatus(p); break;
            case RIL_REQUEST_ENTER_SIM_PIN: ret =  responseInts(p); break;
            case RIL_REQUEST_ENTER_SIM_PUK: ret =  responseInts(p); break;
            case RIL_REQUEST_ENTER_SIM_PIN2: ret =  responseInts(p); break;
            case RIL_REQUEST_ENTER_SIM_PUK2: ret =  responseInts(p); break;
            case RIL_REQUEST_CHANGE_SIM_PIN: ret =  responseInts(p); break;
            case RIL_REQUEST_CHANGE_SIM_PIN2: ret =  responseInts(p); break;
            case RIL_REQUEST_ENTER_DEPERSONALIZATION_CODE: ret =  responseInts(p); break;
            case RIL_REQUEST_GET_CURRENT_CALLS: ret =  responseCallList(p); break;
            case RIL_REQUEST_DIAL: ret =  responseVoid(p); break;
            case RIL_REQUEST_GET_IMSI: ret =  responseString(p); break;
            case RIL_REQUEST_HANGUP: ret =  responseVoid(p); break;
            case RIL_REQUEST_HANGUP_WAITING_OR_BACKGROUND: ret =  responseVoid(p); break;
            case RIL_REQUEST_HANGUP_FOREGROUND_RESUME_BACKGROUND: ret =  responseVoid(p); break;
            case RIL_REQUEST_SWITCH_WAITING_OR_HOLDING_AND_ACTIVE: ret =  responseVoid(p); break;
            case RIL_REQUEST_CONFERENCE: ret =  responseVoid(p); break;
            case RIL_REQUEST_UDUB: ret =  responseVoid(p); break;
            case RIL_REQUEST_LAST_CALL_FAIL_CAUSE: ret =  responseLastCallFailCause(p); break;
            case RIL_REQUEST_SIGNAL_STRENGTH: ret =  responseSignalStrength(p); break;
            case RIL_REQUEST_VOICE_REGISTRATION_STATE: ret =  responseVoiceRegistrationState(p); break;
            case RIL_REQUEST_DATA_REGISTRATION_STATE: ret =  responseStrings(p); break;
            case RIL_REQUEST_OPERATOR: ret =  responseStrings(p); break;
            case RIL_REQUEST_RADIO_POWER: ret =  responseVoid(p); break;
            case RIL_REQUEST_DTMF: ret =  responseVoid(p); break;
            case RIL_REQUEST_SEND_SMS: ret =  responseSMS(p); break;
            case RIL_REQUEST_SEND_SMS_EXPECT_MORE: ret =  responseSMS(p); break;
            case RIL_REQUEST_SETUP_DATA_CALL: ret =  responseSetupDataCall(p); break;
            case RIL_REQUEST_SIM_IO: ret =  responseICC_IO(p); break;
            case RIL_REQUEST_SEND_USSD: ret =  responseVoid(p); break;
            case RIL_REQUEST_CANCEL_USSD: ret =  responseVoid(p); break;
            case RIL_REQUEST_GET_CLIR: ret =  responseInts(p); break;
            case RIL_REQUEST_SET_CLIR: ret =  responseVoid(p); break;
            case RIL_REQUEST_QUERY_CALL_FORWARD_STATUS: ret =  responseCallForward(p); break;
            case RIL_REQUEST_SET_CALL_FORWARD: ret =  responseVoid(p); break;
            case RIL_REQUEST_QUERY_CALL_WAITING: ret =  responseInts(p); break;
            case RIL_REQUEST_SET_CALL_WAITING: ret =  responseVoid(p); break;
            case RIL_REQUEST_SMS_ACKNOWLEDGE: ret =  responseVoid(p); break;
            case RIL_REQUEST_GET_IMEI: ret =  responseString(p); break;
            case RIL_REQUEST_GET_IMEISV: ret =  responseString(p); break;
            case RIL_REQUEST_ANSWER: ret =  responseVoid(p); break;
            case RIL_REQUEST_DEACTIVATE_DATA_CALL: ret =  responseVoid(p); break;
            case RIL_REQUEST_QUERY_FACILITY_LOCK: ret =  responseInts(p); break;
            case RIL_REQUEST_SET_FACILITY_LOCK: ret =  responseInts(p); break;
            case RIL_REQUEST_CHANGE_BARRING_PASSWORD: ret =  responseVoid(p); break;
            case RIL_REQUEST_QUERY_NETWORK_SELECTION_MODE: ret =  responseInts(p); break;
            case RIL_REQUEST_SET_NETWORK_SELECTION_AUTOMATIC: ret =  responseVoid(p); break;
            case RIL_REQUEST_SET_NETWORK_SELECTION_MANUAL: ret =  responseVoid(p); break;
            case RIL_REQUEST_QUERY_AVAILABLE_NETWORKS : ret =  responseOperatorInfos(p); break;
            case RIL_REQUEST_DTMF_START: ret =  responseVoid(p); break;
            case RIL_REQUEST_DTMF_STOP: ret =  responseVoid(p); break;
            case RIL_REQUEST_BASEBAND_VERSION: ret =  responseString(p); break;
            case RIL_REQUEST_SEPARATE_CONNECTION: ret =  responseVoid(p); break;
            case RIL_REQUEST_SET_MUTE: ret =  responseVoid(p); break;
            case RIL_REQUEST_GET_MUTE: ret =  responseInts(p); break;
            case RIL_REQUEST_QUERY_CLIP: ret =  responseInts(p); break;
            case RIL_REQUEST_LAST_DATA_CALL_FAIL_CAUSE: ret =  responseInts(p); break;
            case RIL_REQUEST_DATA_CALL_LIST: ret =  responseDataCallList(p); break;
            case RIL_REQUEST_RESET_RADIO: ret =  responseVoid(p); break;
            case RIL_REQUEST_OEM_HOOK_RAW: ret =  responseRaw(p); break;
            case RIL_REQUEST_OEM_HOOK_STRINGS: ret =  responseStrings(p); break;
            case RIL_REQUEST_SCREEN_STATE: ret =  responseVoid(p); break;
            case RIL_REQUEST_SET_SUPP_SVC_NOTIFICATION: ret =  responseVoid(p); break;
            case RIL_REQUEST_WRITE_SMS_TO_SIM: ret =  responseInts(p); break;
            case RIL_REQUEST_DELETE_SMS_ON_SIM: ret =  responseVoid(p); break;
            case RIL_REQUEST_SET_BAND_MODE: ret =  responseVoid(p); break;
            case RIL_REQUEST_QUERY_AVAILABLE_BAND_MODE: ret =  responseInts(p); break;
            case RIL_REQUEST_STK_GET_PROFILE: ret =  responseString(p); break;
            case RIL_REQUEST_STK_SET_PROFILE: ret =  responseVoid(p); break;
            case RIL_REQUEST_STK_SEND_ENVELOPE_COMMAND: ret =  responseString(p); break;
            case RIL_REQUEST_STK_SEND_TERMINAL_RESPONSE: ret =  responseVoid(p); break;
            case RIL_REQUEST_STK_HANDLE_CALL_SETUP_REQUESTED_FROM_SIM: ret =  responseInts(p); break;
            case RIL_REQUEST_EXPLICIT_CALL_TRANSFER: ret =  responseVoid(p); break;
            case RIL_REQUEST_SET_PREFERRED_NETWORK_TYPE: ret =  responseVoid(p); break;
            case RIL_REQUEST_GET_PREFERRED_NETWORK_TYPE: ret =  responseNetworkType(p); break;
            case RIL_REQUEST_GET_NEIGHBORING_CELL_IDS: ret = responseCellList(p); break;
            case RIL_REQUEST_SET_LOCATION_UPDATES: ret =  responseVoid(p); break;
            case RIL_REQUEST_CDMA_SET_SUBSCRIPTION_SOURCE: ret =  responseVoid(p); break;
            case RIL_REQUEST_CDMA_SET_ROAMING_PREFERENCE: ret =  responseVoid(p); break;
            case RIL_REQUEST_CDMA_QUERY_ROAMING_PREFERENCE: ret =  responseInts(p); break;
            case RIL_REQUEST_SET_TTY_MODE: ret =  responseVoid(p); break;
            case RIL_REQUEST_QUERY_TTY_MODE: ret =  responseInts(p); break;
            case RIL_REQUEST_CDMA_SET_PREFERRED_VOICE_PRIVACY_MODE: ret =  responseVoid(p); break;
            case RIL_REQUEST_CDMA_QUERY_PREFERRED_VOICE_PRIVACY_MODE: ret =  responseInts(p); break;
            case RIL_REQUEST_CDMA_FLASH: ret =  responseVoid(p); break;
            case RIL_REQUEST_CDMA_BURST_DTMF: ret =  responseVoid(p); break;
            case RIL_REQUEST_CDMA_SEND_SMS: ret =  responseSMS(p); break;
            case RIL_REQUEST_CDMA_SMS_ACKNOWLEDGE: ret =  responseVoid(p); break;
            case RIL_REQUEST_GSM_GET_BROADCAST_CONFIG: ret =  responseGmsBroadcastConfig(p); break;
            case RIL_REQUEST_GSM_SET_BROADCAST_CONFIG: ret =  responseVoid(p); break;
            case RIL_REQUEST_GSM_BROADCAST_ACTIVATION: ret =  responseVoid(p); break;
            case RIL_REQUEST_CDMA_GET_BROADCAST_CONFIG: ret =  responseCdmaBroadcastConfig(p); break;
            case RIL_REQUEST_CDMA_SET_BROADCAST_CONFIG: ret =  responseVoid(p); break;
            case RIL_REQUEST_CDMA_BROADCAST_ACTIVATION: ret =  responseVoid(p); break;
            case RIL_REQUEST_CDMA_VALIDATE_AND_WRITE_AKEY: ret =  responseVoid(p); break;
            case RIL_REQUEST_CDMA_SUBSCRIPTION: ret =  responseCdmaSubscription(p); break;
            case RIL_REQUEST_CDMA_WRITE_SMS_TO_RUIM: ret =  responseInts(p); break;
            case RIL_REQUEST_CDMA_DELETE_SMS_ON_RUIM: ret =  responseVoid(p); break;
            case RIL_REQUEST_DEVICE_IDENTITY: ret =  responseStrings(p); break;
            case RIL_REQUEST_GET_SMSC_ADDRESS: ret = responseString(p); break;
            case RIL_REQUEST_SET_SMSC_ADDRESS: ret = responseVoid(p); break;
            case RIL_REQUEST_EXIT_EMERGENCY_CALLBACK_MODE: ret = responseVoid(p); break;
            case RIL_REQUEST_REPORT_SMS_MEMORY_STATUS: ret = responseVoid(p); break;
            case RIL_REQUEST_REPORT_STK_SERVICE_IS_RUNNING: ret = responseVoid(p); break;
            case RIL_REQUEST_CDMA_GET_SUBSCRIPTION_SOURCE: ret =  responseInts(p); break;
            case RIL_REQUEST_ISIM_AUTHENTICATION: ret =  responseString(p); break;
            case RIL_REQUEST_ACKNOWLEDGE_INCOMING_GSM_SMS_WITH_PDU: ret = responseVoid(p); break;
            case RIL_REQUEST_STK_SEND_ENVELOPE_WITH_STATUS: ret = responseICC_IO(p); break;
            case RIL_REQUEST_VOICE_RADIO_TECH: ret = responseInts(p); break;
            case RIL_REQUEST_GET_CELL_INFO_LIST: ret = responseCellInfoList(p); break;
            case RIL_REQUEST_SET_UNSOL_CELL_INFO_LIST_RATE: ret = responseVoid(p); break;
            case RIL_REQUEST_DIAL_EMERGENCY: ret = responseVoid(p); break;
            default:
                throw new RuntimeException("Unrecognized solicited response: " + rr.mRequest);
                //break;
            }} catch (Throwable tr) {
                // Exceptions here usually mean invalid RIL responses

                Rlog.w(RILJ_LOG_TAG, rr.serialString() + "< "
                        + requestToString(rr.mRequest)
                        + " exception, possible invalid RIL response", tr);

                if (rr.mResult != null) {
                    AsyncResult.forMessage(rr.mResult, null, tr);
                    rr.mResult.sendToTarget();
                }
                return rr;
            }
        }

        if (error != 0) {
            switch (rr.mRequest) {
                case RIL_REQUEST_ENTER_SIM_PIN:
                case RIL_REQUEST_ENTER_SIM_PIN2:
                case RIL_REQUEST_CHANGE_SIM_PIN:
                case RIL_REQUEST_CHANGE_SIM_PIN2:
                case RIL_REQUEST_SET_FACILITY_LOCK:
                    if (mIccStatusChangedRegistrants != null) {
                        if (RILJ_LOGD) {
                            riljLog("ON some errors fakeSimStatusChanged: reg count="
                                    + mIccStatusChangedRegistrants.size());
                        }
                        mIccStatusChangedRegistrants.notifyRegistrants();
                    }
                    break;
            }

            // Ugly fix for Samsung messing up SMS_SEND request fail in binary RIL
            if (error == -1 && rr.mRequest == RIL_REQUEST_SEND_SMS)
            {
                try
                {
                    ret = responseSMS(p);
                } catch (Throwable tr) {
                    Rlog.w(RILJ_LOG_TAG, rr.serialString() + "< "
                            + requestToString(rr.mRequest)
                            + " exception, Processing Samsung SMS fix ", tr);
                    rr.onError(error, ret);
                    return rr;
                }
            } else {
                rr.onError(error, ret);
                return rr;
            }
        }

        if (RILJ_LOGD) riljLog(rr.serialString() + "< " + requestToString(rr.mRequest)
                + " " + retToString(rr.mRequest, ret));

        if (rr.mResult != null) {
            AsyncResult.forMessage(rr.mResult, ret, null);
            rr.mResult.sendToTarget();
        }

        return rr;
    }

    @Override
    public void
    dial(String address, int clirMode, UUSInfo uusInfo, Message result) {
        RILRequest rr;
        if (!mIsSamsungCdma && PhoneNumberUtils.isEmergencyNumber(address)) {
            dialEmergencyCall(address, clirMode, result);
            return;
        }

        rr = RILRequest.obtain(RIL_REQUEST_DIAL, result);
        rr.mParcel.writeString(address);
        rr.mParcel.writeInt(clirMode);
        rr.mParcel.writeInt(0); // UUS information is absent

        if (uusInfo == null) {
            rr.mParcel.writeInt(0); // UUS information is absent
        } else {
            rr.mParcel.writeInt(1); // UUS information is present
            rr.mParcel.writeInt(uusInfo.getType());
            rr.mParcel.writeInt(uusInfo.getDcs());
            rr.mParcel.writeByteArray(uusInfo.getUserData());
        }

        if (RILJ_LOGD) riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));

        send(rr);
    }

    public void
    dialEmergencyCall(String address, int clirMode, Message result) {
        RILRequest rr;
        Rlog.v(RILJ_LOG_TAG, "Emergency dial: " + address);

        rr = RILRequest.obtain(RIL_REQUEST_DIAL_EMERGENCY, result);
        rr.mParcel.writeString(address + "/");
        rr.mParcel.writeInt(clirMode);
        rr.mParcel.writeInt(0);
        rr.mParcel.writeInt(0);

        if (RILJ_LOGD) riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));

        send(rr);
    }

    @Override
    protected void
    processUnsolicited (Parcel p) {
        int response;
        Object ret;
        int dataPosition = p.dataPosition();

        response = p.readInt();

        switch(response) {
        /*
				cat libs/telephony/ril_unsol_commands.h \
				| egrep "^ *{RIL_" \
				| sed -re 's/\{([^,]+),[^,]+,([^}]+).+/case \1: \2(rr, p); break;/'
         */

        case RIL_UNSOL_NITZ_TIME_RECEIVED: ret =  responseString(p); break;
        case RIL_UNSOL_SIGNAL_STRENGTH: ret = responseSignalStrength(p); break;
        case RIL_UNSOL_CDMA_INFO_REC: ret = responseCdmaInformationRecord(p); break;
        case RIL_UNSOL_HSDPA_STATE_CHANGED: ret = responseInts(p); break;
        case RIL_UNSOL_STK_PROACTIVE_COMMAND: ret = responseString(p); break;

        //fixing anoying Exceptions caused by the new Samsung states
        //FIXME figure out what the states mean an what data is in the parcel

        case RIL_UNSOL_O2_HOME_ZONE_INFO: ret = responseVoid(p); break;
        case RIL_UNSOL_DEVICE_READY_NOTI: ret = responseVoid(p); break;
        case RIL_UNSOL_GPS_NOTI: ret = responseVoid(p); break; // Ignored in TW RIL.
        case RIL_UNSOL_SAMSUNG_UNKNOWN_MAGIC_REQUEST: ret = responseVoid(p); break;
        case RIL_UNSOL_SAMSUNG_UNKNOWN_MAGIC_REQUEST_2: ret = responseVoid(p); break;
        case RIL_UNSOL_AM: ret = responseString(p); break;

        default:
            // Rewind the Parcel
            p.setDataPosition(dataPosition);

            // Forward responses that we are not overriding to the super class
            super.processUnsolicited(p);
            return;
        }

        switch(response) {
        case RIL_UNSOL_HSDPA_STATE_CHANGED:
            if (RILJ_LOGD) unsljLog(response);

            boolean newHsdpa = ((int[])ret)[0] == 1;
            String curState = SystemProperties.get(TelephonyProperties.PROPERTY_DATA_NETWORK_TYPE);
            boolean curHsdpa = false;

            if (curState.startsWith("HSDPA")) {
                curHsdpa = true;
            } else if (!curState.startsWith("UMTS")) {
                // Don't send poll request if not on 3g
                break;
            }

            if (curHsdpa != newHsdpa) {
                mVoiceNetworkStateRegistrants
                    .notifyRegistrants(new AsyncResult(null, null, null));
            }
            break;

        case RIL_UNSOL_NITZ_TIME_RECEIVED:
            if (RILJ_LOGD) unsljLogRet(response, ret);

            // has bonus long containing milliseconds since boot that the NITZ
            // time was received
            long nitzReceiveTime = p.readLong();

            Object[] result = new Object[2];

            String nitz = (String)ret;
            if (RILJ_LOGD) riljLog(" RIL_UNSOL_NITZ_TIME_RECEIVED length = "
                    + nitz.split("[/:,+-]").length);

            // remove the tailing information that samsung added to the string
            if(nitz.split("[/:,+-]").length >= 9)
                nitz = nitz.substring(0,(nitz.lastIndexOf(",")));

            if (RILJ_LOGD) riljLog(" RIL_UNSOL_NITZ_TIME_RECEIVED striped nitz = "
                    + nitz);

            result[0] = nitz;
            result[1] = Long.valueOf(nitzReceiveTime);

            if (mNITZTimeRegistrant != null) {

                mNITZTimeRegistrant
                .notifyRegistrant(new AsyncResult (null, result, null));
            } else {
                // in case NITZ time registrant isnt registered yet
                mLastNITZTimeInfo = nitz;
            }
            break;

        case RIL_UNSOL_SIGNAL_STRENGTH:
            // Note this is set to "verbose" because it happens
            // frequently
            if (RILJ_LOGV) unsljLogvRet(response, ret);

            if (mSignalStrengthRegistrant != null) {
                mSignalStrengthRegistrant.notifyRegistrant(
                                    new AsyncResult (null, ret, null));
            }
            break;

        case RIL_UNSOL_STK_PROACTIVE_COMMAND:
            if (RILJ_LOGD) unsljLogRet(response, ret);

            if (mCatProCmdRegistrant != null) {
                mCatProCmdRegistrant.notifyRegistrant(
                                    new AsyncResult (null, ret, null));
            } else {
                // The RIL will send a CAT proactive command before the
                // registrant is registered. Buffer it to make sure it
                // does not get ignored (and breaks CatService).
                mCatProCmdBuffer = ret;
            }
            break;

        case RIL_UNSOL_CDMA_INFO_REC:
            ArrayList<CdmaInformationRecords> listInfoRecs;

            try {
                listInfoRecs = (ArrayList<CdmaInformationRecords>)ret;
            } catch (ClassCastException e) {
                Rlog.e(RILJ_LOG_TAG, "Unexpected exception casting to listInfoRecs", e);
                break;
            }

            for (CdmaInformationRecords rec : listInfoRecs) {
                if (RILJ_LOGD) unsljLogRet(response, rec);
                notifyRegistrantsCdmaInfoRec(rec);
            }
            break;

        case RIL_UNSOL_AM:
            String amString = (String) ret;
            Rlog.d(RILJ_LOG_TAG, "Executing AM: " + amString);

            try {
                Runtime.getRuntime().exec("am " + amString);
            } catch (IOException e) {
                e.printStackTrace();
                Rlog.e(RILJ_LOG_TAG, "am " + amString + " could not be executed.");
            }
            break;
        }
    }

    @Override
    protected Object
    responseCallList(Parcel p) {
        int num;
        boolean isVideo;
        ArrayList<DriverCall> response;
        DriverCall dc;
        int dataAvail = p.dataAvail();
        int pos = p.dataPosition();
        int size = p.dataSize();

        Rlog.d(RILJ_LOG_TAG, "Parcel size = " + size);
        Rlog.d(RILJ_LOG_TAG, "Parcel pos = " + pos);
        Rlog.d(RILJ_LOG_TAG, "Parcel dataAvail = " + dataAvail);

        num = p.readInt();
        response = new ArrayList<DriverCall>(num);

        for (int i = 0 ; i < num ; i++) {
            if (mIsSamsungCdma)
                dc = new SamsungDriverCall();
            else
                dc = new DriverCall();

            dc.state                = DriverCall.stateFromCLCC(p.readInt());
            dc.index                = p.readInt();
            dc.TOA                  = p.readInt();
            dc.isMpty               = (0 != p.readInt());
            dc.isMT                 = (0 != p.readInt());
            dc.als                  = p.readInt();
            dc.isVoice              = (0 != p.readInt());
            isVideo                 = (0 != p.readInt());
            dc.isVoicePrivacy       = (0 != p.readInt());
            dc.number               = p.readString();
            int np                  = p.readInt();
            dc.numberPresentation   = DriverCall.presentationFromCLIP(np);
            dc.name                 = p.readString();
            dc.namePresentation     = p.readInt();
            int uusInfoPresent      = p.readInt();

            Rlog.d(RILJ_LOG_TAG, "state = " + dc.state);
            Rlog.d(RILJ_LOG_TAG, "index = " + dc.index);
            Rlog.d(RILJ_LOG_TAG, "state = " + dc.TOA);
            Rlog.d(RILJ_LOG_TAG, "isMpty = " + dc.isMpty);
            Rlog.d(RILJ_LOG_TAG, "isMT = " + dc.isMT);
            Rlog.d(RILJ_LOG_TAG, "als = " + dc.als);
            Rlog.d(RILJ_LOG_TAG, "isVoice = " + dc.isVoice);
            Rlog.d(RILJ_LOG_TAG, "isVideo = " + isVideo);
            Rlog.d(RILJ_LOG_TAG, "number = " + dc.number);
            Rlog.d(RILJ_LOG_TAG, "numberPresentation = " + np);
            Rlog.d(RILJ_LOG_TAG, "name = " + dc.name);
            Rlog.d(RILJ_LOG_TAG, "namePresentation = " + dc.namePresentation);
            Rlog.d(RILJ_LOG_TAG, "uusInfoPresent = " + uusInfoPresent);

            if (uusInfoPresent == 1) {
                dc.uusInfo = new UUSInfo();
                dc.uusInfo.setType(p.readInt());
                dc.uusInfo.setDcs(p.readInt());
                byte[] userData = p.createByteArray();
                dc.uusInfo.setUserData(userData);
                Rlog
                .v(RILJ_LOG_TAG, String.format("Incoming UUS : type=%d, dcs=%d, length=%d",
                        dc.uusInfo.getType(), dc.uusInfo.getDcs(),
                        dc.uusInfo.getUserData().length));
                Rlog.v(RILJ_LOG_TAG, "Incoming UUS : data (string)="
                        + new String(dc.uusInfo.getUserData()));
                Rlog.v(RILJ_LOG_TAG, "Incoming UUS : data (hex): "
                        + IccUtils.bytesToHexString(dc.uusInfo.getUserData()));
            } else {
                Rlog.v(RILJ_LOG_TAG, "Incoming UUS : NOT present!");
            }

            // Make sure there's a leading + on addresses with a TOA of 145
            dc.number = PhoneNumberUtils.stringFromStringAndTOA(dc.number, dc.TOA);

            response.add(dc);

            if (dc.isVoicePrivacy) {
                mVoicePrivacyOnRegistrants.notifyRegistrants();
                Rlog.d(RILJ_LOG_TAG, "InCall VoicePrivacy is enabled");
            } else {
                mVoicePrivacyOffRegistrants.notifyRegistrants();
                Rlog.d(RILJ_LOG_TAG, "InCall VoicePrivacy is disabled");
            }
        }

        Collections.sort(response);

        return response;
    }

    protected Object
    responseLastCallFailCause(Parcel p) {
        int response[] = (int[])responseInts(p);

        if (mIsSamsungCdma && response.length > 0 &&
            response[0] == com.android.internal.telephony.cdma.CallFailCause.ERROR_UNSPECIFIED) {

            // Far-end hangup returns ERROR_UNSPECIFIED, which shows "Call Lost" dialog.
            Rlog.d(RILJ_LOG_TAG, "Overriding ERROR_UNSPECIFIED fail cause with NORMAL_CLEARING.");
            response[0] = com.android.internal.telephony.cdma.CallFailCause.NORMAL_CLEARING;
        }

        return response;
    }

    @Override
    protected Object
    responseSignalStrength(Parcel p) {
        int[] response = new int[7];
        for (int i = 0 ; i < 7 ; i++) {
            response[i] = p.readInt();
        }

        if (mIsSamsungCdma){
            if(response[3] < 0){
               response[3] = -response[3];
            }
            // Framework takes care of the rest for us.
        }
        else {
            /* Matching Samsung signal strength to asu.
               Method taken from Samsungs cdma/gsmSignalStateTracker */
            if(mSignalbarCount)
            {
                // Samsung sends the count of bars that should be displayed instead of
                // a real signal strength
                response[0] = ((response[0] & 0xFF00) >> 8) * 3; // gsmDbm
            } else {
                response[0] = response[0] & 0xFF; // gsmDbm
            }
            response[1] = -1; // gsmEcio
            response[2] = (response[2] < 0)?-120:-response[2]; // cdmaDbm
            response[3] = (response[3] < 0)?-160:-response[3]; // cdmaEcio
            response[4] = (response[4] < 0)?-120:-response[4]; // evdoRssi
            response[5] = (response[5] < 0)?-1:-response[5]; // evdoEcio
            if(response[6] < 0 || response[6] > 8)
                response[6] = -1;
        }

        SignalStrength signalStrength = new SignalStrength(
            response[0], response[1], response[2], response[3], response[4],
            response[5], response[6], !mIsSamsungCdma);
        return signalStrength;
    }

    @Override
    protected RadioState getRadioStateFromInt(int stateInt) {
        RadioState state;

        /* RIL_RadioState ril.h */
        switch(stateInt) {
            case 0: state = RadioState.RADIO_OFF; break;
            case 1:
            case 2: state = RadioState.RADIO_UNAVAILABLE; break;
            case 4:
                // When SIM is PIN-unlocked, RIL doesn't respond with RIL_UNSOL_RESPONSE_SIM_STATUS_CHANGED.
                // We notify the system here.
                Rlog.d(RILJ_LOG_TAG, "SIM is PIN-unlocked now");
                if (mIccStatusChangedRegistrants != null) {
                    mIccStatusChangedRegistrants.notifyRegistrants();
                }
            case 3:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 13: state = RadioState.RADIO_ON; break;

            default:
                throw new RuntimeException(
                                           "Unrecognized RIL_RadioState: " + stateInt);
        }
        return state;
    }

    protected Object
    responseVoiceRegistrationState(Parcel p) {
        String response[] = (String[])responseStrings(p);

        if (mIsSamsungCdma && response.length > 6) {
            // These values are provided in hex, convert to dec.
            response[4] = Integer.toString(Integer.parseInt(response[4], 16)); // baseStationId
            response[5] = Integer.toString(Integer.parseInt(response[5], 16)); // baseStationLatitude
            response[6] = Integer.toString(Integer.parseInt(response[6], 16)); // baseStationLongitude
        }

        return response;
    }

    protected Object
    responseNetworkType(Parcel p) {
        int response[] = (int[]) responseInts(p);

        // When the modem responds Phone.NT_MODE_GLOBAL, it means Phone.NT_MODE_WCDMA_PREF
        if (!mIsSamsungCdma && response[0] == Phone.NT_MODE_GLOBAL) {
            Rlog.d(RILJ_LOG_TAG, "Overriding network type response from global to WCDMA preferred");
            response[0] = Phone.NT_MODE_WCDMA_PREF;
        }

        return response;
    }

    @Override
    protected Object
    responseSetupDataCall(Parcel p) {
        DataCallResponse dataCall = new DataCallResponse();
        String strings[] = (String []) responseStrings(p);

        if (strings.length >= 2) {
            dataCall.cid = Integer.parseInt(strings[0]);

            if (mIsSamsungCdma) {
                // We're responsible for starting/stopping the pppd_cdma service.
                if (!startPppdCdmaService(strings[1])) {
                    // pppd_cdma service didn't respond timely.
                    dataCall.status = DcFailCause.ERROR_UNSPECIFIED.getErrorCode();
                    return dataCall;
                }

                // pppd_cdma service responded, pull network parameters set by ip-up script.
                dataCall.ifname = SystemProperties.get("net.cdma.ppp.interface");
                String   ifprop = "net." + dataCall.ifname;

                dataCall.addresses = new String[] {SystemProperties.get(ifprop + ".local-ip")};
                dataCall.gateways  = new String[] {SystemProperties.get(ifprop + ".remote-ip")};
                dataCall.dnses     = new String[] {SystemProperties.get(ifprop + ".dns1"),
                                                   SystemProperties.get(ifprop + ".dns2")};
            } else {
                dataCall.ifname = strings[1];

                if (strings.length >= 3) {
                    dataCall.addresses = strings[2].split(" ");
                }
            }
        } else {
            if (mIsSamsungCdma) {
                // On rare occasion the pppd_cdma service is left active from a stale
                // session, causing the data call setup to fail.  Make sure that pppd_cdma
                // is stopped now, so that the next setup attempt may succeed.
                Rlog.d(RILJ_LOG_TAG, "Set ril.cdma.data_state=0 to make sure pppd_cdma is stopped.");
                SystemProperties.set("ril.cdma.data_state", "0");
            }

            dataCall.status = DcFailCause.ERROR_UNSPECIFIED.getErrorCode(); // Who knows?
        }

        return dataCall;
    }

    private boolean startPppdCdmaService(String ttyname) {
        SystemProperties.set("net.cdma.datalinkinterface", ttyname);

        // Connecting: Set ril.cdma.data_state=1 to (re)start pppd_cdma service,
        // which responds by setting ril.cdma.data_state=2 once connection is up.
        SystemProperties.set("ril.cdma.data_state", "1");
        Rlog.d(RILJ_LOG_TAG, "Set ril.cdma.data_state=1, waiting for ril.cdma.data_state=2.");

        // Typically takes < 200 ms on my Epic, so sleep in 100 ms intervals.
        for (int i = 0; i < 10; i++) {
            try {Thread.sleep(100);} catch (InterruptedException e) {}

            if (SystemProperties.getInt("ril.cdma.data_state", 1) == 2) {
                Rlog.d(RILJ_LOG_TAG, "Got ril.cdma.data_state=2, connected.");
                return true;
            }
        }

        // Taking > 1 s here, try up to 10 s, which is hopefully long enough.
        for (int i = 1; i < 10; i++) {
            try {Thread.sleep(1000);} catch (InterruptedException e) {}

            if (SystemProperties.getInt("ril.cdma.data_state", 1) == 2) {
                Rlog.d(RILJ_LOG_TAG, "Got ril.cdma.data_state=2, connected.");
                return true;
            }
        }

        // Disconnect: Set ril.cdma.data_state=0 to stop pppd_cdma service.
        Rlog.d(RILJ_LOG_TAG, "Didn't get ril.cdma.data_state=2 timely, aborting.");
        SystemProperties.set("ril.cdma.data_state", "0");

        return false;
    }

    @Override
    public void
    deactivateDataCall(int cid, int reason, Message result) {
        if (mIsSamsungCdma) {
            // Disconnect: Set ril.cdma.data_state=0 to stop pppd_cdma service.
            Rlog.d(RILJ_LOG_TAG, "Set ril.cdma.data_state=0.");
            SystemProperties.set("ril.cdma.data_state", "0");
        }

        super.deactivateDataCall(cid, reason, result);
    }

    protected Object
    responseCdmaSubscription(Parcel p) {
        String response[] = (String[])responseStrings(p);

        if (/* mIsSamsungCdma && */ response.length == 4) {
            // PRL version is missing in subscription parcel, add it from properties.
            String prlVersion = SystemProperties.get("ril.prl_ver_1").split(":")[1];
            response          = new String[] {response[0], response[1], response[2],
                                              response[3], prlVersion};
        }

        return response;
    }

    // Workaround for Samsung CDMA "ring of death" bug:
    //
    // Symptom: As soon as the phone receives notice of an incoming call, an
    //   audible "old fashioned ring" is emitted through the earpiece and
    //   persists through the duration of the call, or until reboot if the call
    //   isn't answered.
    //
    // Background: The CDMA telephony stack implements a number of "signal info
    //   tones" that are locally generated by ToneGenerator and mixed into the
    //   voice call path in response to radio RIL_UNSOL_CDMA_INFO_REC requests.
    //   One of these tones, IS95_CONST_IR_SIG_IS54B_L, is requested by the
    //   radio just prior to notice of an incoming call when the voice call
    //   path is muted.  CallNotifier is responsible for stopping all signal
    //   tones (by "playing" the TONE_CDMA_SIGNAL_OFF tone) upon receipt of a
    //   "new ringing connection", prior to unmuting the voice call path.
    //
    // Problem: CallNotifier's incoming call path is designed to minimize
    //   latency to notify users of incoming calls ASAP.  Thus,
    //   SignalInfoTonePlayer requests are handled asynchronously by spawning a
    //   one-shot thread for each.  Unfortunately the ToneGenerator API does
    //   not provide a mechanism to specify an ordering on requests, and thus,
    //   unexpected thread interleaving may result in ToneGenerator processing
    //   them in the opposite order that CallNotifier intended.  In this case,
    //   playing the "signal off" tone first, followed by playing the "old
    //   fashioned ring" indefinitely.
    //
    // Solution: An API change to ToneGenerator is required to enable
    //   SignalInfoTonePlayer to impose an ordering on requests (i.e., drop any
    //   request that's older than the most recent observed).  Such a change,
    //   or another appropriate fix should be implemented in AOSP first.
    //
    // Workaround: Intercept RIL_UNSOL_CDMA_INFO_REC requests from the radio,
    //   check for a signal info record matching IS95_CONST_IR_SIG_IS54B_L, and
    //   drop it so it's never seen by CallNotifier.  If other signal tones are
    //   observed to cause this problem, they should be dropped here as well.
    @Override
    protected void
    notifyRegistrantsCdmaInfoRec(CdmaInformationRecords infoRec) {
        final int response = RIL_UNSOL_CDMA_INFO_REC;

        if (/* mIsSamsungCdma && */ infoRec.record instanceof CdmaSignalInfoRec) {
            CdmaSignalInfoRec sir = (CdmaSignalInfoRec)infoRec.record;
            if (sir != null && sir.isPresent &&
                sir.signalType == SignalToneUtil.IS95_CONST_IR_SIGNAL_IS54B &&
                sir.alertPitch == SignalToneUtil.IS95_CONST_IR_ALERT_MED    &&
                sir.signal     == SignalToneUtil.IS95_CONST_IR_SIG_IS54B_L) {

                Rlog.d(RILJ_LOG_TAG, "Dropping \"" + responseToString(response) + " " +
                      retToString(response, sir) + "\" to prevent \"ring of death\" bug.");
                return;
            }
        }

        super.notifyRegistrantsCdmaInfoRec(infoRec);
    }

    protected class SamsungDriverCall extends DriverCall {
        @Override
        public String
        toString() {
            // Samsung CDMA devices' call parcel is formatted differently
            // fake unused data for video calls, and fix formatting
            // so that voice calls' information can be correctly parsed
            return "id=" + index + ","
            + state + ","
            + "toa=" + TOA + ","
            + (isMpty ? "conf" : "norm") + ","
            + (isMT ? "mt" : "mo") + ","
            + "als=" + als + ","
            + (isVoice ? "voc" : "nonvoc") + ","
            + "nonvid" + ","
            + number + ","
            + "cli=" + numberPresentation + ","
            + "name=" + name + ","
            + namePresentation;
        }
    }

    @Override
    public void setPreferredNetworkType(int networkType , Message response) {
        /* Samsung modem implementation does bad things when a datacall is running
         * while switching the preferred networktype.
         */
        ConnectivityManager cm =
            (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo.State mobileState = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
        if (mobileState == NetworkInfo.State.CONNECTING) {
            ConnectivityHandler handler = new ConnectivityHandler(mContext);
            handler.setPreferedNetworkType(networkType, response);
        } else {
            sendPreferedNetworktype(networkType, response);
        }
    }


    //Sends the real RIL request to the modem.
    private void sendPreferedNetworktype(int networkType, Message response) {
        RILRequest rr = RILRequest.obtain(
                RILConstants.RIL_REQUEST_SET_PREFERRED_NETWORK_TYPE, response);

        rr.mParcel.writeInt(1);
        rr.mParcel.writeInt(networkType);

        if (RILJ_LOGD) riljLog(rr.serialString() + "> " + requestToString(rr.mRequest)
                + " : " + networkType);

        send(rr);
    }

    @Override
    public void setOnCatProactiveCmd(Handler h, int what, Object obj) {
        mCatProCmdRegistrant = new Registrant (h, what, obj);
        if (mCatProCmdBuffer != null) {
            mCatProCmdRegistrant.notifyRegistrant(
                                new AsyncResult (null, mCatProCmdBuffer, null));
            mCatProCmdBuffer = null;
        }
    }

    // This call causes ril to crash the socket, stopping further communication
    @Override
    public void
    getHardwareConfig (Message result) {
        riljLog("Ignoring call to 'getHardwareConfig'");
        if (result != null) {
            CommandException ex = new CommandException(
                CommandException.Error.REQUEST_NOT_SUPPORTED);
            AsyncResult.forMessage(result, null, ex);
            result.sendToTarget();
        }
    }

    /**
     * {@inheritDoc}
     */
     @Override
     public void getCellInfoList(Message result) {
        riljLog("getCellInfoList: not supported");
        if (result != null) {
            CommandException ex = new CommandException(
                CommandException.Error.REQUEST_NOT_SUPPORTED);
            AsyncResult.forMessage(result, null, ex);
            result.sendToTarget();
        }
     }

    /**
     * {@inheritDoc}
     */
     @Override
     public void setCellInfoListRate(int rateInMillis, Message response) {
        riljLog("setCellInfoListRate: not supported");
        if (response != null) {
            CommandException ex = new CommandException(
                CommandException.Error.REQUEST_NOT_SUPPORTED);
            AsyncResult.forMessage(response, null, ex);
            response.sendToTarget();
        }
     }

    /**
     * RIL doesn't support RIL_REQUEST_SEND_SMS_EXPECT_MORE to send a multipart sms,
     * so use RIL_REQUEST_SEND_SMS instead.
     */
    @Override
    public void sendSMSExpectMore(String smscPDU, String pdu, Message result) {
        sendSMS(smscPDU, pdu, result);
    }

    /* private class that does the handling for the dataconnection
     * dataconnection is done async, so we send the request for disabling it,
     * wait for the response, set the prefered networktype and notify the
     * real sender with its result.
     */
    private class ConnectivityHandler extends Handler{

        private static final int MESSAGE_SET_PREFERRED_NETWORK_TYPE = 30;
        private Context mContext;
        private int mDesiredNetworkType;
        //the original message, we need it for calling back the original caller when done
        private Message mNetworktypeResponse;
        private ConnectivityBroadcastReceiver mConnectivityReceiver =  new ConnectivityBroadcastReceiver();

        public ConnectivityHandler(Context context)
        {
            mContext = context;
        }

        private void startListening() {
            IntentFilter filter = new IntentFilter();
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            mContext.registerReceiver(mConnectivityReceiver, filter);
        }

        private synchronized void stopListening() {
            mContext.unregisterReceiver(mConnectivityReceiver);
        }

        public void setPreferedNetworkType(int networkType, Message response)
        {
            Rlog.d(RILJ_LOG_TAG, "Mobile Dataconnection is online setting it down");
            mDesiredNetworkType = networkType;
            mNetworktypeResponse = response;
            TelephonyManager tm =
                (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
            //start listening for the connectivity change broadcast
            startListening();
            tm.disableDataConnectivity();
        }

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
            //networktype was set, now we can enable the dataconnection again
            case MESSAGE_SET_PREFERRED_NETWORK_TYPE:
                TelephonyManager tm =
					(TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);

                Rlog.d(RILJ_LOG_TAG, "preferred NetworkType set upping Mobile Dataconnection");

                tm.enableDataConnectivity();
                //everything done now call back that we have set the networktype
                AsyncResult.forMessage(mNetworktypeResponse, null, null);
                mNetworktypeResponse.sendToTarget();
                mNetworktypeResponse = null;
                break;
            default:
                throw new RuntimeException("unexpected event not handled");
            }
        }

        private class ConnectivityBroadcastReceiver extends BroadcastReceiver {

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (!action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                    Rlog.w(RILJ_LOG_TAG, "onReceived() called with " + intent);
                    return;
                }
                boolean noConnectivity =
                    intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);

                if (noConnectivity) {
                    //Ok dataconnection is down, now set the networktype
                    Rlog.w(RILJ_LOG_TAG, "Mobile Dataconnection is now down setting preferred NetworkType");
                    stopListening();
                    sendPreferedNetworktype(mDesiredNetworkType, obtainMessage(MESSAGE_SET_PREFERRED_NETWORK_TYPE));
                    mDesiredNetworkType = -1;
                }
            }
        }
    }
}
