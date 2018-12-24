package cn.edu.bjtu.mysport.activity;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cn.edu.bjtu.mysport.article.Article1;
import cn.edu.bjtu.mysport.article.Article2;
import cn.edu.bjtu.mysport.article.Article3;
import cn.edu.bjtu.mysport.article.Article4;
import cn.edu.bjtu.mysport.article.Article5;
import cn.edu.bjtu.mysport.article.Article6;
import cn.edu.bjtu.mysport.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class DashBoardFragment extends Fragment {


    public DashBoardFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_main, container, false);

        TextView article1 = view.findViewById(R.id.article_text1);
        TextView article2 = view.findViewById(R.id.article_text2);
        TextView article3 = view.findViewById(R.id.article_text3);
        TextView article4 = view.findViewById(R.id.article_text4);
        TextView article5 = view.findViewById(R.id.article_text5);
        TextView article6 = view.findViewById(R.id.article_text6);

        article1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),Article1.class);
                startActivity(intent);
            }
        });

        article2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),Article2.class);
                startActivity(intent);
            }
        });

        article3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),Article3.class);
                startActivity(intent);
            }
        });

        article4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),Article4.class);
                startActivity(intent);
            }
        });
        article5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),Article5.class);
                startActivity(intent);
            }
        });

        article6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),Article6.class);
                startActivity(intent);
            }
        });


        return view;
    }


}
