package com.dgsw.bamboo.activity;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.dgsw.bamboo.fragment.AdminFragment;
import com.dgsw.bamboo.data.Data;
import com.dgsw.bamboo.fragment.PostFragment;
import com.dgsw.bamboo.fragment.PostViewFragment;
import com.dgsw.bamboo.R;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    ActionBarDrawerToggle toggle;
    NavigationView navigationView;

    private boolean adminMenu = false;

    PostViewFragment postViewFragment = PostViewFragment.newInstance();
    PostFragment postFragment = PostFragment.newInstance();
    AdminFragment adminFragment = AdminFragment.newInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View v = getWindow().getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = v.getSystemUiVisibility();

            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;

            v.setSystemUiVisibility(flags);
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        drawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                //0.0 ~ 1.0
                //0.0 ~ 0.49? : close
                //0.5 ~ 1.0 : open
                if (slideOffset >= 0.2) {
                    clearLightStatusNavigationBar(v, MainActivity.this);
                } else if (slideOffset <= 0.1) {
                    setLightStatusNavigationBar(v, MainActivity.this);
                }
            }
        });
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.menu_home);

        if (savedInstanceState == null) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_content, postViewFragment);
            fragmentTransaction.commit();
        } else {
            Data.adminName = savedInstanceState.getString("adminName");
            Data.setToken(savedInstanceState.getByteArray("token"));
            adminMenu = savedInstanceState.getBoolean("adminMenu");
            adminMenuVisible(adminMenu);
            if (savedInstanceState.getBoolean("drawerOpen"))
                clearLightStatusNavigationBar(v, this);
        }
    }

    public void adminMenuVisible(boolean visible) {
        adminMenu = visible;
        navigationView.getMenu().getItem(4).setVisible(visible);
    }

    public void toPostViewFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_content, postViewFragment);
        transaction.commit();
    }

    public static void setLightStatusNavigationBar(View view, AppCompatActivity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = activity.getWindow();
            int flags = view.getSystemUiVisibility();

            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;

            animateStatusNavigationBarColor(window, ContextCompat.getColor(activity, R.color.colorPrimary));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                animateNavigationBarColor(window, ContextCompat.getColor(activity, R.color.colorPrimary));

            final int fi_flags = flags;
            new Handler().postDelayed(() -> view.setSystemUiVisibility(fi_flags), 450);
        }
    }

    public static void clearLightStatusNavigationBar(View view, AppCompatActivity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = activity.getWindow();
            int flags = view.getSystemUiVisibility();

            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;

            animateStatusNavigationBarColor(window, ContextCompat.getColor(activity, android.R.color.transparent));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                animateNavigationBarColor(window, ContextCompat.getColor(activity, R.color.colorPrimaryDark));

            final int fi_flags = flags;
            new Handler().postDelayed(() -> view.setSystemUiVisibility(fi_flags), 450);
        }
    }

    private static void animateStatusNavigationBarColor(Window window, int afterColor) {
        ValueAnimator colorAnimation = ValueAnimator.ofArgb(window.getStatusBarColor(), afterColor);
        colorAnimation.addUpdateListener(animator -> window.setStatusBarColor((Integer) animator.getAnimatedValue()));
        colorAnimation.start();
    }

    private static void animateNavigationBarColor(Window window, int afterColor) {
        ValueAnimator colorAnimation = ValueAnimator.ofArgb(window.getNavigationBarColor(), afterColor);
        colorAnimation.addUpdateListener(animator -> window.setNavigationBarColor((Integer) animator.getAnimatedValue()));
        colorAnimation.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Data.clear();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        outState.putString("adminName", Data.adminName);
        outState.putByteArray("token", Data.getToken());
        outState.putBoolean("adminMenu", adminMenu);
        outState.putBoolean("drawerOpen", drawer.isDrawerOpen(GravityCompat.START));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        String title = item.getTitle().toString();
        boolean isUI = true;
        switch (item.getItemId()) {
            case R.id.menu_home:
                title = getString(R.string.app_name);
                transaction.replace(R.id.fragment_content, postViewFragment);
                break;
            case R.id.menu_write:
                transaction.replace(R.id.fragment_content, postFragment);
                break;
            case R.id.menu_admin:
                transaction.replace(R.id.fragment_content, adminFragment);
                break;
            case R.id.menu_github:
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/seojeenyeok/dgsw-bamboo-mobile")));
                isUI = false;
                break;
            case R.id.menu_bug_report:
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/seojeenyeok/dgsw-bamboo-mobile/issues/new")));
                isUI = false;
                break;
            case R.id.menu_logout:
                Data.clear();
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_content);
                if (fragment != null)
                    if (fragment instanceof AdminFragment) {
                        ((AdminFragment) fragment).setViewVisibility(View.GONE, View.VISIBLE);
                    }
                adminMenuVisible(false);
                isUI = false;
                break;
        }
        if (isUI) {
            setTitle(title);
            transaction.commit();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}