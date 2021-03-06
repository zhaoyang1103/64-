package com.mad.trafficclient.st_java.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.gson.Gson;
import com.mad.trafficclient.R;
import com.mad.trafficclient.st_java.bean.Get_all_sense;
import com.mad.trafficclient.st_java.bean.Get_weather;
import com.mad.trafficclient.st_java.fragment.shzsfraxia.SHZSFraXia1;
import com.mad.trafficclient.st_java.fragment.shzsfraxia.SHZSFraXia2;
import com.mad.trafficclient.st_java.fragment.shzsfraxia.SHZSFraXia3;
import com.mad.trafficclient.st_java.fragment.shzsfraxia.SHZSFraXia4;
import com.mad.trafficclient.util.UrlBean;
import com.mad.trafficclient.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SHZSFra extends Fragment {
    private TextView WCurrent;
    private TextView temperature;
    private ImageView refresh;
    private LineChart lineChart;
    private GridView gridView;
    private ViewPager viewPager;
    private TextView tv_bottom_1;
    private TextView tv_bottom_2;
    private TextView tv_bottom_3;
    private TextView tv_bottom_4;
    private Context context;
    private UrlBean urlBean;
    private String userName;
    private RequestQueue requestQueue;
    private Timer timer;
    private ArrayList<ZhongBean> zhongBeans;
    private ZhongAda zhongAda;
    private ArrayList<Fragment> fragments;
    private SharedPreferences shzsFra_sp;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fra_shzs, container, false);
        initView(view);
        initData();
        initListener();
        return view;
    }

    private void initListener() {
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shangData();
            }
        });

        tv_bottom_1.setBackgroundResource(R.drawable.tv_back_shzs_on);
        tv_bottom_2.setBackgroundResource(R.drawable.tv_back_shzs_off);
        tv_bottom_3.setBackgroundResource(R.drawable.tv_back_shzs_off);
        tv_bottom_4.setBackgroundResource(R.drawable.tv_back_shzs_off);

        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        tv_bottom_1.setBackgroundResource(R.drawable.tv_back_shzs_on);
                        tv_bottom_2.setBackgroundResource(R.drawable.tv_back_shzs_off);
                        tv_bottom_3.setBackgroundResource(R.drawable.tv_back_shzs_off);
                        tv_bottom_4.setBackgroundResource(R.drawable.tv_back_shzs_off);
                        break;
                    case 1:
                        tv_bottom_1.setBackgroundResource(R.drawable.tv_back_shzs_off);
                        tv_bottom_2.setBackgroundResource(R.drawable.tv_back_shzs_on);
                        tv_bottom_3.setBackgroundResource(R.drawable.tv_back_shzs_off);
                        tv_bottom_4.setBackgroundResource(R.drawable.tv_back_shzs_off);
                        break;
                    case 2:
                        tv_bottom_1.setBackgroundResource(R.drawable.tv_back_shzs_off);
                        tv_bottom_2.setBackgroundResource(R.drawable.tv_back_shzs_off);
                        tv_bottom_3.setBackgroundResource(R.drawable.tv_back_shzs_on);
                        tv_bottom_4.setBackgroundResource(R.drawable.tv_back_shzs_off);
                        break;
                    case 3:
                        tv_bottom_1.setBackgroundResource(R.drawable.tv_back_shzs_off);
                        tv_bottom_2.setBackgroundResource(R.drawable.tv_back_shzs_off);
                        tv_bottom_3.setBackgroundResource(R.drawable.tv_back_shzs_off);
                        tv_bottom_4.setBackgroundResource(R.drawable.tv_back_shzs_on);
                        break;
                    default:
                        break;
                }
                super.onPageSelected(position);
            }
        });

        tv_bottom_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(0);
            }
        });
        tv_bottom_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(1);
            }
        });
        tv_bottom_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(2);
            }
        });
        tv_bottom_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(3);
            }
        });
    }

    private void initData() {
        context = getContext();
        urlBean = Util.loadSetting(context);
        userName = Util.getUserName(context);
        requestQueue = Volley.newRequestQueue(context);
        shzsFra_sp = context.getSharedPreferences("SHZSFra_sp", Context.MODE_PRIVATE);

        /**
         * 上部   气象信息查询http://localhost:8080/api/v2/get_weather
         */
        shangData();

        /**
         * 中部 计时器 查询“所有传感器”的当前值http://localhost:8080/api/v2/get_all_sense
         */
        zhongBeans = new ArrayList<>();
        zhongBeans.add(new ZhongBean(R.drawable.icon101, "紫外线指数", "", ""));
        zhongBeans.add(new ZhongBean(R.drawable.icon102, "感冒指数", "", ""));
        zhongBeans.add(new ZhongBean(R.drawable.icon103, "穿衣指数", "", ""));
        zhongBeans.add(new ZhongBean(R.drawable.icon104, "运动指数", "", ""));
        zhongBeans.add(new ZhongBean(R.drawable.icon105, "空气污染扩散指数", "", ""));
        zhongAda = new ZhongAda();
        gridView.setAdapter(zhongAda);
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("UserName", userName);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                requestQueue.add(new JsonObjectRequest(Request.Method.POST, "http://" + urlBean.getUrl() + ":" + urlBean.getPort() + "/api/v2/get_all_sense", jsonObject, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        if (jsonObject.optString("RESULT").equals("S")) {
                            Gson gson = new Gson();
                            Get_all_sense get_all_sense = gson.fromJson(jsonObject.toString(), Get_all_sense.class);


                            /**
                             * 处理中部数据
                             */
                            int temp = get_all_sense.getLightIntensity();
                            ZhongBean zhongBean = zhongBeans.get(0);
                            if (temp < 1000) {
                                zhongBean.setValue1("弱(" + temp + ")");
                                zhongBean.setValue2("辐射较弱，涂擦SPF12~15、PA+护肤品");
                            } else if (temp <= 3000) {
                                zhongBean.setValue1("中等(" + temp + ")");
                                zhongBean.setValue2("涂擦SPF大于15、PA+防晒护肤品");
                            } else {
                                zhongBean.setValue1("强(" + temp + ")");
                                zhongBean.setValue2("尽量减少外出，需要涂抹高倍数防晒霜");
                            }
                            temp = get_all_sense.getTemperature();
                            zhongBean = zhongBeans.get(1);
                            if (temp < 8) {
                                zhongBean.setValue1("较易发(" + temp + ")");
                                zhongBean.setValue2("温度低，风较大，较易发生感冒，注意防护");
                            } else {
                                zhongBean.setValue1("少发(" + temp + ")");
                                zhongBean.setValue2("无明显降温，感冒机率较低");
                            }
                            zhongBean = zhongBeans.get(2);
                            if (temp < 12) {
                                zhongBean.setValue1("冷(" + temp + ")");
                                zhongBean.setValue2("建议穿长袖衬衫、单裤等服装");
                            } else if (temp <= 21) {
                                zhongBean.setValue1("舒适(" + temp + ")");
                                zhongBean.setValue2("建议穿短袖衬衫、单裤等服装");
                            } else {
                                zhongBean.setValue1("热(" + temp + ")");
                                zhongBean.setValue2("建议穿短袖衬衫、单裤等服装");
                            }
                            temp = get_all_sense.getCo2();
                            zhongBean = zhongBeans.get(3);
                            if (temp < 3000) {
                                zhongBean.setValue1("适宜(" + temp + ")");
                                zhongBean.setValue2("气候适宜，推荐您进行户外运动");
                            } else if (temp <= 6000) {
                                zhongBean.setValue1("中(" + temp + ")");
                                zhongBean.setValue2("易感人群应适当减少室外活动");
                            } else {
                                zhongBean.setValue1("较不宜(" + temp + ")");
                                zhongBean.setValue2("空气氧气含量低，请在室内进行休闲运动");
                            }
                            temp = get_all_sense.get_$Pm25126();
                            zhongBean = zhongBeans.get(4);
                            if (temp < 30) {
                                zhongBean.setValue1("优(" + temp + ")");
                                zhongBean.setValue2("空气质量非常好，非常适合户外活动，趁机出去多呼吸新鲜空气");
                            } else if (temp <= 100) {
                                zhongBean.setValue1("良(" + temp + ")");
                                zhongBean.setValue2("易感人群应适当减少室外活动");
                            } else {
                                zhongBean.setValue1("污染(" + temp + ")");
                                zhongBean.setValue2("空气质量差，不适合户外活动");
                            }

                            /**
                             * 为下部提供数据
                             */
                            shzsFra_sp.edit().putString("get_$Pm25126", shzsFra_sp.getString("get_$Pm25126", "") + get_all_sense.get_$Pm25126() + "-").commit();
                            shzsFra_sp.edit().putString("getTemperature", shzsFra_sp.getString("getTemperature", "") + get_all_sense.getTemperature() + "-").commit();
                            shzsFra_sp.edit().putString("getHumidity", shzsFra_sp.getString("getHumidity", "") + get_all_sense.getHumidity() + "-").commit();
                            shzsFra_sp.edit().putString("getCo2", shzsFra_sp.getString("getCo2", "") + get_all_sense.getCo2() + "-").commit();

                            zhongAda.notifyDataSetChanged();

                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }
                }));
            }
        }, 0, 3000);

        /**
         * 下部
         */
        fragments = new ArrayList<>();
        fragments.add(new SHZSFraXia1());
        fragments.add(new SHZSFraXia2());
        fragments.add(new SHZSFraXia3());
        fragments.add(new SHZSFraXia4());
        viewPager.setAdapter(new FragmentStatePagerAdapter(getFragmentManager()) {
            @Override
            public Fragment getItem(int i) {
                return fragments != null ? fragments.get(i) : new Fragment();
            }

            @Override
            public int getCount() {
                return fragments.size();
            }
        });
    }

    private void shangData() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("UserName", userName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        requestQueue.add(new JsonObjectRequest(Request.Method.POST, "http://" + urlBean.getUrl() + ":" + urlBean.getPort() + "/api/v2/get_weather", jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                if (jsonObject.optString("RESULT").equals("S")) {
                    Gson gson = new Gson();
                    Get_weather get_weather = gson.fromJson(jsonObject.toString(), Get_weather.class);
                    WCurrent.setText(get_weather.getWCurrent() + "°");
                    String[] split = get_weather.getROWS_DETAIL().get(1).getTemperature().split("~");
                    if (split.length == 2) {
                        temperature.setText("今天:" + split[0] + "-" + split[1] + "℃");
                    }

                    List<Get_weather.ROWSDETAILBean> rows_detail = get_weather.getROWS_DETAIL();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    ArrayList<String> strings = new ArrayList<>();
                    ArrayList<Float[]> floats = new ArrayList<>();
                    for (int i = 0; i < 6; i++) {
                        Get_weather.ROWSDETAILBean rowsdetailBean = rows_detail.get(i);
                        if (i == 0) {
                            strings.add("昨天");
                        } else if (i == 1) {
                            strings.add("今天");
                        } else if (i == 2) {
                            strings.add("明天");
                        } else {
                            try {
                                strings.add("周" + Util.getWeek(simpleDateFormat.parse(rowsdetailBean.getWData())));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                        String[] split1 = rowsdetailBean.getTemperature().split("~");
                        if (split1.length == 2) {
                            Float[] floats1 = {Float.valueOf(Integer.parseInt(split1[1])), Float.valueOf(Integer.parseInt(split1[0]))};
                            floats.add(floats1);
                        }
                    }

                    ShangLineManage shangLineManage = new ShangLineManage(lineChart);
                    shangLineManage.show(strings, floats);


                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        }));
    }

    private void initView(View view) {
        WCurrent = (TextView) view.findViewById(R.id.WCurrent);
        temperature = (TextView) view.findViewById(R.id.temperature);
        refresh = (ImageView) view.findViewById(R.id.refresh);
        lineChart = (LineChart) view.findViewById(R.id.lineChart);
        gridView = (GridView) view.findViewById(R.id.gridView);
        viewPager = (ViewPager) view.findViewById(R.id.viewPager);
        tv_bottom_1 = (TextView) view.findViewById(R.id.tv_bottom_1);
        tv_bottom_2 = (TextView) view.findViewById(R.id.tv_bottom_2);
        tv_bottom_3 = (TextView) view.findViewById(R.id.tv_bottom_3);
        tv_bottom_4 = (TextView) view.findViewById(R.id.tv_bottom_4);
    }

    public class ZhongBean {
        private int icon;
        private String name;
        private String value1;
        private String value2;

        public ZhongBean(int icon, String name, String value1, String value2) {
            this.icon = icon;
            this.name = name;
            this.value1 = value1;
            this.value2 = value2;
        }

        public int getIcon() {
            return icon;
        }

        public void setIcon(int icon) {
            this.icon = icon;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue1() {
            return value1;
        }

        public void setValue1(String value1) {
            this.value1 = value1;
        }

        public String getValue2() {
            return value2;
        }

        public void setValue2(String value2) {
            this.value2 = value2;
        }
    }

    public class ZhongAda extends BaseAdapter {

        @Override
        public int getCount() {
            return zhongBeans != null ? zhongBeans.size() : 0;
        }

        @Override
        public ZhongBean getItem(int position) {
            return zhongBeans.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ZhongBean item = getItem(position);
            View view = View.inflate(context, R.layout.ada_shzs_zhong, null);
            ViewHolder viewHolder = new ViewHolder(view);
            viewHolder.icon.setBackgroundResource(item.getIcon());
            viewHolder.name.setText(item.getName() + "");
            viewHolder.value1.setText(item.getValue1() + "");
            viewHolder.value2.setText(item.getValue2() + "");
            return view;
        }

        public class ViewHolder {
            public View rootView;
            public ImageView icon;
            public TextView name;
            public TextView value1;
            public TextView value2;

            public ViewHolder(View rootView) {
                this.rootView = rootView;
                this.icon = (ImageView) rootView.findViewById(R.id.icon);
                this.name = (TextView) rootView.findViewById(R.id.name);
                this.value1 = (TextView) rootView.findViewById(R.id.value1);
                this.value2 = (TextView) rootView.findViewById(R.id.value2);
            }

        }
    }

    public class ShangLineManage {
        private LineChart lineChart;
        private final YAxis axisLeft;
        private final YAxis axisRight;
        private final XAxis xAxis;

        public ShangLineManage(LineChart lineChart) {
            this.lineChart = lineChart;
            xAxis = lineChart.getXAxis();
            axisLeft = lineChart.getAxisLeft();
            axisRight = lineChart.getAxisRight();
            initStyle();
        }

        private void initStyle() {
            lineChart.setDescription("");
            lineChart.getLegend().setEnabled(false);
            xAxis.setTextSize(30);
            xAxis.setTextColor(Color.parseColor("#4052B5"));
            xAxis.setPosition(XAxis.XAxisPosition.TOP);
            xAxis.setDrawAxisLine(false);
            xAxis.setDrawGridLines(false);
            axisLeft.setDrawLabels(false);
            axisLeft.setDrawAxisLine(false);
            axisLeft.setDrawGridLines(true);
            axisRight.setEnabled(false);
        }

        public void show(ArrayList<String> x, ArrayList<Float[]> y) {
            ArrayList<LineDataSet> lineDataSets = new ArrayList<>();
            int[] colors = {Color.RED, Color.BLUE};
            for (int i = 0; i < 2; i++) {
                ArrayList<Entry> entries = new ArrayList<>();
                for (int j = 0; j < y.size(); j++) {
                    entries.add(new Entry(y.get(j)[i], j));
                }
                LineDataSet lineDataSet = new LineDataSet(entries, "");
                lineDataSet.setColor(colors[i]);
                lineDataSet.setDrawCircleHole(false);
                lineDataSet.setCircleColor(colors[i]);
                lineDataSets.add(lineDataSet);
            }
            LineData lineData = new LineData(x, lineDataSets);
            lineChart.setData(lineData);
            lineChart.invalidate();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }
}
