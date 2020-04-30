package com.example.myapplication.util;

import android.text.TextUtils;

import com.example.myapplication.dp.City;
import com.example.myapplication.dp.County;
import com.example.myapplication.dp.Province;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utility {
    //解析和处理从服务器返回的省级数局
    public static boolean handleProvinceResponse(String response){
        if (!TextUtils.isEmpty(response)){
                try {
                    JSONArray jsonArray=new JSONArray(response);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        Province province = new Province();
                        province.setProvinceName(jsonObject.getString("name"));
                        province.setProvinceCode(jsonObject.getInt("id"));
                        province.save();
                    }
                    return true;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
        }
        return false;
    }


    //解析和处理返回市级的数据
    public static boolean handlerCityResponse(String response,int provinceId){
        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray jsonArray=new JSONArray(response);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    City city = new City();
                    city.setCityName(jsonObject.getString("name"));
                    city.setCityCode(jsonObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return  true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    //解析和处理县级数据
    public static boolean handleCountyResponse(String responce,int cithId){
        if (!TextUtils.isEmpty(responce)){
            try {
                JSONArray jsonArray=new JSONArray(responce);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(jsonObject.getString("name"));
                    county.setWeatherId(jsonObject.getString("weather_id"));
                    county.setCityId(cithId);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
