package cn.edu.bjtu.mysport.activity;


import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import cn.edu.bjtu.mysport.R;
import cn.edu.bjtu.mysport.bean.ResultBean;
import cn.edu.bjtu.mysport.net.NetConnection;
import cn.edu.bjtu.mysport.ui.ExpandListView;

import static android.content.Context.MODE_PRIVATE;

import cn.edu.bjtu.mysport.user.HttpService;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * A simple {@link Fragment} subclass.
 */
public class QueryFragment extends Fragment {

    //控件
    private EditText wordInput;
    private TextView us_phonetic, uk_phonetic;
    private ListView explansListView, webListView;
    private ImageButton uk_voice, us_voice,queryButton;
    private ScrollView scrollView;
    private Button button_add;

    private MediaPlayer player;

    private NetConnection connection = null;
    private volatile ResultBean resultBean = null;
    private volatile boolean singleFlag = false; //进行按钮阻塞
    private volatile boolean playFlag = false; //为true时，发音按钮才可以点击


    //数据适配器
    private ArrayAdapter<String> basicExpAdapter;
    private SimpleAdapter webExpAdapter;
    private ArrayList<HashMap<String, String>> webDataList = new ArrayList<>();
    private HashMap<String, String> webDataMap;


    /**
     * 绑定View
     */
    private void findView() {
        wordInput = (EditText) getView().findViewById(R.id.wordInput);
        us_phonetic = (TextView) getView().findViewById(R.id.us_phonetic);
        uk_phonetic = (TextView) getView().findViewById(R.id.uk_phonetic);
        explansListView = (ExpandListView) getView().findViewById(R.id.explansListView);
        webListView = (ExpandListView) getView().findViewById(R.id.webListView);
        uk_voice = (ImageButton) getView().findViewById(R.id.uk_voice);
        us_voice = (ImageButton) getView().findViewById(R.id.us_voice);
        scrollView = (ScrollView) getView().findViewById(R.id.scrollView);
        queryButton = (ImageButton) getView().findViewById(R.id.queryButton);
        button_add = (Button) getView().findViewById(R.id.button_add);

        uk_voice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playVoice(v);
            }
        });

        us_voice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playVoice(v);
            }
        });

        queryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doQuery(v);
            }
        });

        button_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String word = wordInput.getText().toString();
                if(word.length()==0){
                    Toast.makeText(getActivity().getApplicationContext(),"请先输入单词！",Toast.LENGTH_SHORT).show();
                    return;
                }
                SharedPreferences sp = getActivity().getSharedPreferences("loginInfo", MODE_PRIVATE);
                String email = sp.getString("email", "none");
                if(email.equals("none")){
                    Toast.makeText(getActivity().getApplicationContext(),"请先登录再使用本功能！",Toast.LENGTH_SHORT).show();
                    return;
                }
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("http://192.168.179.1:8000/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                        .build();

                HttpService service = retrofit.create(HttpService.class);
                Call<ResponseBody> call = service.addword(email,word);
                try {
                    call.execute();
                    Toast.makeText(getActivity().getApplicationContext(),"成功添加到生词本！",Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //点击按钮查询
    public void doQuery(View v) {
        connection.setQuery("" + wordInput.getText().toString().trim());

        //如果查询内容没变，则不进行请求
        if (resultBean != null && connection.getQuery().equals(resultBean.getQuery())) {
            return;
        }

        if (!connection.getQuery().equals("") && !singleFlag) {

            Toast.makeText(getActivity().getApplicationContext(), "开始查询", Toast.LENGTH_SHORT).show();
            singleFlag = true;
            //数据初始化
            resetInitData();
            //执行查询操作,网络需要开启线程
            new Thread() {
                @Override
                public void run() {
                    resultBean = connection.doQuery();

                    if (resultBean != null && resultBean.getErrorCode() == 0) {
                        showData(resultBean);
                    } else {
                        try {
                            sleep(1500);
                        } catch (InterruptedException e) {
                        } finally {
                            singleFlag = false;

                            //显示网络连接失败提示信息
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity().getApplicationContext(), "网络连接失败", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }
            }.start();

        } else {
            Toast.makeText(getActivity().getApplicationContext(), "查询中，请稍等...", Toast.LENGTH_SHORT).show();
        }
    }

    public void playVoice(View view) {
        //返回数据才允许播放语音
        if (resultBean != null && playFlag) {

            ImageButton clickedView = (ImageButton) view;
            int type = clickedView == us_voice ? 1 : 2;
            try {
                player.reset();
                player.setDataSource(getActivity(), Uri.parse(connection.getVoiceURI() + "?audio=" + resultBean.getQuery() + "&type=" + type));
                player.prepareAsync();
                player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        mediaPlayer.start();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 数据初始化
     */
    private void resetInitData() {

        scrollView.setVisibility(View.INVISIBLE);
        resultBean = null;
        //清空适配器中的数据
        basicExpAdapter.clear();
        basicExpAdapter.notifyDataSetChanged();
        webDataList.clear();
        webExpAdapter.notifyDataSetChanged();

        us_phonetic.setText("");
        uk_phonetic.setText("");
        playFlag = false;
    }

    /**
     * 通过UI线程更新数据显示
     */
    private void showData(final ResultBean bean) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (bean.getBasic() != null) {
                    scrollView.setVisibility(View.VISIBLE);
                    Toast.makeText(getActivity().getApplicationContext(), "查询到结果，正在刷新数据....", Toast.LENGTH_LONG).show();
                    //发音为null,不显示null
                    if (bean.getBasic().getUk_phonetic() != null)
                        uk_phonetic.setText(bean.getBasic().getUk_phonetic());
                    if (bean.getBasic().getUs_phonetic() != null)
                        us_phonetic.setText(bean.getBasic().getUs_phonetic());

                    //适配器数据
                    basicExpAdapter.addAll(resultBean.getBasic().getExplains());
                    basicExpAdapter.notifyDataSetChanged();
                    if (bean.getWeb() != null) {
                        for (ResultBean.WebEntity webEntity : bean.getWeb()){
                            webDataMap = new HashMap<String, String>();
                            webDataMap.put("key",webEntity.getKey());
                            webDataMap.put("value",webEntity.getValues());
                            webDataList.add(webDataMap);
                        }
                        webExpAdapter.notifyDataSetChanged();
                    }
                    playFlag = true; //查询到结果才可以发音
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "未查到结果", Toast.LENGTH_LONG).show();
                }
                singleFlag = false;
            }
        });
    }

    public QueryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        connection = NetConnection.getInstance();
        findView();

        basicExpAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, new ArrayList<String>());
        explansListView.setAdapter(basicExpAdapter);
        webExpAdapter = new SimpleAdapter(getActivity(), webDataList, R.layout.webitem_layout, new String[]{"key", "value"}, new int[]{R.id.webKey, R.id.webValue});
        webListView.setAdapter(webExpAdapter);

        player = new MediaPlayer();
        resultBean = new ResultBean();

        //事件监听，回车键即开始搜索
        wordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (wordInput.getText().toString().trim().length() == 0) {
                    resetInitData();
                }
                if (wordInput.getText().toString().contains("\n")) {

                    wordInput.setText(wordInput.getText().toString().replaceAll("\\n", ""));
                    wordInput.setSelection(wordInput.getText().toString().length());
                    doQuery(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        StrictMode.ThreadPolicy policy=new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        return inflater.inflate(R.layout.fragment_query, container, false);
    }

}
