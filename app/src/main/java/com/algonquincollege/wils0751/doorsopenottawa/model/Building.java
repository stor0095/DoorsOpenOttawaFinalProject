package com.algonquincollege.wils0751.doorsopenottawa.model;

import android.graphics.Bitmap;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

/**
 * Model handles the getters and setters and the open_hours
 *
 * @author Geemakun Storey (Stor0095)
 *
 */
public class Building {
    private Integer buildingId;
    private String name;
    private String address;
    private String image;
    private JSONArray openHours;
    private String date = "";
    private Bitmap bitmap;
    private String description;

    public Integer getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(Integer buildingId) {
        this.buildingId = buildingId;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address + "Ottawa, Ontario";
    }


    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }


    public String getDate() {
        return date;
    }

    public void setOpenHours(JSONArray openHours) {
        this.openHours = openHours;

        for (int i = 0; i < openHours.length(); i++) {
            try {
                date += openHours.getJSONObject(i).getString("date") + "\n";
            } catch (JSONException e) {

            }
        }

    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

