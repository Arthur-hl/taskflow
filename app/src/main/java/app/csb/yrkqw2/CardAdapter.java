package app.csb.yrkqw2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import app.csb.yrkqw2.db.entity.CardEntity;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {
    private List<CardEntity> cardList;
    private OnCardClickListener listener;

    public interface OnCardClickListener {
        void onCardClick(CardEntity card);
    }

    public CardAdapter(List<CardEntity> cardList, OnCardClickListener listener) {
        this.cardList = cardList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        CardEntity card = cardList.get(position);
        holder.cardTitle.setText(card.title);
        if (card.description != null && !card.description.isEmpty()) {
            holder.cardDescription.setText(card.description);
            holder.cardDescription.setVisibility(View.VISIBLE);
        } else {
            holder.cardDescription.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(v -> listener.onCardClick(card));
    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {
        TextView cardTitle, cardDescription;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardTitle = itemView.findViewById(R.id.cardTitle);
            cardDescription = itemView.findViewById(R.id.cardDescription);
        }
    }
}
