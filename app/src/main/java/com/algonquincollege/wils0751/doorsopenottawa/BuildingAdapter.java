package com.algonquincollege.wils0751.doorsopenottawa;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Filter;
import android.widget.Filterable;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.algonquincollege.wils0751.doorsopenottawa.model.Building;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapts the array from the getters
 *
 * @author Geemakun Storey (Stor0095)
 */

public class BuildingAdapter extends ArrayAdapter<Building> implements Filterable {
    private Context context;
    private final ArrayList<Building> arraylist;
    private ArrayList<Building> mOriginalValues; // Original Values
    private ArrayList<Building> mDisplayedValues;    // Values to be displayed
    private List<Boolean> newArray;
    private List<Building> buildingList;
    private List<Building> temporarydata;


    private LruCache<Integer, Bitmap> imageCache;

    public BuildingAdapter(Context context, int resource, List<Building> building) {
        super(context, resource, building);
        this.context = context;
        this.buildingList = building;
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        newArray=new ArrayList<>();
        final int cacheSize = maxMemory / 8;
        imageCache = new LruCache<>(cacheSize);
        arraylist = new ArrayList<Building>();
        arraylist.addAll(buildingList);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater =
                (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.item_building, parent, false);

        //Display planet name in the TextView widget
        Building building = buildingList.get(position);
        TextView tv = (TextView) view.findViewById(R.id.buildingCellDescription);
        TextView tv1 = (TextView) view.findViewById(R.id.addressCell);

        tv.setText(building.getName());
        tv1.setText(building.getAddress());

        Bitmap bitmap = imageCache.get(building.getBuildingId());

        if (bitmap != null) {
            Log.i("BUILDINGS", building.getName() + "\tbitmap in cache");
         //   ImageView image = (ImageView) view.findViewById(R.id.imageView1);
         //   image.setImageBitmap(building.getBitmap());
        } else {
            Log.i("BUILDINGS", building.getName() + "\tfetching bitmap using AsyncTask");
            BuildingAndView container = new BuildingAndView();
            container.building = building;
            container.view = view;

            ImageLoader loader = new ImageLoader();
            loader.execute(container);
        }

        return view;
    }

    private class BuildingAndView {
        public Building building;
        public View view;
        public Bitmap bitmap;
    }

    private class ImageLoader extends AsyncTask<BuildingAndView, Void, BuildingAndView> {

        @Override
        protected BuildingAndView doInBackground(BuildingAndView... params) {

            BuildingAndView container = params[0];
            Building building = container.building;

            try {
                String imageUrl = MainActivity.IMAGES_BASE_URL + building.getImage();
                InputStream in = (InputStream) new URL(imageUrl).getContent();
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                building.setBitmap(bitmap);
                in.close();
                container.bitmap = bitmap;
                return container;
            } catch (Exception e) {
                System.err.println("IMAGE: " + building.getName());
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(BuildingAndView result) {

         //   ImageView image = (ImageView) result.view.findViewById(R.id.imageView1);
          //  image.setImageBitmap(result.bitmap);
//        result.building.setBitmap(result.bitmap);
//            if(result.building.getBuildingId()!=null && result.bitmap!=null) {
//                imageCache.put(result.building.getBuildingId(), result.bitmap);
//            } else {
//                return;
//            }

        }
    }



    public int getItemCount() {
        return buildingList.size();
    }

    public Building getItem(int position) {
        return buildingList.get(position);
    }

    public List<Building> getData() {
        return buildingList;
    }


    // Filter through building list array
    // To be called on in MainActivity for the search
    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        buildingList.clear();
        if (charText.length() == 0) {
            buildingList.addAll(arraylist);
        }
        else {
            for (Building b: arraylist) {
                if (b.getName().toLowerCase(Locale.getDefault()).contains(charText)) {
                    buildingList.add(b);
                }
            }
        }
        notifyDataSetChanged();
    }
}
