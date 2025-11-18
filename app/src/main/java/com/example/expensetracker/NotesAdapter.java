package com.example.expensetracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    private final Context context;
    private final ArrayList<Note> notes;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onDeleteClick(int noteId);
        void onItemClick(Note note);
    }

    public NotesAdapter(Context context, ArrayList<Note> notes, OnItemClickListener listener) {
        this.context = context;
        this.notes = notes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);
        holder.tvTitle.setText(note.getTitle());
        holder.tvContent.setText(note.getContent());

        // Show reminder time if set
        long reminder = note.getReminderTime();
        if (reminder > 0) {
            holder.tvReminder.setText("Reminder: " + Note.formatTime(reminder));
            holder.tvReminder.setVisibility(View.VISIBLE);
        } else {
            holder.tvReminder.setVisibility(View.GONE);
        }

        // Click listeners
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(note.getId()));
        holder.itemView.setOnClickListener(v -> listener.onItemClick(note));
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvReminder;
        ImageButton btnDelete;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvNoteTitle);
            tvContent = itemView.findViewById(R.id.tvNoteContent);
            tvReminder = itemView.findViewById(R.id.tvReminderTime);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}