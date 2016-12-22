/*
 * Copyright (C) 2014 The Android Open Source Project
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

package android.telecom;

import android.os.Parcel;
import android.os.Parcelable;
import android.media.ToneGenerator;
import android.text.TextUtils;

import java.util.Objects;

/**
 * Describes the cause of a disconnected call. This always includes a code describing the generic
 * cause of the disconnect. Optionally, it may include a label and/or description to display to the
 * user. It is the responsibility of the {@link ConnectionService} to provide localized versions of
 * the label and description. It also may contain a reason for the disconnect, which is intended for
 * logging and not for display to the user.
 */
public final class DisconnectCause implements Parcelable {

    /** Disconnected because of an unknown or unspecified reason. */
    public static final int UNKNOWN = 0;
    /** Disconnected because there was an error, such as a problem with the network. */
    public static final int ERROR = 1;
    /** Disconnected because of a local user-initiated action, such as hanging up. */
    public static final int LOCAL = 2;
    /**
     * Disconnected because of a remote user-initiated action, such as the other party hanging up
     * up.
     */
    public static final int REMOTE = 3;
    /** Disconnected because it has been canceled. */
    public static final int CANCELED = 4;
    /** Disconnected because there was no response to an incoming call. */
    public static final int MISSED = 5;
    /** Disconnected because the user rejected an incoming call. */
    public static final int REJECTED = 6;
    /** Disconnected because the other party was busy. */
    public static final int BUSY = 7;
    /**
     * Disconnected because of a restriction on placing the call, such as dialing in airplane
     * mode.
     */
    public static final int RESTRICTED = 8;
    /** Disconnected for reason not described by other disconnect codes. */
    public static final int OTHER = 9;
    /**
     * Disconnected because the connection manager did not support the call. The call will be tried
     * again without a connection manager. See {@link PhoneAccount#CAPABILITY_CONNECTION_MANAGER}.
     */
    public static final int CONNECTION_MANAGER_NOT_SUPPORTED = 10;

    //@darren 20151012 add for common clearcode
   /**
     * @hide
     */
    public static final int UNOBTAINABLE_NUMBER = 11;
   /**
     * @hide
     */
    public static final int CONGESTION = 12;
   /**
     * @hide
     */
    public static final int LIMIT_EXCEEDED = 13;
   /**
     * @hide
     */
    public static final int NO_USER_RESPONDING = 14;
   /**
     * @hide
     */
    public static final int USER_ALERTING_NO_ANSWER = 15;
   /**
     * @hide
     */
    public static final int NO_ROUTE_TO_DESTINATION = 16;
   /**
     * @hide
     */
    public static final int NORMAL_CLEARING = 17;
   /**
     * @hide
     */
    public static final int CALL_REJECTED = 18;
   /**
     * @hide
     */
    public static final int NUMBER_CHANGED = 19;
   /**
     * @hide
     */
    public static final int INVALID_NUMBER_FORMAT = 20;
   /**
     * @hide
     */
    public static final int FACILITY_REJECTED = 21;
   /**
     * @hide
     */
    public static final int STATUS_ENQUIRY = 22;
   /**
     * @hide
     */
    public static final int NORMAL_UNSPECIFIED = 23;
   /**
     * @hide
     */
    public static final int NO_CIRCUIT_AVAIL = 24;
   /**
     * @hide
     */
    public static final int NETWORK_OUT_OF_ORDER = 25;
   /**
     * @hide
     */
    public static final int SWITCHING_CONGESTION = 26;
   /**
     * @hide
     */
    public static final int CHANNEL_NOT_AVAIL = 27;
   /**
     * @hide
     */
    public static final int RESOURCE_UNAVAILABLE = 28;
   /**
     * @hide
     */
    public static final int QOS_NOT_AVAIL = 29;
   /**
     * @hide
     */
    public static final int BEARER_NOT_AVAIL = 30;
   /**
     * @hide
     */
    public static final int BEARER_NOT_AUTHORIZED = 31;
   /**
     * @hide
     */
    public static final int SERVICE_NOT_AVAILABLE = 32;
   /**
     * @hide
     */
    public static final int BEARER_NOT_IMPLEMENT = 33;
   /**
     * @hide
     */
    public static final int FACILITY_NOT_IMPLEMENT = 34;
   /**
     * @hide
     */
    public static final int RESTRICTED_BEARER_AVAILABLE = 35;
   /**
     * @hide
     */
    public static final int OPTION_NOT_AVAILABLE = 36;
   /**
     * @hide
     */
    public static final int INCOMPATIBLE_DESTINATION = 37;
   /**
     * @hide
     */
    public static final int CHANNEL_UNACCEPTABLE = 38;
   /**
     * @hide
     */
    public static final int OPERATOR_DETERMINED_BARRING = 39;
   /**
     * @hide
     */
    public static final int PRE_EMPTION = 40;
   /**
     * @hide
     */
    public static final int NON_SELECTED_USER_CLEARING = 41;
   /**
     * @hide
     */
    public static final int DESTINATION_OUT_OF_ORDER = 42;
   /**
     * @hide
     */
    public static final int ACCESS_INFORMATION_DISCARDED = 43;
   /**
     * @hide
     */
    public static final int REQUESTED_FACILITY_NOT_SUBSCRIBED = 44;
   /**
     * @hide
     */
    public static final int INCOMING_CALL_BARRED_WITHIN_CUG = 45;
   /**
     * @hide
     */
    public static final int INVALID_TRANSACTION_ID_VALUE = 46;
   /**
     * @hide
     */
    public static final int USER_NOT_MEMBER_OF_CUG = 47;
   /**
     * @hide
     */
    public static final int INVALID_TRANSIT_NETWORK_SELECTION = 48;
   /**
     * @hide
     */
    public static final int SEMANTICALLY_INCORRECT_MESSAGE = 49;
   /**
     * @hide
     */
    public static final int INVALID_MANDATORY_INFORMATION = 50;
   /**
     * @hide
     */
    public static final int MESSAGE_TYPE_NON_EXISTENT = 51;
   /**
     * @hide
     */
    public static final int MESSAGE_TYPE_NOT_COMPATIBLE_WITH_PROT_STATE = 52;	
   /**
     * @hide
     */
    public static final int IE_NON_EXISTENT_OR_NOT_IMPLEMENTED = 53;	
   /**
     * @hide
     */
    public static final int CONDITIONAL_IE_ERROR = 54;	
   /**
     * @hide
     */
    public static final int MESSAGE_NOT_COMPATIBLE_WITH_PROTOCOL_STATE = 55;	
   /**
     * @hide
     */
    public static final int RECOVERY_ON_TIMER_EXPIRY = 56;	
   /**
     * @hide
     */
    public static final int PROTOCOL_ERROR_UNSPECIFIED = 57;	
   /**
     * @hide
     */
    public static final int INTERWORKING_UNSPECIFIED = 58;	
    //@darren add end	
	
	///M: WFC @{
    /**
     * @hide  Disconnected because of WFC related reasons
     * mode.
     */
    public static final int WFC_CALL_ERROR = 59;
    ///@}
    private int mDisconnectCode;
    private CharSequence mDisconnectLabel;
    private CharSequence mDisconnectDescription;
    private String mDisconnectReason;
    private int mToneToPlay;

    /**
     * Creates a new DisconnectCause.
     *
     * @param code The code for the disconnect cause.
     */
    public DisconnectCause(int code) {
        this(code, null, null, null, ToneGenerator.TONE_UNKNOWN);
    }

    /**
     * Creates a new DisconnectCause.
     *
     * @param code The code for the disconnect cause.
     * @param reason The reason for the disconnect.
     */
    public DisconnectCause(int code, String reason) {
        this(code, null, null, reason, ToneGenerator.TONE_UNKNOWN);
    }

    /**
     * Creates a new DisconnectCause.
     *
     * @param label The localized label to show to the user to explain the disconnect.
     * @param code The code for the disconnect cause.
     * @param description The localized description to show to the user to explain the disconnect.
     * @param reason The reason for the disconnect.
     */
    public DisconnectCause(int code, CharSequence label, CharSequence description, String reason) {
        this(code, label, description, reason, ToneGenerator.TONE_UNKNOWN);
    }

    /**
     * Creates a new DisconnectCause.
     *
     * @param code The code for the disconnect cause.
     * @param label The localized label to show to the user to explain the disconnect.
     * @param description The localized description to show to the user to explain the disconnect.
     * @param reason The reason for the disconnect.
     * @param toneToPlay The tone to play on disconnect, as defined in {@link ToneGenerator}.
     */
    public DisconnectCause(int code, CharSequence label, CharSequence description, String reason,
            int toneToPlay) {
        mDisconnectCode = code;
        mDisconnectLabel = label;
        mDisconnectDescription = description;
        mDisconnectReason = reason;
        mToneToPlay = toneToPlay;
    }

    /**
     * Returns the code for the reason for this disconnect.
     *
     * @return The disconnect code.
     */
    public int getCode() {
        return mDisconnectCode;
    }

    /**
     * Returns a short label which explains the reason for the disconnect cause and is for display
     * in the user interface. If not null, it is expected that the In-Call UI should display this
     * text where it would normally display the call state ("Dialing", "Disconnected") and is
     * therefore expected to be relatively small. The {@link ConnectionService } is responsible for
     * providing and localizing this label. If there is no string provided, returns null.
     *
     * @return The disconnect label.
     */
    public CharSequence getLabel() {
        return mDisconnectLabel;
    }

    /**
     * Returns a description which explains the reason for the disconnect cause and is for display
     * in the user interface. This optional text is generally a longer and more descriptive version
     * of {@link #getLabel}, however it can exist even if {@link #getLabel} is empty. The In-Call UI
     * should display this relatively prominently; the traditional implementation displays this as
     * an alert dialog. The {@link ConnectionService} is responsible for providing and localizing
     * this message. If there is no string provided, returns null.
     *
     * @return The disconnect description.
     */
    public CharSequence getDescription() {
        return mDisconnectDescription;
    }

    /**
     * Returns an explanation of the reason for the disconnect. This is not intended for display to
     * the user and is used mainly for logging.
     *
     * @return The disconnect reason.
     */
    public String getReason() {
        return mDisconnectReason;
    }

    /**
     * Returns the tone to play when disconnected.
     *
     * @return the tone as defined in {@link ToneGenerator} to play when disconnected.
     */
    public int getTone() {
        return mToneToPlay;
    }

    public static final Creator<DisconnectCause> CREATOR = new Creator<DisconnectCause>() {
        @Override
        public DisconnectCause createFromParcel(Parcel source) {
            int code = source.readInt();
            CharSequence label = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
            CharSequence description = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
            String reason = source.readString();
            int tone = source.readInt();
            return new DisconnectCause(code, label, description, reason, tone);
        }

        @Override
        public DisconnectCause[] newArray(int size) {
            return new DisconnectCause[size];
        }
    };

    @Override
    public void writeToParcel(Parcel destination, int flags) {
        destination.writeInt(mDisconnectCode);
        TextUtils.writeToParcel(mDisconnectLabel, destination, flags);
        TextUtils.writeToParcel(mDisconnectDescription, destination, flags);
        destination.writeString(mDisconnectReason);
        destination.writeInt(mToneToPlay);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mDisconnectCode)
                + Objects.hashCode(mDisconnectLabel)
                + Objects.hashCode(mDisconnectDescription)
                + Objects.hashCode(mDisconnectReason)
                + Objects.hashCode(mToneToPlay);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof DisconnectCause) {
            DisconnectCause d = (DisconnectCause) o;
            return Objects.equals(mDisconnectCode, d.getCode())
                    && Objects.equals(mDisconnectLabel, d.getLabel())
                    && Objects.equals(mDisconnectDescription, d.getDescription())
                    && Objects.equals(mDisconnectReason, d.getReason())
                    && Objects.equals(mToneToPlay, d.getTone());
        }
        return false;
    }

    @Override
    public String toString() {
        String code = "";
        switch (mDisconnectCode) {
            case UNKNOWN:
                code = "UNKNOWN";
                break;
            case ERROR:
                code = "ERROR";
                break;
            case LOCAL:
                code = "LOCAL";
                break;
            case REMOTE:
                code = "REMOTE";
                break;
            case CANCELED:
                code = "CANCELED";
                break;
            case MISSED:
                code = "MISSED";
                break;
            case REJECTED:
                code = "REJECTED";
                break;
            case BUSY:
                code = "BUSY";
                break;
            case RESTRICTED:
                code = "RESTRICTED";
                break;
            case OTHER:
                code = "OTHER";
                break;
            case CONNECTION_MANAGER_NOT_SUPPORTED:
                code = "CONNECTION_MANAGER_NOT_SUPPORTED";
                break;
	    //@darren 20151012 add for common clearcode	    
	    case UNOBTAINABLE_NUMBER:
                code = "UNOBTAINABLE_NUMBER";
                break;
	    case CONGESTION:
                code = "CONGESTION";
                break;
	    case LIMIT_EXCEEDED:
                code = "LIMIT_EXCEEDED";
                break;
	    case NO_USER_RESPONDING:
                code = "NO_USER_RESPONDING";
                break;
	    case USER_ALERTING_NO_ANSWER:
                code = "USER_ALERTING_NO_ANSWER";
                break;
	    case NO_ROUTE_TO_DESTINATION:
                code = "NO_ROUTE_TO_DESTINATION";
                break;
	    case NORMAL_CLEARING:
                code = "NORMAL_CLEARING";
                break;
	    case CALL_REJECTED:
                code = "CALL_REJECTED";
                break;
	    case NUMBER_CHANGED:
                code = "NUMBER_CHANGED";
                break;
	    case INVALID_NUMBER_FORMAT:
                code = "INVALID_NUMBER_FORMAT";
                break;
	    case FACILITY_REJECTED:
                code = "FACILITY_REJECTED";
                break;
	    case STATUS_ENQUIRY:
                code = "STATUS_ENQUIRY";
                break;
	    case NORMAL_UNSPECIFIED:
                code = "NORMAL_UNSPECIFIED";
                break;
	    case NO_CIRCUIT_AVAIL:
                code = "NO_CIRCUIT_AVAIL";
                break;
	    case NETWORK_OUT_OF_ORDER:
                code = "NETWORK_OUT_OF_ORDER";
                break;
	    case SWITCHING_CONGESTION:
                code = "SWITCHING_CONGESTION";
                break;
	    case CHANNEL_NOT_AVAIL:
                code = "CHANNEL_NOT_AVAIL";
                break;
	    case RESOURCE_UNAVAILABLE:
                code = "RESOURCE_UNAVAILABLE";
                break;
	    case QOS_NOT_AVAIL:
                code = "QOS_NOT_AVAIL";
                break;
	    case BEARER_NOT_AVAIL:
                code = "BEARER_NOT_AVAIL";
                break;
	    case BEARER_NOT_AUTHORIZED:
                code = "BEARER_NOT_AUTHORIZED";
                break;
	    case SERVICE_NOT_AVAILABLE:
                code = "SERVICE_NOT_AVAILABLE";
                break;
	    case BEARER_NOT_IMPLEMENT:
                code = "BEARER_NOT_IMPLEMENT";
                break;
	    case FACILITY_NOT_IMPLEMENT:
                code = "FACILITY_NOT_IMPLEMENT";
                break;
	    case RESTRICTED_BEARER_AVAILABLE:
                code = "RESTRICTED_BEARER_AVAILABLE";
                break;
	    case OPTION_NOT_AVAILABLE:
                code = "OPTION_NOT_AVAILABLE";
                break;
	    case INCOMPATIBLE_DESTINATION:
                code = "INCOMPATIBLE_DESTINATION";
                break;
	    case CHANNEL_UNACCEPTABLE:
                code = "CHANNEL_UNACCEPTABLE";
                break;
	    case OPERATOR_DETERMINED_BARRING:
                code = "OPERATOR_DETERMINED_BARRING";
                break;
	    case PRE_EMPTION:
                code = "PRE_EMPTION";
                break;
	    case NON_SELECTED_USER_CLEARING:
                code = "NON_SELECTED_USER_CLEARING";
                break;
	    case DESTINATION_OUT_OF_ORDER:
                code = "DESTINATION_OUT_OF_ORDER";
                break;
	    case ACCESS_INFORMATION_DISCARDED:
                code = "ACCESS_INFORMATION_DISCARDED";
                break;
	    case REQUESTED_FACILITY_NOT_SUBSCRIBED:
                code = "REQUESTED_FACILITY_NOT_SUBSCRIBED";
                break;
	    case INCOMING_CALL_BARRED_WITHIN_CUG:
                code = "INCOMING_CALL_BARRED_WITHIN_CUG";
                break;
	    case INVALID_TRANSACTION_ID_VALUE:
                code = "INVALID_TRANSACTION_ID_VALUE";
                break;
	    case USER_NOT_MEMBER_OF_CUG:
                code = "USER_NOT_MEMBER_OF_CUG";
                break;
	    case INVALID_TRANSIT_NETWORK_SELECTION:
                code = "INVALID_TRANSIT_NETWORK_SELECTION";
                break;
	    case SEMANTICALLY_INCORRECT_MESSAGE:
                code = "SEMANTICALLY_INCORRECT_MESSAGE";
                break;
	    case INVALID_MANDATORY_INFORMATION:
                code = "INVALID_MANDATORY_INFORMATION";
                break;
	    case MESSAGE_TYPE_NON_EXISTENT:
                code = "MESSAGE_TYPE_NON_EXISTENT";
                break;
	    case MESSAGE_TYPE_NOT_COMPATIBLE_WITH_PROT_STATE:
                code = "MESSAGE_TYPE_NOT_COMPATIBLE_WITH_PROT_STATE";
                break;
	     case IE_NON_EXISTENT_OR_NOT_IMPLEMENTED:
                code = "IE_NON_EXISTENT_OR_NOT_IMPLEMENTED";
                break;			
	     case CONDITIONAL_IE_ERROR:
                code = "CONDITIONAL_IE_ERROR";
                break;
	     case MESSAGE_NOT_COMPATIBLE_WITH_PROTOCOL_STATE:
                code = "MESSAGE_NOT_COMPATIBLE_WITH_PROTOCOL_STATE";
                break;
	     case RECOVERY_ON_TIMER_EXPIRY:
                code = "RECOVERY_ON_TIMER_EXPIRY";
                break;
	     case PROTOCOL_ERROR_UNSPECIFIED:
                code = "PROTOCOL_ERROR_UNSPECIFIED";
                break;
	     case INTERWORKING_UNSPECIFIED:
                code = "INTERWORKING_UNSPECIFIED";
                break;				
	    //@darren add end
            ///M: WFC @{
            case WFC_CALL_ERROR:
                code = "WFC_CALL_ERROR";
                break;
            ///@}
            default:
                code = "invalid code: " + mDisconnectCode;
                break;
        }
        String label = mDisconnectLabel == null ? "" : mDisconnectLabel.toString();
        String description = mDisconnectDescription == null
                ? "" : mDisconnectDescription.toString();
        String reason = mDisconnectReason == null ? "" : mDisconnectReason;
        return "DisconnectCause [ Code: (" + code + ")"
                + " Label: (" + label + ")"
                + " Description: (" + description + ")"
                + " Reason: (" + reason + ")"
                + " Tone: (" + mToneToPlay + ") ]";
    }
}
