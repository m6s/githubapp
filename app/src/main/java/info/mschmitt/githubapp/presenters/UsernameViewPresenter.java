package info.mschmitt.githubapp.presenters;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import javax.inject.Inject;

import info.mschmitt.githubapp.AnalyticsManager;
import info.mschmitt.githubapp.BR;
import info.mschmitt.githubapp.Validator;
import info.mschmitt.githubapp.android.presentation.ObjectsBackport;
import info.mschmitt.githubapp.android.presentation.OnErrorListener;
import info.mschmitt.githubapp.android.presentation.OnLoadingListener;
import info.mschmitt.githubapp.network.GitHubService;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * @author Matthias Schmitt
 */
public class UsernameViewPresenter extends BaseObservable {
    public static final String STATE_USER_NAME = "STATE_USER_NAME";
    private final CompositeSubscription mSubscriptions = new CompositeSubscription();
    private final Validator mValidator;
    private final GitHubService mGitHubService;
    private final AnalyticsManager mAnalyticsManager;
    private final UsernameSceneView mView;
    private String mUsername;
    private String mUsernameError;
    private final TextWatcher mUsernameTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            mUsername = s.toString();
            setUsernameError(null);
        }
    };
    private boolean mLoading;
    private final View.OnClickListener mOnShowRepositoriesClickListener = v -> showRepositories();

    @Inject
    public UsernameViewPresenter(UsernameSceneView view, Validator validator,
                                 GitHubService gitHubService, AnalyticsManager analyticsManager) {
        mView = view;
        mValidator = validator;
        mGitHubService = gitHubService;
        mAnalyticsManager = analyticsManager;
    }

    public String getUsername() {
        return mUsername;
    }

    private void showRepositories() {
        if (!mValidator.validateUsername(mUsername)) {
            setUsernameError("Validation error");
            return;
        }
        Subscription subscription =
                mGitHubService.getUser(mUsername).observeOn(AndroidSchedulers.mainThread())
                        .doOnUnsubscribe(() -> {
                            setLoading(false);
                            mView.getParentPresenter().onLoading(this, true, null);
                        }).subscribe(
                        user -> onShowRepositories(UsernameViewPresenter.this, mUsername),
                        throwable -> mView.getParentPresenter().onError(this, throwable,
                                this::showRepositories));
        mSubscriptions.add(subscription);
        setLoading(true);
        mView.getParentPresenter().onLoading(this, false, subscription::unsubscribe);
    }

    public void onShowRepositories(Object sender, String username) {
        mView.showRepositories(sender, username);
    }

    public View.OnClickListener getOnShowRepositoriesClickListener() {
        return mOnShowRepositoriesClickListener;
    }

    public TextWatcher getUsernameTextWatcher() {
        return mUsernameTextWatcher;
    }

    @Bindable
    public String getUsernameError() {
        return mUsernameError;
    }

    private void setUsernameError(String usernameError) {
        if (ObjectsBackport.equals(usernameError, mUsernameError)) {
            return;
        }
        mUsernameError = usernameError;
        notifyPropertyChanged(BR.usernameError);
    }

    @Bindable
    public boolean isLoading() {
        return mLoading;
    }

    private void setLoading(boolean loading) {
        if (loading == mLoading) {
            return;
        }
        mLoading = loading;
        notifyPropertyChanged(BR.loading);
    }

    public void onCreate(Bundle savedState) {
        if (savedState != null) {
            mUsername = savedState.getString(STATE_USER_NAME);
        }
    }

    public void onSave(Bundle outState) {
        outState.putString(STATE_USER_NAME, mUsername);
    }

    public void onDestroy() {
        mSubscriptions.unsubscribe();
    }

    public interface UsernameSceneView {
        ParentPresenter getParentPresenter();

        void showRepositories(Object sender, String username);
    }

    public interface ParentPresenter extends OnLoadingListener, OnErrorListener {
    }
}
