package com.sana.circleup;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactViewHolder> {
    private Context context;
    private List<Contacts> contactList;
    private OnContactSelectListener selectListener;

    public ContactsAdapter(Context context, List<Contacts> contactList, OnContactSelectListener listener) {
        this.context = context;
        this.contactList = contactList;
        this.selectListener = listener;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.contact_item_layout, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contacts contact = contactList.get(position);
        holder.userName.setText(contact.getUsername());
        holder.userStatus.setText(contact.getStatus());

        // Decode and set profile image
        if (contact.getProfileImage() != null && !contact.getProfileImage().isEmpty()) {
            Bitmap bitmap = decodeBase64(contact.getProfileImage());
            if (bitmap != null) {
                holder.profileImage.setImageBitmap(bitmap);
            } else {
                holder.profileImage.setImageResource(R.drawable.default_profile_img); // Set default image if decoding fails
            }
        } else {
            holder.profileImage.setImageResource(R.drawable.default_profile_img); // Default image for empty profiles
        }

        holder.itemView.setOnClickListener(v -> {
            if (holder.itemView.isSelected()) {
                holder.itemView.setSelected(false);
                selectListener.onContactUnselected(contact);
            } else {
                holder.itemView.setSelected(true);
                selectListener.onContactSelected(contact);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userStatus;
        ImageView profileImage;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.username);
            userStatus = itemView.findViewById(R.id.status);
            profileImage = itemView.findViewById(R.id.contactProfileImage); // Ensure your XML has this ImageView
        }
    }

    // Method to decode Base64 string to Bitmap
    private Bitmap decodeBase64(String encodedImage) {
        try {
            byte[] decodedBytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public interface OnContactSelectListener {
        void onContactSelected(Contacts contact);
        void onContactUnselected(Contacts contact);
    }
}
