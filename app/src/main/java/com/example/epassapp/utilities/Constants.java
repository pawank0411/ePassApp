package com.example.epassapp.utilities;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Constants {
    public static final String USER_ACCOUNTS = "users";
    public static final String E_PASSES = "epass";
    public static final String USER_NAME = "username";
    public static final String POST_CONTRACTOR = "Contractor";
    public static final String POST_SITEINCHARGE = "Site Incharge";
    public static final String POST_WAYBRIDGE = "Way Bridge";
    public static final String POST_TRUCKDRIVER = "Truck driver";
    public static final String POST_PITOWNER = "Pit Owner";
    public static final String USER_ID = "user_id";
    public static final String PASS_PITOWNER = "pit_owner";
    public static final String PASS_SECTIONNO = "section_no";
    public static final String PASS_BENCHNO = "bench_no";
    public static final String PASS_DATE = "date";
    public static final String PASS_TRUCKNO = "truck_no";
    public static final String PASS_MINENO = "mine_no";
    public static final String PASS_SERIALNO = "serial_no";
    public static final String PASS_APPROVED = "pass_approved";
    public static final String PASS_ACCEPTED = "Approved";
    public static final String PASS_PENDING = "Pending";
    public static final String PASS_REJECTED = "Rejected";
    public static final String APPROVER_NAME = "approver_name";
    public static final String PASS_CONTRACTOR = "contractor_name";
    public static final String PASS_CREATED_TIME = "pass_time";
    public static final String PASS_CREATED_BY = "ex_user_name";

    private static SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
    private static SimpleDateFormat time_sdf = new SimpleDateFormat("hh:mm a", Locale.US);
    public static final String date = sdf.format(new Date());
    public static final String time = time_sdf.format(new Date());
}
