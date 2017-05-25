package kr.koogle.android.smartconstruction.http;

import java.util.ArrayList;

public class SmartSingleton {
    private volatile static SmartSingleton uniqueInstance = null; // DCL
    private SmartSingleton() {}

    public static double strLat;
    public static double strLng;

    public static ArrayList<SmartBuild> arrSmartBuilds;
    public static ArrayList<SmartWork> arrSmartWorks;
    public static ArrayList<SmartClient> arrSmartClients;
    public static ArrayList<SmartApproval> arrSmartApprovals;
    public static ArrayList<SmartOrder> arrSmartOrders;
    public static ArrayList<SmartPhoto> arrSmartPhotos;
    public static ArrayList<SmartEmployee> arrSmartEmployees;

    public static ArrayList<SmartCategory> arrLaborCategorys;
    public static ArrayList<SmartCategory> arrMaterialCategorys;
    public static ArrayList<SmartCategory> arrEquipmentCategorys;
    public static ArrayList<SmartCategory> arrUnitCategorys;
    public static ArrayList<SmartCategory> arrWeatherCategorys;

    public static ArrayList<SmartComment> arrComments;
    public static ArrayList<SmartApprovalComment> arrApprovalComments;
    public static ArrayList<SmartApprovalDetails> arrApprovalDetails;
    public static ArrayList<SmartApprovalSign> arrApprovalSign;

    public static SmartBuild smartBuild;
    public static SmartWork smartWork;
    public static SmartClient smartClient;
    public static SmartApproval smartApproval;
    public static SmartOrder smartOrder;
    public static SmartPhoto smartPhoto;

    public static SmartSingleton getInstance() {
        if (uniqueInstance == null) {
            // 이렇게 하면 처음에만 동기화 됨!!
            synchronized (SmartSingleton.class) {
                if (uniqueInstance == null) {
                    uniqueInstance = new SmartSingleton();

                    arrSmartBuilds = new ArrayList<SmartBuild>();
                    arrSmartWorks = new ArrayList<SmartWork>();
                    arrSmartClients = new ArrayList<SmartClient>();
                    arrSmartApprovals = new ArrayList<SmartApproval>();
                    arrSmartOrders = new ArrayList<SmartOrder>();
                    arrSmartPhotos = new ArrayList<SmartPhoto>();
                    arrSmartEmployees = new ArrayList<SmartEmployee>();

                    arrLaborCategorys = new ArrayList<SmartCategory>();
                    arrMaterialCategorys = new ArrayList<SmartCategory>();
                    arrEquipmentCategorys = new ArrayList<SmartCategory>();
                    arrUnitCategorys = new ArrayList<SmartCategory>();
                    arrWeatherCategorys = new ArrayList<SmartCategory>();

                    arrComments = new ArrayList<SmartComment>();
                    arrApprovalComments = new ArrayList<SmartApprovalComment>();
                    arrApprovalDetails = new ArrayList<SmartApprovalDetails>();
                    arrApprovalSign = new ArrayList<SmartApprovalSign>();

                    smartBuild = new SmartBuild();
                    smartWork = new SmartWork();
                    smartClient = new SmartClient();
                    smartApproval = new SmartApproval();
                    smartOrder = new SmartOrder();
                    smartPhoto = new SmartPhoto();

                }
            }
        }
        return uniqueInstance;
    }

    public static void clearInstance() {
        uniqueInstance = null;
    }

}

