
package com.android.mcafee.apphub.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.util.JsonReader;

/**
 * Wrapper class for loader data {@link AppHubListLoaderData & @link
 * AppHubDetailsJsonData}
 * 
 * @author Anukalp
 **/
public class CustomJSONWrapper implements JsonDeSerializer {

    private List<AppHubDetailsJsonData> AppHubDetailsJsonData;

    @Override
    public void populateJsonData(InputStream is) throws IOException {

    }

    @Override
    public void populateJsonData(JsonReader reader) throws IOException {
        // TODO : Do Nothing
    }

    public List<AppHubDetailsJsonData> getListData() {
        return (ArrayList<AppHubDetailsJsonData>)AppHubDetailsJsonData;
    }

    public List<AppHubDetailsJsonData> getAppHubDetailsJsonData() {
        return AppHubDetailsJsonData;
    }

    public void setAppHubDetailsJsonData(List<AppHubDetailsJsonData> list) {
        this.AppHubDetailsJsonData = list;
    }
}
