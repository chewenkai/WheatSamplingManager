package com.aj.database;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table "SAMPLINGTABLE".
 */
public class SAMPLINGTABLE {

    private Long id;
    private Long taskID;
    private Long templetID;
    private String show_name;
    private String sampling_address;
    private String sampling_content;
    private String media_folder;
    private Boolean is_saved;
    private Boolean is_uploaded;
    private Boolean is_server_sampling;
    private Boolean is_make_up;
    private Integer check_status;
    private Long saved_time;
    private Long uploaded_time;
    private Long sid_of_server;
    private Double latitude;
    private Double longitude;
    private Integer location_mode;
    private String sampling_unique_num;

    public SAMPLINGTABLE() {
    }

    public SAMPLINGTABLE(Long id) {
        this.id = id;
    }

    public SAMPLINGTABLE(Long id, Long taskID, Long templetID, String show_name, String sampling_address, String sampling_content, String media_folder, Boolean is_saved, Boolean is_uploaded, Boolean is_server_sampling, Boolean is_make_up, Integer check_status, Long saved_time, Long uploaded_time, Long sid_of_server, Double latitude, Double longitude, Integer location_mode, String sampling_unique_num) {
        this.id = id;
        this.taskID = taskID;
        this.templetID = templetID;
        this.show_name = show_name;
        this.sampling_address = sampling_address;
        this.sampling_content = sampling_content;
        this.media_folder = media_folder;
        this.is_saved = is_saved;
        this.is_uploaded = is_uploaded;
        this.is_server_sampling = is_server_sampling;
        this.is_make_up = is_make_up;
        this.check_status = check_status;
        this.saved_time = saved_time;
        this.uploaded_time = uploaded_time;
        this.sid_of_server = sid_of_server;
        this.latitude = latitude;
        this.longitude = longitude;
        this.location_mode = location_mode;
        this.sampling_unique_num = sampling_unique_num;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTaskID() {
        return taskID;
    }

    public void setTaskID(Long taskID) {
        this.taskID = taskID;
    }

    public Long getTempletID() {
        return templetID;
    }

    public void setTempletID(Long templetID) {
        this.templetID = templetID;
    }

    public String getShow_name() {
        return show_name;
    }

    public void setShow_name(String show_name) {
        this.show_name = show_name;
    }

    public String getSampling_address() {
        return sampling_address;
    }

    public void setSampling_address(String sampling_address) {
        this.sampling_address = sampling_address;
    }

    public String getSampling_content() {
        return sampling_content;
    }

    public void setSampling_content(String sampling_content) {
        this.sampling_content = sampling_content;
    }

    public String getMedia_folder() {
        return media_folder;
    }

    public void setMedia_folder(String media_folder) {
        this.media_folder = media_folder;
    }

    public Boolean getIs_saved() {
        return is_saved;
    }

    public void setIs_saved(Boolean is_saved) {
        this.is_saved = is_saved;
    }

    public Boolean getIs_uploaded() {
        return is_uploaded;
    }

    public void setIs_uploaded(Boolean is_uploaded) {
        this.is_uploaded = is_uploaded;
    }

    public Boolean getIs_server_sampling() {
        return is_server_sampling;
    }

    public void setIs_server_sampling(Boolean is_server_sampling) {
        this.is_server_sampling = is_server_sampling;
    }

    public Boolean getIs_make_up() {
        return is_make_up;
    }

    public void setIs_make_up(Boolean is_make_up) {
        this.is_make_up = is_make_up;
    }

    public Integer getCheck_status() {
        return check_status;
    }

    public void setCheck_status(Integer check_status) {
        this.check_status = check_status;
    }

    public Long getSaved_time() {
        return saved_time;
    }

    public void setSaved_time(Long saved_time) {
        this.saved_time = saved_time;
    }

    public Long getUploaded_time() {
        return uploaded_time;
    }

    public void setUploaded_time(Long uploaded_time) {
        this.uploaded_time = uploaded_time;
    }

    public Long getSid_of_server() {
        return sid_of_server;
    }

    public void setSid_of_server(Long sid_of_server) {
        this.sid_of_server = sid_of_server;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Integer getLocation_mode() {
        return location_mode;
    }

    public void setLocation_mode(Integer location_mode) {
        this.location_mode = location_mode;
    }

    public String getSampling_unique_num() {
        return sampling_unique_num;
    }

    public void setSampling_unique_num(String sampling_unique_num) {
        this.sampling_unique_num = sampling_unique_num;
    }

}