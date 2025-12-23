package edu.ueh.final_android_app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.ueh.final_android_app.R;
import edu.ueh.final_android_app.models.Account;

public class AccountSearchAdapter extends RecyclerView.Adapter<AccountSearchAdapter.Holder> {

    private final List<Account> list;
    private final OnUserClick listener;

    public interface OnUserClick { void onClick(Account user); }

    public AccountSearchAdapter(List<Account> list, OnUserClick listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_search, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int pos) {
        Account u = list.get(pos);
        h.tvName.setText("@" + u.getUsername());
        h.itemView.setOnClickListener(v -> listener.onClick(u));
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class Holder extends RecyclerView.ViewHolder {
        TextView tvName;
        public Holder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvUser);
        }
    }
}
