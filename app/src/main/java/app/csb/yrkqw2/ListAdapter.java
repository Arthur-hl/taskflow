package app.csb.yrkqw2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import app.csb.yrkqw2.db.entity.ListEntity;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ListViewHolder> {
    private List<ListEntity> listItems;
    private OnListClickListener listener;

    public interface OnListClickListener {
        void onListClick(ListEntity list);
    }

    public ListAdapter(List<ListEntity> listItems, OnListClickListener listener) {
        this.listItems = listItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false);
        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        ListEntity list = listItems.get(position);
        holder.listTitle.setText(list.title);
        holder.itemView.setOnClickListener(v -> listener.onListClick(list));
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    static class ListViewHolder extends RecyclerView.ViewHolder {
        TextView listTitle;

        public ListViewHolder(@NonNull View itemView) {
            super(itemView);
            listTitle = itemView.findViewById(R.id.listTitle);
        }
    }
}
