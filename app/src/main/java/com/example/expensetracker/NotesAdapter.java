package com.example.expensetracker;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    private final ArrayList<Note> noteList;
    private final Context context;
    private final NotesDBHelper dbHelper;

    public NotesAdapter(ArrayList<Note> noteList, Context context) {
        this.noteList = noteList;
        this.context = context;
        this.dbHelper = new NotesDBHelper(context);
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = noteList.get(position);

        holder.tvTitle.setText(note.getTitle());
        holder.tvContent.setText(note.getContent());

        // Display reminder time with AM/PM
        if (note.getReminderTime() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
            holder.tvReminderTime.setText(sdf.format(note.getReminderTime()));
        } else {
            holder.tvReminderTime.setText("");
        }

        // Reset background to avoid RecyclerView glitches
        if (note.isImportant()) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorImportant));
        } else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorNormal));
        }

        // Open AddEditNoteActivity on click
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddEditNoteActivity.class);
            intent.putExtra("noteId", note.getId());
            context.startActivity(intent);
        });

        // Delete note
        holder.btnDelete.setOnClickListener(v -> {
            int rowsDeleted = dbHelper.deleteNote(note.getId());
            if (rowsDeleted > 0) {
                noteList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, noteList.size());
                Toast.makeText(context, "Note deleted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Failed to delete note", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    // ViewHolder class
    public static class NoteViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle, tvContent, tvReminderTime;
        ImageButton btnDelete;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvNoteTitle);
            tvContent = itemView.findViewById(R.id.tvNoteContent);
            tvReminderTime = itemView.findViewById(R.id.tvReminderTime); // Add this TextView in item_note.xml
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}