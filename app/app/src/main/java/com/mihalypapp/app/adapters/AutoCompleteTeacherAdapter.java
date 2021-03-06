package com.mihalypapp.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textview.MaterialTextView;
import com.mihalypapp.app.R;
import com.mihalypapp.app.models.User;

import java.util.ArrayList;
import java.util.List;

public class AutoCompleteTeacherAdapter extends ArrayAdapter<User> {
    private List<User> teacherListFull;

    public AutoCompleteTeacherAdapter(@NonNull Context context, @NonNull List<User> teacherList) {
        super(context, 0, teacherList);
        teacherListFull = new ArrayList<>(teacherList);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return teacherFilter;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.dropdown_menu_popup_item_teacher, parent, false);
        }

        MaterialTextView textViewTeacherName = convertView.findViewById(R.id.text_view_teacher_name);
        MaterialTextView textViewTeacherEmail = convertView.findViewById(R.id.text_view_teacher_email);

        User teacher = getItem(position);

        if (teacher != null) {
            textViewTeacherName.setText(teacher.getName());
            textViewTeacherEmail.setText(teacher.getEmail());
        }

        return convertView;
    }

    private Filter teacherFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            FilterResults results = new FilterResults();
            List<User> suggestions = new ArrayList<>();

            if (charSequence == null || charSequence.length() == 0) {
                suggestions.addAll(teacherListFull);
            } else {
                String filterPattern = charSequence.toString().toLowerCase().trim();

                for (User item : teacherListFull) {
                    if (item.getName().toLowerCase().contains(filterPattern) || item.getEmail().toLowerCase().contains(filterPattern)) {
                        suggestions.add(item);
                    }
                }
            }

            results.values = suggestions;
            results.count = suggestions.size();

            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            clear();
            addAll((List) filterResults.values);
            notifyDataSetChanged();
        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return ((User) resultValue).getEmail();
        }
    };
}
