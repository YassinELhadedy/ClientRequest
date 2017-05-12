package kk.ljq.hhh.clientrequest;

/**
 * Created by Elhadedy on 2/1/2017.
 */

public class DriverInfo {
    private double d_lat;
    private double d_lon;
    private String d_name;

    private String d_Email;
    private String d_photo;


    private String add_location_order;
    private String add_time_order;
    private String add_rating_order;




    public DriverInfo() {
    }

    public DriverInfo(String add_location_order, String add_rating_order, String add_time_order, String d_Email,String d_photo) {
        this.add_location_order = add_location_order;
        this.add_rating_order = add_rating_order;
        this.add_time_order = add_time_order;
        this.d_Email = d_Email;
        this.d_photo=d_photo;
    }


    public double getD_lat() {
        return d_lat;
    }



    public double getD_lon() {
        return d_lon;
    }

    public String getD_name() {
        return d_name;
    }

    public String getD_Email() {
        return d_Email;
    }




}
