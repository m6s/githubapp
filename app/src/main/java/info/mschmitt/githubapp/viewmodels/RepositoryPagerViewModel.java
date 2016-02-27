package info.mschmitt.githubapp.viewmodels;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;

import info.mschmitt.githubapp.BR;
import info.mschmitt.githubapp.di.RepositoryPagerViewScope;
import info.mschmitt.githubapp.di.qualifiers.RepositoryMapObservable;
import info.mschmitt.githubapp.di.qualifiers.SelectedRepositorySubject;
import info.mschmitt.githubapp.domain.AnalyticsService;
import info.mschmitt.githubapp.entities.Repository;
import rx.Observable;
import rx.subjects.Subject;
import rx.subscriptions.CompositeSubscription;

/**
 * @author Matthias Schmitt
 */
@RepositoryPagerViewScope
public class RepositoryPagerViewModel extends BaseObservable {
    private static final String ARG_CURRENT_REPOSITORY_ID = "ARG_CURRENT_REPOSITORY_ID";
    private final Observable<LinkedHashMap<Long, Repository>> mRepositoryMapObservable;
    private final Subject<Repository, Repository> mSelectedRepositorySubject;
    private final AnalyticsService mAnalyticsService;
    private final ObservableList<Repository> mRepositories = new ObservableArrayList<>();
    private final Map<Long, Integer> mPageIndexes = new HashMap<>();
    private final NavigationHandler mNavigationHandler;
    private CompositeSubscription mSubscriptions;
    private int mSelectedPagePosition;
    private final ViewPager.OnPageChangeListener mOnPageChangeListener =
            new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset,
                                           int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    if (mSelectedPagePosition == position) {
                        return;
                    }
                    mSelectedPagePosition = position;
                    Repository repository = mRepositories.get(position);
                    mSelectedRepositorySubject.onNext(repository);
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            };
    private long mCurrentRepositoryId;

    @Inject
    public RepositoryPagerViewModel(@RepositoryMapObservable
                                    Observable<LinkedHashMap<Long, Repository>>
                                                repositoryMapObservable,
                                    @SelectedRepositorySubject
                                    Subject<Repository, Repository> selectedRepositorySubject,
                                    AnalyticsService analyticsService,
                                    NavigationHandler navigationHandler) {
        mRepositoryMapObservable = repositoryMapObservable;
        mSelectedRepositorySubject = selectedRepositorySubject;
        mAnalyticsService = analyticsService;
        mNavigationHandler = navigationHandler;
    }

    public void onLoad(Bundle savedState) {
        mCurrentRepositoryId =
                savedState != null ? savedState.getLong(ARG_CURRENT_REPOSITORY_ID) : -1;
    }

    public void onResume() {
        mSubscriptions = new CompositeSubscription();
        mSubscriptions.add(mRepositoryMapObservable.subscribe((repositoryMap) -> {
            mPageIndexes.clear();
            int i = 0;
            for (long id : repositoryMap.keySet()) {
                mPageIndexes.put(id, i++);
            }
            mRepositories.clear();
            mRepositories.addAll(repositoryMap.values());
            if (mCurrentRepositoryId != -1) {
                setCurrentRepositoryId(mCurrentRepositoryId);
            }
        }));
        mSubscriptions.add(mSelectedRepositorySubject.subscribe(this::selectRepository));
        mAnalyticsService.logScreenView(getClass().getName());
    }

    private void setCurrentRepositoryId(long repositoryId) {
        mCurrentRepositoryId = repositoryId;
        notifyPropertyChanged(BR.currentItem);
    }

    public void onSave(Bundle outState) {
        outState.putLong(ARG_CURRENT_REPOSITORY_ID, mCurrentRepositoryId);
    }

    public ViewPager.OnPageChangeListener getOnPageChangeListener() {
        return mOnPageChangeListener;
    }

    public void onPause() {
        mSubscriptions.unsubscribe();
    }

    public void selectRepository(Repository repository) {
        setCurrentRepositoryId(repository.id());
    }

    @Bindable
    public int getCurrentItem() {
        Integer integer = mPageIndexes.get(mCurrentRepositoryId);
        return integer != null ? integer : -1;
    }

    public ObservableList<Repository> getRepositories() {
        return mRepositories;
    }

    public interface NavigationHandler {
    }
}
