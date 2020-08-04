package com.example.quizzer;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.viewHolder>
{
    private List<CategoriesModel> categoriesModelList;

    public CategoryAdapter(List<CategoriesModel> categoriesModelList) {
        this.categoriesModelList = categoriesModelList;
    }

   @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item,parent,false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        holder.setData(categoriesModelList.get(position).getUrl(),categoriesModelList.get(position).getName(),position);
    }

    @Override
    public int getItemCount() {
        return categoriesModelList.size();
    }

    class viewHolder extends RecyclerView.ViewHolder{
       private CircleImageView imageView;
       private TextView title;

        public viewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageview);
            title  = itemView.findViewById(R.id.title);
           }
        private void setData(String url, final String title, final int position)
        {
            Glide.with(itemView.getContext()).load(url).into(imageView);
            this.title.setText(title);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent setIntent = new Intent(itemView.getContext(),SetsActivity.class);
                    setIntent.putExtra("title",title);
                    setIntent.putExtra("position",position);
                    itemView.getContext().startActivity(setIntent);
                }
            });
        }
    }

}
