package cn.edu.bjtu.mysport.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.edu.bjtu.mysport.R;
import cn.edu.bjtu.mysport.user.HttpService;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


public class BookList extends AppCompatActivity {
    private RecyclerView mRecyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy=new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recyclerview);

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);


        MyAdapter myAdapter = new MyAdapter(this);
        mRecyclerView.setAdapter(myAdapter);
        ItemTouchHelper.Callback callback = new myItemTouchHelperCallBack(myAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(mRecyclerView);

    }

    //adapter
    class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> implements ItemTouchHelperAdapter {

        LayoutInflater mInflater;
        List<MData> mList = addData();

        public MyAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.cardview, parent, false);
            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(MyAdapter.ViewHolder holder, int position) {
            holder.mName.setText(mList.get(position).getName());
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }

        @Override
        public void onItemMove(int fromPosition, int toPosition) {
            Collections.swap(mList, fromPosition, toPosition);
            notifyItemMoved(fromPosition, toPosition);
        }

        @Override
        public void onItemDelete(int position) {
            String word = mList.get(position).name;
            SharedPreferences sp = getSharedPreferences("loginInfo", MODE_PRIVATE);
            String email = sp.getString("email", "none");
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://192.168.179.1:8000/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .build();
            HttpService service = retrofit.create(HttpService.class);
            Call<ResponseBody> call = service.removeword(email,word);
            try {
                call.execute();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mList.remove(position);
            notifyItemRemoved(position);
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private TextView mName;

            public ViewHolder(View itemView) {
                super(itemView);
                mName = itemView.findViewById(R.id.txt_name);
            }
        }
    }

    public interface ItemTouchHelperAdapter {
        public void onItemMove(int fromPosition, int toPosition);

        public void onItemDelete(int position);
    }

    public class myItemTouchHelperCallBack extends ItemTouchHelper.Callback {
        private ItemTouchHelperAdapter itemTouchHelperAdapter;

        public myItemTouchHelperCallBack(ItemTouchHelperAdapter itemTouchHelperAdapter) {
            this.itemTouchHelperAdapter = itemTouchHelperAdapter;
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            int swipeFlags = ItemTouchHelper.LEFT;
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {

            itemTouchHelperAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            itemTouchHelperAdapter.onItemDelete(viewHolder.getAdapterPosition());
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return true;
        }
    }

    private class MData {
        String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    private List<MData> addData() {
        List<MData> list = new ArrayList();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.179.1:8000/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        HttpService service = retrofit.create(HttpService.class);
        SharedPreferences sp = getSharedPreferences("loginInfo", MODE_PRIVATE);
        String email = sp.getString("email", "none");

        Call<ResponseBody> call = service.getBook(email);
        try {
            JsonObject json = (JsonObject) new JsonParser().parse(call.execute().body().string());
            Integer num = Integer.parseInt(json.get("num").toString());
            if(num==-1){
                Toast.makeText(BookList.this,"请登录后使用单词本功能",Toast.LENGTH_LONG).show();
            }
            else{
                for(Integer i=0;i<num;i++){
                    String[] temp = json.get(i.toString()).toString().split("\"");
                    MData mData = new MData();
                    mData.setName(temp[1]);
                    list.add(mData);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }
}
