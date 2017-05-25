package kr.koogle.android.smartconstruction.http;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017-01-02.
 */

public class SmartApproval {

    public int intId;
    public String strId;
    public String strSubject;
    public String strSiteId;
    public String strDocNo;
    public String strCate1;
    public String strCate2;
    public String strCost1;
    public String strCost2;
    public String strCost3;
    public String strTitle;
    public String strContent;
    public String strName;
    public String strUserId;
    public String strNowSign;
    public String strSignCode;
    public String strSignDate;
    public int intState;
    public int intType;
    public int intLevel;
    public String datRegist;
    public String strKeep;
    public String strImportance;
    public String strWho;
    public String strReturn;
    public int comCnt;
    public ArrayList<SmartApprovalComment> arrApprovalComments;
    public ArrayList<SmartApprovalDetails> arrApprovalDetails;
    public ArrayList<SmartApprovalSign> arrApprovalSign;

    public ArrayList<SmartFile> arrFiles;

    public SmartApproval() {
        arrFiles = new ArrayList<SmartFile>();
        arrApprovalComments = new ArrayList<SmartApprovalComment>();
        arrApprovalDetails = new ArrayList<SmartApprovalDetails>();
        arrApprovalSign = new ArrayList<SmartApprovalSign>();
    }
}
