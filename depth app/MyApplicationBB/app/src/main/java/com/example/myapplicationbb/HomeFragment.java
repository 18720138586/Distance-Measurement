package com.example.myapplicationbb;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        Button startMeasureButton = view.findViewById(R.id.start_measure_button);
        startMeasureButton.setOnClickListener(v -> {
            // 使用与MainActivity中相同的方式创建和显示CameraFragment
            // 这样可以确保Fragment的生命周期方法被正确调用
            CameraFragment cameraFragment = new CameraFragment();
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, cameraFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .addToBackStack(null)
                    .commit();

            // 更新底部导航栏选中状态
            BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation);
            bottomNavigationView.setSelectedItemId(R.id.navigation_camera);
        });

        return view;
    }
}