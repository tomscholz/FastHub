package com.fastaccess.ui.adapter.viewholder;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.transition.ChangeBounds;
import android.support.transition.TransitionManager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fastaccess.R;
import com.fastaccess.data.dao.ReactionsModel;
import com.fastaccess.data.dao.model.Issue;
import com.fastaccess.data.dao.model.PullRequest;
import com.fastaccess.data.dao.model.User;
import com.fastaccess.data.dao.timeline.PullRequestTimelineModel;
import com.fastaccess.helper.InputHelper;
import com.fastaccess.helper.ParseDateFormat;
import com.fastaccess.provider.scheme.LinkParserHelper;
import com.fastaccess.provider.timeline.CommentsHelper;
import com.fastaccess.provider.timeline.HtmlHelper;
import com.fastaccess.provider.timeline.handler.drawable.DrawableGetter;
import com.fastaccess.ui.adapter.callback.OnToggleView;
import com.fastaccess.ui.adapter.callback.ReactionsCallback;
import com.fastaccess.ui.widgets.AvatarLayout;
import com.fastaccess.ui.widgets.FontTextView;
import com.fastaccess.ui.widgets.SpannableBuilder;
import com.fastaccess.ui.widgets.recyclerview.BaseRecyclerAdapter;
import com.fastaccess.ui.widgets.recyclerview.BaseViewHolder;

import java.util.Date;

import butterknife.BindView;

/**
 * Created by Kosh on 13 Dec 2016, 1:03 AM
 */

public class PullRequestDetailsViewHolder extends BaseViewHolder<PullRequestTimelineModel> {

    @BindView(R.id.avatarView) AvatarLayout avatar;
    @BindView(R.id.date) FontTextView date;
    @BindView(R.id.name) FontTextView name;
    @BindView(R.id.comment) FontTextView comment;
    @BindView(R.id.thumbsUp) FontTextView thumbsUp;
    @BindView(R.id.thumbsDown) FontTextView thumbsDown;
    @BindView(R.id.laugh) FontTextView laugh;
    @BindView(R.id.sad) FontTextView sad;
    @BindView(R.id.hurray) FontTextView hooray;
    @BindView(R.id.heart) FontTextView heart;
    @BindView(R.id.toggle) View toggle;
    @BindView(R.id.commentMenu) View commentMenu;
    @BindView(R.id.commentOptions) View commentOptions;
    @BindView(R.id.toggleHolder) View toggleHolder;
    @BindView(R.id.emojiesList) View emojiesList;
    @BindView(R.id.reactionsText) TextView reactionsText;
    @BindView(R.id.owner) TextView owner;
    private OnToggleView onToggleView;
    private ReactionsCallback reactionsCallback;
    private ViewGroup viewGroup;
    private String repoOwner;
    private String poster;

    private PullRequestDetailsViewHolder(@NonNull View itemView, @NonNull ViewGroup viewGroup, @Nullable BaseRecyclerAdapter adapter,
                                         @NonNull OnToggleView onToggleView, @NonNull ReactionsCallback reactionsCallback,
                                         String repoOwner, String poster) {
        super(itemView, adapter);
        this.onToggleView = onToggleView;
        this.viewGroup = viewGroup;
        this.reactionsCallback = reactionsCallback;
        this.repoOwner = repoOwner;
        this.poster = poster;
        itemView.setOnClickListener(null);
        itemView.setOnLongClickListener(null);
        commentMenu.setOnClickListener(this);
        toggle.setOnClickListener(this);
        toggleHolder.setOnClickListener(this);
        laugh.setOnClickListener(this);
        sad.setOnClickListener(this);
        thumbsDown.setOnClickListener(this);
        thumbsUp.setOnClickListener(this);
        hooray.setOnClickListener(this);
        laugh.setOnLongClickListener(this);
        sad.setOnLongClickListener(this);
        thumbsDown.setOnLongClickListener(this);
        thumbsUp.setOnLongClickListener(this);
        hooray.setOnLongClickListener(this);
        heart.setOnLongClickListener(this);
        heart.setOnClickListener(this);
    }

    public static PullRequestDetailsViewHolder newInstance(@NonNull ViewGroup viewGroup, @Nullable BaseRecyclerAdapter adapter,
                                                           @NonNull OnToggleView onToggleView, @NonNull ReactionsCallback reactionsCallback,
                                                           @NonNull String repoOwner, @NonNull String poster) {
        return new PullRequestDetailsViewHolder(getView(viewGroup, R.layout.issue_detail_header_row_item), viewGroup,
                adapter, onToggleView, reactionsCallback, repoOwner, poster);
    }

    @Override public void bind(@NonNull PullRequestTimelineModel timelineModel) {
        if (timelineModel.getPullRequest() != null) {
            bind(timelineModel.getPullRequest());
        }
        if (onToggleView != null) onToggle(onToggleView.isCollapsed(getAdapterPosition()), false);
    }

    @Override public void onClick(View v) {
        if (v.getId() == R.id.toggle || v.getId() == R.id.toggleHolder) {
            if (onToggleView != null) {
                int position = getAdapterPosition();
                onToggleView.onToggle(position, !onToggleView.isCollapsed(position));
                onToggle(onToggleView.isCollapsed(position), true);
            }
        } else {
            addReactionCount(v);
            super.onClick(v);
        }
    }

    private void addReactionCount(View v) {
        if (adapter != null) {
            PullRequestTimelineModel timelineModel = (PullRequestTimelineModel) adapter.getItem(getAdapterPosition());
            if (timelineModel == null) return;
            ReactionsModel reactionsModel = null;
            PullRequest pullRequest = timelineModel.getPullRequest();
            int number = 0;
            if (pullRequest != null) {
                reactionsModel = pullRequest.getReactions();
                number = pullRequest.getNumber();
            }
            if (reactionsModel == null) reactionsModel = new ReactionsModel();
            boolean isReacted = reactionsCallback == null || reactionsCallback.isPreviouslyReacted(number, v.getId());
            boolean isCallingApi = reactionsCallback != null && reactionsCallback.isCallingApi(number, v.getId());
            switch (v.getId()) {
                case R.id.heart:
                    reactionsModel.setHeart(!isReacted ? reactionsModel.getHeart() + 1 : reactionsModel.getHeart() - 1);
                    break;
                case R.id.sad:
                    reactionsModel.setConfused(!isReacted ? reactionsModel.getConfused() + 1 : reactionsModel.getConfused() - 1);
                    break;
                case R.id.thumbsDown:
                    reactionsModel.setMinusOne(!isReacted ? reactionsModel.getMinusOne() + 1 : reactionsModel.getMinusOne() - 1);
                    break;
                case R.id.thumbsUp:
                    reactionsModel.setPlusOne(!isReacted ? reactionsModel.getPlusOne() + 1 : reactionsModel.getPlusOne() - 1);
                    break;
                case R.id.laugh:
                    reactionsModel.setLaugh(!isReacted ? reactionsModel.getLaugh() + 1 : reactionsModel.getLaugh() - 1);
                    break;
                case R.id.hurray:
                    reactionsModel.setHooray(!isReacted ? reactionsModel.getHooray() + 1 : reactionsModel.getHooray() - 1);
                    break;
            }
            if (pullRequest != null) {
                pullRequest.setReactions(reactionsModel);
                appendEmojies(reactionsModel);
                timelineModel.setPullRequest(pullRequest);
            }
        }
    }

    private void bind(@NonNull Issue issueModel) {
        setup(issueModel.getUser(), issueModel.getBodyHtml(), issueModel.getReactions());
        setupDate(issueModel.getCreatedAt(), issueModel.getUpdatedAt());
    }

    private void bind(@NonNull PullRequest pullRequest) {
        setup(pullRequest.getUser(), pullRequest.getBodyHtml(), pullRequest.getReactions());
        setupDate(pullRequest.getCreatedAt(), pullRequest.getUpdatedAt());
    }

    private void setup(User user, String description, ReactionsModel reactionsModel) {
        avatar.setUrl(user.getAvatarUrl(), user.getLogin(), user.isOrganizationType(), LinkParserHelper.isEnterprise(user.getHtmlUrl()));
        name.setText(user.getLogin());
        boolean isOwner = TextUtils.equals(repoOwner, user.getLogin());
        if (isOwner) {
            owner.setVisibility(View.VISIBLE);
            owner.setText(R.string.owner);
        } else {
            owner.setText(null);
            owner.setVisibility(View.GONE);
        }
        if (reactionsModel != null) {
            appendEmojies(reactionsModel);
        }
        if (description != null && !description.trim().isEmpty()) {
            HtmlHelper.htmlIntoTextView(comment, description, viewGroup.getWidth());
        } else {
            comment.setText(R.string.no_description_provided);
        }
    }

    private void setupDate(@NonNull Date createdDate, @NonNull Date updated) {
        date.setText(ParseDateFormat.getTimeAgo(createdDate));
    }

    private void appendEmojies(@NonNull ReactionsModel reaction) {
        SpannableBuilder spannableBuilder = SpannableBuilder.builder();
        reactionsText.setText("");
        thumbsUp.setText(SpannableBuilder.builder()
                .append(CommentsHelper.getThumbsUp()).append(" ")
                .append(String.valueOf(reaction.getPlusOne()))
                .append("   "));
        thumbsDown.setText(SpannableBuilder.builder()
                .append(CommentsHelper.getThumbsDown()).append(" ")
                .append(String.valueOf(reaction.getMinusOne()))
                .append("   "));
        hooray.setText(SpannableBuilder.builder()
                .append(CommentsHelper.getHooray()).append(" ")
                .append(String.valueOf(reaction.getHooray()))
                .append("   "));
        sad.setText(SpannableBuilder.builder()
                .append(CommentsHelper.getSad()).append(" ")
                .append(String.valueOf(reaction.getConfused()))
                .append("   "));
        laugh.setText(SpannableBuilder.builder()
                .append(CommentsHelper.getLaugh()).append(" ")
                .append(String.valueOf(reaction.getLaugh()))
                .append("   "));
        heart.setText(SpannableBuilder.builder()
                .append(CommentsHelper.getHeart()).append(" ")
                .append(String.valueOf(reaction.getHeart())));
        if (reaction.getPlusOne() > 0) {
            spannableBuilder.append(CommentsHelper.getThumbsUp())
                    .append(" ")
                    .append(String.valueOf(reaction.getPlusOne()))
                    .append("   ");
        }
        if (reaction.getMinusOne() > 0) {
            spannableBuilder.append(CommentsHelper.getThumbsDown())
                    .append(" ")
                    .append(String.valueOf(reaction.getMinusOne()))
                    .append("   ");
        }
        if (reaction.getLaugh() > 0) {
            spannableBuilder.append(CommentsHelper.getLaugh())
                    .append(" ")
                    .append(String.valueOf(reaction.getLaugh()))
                    .append("   ");
        }
        if (reaction.getHooray() > 0) {
            spannableBuilder.append(CommentsHelper.getHooray())
                    .append(" ")
                    .append(String.valueOf(reaction.getHooray()))
                    .append("   ");
        }
        if (reaction.getConfused() > 0) {
            spannableBuilder.append(CommentsHelper.getSad())
                    .append(" ")
                    .append(String.valueOf(reaction.getConfused()))
                    .append("   ");
        }
        if (reaction.getHeart() > 0) {
            spannableBuilder.append(CommentsHelper.getHeart())
                    .append(" ")
                    .append(String.valueOf(reaction.getHeart()));
        }
        if (spannableBuilder.length() > 0) {
            reactionsText.setText(spannableBuilder);
            if (!onToggleView.isCollapsed(getAdapterPosition())) {
                reactionsText.setVisibility(View.VISIBLE);
            }
        } else {
            reactionsText.setVisibility(View.GONE);
        }
    }

    private void onToggle(boolean expanded, boolean animate) {
        if (animate) {
            TransitionManager.beginDelayedTransition(viewGroup, new ChangeBounds());
        }
        toggle.setRotation(!expanded ? 0.0F : 180F);
        commentOptions.setVisibility(!expanded ? View.GONE : View.VISIBLE);
        if (!InputHelper.isEmpty(reactionsText)) {
            reactionsText.setVisibility(!expanded ? View.VISIBLE : View.GONE);
        }
    }

    @Override protected void onViewIsDetaching() {
        DrawableGetter drawableGetter = (DrawableGetter) comment.getTag(R.id.drawable_callback);
        if (drawableGetter != null) {
            drawableGetter.clear(drawableGetter);
        }
    }
}
