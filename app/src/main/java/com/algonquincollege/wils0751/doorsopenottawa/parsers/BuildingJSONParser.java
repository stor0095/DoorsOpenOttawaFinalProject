package com.algonquincollege.wils0751.doorsopenottawa.parsers;

import android.util.Log;

import com.algonquincollege.wils0751.doorsopenottawa.model.Building;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses the JSON file coming in and get specific information from it returning strings
 *
 * @author Shannon Wilson (Wils0751) Geemakun Storey (Stor0095)
 */

public class BuildingJSONParser {
    public static List<Building> parseFeed(String content) {

        try {
            JSONObject jsonResponse = new JSONObject(content);
            JSONArray buildingArray = jsonResponse.getJSONArray("buildings");
            List<Building> buildingList = new ArrayList<>();


            for (int i = 0; i < buildingArray.length(); i++) {

                JSONObject obj = buildingArray.getJSONObject(i);
                Building building = new Building();

                building.setBuildingId(obj.getInt("buildingId"));
                building.setName(obj.getString("name"));
                building.setAddress(obj.getString("address"));
                building.setImage(obj.getString("image"));
                building.setDescription(obj.getString("description"));
                building.setOpenHours(obj.getJSONArray("open_hours"));

                buildingList.add(building);
            }

            return buildingList;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
