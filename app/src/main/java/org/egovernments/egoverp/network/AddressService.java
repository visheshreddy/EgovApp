package org.egovernments.egoverp.network;


import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.text.TextUtils;
import android.util.Log;

import org.egovernments.egoverp.events.AddressReadyEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Service resolves lat/lng data to address in background when
 **/

public class AddressService extends IntentService {

    public static final String LAT = "LAT";
    public static final String LNG = "LNG";

    public static String addressResult = "";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public AddressService() {
        super("Default");
    }


    @Override
    protected void onHandleIntent(Intent intent) {

        // Get the location passed to this service through an extra.
        Double lat = intent.getDoubleExtra(LAT, 0);
        Double lng = intent.getDoubleExtra(LNG, 0);


        List<Address> addresses = null;

        Geocoder geocoder = new Geocoder(getApplicationContext());

        try {
            addresses = geocoder.getFromLocation(lat, lng, 1);
        } catch (IOException | IllegalArgumentException ioException) {
            // Catch network or other I/O problems.
            ioException.printStackTrace();
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size() == 0) {
            Log.e("Address null", "");
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<>();

            // Fetch the address lines using getAddressLine,
            // join them, and send them to the thread.
            for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }

            addressResult = TextUtils.join(System.getProperty("line.separator"), addressFragments);
            //Post event on success so that all relevant subscribers can react
            EventBus.getDefault().post(new AddressReadyEvent());
        }
    }
}
