package com.example.quizzer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.viewholder>{

    private List<QuestionModel> list;

    public BookmarkAdapter(List<QuestionModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view  = LayoutInflater.from(parent.getContext()).inflate(R.layout.bookmark_item,parent,false);
        return new viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewholder holder, int position) {
        holder.setdata(list.get(position).getQuestion(),list.get(position).getAnswer(),position);


    }

    @Override
    public int getItemCount() {
       return list.size();
    }
    class viewholder extends RecyclerView.ViewHolder{

        private TextView question, answer;
        private ImageButton deletebtn;
        public viewholder(@NonNull View itemView) {
            super(itemView);

            question = itemView.findViewById(R.id.question);
            answer =itemView.findViewById(R.id.answer);
            deletebtn = itemView.findViewById(R.id.delete_btn);

        }

        private void setdata(String question, String answer,final int position)
        {
            this.question.setText(question);
            this.answer.setText(answer);

            deletebtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    list.remove( position);
                    notifyItemRemoved(position);
                }
            });
        }

    }


}
