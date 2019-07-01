package com.newmedia.erxeslibrary.helper;

import com.apollographql.apollo.response.CustomTypeAdapter;
import com.apollographql.apollo.response.CustomTypeValue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.Nonnull;

public class JsonCustomTypeAdapter2 implements CustomTypeAdapter<Json> {


    public Json decode(@Nonnull CustomTypeValue value) {
        String str = value.value.toString();
        if (str.length() > 0) {

            //is json array
            if (str.charAt(0) == '[') {
                try {
                    JSONArray a = new JSONArray(str);
                    return new Json(a);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    JSONObject a = new JSONObject(str);
                    return new Json(a);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;

    }
    public CustomTypeValue encode(@Nonnull Json value){
        if(value.is_object)
            return new CustomTypeValue.GraphQLJson(value.convert_object());
        else
            return null;
    }
}