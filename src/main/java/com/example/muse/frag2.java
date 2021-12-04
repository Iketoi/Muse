package com.example.muse;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

public class frag2 extends Fragment {
    private View favo;

    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        favo = inflater.inflate(R.layout.frag2_layout, null);
        ImageView listView = favo.findViewById(R.id.lv);
        return favo;

    }
}
