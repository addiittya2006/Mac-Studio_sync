package xyz.addiittya.infinitescrollapi.adapters;

/**
 * Created by addiittya on 30/04/16.
 */

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import xyz.addiittya.infinitescrollapi.R;
import xyz.addiittya.infinitescrollapi.Utils.Question;

// Create the basic adapter extending from RecyclerView.Adapter
// Note that we specify the custom ViewHolder which gives us access to our views
public class QuestionsAdapter extends
        RecyclerView.Adapter<QuestionsAdapter.ViewHolder> {

    // Store a member variable for the Questions
    private List<Question> mQuestions;

    // Pass in the Question array into the constructor
    public QuestionsAdapter(List<Question> Questions) {
        mQuestions = Questions;
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView nameTextView;
        public Button messageButton;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            nameTextView = (TextView) itemView.findViewById(R.id.question_area);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View QuestionView = inflater.inflate(R.layout.question_layout, parent, false);

        ViewHolder viewHolder = new ViewHolder(QuestionView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Question Question = mQuestions.get(position);

        TextView textView = viewHolder.nameTextView;
        textView.setText(Question.getQuestion());

    }

    @Override
    public int getItemCount() {
        return mQuestions.size();
    }
}