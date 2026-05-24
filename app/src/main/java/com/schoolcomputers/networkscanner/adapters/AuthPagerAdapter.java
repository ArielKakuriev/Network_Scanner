package com.networkscanner.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.networkscanner.fragments.ForgotPasswordFragment;
import com.networkscanner.fragments.LoginFragment;
import com.networkscanner.fragments.RegisterFragment;

/**
 * ViewPager2 adapter for the AuthActivity.
 * Pages: Login (0) | Register (1) | Forgot Password (2).
 */
public class AuthPagerAdapter extends FragmentStateAdapter {

    public AuthPagerAdapter(@NonNull FragmentActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 1:  return new RegisterFragment();
            case 2:  return new ForgotPasswordFragment();
            default: return new LoginFragment();
        }
    }

    @Override
    public int getItemCount() { return 3; }
}
