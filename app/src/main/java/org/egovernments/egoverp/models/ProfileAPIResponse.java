
package org.egovernments.egoverp.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * POJO class, response to getProfile and updateProfiled calls
 **/

public class ProfileAPIResponse {

    @SerializedName("result")
    @Expose
    private Profile profile;

    /**
     * @return The profile
     */
    public Profile getProfile() {
        return profile;
    }

}
