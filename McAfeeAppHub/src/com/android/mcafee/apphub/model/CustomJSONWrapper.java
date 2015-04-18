
package com.android.mcafee.apphub.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.util.JsonReader;

/**
 * Wrapper class for loader data {@link AppHubListLoaderData & @link
 * AppHubDetailsJsonData}
 * 
 * @author Anukalp
 **/
public class CustomJSONWrapper implements JsonDeSerializer {

    private ArrayList<AppHubDetailsJsonData> mListData;

    @Override
    public void populateJsonData(InputStream is) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(is, "UTF-8"));
        reader.setLenient(true);
        try {
            mListData = new ArrayList<AppHubDetailsJsonData>();
            parseJsonArray(reader);
        } finally {
            reader.close();
        }
    }

    private void parseJsonArray(JsonReader reader) throws IOException {
        reader.beginArray();
        while (reader.hasNext()) {
            mListData.add(parseAppHubData(reader));
        }
        reader.endArray();
    }

    public AppHubDetailsJsonData parseAppHubData(JsonReader reader) throws IOException {
        String mName = null;
        String image = null;
        String type = null;
        String lastUpdate = null;
        long users = 0;
        String url = null;
        double price = 0;
        double rating = 0;
        String description = null;
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals(AppHubDetailsJsonData.TAG_NAME)) {
                mName = reader.nextString();
            } else if (name.equals(AppHubDetailsJsonData.TAG_IMAGE)) {
                image = reader.nextString();
            } else if (name.equals(AppHubDetailsJsonData.TAG_TYPE)) {
                type = reader.nextString();
            } else if (name.equals(AppHubDetailsJsonData.TAG_PRICE)) {
                price = reader.nextDouble();
            } else if (name.equals(AppHubDetailsJsonData.TAG_RATING)) {
                rating = reader.nextDouble();
            } else if (name.equals(AppHubDetailsJsonData.TAG_USERS)) {
                users = reader.nextLong();
            } else if (name.equals(AppHubDetailsJsonData.TAG_UPDATE)) {
                lastUpdate = reader.nextString();
            } else if (name.equals(AppHubDetailsJsonData.TAG_DESCRIPTION)) {
                description = reader.nextString();
            } else if (name.equals(AppHubDetailsJsonData.TAG_GOOGLE_PLAY)) {
                url = reader.nextString();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return new AppHubDetailsJsonData(mName, price, rating, image, type, lastUpdate,
                users, description, url);
    }

    @Override
    public void populateJsonData(JsonReader reader) throws IOException {
        // TODO : Do Nothing
    }

    public ArrayList<AppHubDetailsJsonData> getListData() {
        return mListData;
    }
}
