package com.example.permission.view.Adapters;

import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.permission.R;

import java.util.ArrayList;
import java.util.List;

public class RecorderAdapter extends RecyclerView.Adapter<RecorderAdapter.BaseViewHolder> {
    private List<String> mFilesList;
    private OnFileClickListener mFileClickListener;

    public RecorderAdapter(@NonNull List<String> filesList) {
        mFilesList = new ArrayList<>(filesList);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new BaseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.bindView(mFilesList.get(position));
    }

    public void setClickListener(@Nullable OnFileClickListener clickListener) {
        mFileClickListener = clickListener;
    }

    public void updateData(@NonNull List<String> fileNames) {
        mFilesList.clear();
        mFilesList.addAll(fileNames);
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return mFilesList.size();
    }

    public class BaseViewHolder extends RecyclerView.ViewHolder {
        private TextView mFileName;

        @RequiresApi(api = Build.VERSION_CODES.M)
        public BaseViewHolder(@NonNull View itemView) {
            super(itemView);
            mFileName = itemView.findViewById(android.R.id.text1);
            mFileName.setTextAppearance(R.style.TextAppearance_AppCompat_Medium);
        }

        private void bindView(final String fileName) {
            mFileName.setText(fileName);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mFileClickListener != null) {
                        mFileClickListener.onItemClick(fileName);
                    }
                }
            });
        }
    }
}

