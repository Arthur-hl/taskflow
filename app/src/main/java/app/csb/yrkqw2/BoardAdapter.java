package app.csb.yrkqw2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import app.csb.yrkqw2.db.entity.BoardEntity;

public class BoardAdapter extends RecyclerView.Adapter<BoardAdapter.BoardViewHolder> {
    private List<BoardEntity> boardList;
    private OnBoardClickListener listener;

    public interface OnBoardClickListener {
        void onBoardClick(BoardEntity board);
    }

    public BoardAdapter(List<BoardEntity> boardList, OnBoardClickListener listener) {
        this.boardList = boardList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BoardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_board, parent, false);
        return new BoardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BoardViewHolder holder, int position) {
        BoardEntity board = boardList.get(position);
        holder.boardTitle.setText(board.title);
        holder.itemView.setOnClickListener(v -> listener.onBoardClick(board));
    }

    @Override
    public int getItemCount() {
        return boardList.size();
    }

    static class BoardViewHolder extends RecyclerView.ViewHolder {
        TextView boardTitle;

        public BoardViewHolder(@NonNull View itemView) {
            super(itemView);
            boardTitle = itemView.findViewById(R.id.boardTitle);
        }
    }
}
