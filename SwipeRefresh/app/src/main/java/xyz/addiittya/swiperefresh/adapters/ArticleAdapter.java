package xyz.addiittya.swiperefresh.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import xyz.addiittya.swiperefresh.R;
import xyz.addiittya.swiperefresh.util.Article;

/**
 * Created by addiittya on 04/05/16.
 */

public class ArticleAdapter extends BaseAdapter {

    private LayoutInflater lf;

    ArrayList<Article> mArrArticle = new ArrayList<>();

    public ArticleAdapter(ArrayList arr, Context c) {
        this.mArrArticle = arr;
        lf = LayoutInflater.from(c);
    }

    class  ViewHolder {
        TextView tvTitle;
        TextView tvText;
        TextView tvId;
    }

    @Override
    public int getCount() {
        return mArrArticle.size();
    }

    @Override
    public Object getItem(int i) {
        return mArrArticle.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder vh ;
        if(view == null){
            vh = new ViewHolder();
            view = lf.inflate(R.layout.row_listview,null);
            vh.tvTitle = (TextView) view.findViewById(R.id.txtTitle);
            vh.tvText = (TextView) view.findViewById(R.id.txtText);
            vh.tvId = (TextView) view.findViewById(R.id.txtId);
            view.setTag(vh);
        }
        else{
            vh = (ViewHolder) view.getTag();
        }

        Article article = mArrArticle.get(i);
        vh.tvTitle.setText(article.getTitle());
        vh.tvText.setText(article.getText());
        vh.tvId.setText(article.getId());
        return view;
    }

}
