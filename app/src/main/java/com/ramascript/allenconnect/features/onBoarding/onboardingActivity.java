package com.ramascript.allenconnect.features.onBoarding;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.ramascript.allenconnect.features.auth.loginAs;
import com.ramascript.allenconnect.features.auth.registerAs;
import com.ramascript.allenconnect.R;

public class onboardingActivity extends AppCompatActivity {

    private LinearLayout layoutIndicators;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        layoutIndicators = findViewById(R.id.layoutIndicators);
        viewPager = findViewById(R.id.viewPager);

        setupOnboardingItems();
        setupIndicators();
        setCurrentIndicator(0);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setCurrentIndicator(position);
            }
        });

        findViewById(R.id.buttonJoinNow).setOnClickListener(view -> {
            startActivity(new Intent(this, registerAs.class));
            finish();
        });

        findViewById(R.id.textSignIn).setOnClickListener(view -> {
            startActivity(new Intent(this, loginAs.class));
            finish();
        });
    }

    private void setupOnboardingItems() {
        OnboardingAdapter onboardingAdapter = new OnboardingAdapter(
                new OnboardingItem[] {
                    new OnboardingItem(
                        R.drawable.onboarding1,
                        "Connect with Students & Alumni",
                        "Stay in touch with classmates, seniors, and professors in one place."
                    ),
                    new OnboardingItem(
                        R.drawable.onboarding2,
                        "Join Communities & Collaborate",
                        "Engage in discussions, share knowledge, and build your professional network."
                    ),
                    new OnboardingItem(
                        R.drawable.onboarding3,
                        "Stay Updated & Share Experiences",
                        "Post updates, share memories, and stay connected with your Allenhouse family."
                    )
                });
        viewPager.setAdapter(onboardingAdapter);
    }

    private void setupIndicators() {
        ImageView[] indicators = new ImageView[3];
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(8, 0, 8, 0);

        for (int i = 0; i < indicators.length; i++) {
            indicators[i] = new ImageView(getApplicationContext());
            indicators[i].setImageDrawable(ContextCompat.getDrawable(
                    getApplicationContext(),
                    R.drawable.indicator_inactive));
            indicators[i].setLayoutParams(layoutParams);
            layoutIndicators.addView(indicators[i]);
        }
    }

    private void setCurrentIndicator(int index) {
        int childCount = layoutIndicators.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ImageView imageView = (ImageView) layoutIndicators.getChildAt(i);
            if (i == index) {
                imageView.setImageDrawable(ContextCompat.getDrawable(
                        getApplicationContext(),
                        R.drawable.indicator_active));
            } else {
                imageView.setImageDrawable(ContextCompat.getDrawable(
                        getApplicationContext(),
                        R.drawable.indicator_inactive));
            }
        }
    }

    private static class OnboardingItem {
        private final int image;
        private final String title;
        private final String description;

        OnboardingItem(int image, String title, String description) {
            this.image = image;
            this.title = title;
            this.description = description;
        }
    }

    private class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {

        private final OnboardingItem[] onboardingItems;

        OnboardingAdapter(OnboardingItem[] onboardingItems) {
            this.onboardingItems = onboardingItems;
        }

        @NonNull
        @Override
        public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new OnboardingViewHolder(getLayoutInflater().inflate(
                    R.layout.item_onboarding, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
            holder.setOnboardingData(onboardingItems[position]);
        }

        @Override
        public int getItemCount() {
            return onboardingItems.length;
        }

        class OnboardingViewHolder extends RecyclerView.ViewHolder {

            private final ImageView imageOnboarding;
            private final android.widget.TextView textTitle;
            private final android.widget.TextView textDescription;

            OnboardingViewHolder(@NonNull android.view.View itemView) {
                super(itemView);
                imageOnboarding = itemView.findViewById(R.id.imageOnboarding);
                textTitle = itemView.findViewById(R.id.textTitle);
                textDescription = itemView.findViewById(R.id.textDescription);
            }

            void setOnboardingData(OnboardingItem onboardingItem) {
                imageOnboarding.setImageResource(onboardingItem.image);
                textTitle.setText(onboardingItem.title);
                textDescription.setText(onboardingItem.description);
            }
        }
    }
}