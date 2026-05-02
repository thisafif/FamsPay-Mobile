package com.kelompoksepuluh.famspay;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.airbnb.lottie.LottieAnimationView;
import java.util.List;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {

    private final List<OnboardingItem> onboardingItems;

    public OnboardingAdapter(List<OnboardingItem> onboardingItems) {
        this.onboardingItems = onboardingItems;
    }

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new OnboardingViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.item_onboarding, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        holder.bind(onboardingItems.get(position));
    }

    @Override
    public int getItemCount() {
        return onboardingItems.size();
    }

    class OnboardingViewHolder extends RecyclerView.ViewHolder {
        private final LottieAnimationView lottieAnimation;
        private final TextView textTitle;
        private final TextView textDescription;

        OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);
            lottieAnimation = itemView.findViewById(R.id.lottieAnimation);
            textTitle = itemView.findViewById(R.id.tvOnboardingTitle);
            textDescription = itemView.findViewById(R.id.tvOnboardingDescription);
        }

        void bind(OnboardingItem item) {
            lottieAnimation.setAnimation(item.lottieResId);
            textTitle.setText(item.titleStringId);
            textDescription.setText(item.descStringId);
        }
    }
}