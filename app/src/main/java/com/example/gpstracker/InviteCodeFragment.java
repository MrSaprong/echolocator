package com.example.gpstracker;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class InviteCodeFragment extends Fragment {

    private String inviteCode;

    public InviteCodeFragment(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invite_code, container, false);

        TextView codeTextView = view.findViewById(R.id.codeTextView);
        ImageView copyIcon = view.findViewById(R.id.copyIcon);
        ImageView shareIcon = view.findViewById(R.id.shareIcon);

        codeTextView.setText(inviteCode);

        copyIcon.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) requireActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Invite Code", inviteCode);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(requireContext(), "Code copied to clipboard", Toast.LENGTH_SHORT).show();
        });

        shareIcon.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Join my circle using this code: " + inviteCode);
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        });

        return view;
    }
}
