/**
 *
 */
package com.mad.trafficclient.zy_java.view;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.mad.trafficclient.R;
import com.mad.trafficclient.util.Util;
import com.mad.trafficclient.ws_java.ob5.IndexBean;
import com.mad.trafficclient.ws_java.ob5.SenseBean;
import com.mad.trafficclient.ws_java.ob5.SenseUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class Left_Message extends Fragment {

    private Spinner spinner_message;
    private ListView list_message;
    private List<IndexBean> list_data;
    private List<SenseBean> list_yuizhi;
    private Context context;
    private RequestQueue requestQueue;
    private TextView tx_message_show;
    private Timer timer;
    private LeftMessageAdapter adapter;
    private String[] indexName = {"全部", "温度", "湿度", "co2", "光照强度", "PM2.5"};
    private static int[] tu = new int[5];

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater
                .inflate(R.layout.left_message, container, false);

        initView(view);
        return view;
    }

    public static int[] getTu() {
        return tu;
    }

    public static void setTu(int[] tu) {
        Left_Message.tu = tu;
    }

    private void initView(View view) {
        spinner_message = (Spinner) view.findViewById(R.id.spinner_message);
        list_message = (ListView) view.findViewById(R.id.list_message);
        context = getContext();
        requestQueue = Volley.newRequestQueue(context);
        list_data = new ArrayList<>();
        list_yuizhi = new ArrayList<>();

        tx_message_show = (TextView) view.findViewById(R.id.tx_message_show);

        adapter = new LeftMessageAdapter(list_yuizhi);
        list_message.setAdapter(adapter);
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                GetAllSense();
            }
        }, 0, 3000);
        spinner_message.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String name = spinner_message.getSelectedItem().toString();
                junInitnData(name);
//                if (name.equals("全部")) {
//                    GetAllSense();
//                }
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void junInitnData(String name) {
        List<SenseBean> list = new ArrayList<>();
        if (!name.equals("全部")) {
            for (int i = 0; i < list_yuizhi.size(); i++) {
                if (list_yuizhi.get(i).getName().equals(name)) {
                    list.add(list_yuizhi.get(i));
                    break;
                }

            }
            LeftMessageAdapter adapter = new LeftMessageAdapter(list);
            list_message.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        } else {
            LeftMessageAdapter adapter = new LeftMessageAdapter(list_yuizhi);
            list_message.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
//        else {
//            GetAllSense();
//        }
    }

    @Override
    public void onDestroy() {
        timer.cancel();
        super.onDestroy();
    }

    public void GetAllSense() {
        String URL = "http://" + Util.loadSetting(context).getUrl() + ":" + Util.loadSetting(context).getPort() + "/api/v2/get_all_sense";

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("UserName", Util.getUserName(context));

            requestQueue.add(new JsonObjectRequest(Request.Method.POST, URL, jsonObject, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    list_yuizhi.clear();
                    if (jsonObject.optString("RESULT").equals("S")) {
                        Gson gson = new Gson();
                        IndexBean indexBean = gson.fromJson(jsonObject.toString(), IndexBean.class);
                        if (indexBean.getTemperature() > SenseUtil.getYuzhi(context, "wendu")) {
                            list_yuizhi.add(new SenseBean("温度", indexBean.getTemperature(), SenseUtil.getYuzhi(context, "wendu")));
                            tu[2]++;
                        }
                        if (indexBean.getHumidity() > SenseUtil.getYuzhi(context, "shidu")) {
                            tu[3]++;
                            list_yuizhi.add(new SenseBean("湿度", indexBean.getHumidity(), SenseUtil.getYuzhi(context, "shidu")));
                        }
                        if (indexBean.getLightIntensity() > SenseUtil.getYuzhi(context, "guang")) {
                            tu[1]++;
                            list_yuizhi.add(new SenseBean("光照强度", indexBean.getLightIntensity(), SenseUtil.getYuzhi(context, "guang")));
                        }
                        if (indexBean.getCo2() > SenseUtil.getYuzhi(context, "co2")) {
                            tu[4]++;
                            list_yuizhi.add(new SenseBean("co2", indexBean.getCo2(), SenseUtil.getYuzhi(context, "co2")));
                        }
                        if (indexBean.get_$Pm25316() > SenseUtil.getYuzhi(context, "pm25")) {
                            tu[0]++;
                            list_yuizhi.add(new SenseBean("PM2.5", indexBean.get_$Pm25316(), SenseUtil.getYuzhi(context, "pm25")));
                        }
                        junInitnData(spinner_message.getSelectedItem().toString());
                        adapter.notifyDataSetChanged();

                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Toast.makeText(context, "传感器各项值获取失败", Toast.LENGTH_SHORT).show();
                }
            }));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void GetRoadStatus() {
        String URL = "http://" + Util.loadSetting(context).getUrl() + ":" + Util.loadSetting(context).getPort() + "/api/v2/get_road_status";

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("RoadId", 1);
            jsonObject.put("UserName", Util.getUserName(context));


            requestQueue.add(new JsonObjectRequest(Request.Method.POST, URL, jsonObject, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    if (jsonObject.optString("RESULT").equals("S")) {
                        try {
                            if (jsonObject.getInt("Status") > SenseUtil.getYuzhi(context, "pm25")) {
//                                list_yuizhi.add(new SenseBean("道路状况", jsonObject.getInt("Status"), SenseUtil.getYuzhi(context, "status")));
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Toast.makeText(context, "道路状态获取失败", Toast.LENGTH_SHORT).show();
                }
            }));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    class LeftMessageAdapter extends BaseAdapter {
        private List<SenseBean> list_yuzhi;

        public LeftMessageAdapter(List<SenseBean> list_yuzhi) {
            this.list_yuzhi = list_yuzhi;
        }

        @Override
        public int getCount() {
            if (list_yuizhi.size() == 0) {
                tx_message_show.setText("当前还未有报警信息");

            } else {
                tx_message_show.setText("");

            }
            return list_yuizhi.size();
        }

        @Override
        public Object getItem(int position) {
            return list_yuizhi.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = View.inflate(context, R.layout.item_left_message, null);
            ViewHolder viewHolder = new ViewHolder(convertView);
            if (list_yuizhi.size() == 0) {
                tx_message_show.setText("当前还未有报警信息");

            }
//            if(spinner_message.getSelectedItem().toString())
            viewHolder.item_xuahoa.setText((position + 1) + "");
            viewHolder.item_baojingleixing.setText("【" + list_yuizhi.get(position).getName() + "】报警");
            viewHolder.item_yuzhi.setText(list_yuizhi.get(position).getYuzhi() + "");
            viewHolder.item_index.setText(list_yuizhi.get(position).getZhi() + "");
            tx_message_show.setText("");
            return convertView;
        }

        public
        class ViewHolder {
            public View rootView;
            public TextView item_xuahoa;
            public TextView item_baojingleixing;
            public TextView item_yuzhi;
            public TextView item_index;

            public ViewHolder(View rootView) {
                this.rootView = rootView;
                this.item_xuahoa = (TextView) rootView.findViewById(R.id.item_xuahoa);
                this.item_baojingleixing = (TextView) rootView.findViewById(R.id.item_baojingleixing);
                this.item_yuzhi = (TextView) rootView.findViewById(R.id.item_yuzhi);
                this.item_index = (TextView) rootView.findViewById(R.id.item_index);
            }

        }
    }
}
