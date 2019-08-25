package com.example.samue.login;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

 public class RVadapter extends RecyclerView.Adapter<RVadapter.FriendViewHolder> {

    List<Friends> friends;
    private boolean seleccion;
    private SparseBooleanArray selectedItems;

    public RVadapter(List<Friends> friendgroup) {
        this.friends = friendgroup;
        selectedItems = new SparseBooleanArray();
    }

    @Override
    public int getItemCount() {
        return friends.size();
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
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    //VIEWHOLDER donde implementamos la seleccion de items del cardview
    public class FriendViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        ImageView img_friendgroup;
        TextView name_friendgroup;
        private CardView tv_texto;
        private View item;

        FriendViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.cv_friend);
            name_friendgroup = (TextView) itemView.findViewById(R.id.name_friendgroup);
            img_friendgroup = (ImageView) itemView.findViewById(R.id.img_friendgroup);
        }

        public void bindView(Friends friend) {

            //Selecciona el objeto si estaba seleccionado
            if (selectedItems.get(getAdapterPosition())) {
                item.setSelected(true);
            } else
                item.setSelected(false);

            /**Activa el modo de selección*/
            item.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (!seleccion) {
                        seleccion = true;
                        v.setSelected(true);
                        selectedItems.put(getAdapterPosition(), true);
                    }

                    return true;
                }
            });

            /**Selecciona/deselecciona un ítem si está activado el modo selección*/
            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (seleccion) {
                        if (!v.isSelected()) {
                            v.setSelected(true);
                            selectedItems.put(getAdapterPosition(), true);
                        } else {
                            v.setSelected(false);
                            selectedItems.put(getAdapterPosition(), false);
                            if (!haySeleccionados())
                                seleccion = false;
                        }
                    }
                }
            });
            /**Activa el modo de selección*/
            item.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (!seleccion) {
                        seleccion = true;
                        v.setSelected(true);
                        selectedItems.put(getAdapterPosition(), true);
                    }
                    return true;
                }
            });
            /**Selecciona el objeto si estaba seleccionado*/
            if (selectedItems.get(getAdapterPosition()))
                item.setSelected(true);
            else
                item.setSelected(false);
            /**Selecciona un item si está activado el modo selección*/
            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (seleccion) {
                        if (!v.isSelected()) {
                            v.setSelected(true);
                            selectedItems.put(getAdapterPosition(), true);
                        } else {
                            v.setSelected(false);
                            selectedItems.put(getAdapterPosition(), false);
                            if (!haySeleccionados())
                                seleccion = false;
                        }
                    }
                }
            });
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
    public ArrayList<Friends> obtenerSeleccionados() {
        ArrayList<Friends> marcados = new ArrayList<>();
        for (int i = 0; i < friends.size(); i++) {
            if (selectedItems.get(i)) {
                marcados.add(friends.get(i));
            }
        }
        return marcados;
    }


}
