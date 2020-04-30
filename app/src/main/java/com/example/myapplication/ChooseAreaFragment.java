package com.example.myapplication;


import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.dp.City;
import com.example.myapplication.dp.County;
import com.example.myapplication.dp.Province;
import com.example.myapplication.util.HttpUrl;
import com.example.myapplication.util.HttpUtil;
import com.example.myapplication.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVINCE=0;
    public static final int LEVEL_CITY=1;
    public static final int LEVEL_COUNTY=2;

    private List<String> detaList=new ArrayList<>();
    private Button backButton;
    private TextView titleText;
    private ListView listView;

    //省列表
    private List<Province> provinceList;
    //市列表
    private List<City> cityList;
    //县列表
    private List<County> countyList;

    //选中的省份
    private Province selectedProvice;
    //选中的城市
    private City selectedCity;
    //选中的级别
    private int currentLevel;
    private ArrayAdapter<String> stringArrayAdapter;
    private HttpUrl httpUrl;
    private ProgressDialog progressDialog;
    private List<Province> provinceList1;

    public ChooseAreaFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View inflate = inflater.inflate(R.layout.choose_area, container, false);
        titleText = inflate.findViewById(R.id.title_text);
        backButton = inflate.findViewById(R.id.back_button);
        listView = inflate.findViewById(R.id.list_view);
        stringArrayAdapter = new ArrayAdapter<>(getContext(), R.layout.support_simple_spinner_dropdown_item, detaList);
        listView.setAdapter(stringArrayAdapter);
        httpUrl = new HttpUrl();
        return inflate;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (currentLevel==LEVEL_PROVINCE){
                    selectedProvice=provinceList.get(i);
                    queryCities();
                }else if (currentLevel==LEVEL_CITY){
                    selectedCity=cityList.get(i);
                    queryCounties();

                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLevel==LEVEL_COUNTY){
                    queryCities();
                }else if (currentLevel==LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }


    /*查询选中市内所有的的县，优先从数据库中查询，如果没有查询到再去服务器中查询
    * */
    private void queryCounties() {
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityid=?", String.valueOf(selectedCity.getId())).find(County.class);
        if (countyList.size()>0){
            detaList.clear();
            for (County county:countyList) {
                detaList.add(county.getCountyName());
            }
            stringArrayAdapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_COUNTY;
        }else {
            int provinceCode = selectedProvice.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address=httpUrl.Url+provinceCode+"/"+cityCode;
            queryFromServer(address,"county");
        }
    }

    //根据传入的地址和类型从服务器上查询省市县数据
    private void queryFromServer(String address, final String county) {
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //通过runOnUiThread方法回到主线程处理逻辑
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        claseProgressDialog();
                        Toast.makeText(getActivity(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                String responseText = response.body().string();
                boolean result=false;
                if ("province".equals(county)){
                    result= Utility.handleProvinceResponse(responseText);
                }else if ("city".equals(county)){
                    result=Utility.handlerCityResponse(responseText,selectedProvice.getId());
                }else if ("county".equals(county)){
                    result=Utility.handleCountyResponse(responseText,selectedCity.getId());
                }

                if (result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            claseProgressDialog();
                            if ("province".equals(county)){
                                queryProvinces();
                            }else if ("city".equals(county)){
                                queryCities();
                            }else if ("county".equals(county)){
                                queryCounties();
                            }
                        }
                    });
                }
            }
        });
    }

    //查询全国所有的省，优先查询数据库，数据库中没有再去服务器中查询
    private void queryProvinces() {
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(Province.class);
        if (provinceList.size()>0){
            detaList.clear();
            for (Province province :provinceList) {
                detaList.add(province.getProvinceName());
            }
            stringArrayAdapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_PROVINCE;
        }else{
            String address="http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }
    }

    //查询选中市内所有的市，优先到数据库查询，如果没有查询到在去服务器查询
    private void queryCities() {
        titleText.setText(selectedProvice.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceid = ?", String.valueOf(selectedProvice.getId())).find(City.class);
        if (cityList.size()>0){
            detaList.clear();
            for (City city:cityList) {
                detaList.add(city.getCityName());
            }
            stringArrayAdapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_CITY;
        }else{
            int provinceCode = selectedProvice.getProvinceCode();
            String address= httpUrl.Url+provinceCode;
            queryFromServer(address,"city");
        }
    }




    //显示进度对话框
    private void showProgressDialog() {
        if (progressDialog==null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载中。。。");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /*
    * 关闭进度条对话框
    * */
    private void claseProgressDialog(){
        if (progressDialog!=null){
            progressDialog.dismiss();
        }
    }
}
