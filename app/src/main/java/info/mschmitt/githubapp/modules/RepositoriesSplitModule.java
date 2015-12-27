package info.mschmitt.githubapp.modules;

import java.util.LinkedHashMap;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import info.mschmitt.githubapp.domain.AnalyticsManager;
import info.mschmitt.githubapp.entities.Repository;
import info.mschmitt.githubapp.network.GitHubService;
import info.mschmitt.githubapp.presenters.RepositorySplitPresenter;
import rx.Observable;

/**
 * @author Matthias Schmitt
 */
@Module
public class RepositoriesSplitModule {
    @Provides
    Observable<LinkedHashMap<Long, Repository>> provideRepositories(
            RepositorySplitPresenter presenter) {
        return presenter.getRepositories();
    }

    @Provides
    @Singleton
    public RepositorySplitPresenter providePresenter(GitHubService gitHubService,
                                                     AnalyticsManager analyticsManager) {
        return new RepositorySplitPresenter(gitHubService, analyticsManager);
    }
}