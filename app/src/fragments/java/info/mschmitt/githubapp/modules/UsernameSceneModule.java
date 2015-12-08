package info.mschmitt.githubapp.modules;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import info.mschmitt.githubapp.presenters.UsernameSceneViewPresenter;

/**
 * @author Matthias Schmitt
 */
@Module
public class UsernameSceneModule {
    private UsernameSceneViewPresenter.UsernameSceneView mView;

    public UsernameSceneModule(UsernameSceneViewPresenter.UsernameSceneView view) {
        mView = view;
    }

    @Provides
    @Singleton
    public UsernameSceneViewPresenter providePresenter() {
        return new UsernameSceneViewPresenter(mView);
    }
}