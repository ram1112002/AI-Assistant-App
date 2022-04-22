package com.example.aiassistant;

import android.content.Context;
import android.icu.number.CompactNotation;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class Adapter extends RecyclerView.Adapter {

    private ArrayList<ChatsModel> chatsModelsArrayList;
    private Context context;

    public Adapter(ArrayList<ChatsModel> chatsModelsArrayList, Context context) {
        this.chatsModelsArrayList = chatsModelsArrayList;
        this.context = context;
    }



    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType){
            case 0:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_area,parent,false);
                return new UserViewHolder(view);

            case 1:
                view  = LayoutInflater.from(parent.getContext()).inflate(R.layout.bot_response,parent,false);
                return new BotViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        ChatsModel chatsModel = chatsModelsArrayList.get(position);
        switch (chatsModel.getSender()){
            case "user":
                ((UserViewHolder)holder).user.setText(chatsModel.getMessage());
                break;
            case "bot":
                ((BotViewHolder)holder).bot.setText(chatsModel.getMessage());
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        switch (chatsModelsArrayList.get(position).getSender()){
            case "user":
                return 0;
            case "bot":
                return 1;
            default:
                return -1;
        }
    }


    @Override
    public int getItemCount() {
        return chatsModelsArrayList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder{
        TextView user;
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            user = itemView.findViewById(R.id.user_textView);
        }
    }

    public static class BotViewHolder extends RecyclerView.ViewHolder{
        TextView bot;
        public BotViewHolder(@NonNull View itemView) {
            super(itemView);

            bot = itemView.findViewById(R.id.idBot);
        }
    }
}
