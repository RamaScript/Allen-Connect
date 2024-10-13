package com.ramascript.allenconnect.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.ramascript.allenconnect.ChatDetailActivity;
import com.ramascript.allenconnect.Models.ChatMsgModel;
import com.ramascript.allenconnect.R;

import java.util.ArrayList;

public class ChatMsgAdapter extends RecyclerView.Adapter{

    ArrayList<ChatMsgModel> list;
    Context context;

    int SENDER_VIEW_TYPE = 1;
    int RECIEVER_VIEW_TYPE = 2 ;

    public ChatMsgAdapter(ArrayList<ChatMsgModel> chatMsgModel, Context context) {
        this.list = chatMsgModel;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == SENDER_VIEW_TYPE){
            View view = LayoutInflater.from(context).inflate(R.layout.rv_sample_sender,parent,false);
            return new senderViewHolder(view);
        }else {
            View view = LayoutInflater.from(context).inflate(R.layout.rv_sample_receiver,parent,false);
            return new recieverViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(list.get(position).getuId().equals(FirebaseAuth.getInstance().getUid())){
            return SENDER_VIEW_TYPE;
        }else {
            return RECIEVER_VIEW_TYPE;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMsgModel messageModel = list.get(position);

        if (holder.getClass()==senderViewHolder.class){
            ((senderViewHolder)holder).senderMsg.setText(messageModel.getMessage());
        }else {
            ((recieverViewHolder)holder).receiverMsg.setText(messageModel.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class recieverViewHolder extends RecyclerView.ViewHolder{

        TextView receiverMsg, receiverTime;

        public recieverViewHolder(@NonNull View itemView) {
            super(itemView);
            receiverMsg = itemView.findViewById(R.id.recieverText);
            receiverTime = itemView.findViewById(R.id.recieveTime);
        }
    }

    public class senderViewHolder extends RecyclerView.ViewHolder{

        TextView senderMsg, senderTime;
        public senderViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMsg = itemView.findViewById(R.id.senderText);
            senderTime = itemView.findViewById(R.id.senderTime);
        }
    }
}
