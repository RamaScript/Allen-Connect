package com.ramascript.allenconnect.features.notification;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ramascript.allenconnect.R;

import java.util.ArrayList;

public class notificationAdapter extends RecyclerView.Adapter<notificationAdapter.viewHolder>{

    ArrayList<notificationModel> list;
    Context context;

    public notificationAdapter(ArrayList<notificationModel> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);

        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        notificationModel model = list.get(position);

        holder.profile.setImageResource(model.getProfile());
        holder.notification.setText(Html.fromHtml(model.getNotification() ));
        holder.time.setText(model.getTime());

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder{

        ImageView profile;
        TextView notification, time;

        public viewHolder(@NonNull View itemView) {
            super(itemView);

            profile = itemView.findViewById(R.id.notification_profile_image);
            notification = itemView.findViewById(R.id.notificationTV);
            time = itemView.findViewById(R.id.notificationTimeTV);
        }
    }
}
