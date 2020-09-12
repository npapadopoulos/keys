package com.property.keys.tasks.properties;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;

import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapquest.mapping.MapQuest;
import com.property.keys.R;
import com.property.keys.entities.Property;
import com.property.keys.tasks.AbstractAsyncTask;

import java.net.URLEncoder;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiresApi(api = Build.VERSION_CODES.R)
@RequiredArgsConstructor
public class PropertyMapCreateTask extends AbstractAsyncTask {
    private static final String TAG = PropertyMapCreateTask.class.getSimpleName();

    private final Context context;
    private final Bundle savedInstanceState;
    private final MapView view;
    private final Property property;

    private LatLng A_PERICLEOUS_PROPERTIES_LTD = new LatLng(34.765009, 32.411239);

    @SneakyThrows
    @Override
    public void runInBackground() {
        String request = "http://www.mapquestapi.com/geocoding/v1/address?key="
                + context.getResources().getResourceName(R.string.maps_key)
                + "&location=" + URLEncoder.encode(property.getAddress(), "UTF-8") + "&thumbMaps=false";


        /**
         * {
         *   "shapePoints": [
         *     39.750307,
         *     -104.999472
         *   ],
         *   "options": {
         *     "maxMatches": 4
         *   }
         * }
         */
        MapQuest.start(context);
        view.onCreate(savedInstanceState);
        view.getMapAsync(box -> {
            // create and add marker
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(A_PERICLEOUS_PROPERTIES_LTD);
            markerOptions.title("A.Pericleous Properties LTD");
            box.addMarker(markerOptions);

            // set map center and zoom
            box.moveCamera(CameraUpdateFactory.newLatLngZoom(A_PERICLEOUS_PROPERTIES_LTD, 14));
        });
    }
//
//    private class Geocoding extends AbstractAsyncTask {
//
//        @Override
//        public void runInBackground() {
//            JSONObject postData = new JSONObject();
//            try {
//                // JSONArray of start and finish
//                JSONArray locations = new JSONArray();
//                locations.put(URLEncoder.encode(args[0], "UTF-8"));
//                locations.put(URLEncoder.encode(args[1], "UTF-8"));
//                postData.put("locations", locations); // put array inside main object
//
//                // JSONObject options
//                JSONObject options = new JSONObject();
//                options.put("routeType", args[2]);
//                options.put("generalize", "0");
//                postData.put("options", options);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//            // create the api request string
//            String urlstring = "http://www.mapquestapi.com/directions/v2/route" +
//                    "?key=brK53YAgFqbRjCvs7rlH65HqS1GGVAlK&json=" +
//                    postData.toString();
//
//            // make the GET request and prep the response string
//            StringBuilder json = new StringBuilder();
//            try {
//                URL url = new URL(urlstring);
//                HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
//                try {
//                    BufferedReader rd = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
//                    String line;
//                    while ((line = rd.readLine()) != null) {
//                        json.append(line);
//                    }
//                } catch (Exception e) {
//                    System.out.println("catch B: " + e.toString());
//                } finally {
//                    urlConn.disconnect();
//                }
//            } catch (Exception e) {
//                System.out.println("catch C: " + e.toString());
//            }
//
//            json.toString();
//        }
//    }
}
