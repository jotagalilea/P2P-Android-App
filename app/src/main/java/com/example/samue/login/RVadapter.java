package com.example.samue.login;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

 public class RVadapter extends RecyclerView.Adapter<RVadapter.FriendViewHolder> {

    List<Friends> friends;
    public SparseBooleanArray selectedItems;
    public  List<Friends> marcados;



     public RVadapter(List<Friends> friendgroup) {
        this.friends = friendgroup;
        selectedItems = new SparseBooleanArray();
        marcados = new ArrayList<Friends>();
    }

      List<Friends> getCheckedItems() {
         List<Friends> checkedItems = new ArrayList<>();
         for (int i = 0; i < getItemCount(); i++) {
             if (selectedItems.get(i)) {
                 checkedItems.add(getItem(i));
             }
         }
         return checkedItems;
     }

    @Override
    public int getItemCount() {
        return friends.size();
    }

     public Friends getItem(int position) {
         return friends.get(position);
     }

    @Override
    public FriendViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_friendgroup, viewGroup, false);
        FriendViewHolder pvh = new FriendViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(FriendViewHolder friendViewHolder, int i) {
        friendViewHolder.name_friendgroup.setText(friends.get(i).getNombre());
        friendViewHolder.img_friendgroup.setImageResource(friends.get(i).img);
        friendViewHolder.cv.setSelected(selectedItems.get(i,false));
            }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    //VIEWHOLDER donde implementamos la seleccion de items del cardview
    public class FriendViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CardView cv;
        ImageView img_friendgroup;
        TextView name_friendgroup;
        private CardView tv_texto;
        private View item;
        RelativeLayout layout;

        public FriendViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.cv_friend);
            name_friendgroup = (TextView) itemView.findViewById(R.id.name_friendgroup);
            img_friendgroup = (ImageView) itemView.findViewById(R.id.img_friendgroup);
            cv.setOnClickListener(this);
            layout= (RelativeLayout) itemView.findViewById(R.id.carview_layout);
        }

        @Override
        public void onClick(View v) {
            if (!layout.isSelected()) {
                layout.setSelected(true);
                selectedItems.put(getAdapterPosition(), true);
            } else {
                layout.setSelected(false);
                selectedItems.put(getAdapterPosition(), false);
            }
            getCheckedItems();

            Log.d("CREATION",selectedItems.toString());
        }

    }

    public boolean haySeleccionados() {
        for (int i = 0; i <= friends.size(); i++) {
            if (selectedItems.get(i))
                return true;
        }
        return false;
    }
     /**
      * Devuelve aquellos objetos marcados.
      */
     public  ArrayList<Friends> obtenerSeleccionados() {
         ArrayList<Friends> marcados = new ArrayList<>();
         for (int i = 0; i < friends.size(); i++) {
             if (selectedItems.get(i)) {
                 marcados.add(friends.get(i));
             }
         }
         return marcados;
     }






}
