package info.mschmitt.githubbrowser.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import info.mschmitt.githubbrowser.R;
import info.mschmitt.githubbrowser.android.FragmentUtils;
import info.mschmitt.githubbrowser.databinding.RepositorySplitViewBinding;
import info.mschmitt.githubbrowser.ui.viewmodels.RepositorySplitViewModel;

/**
 * @author Matthias Schmitt
 */
public class RepositorySplitViewFragment extends Fragment
        implements RepositoryListViewFragment.FragmentHost,
        RepositoryPagerViewFragment.FragmentHost {
    private static final String ARG_USERNAME = "ARG_USERNAME";
    @Inject Component mComponent;
    @Inject RepositorySplitViewModel mViewModel;
    private String mUsername;

    public static RepositorySplitViewFragment newInstance(String username) {
        RepositorySplitViewFragment fragment = new RepositorySplitViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USERNAME, username);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentUtils.getHost(this, FragmentHost.class).repositorySplitViewComponent(this)
                .inject(this);
        mUsername = getArguments().getString(ARG_USERNAME);
        mViewModel.onLoad(mUsername, savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        RepositorySplitViewBinding binding =
                RepositorySplitViewBinding.inflate(inflater, container, false);
        binding.setViewModel(mViewModel);
        if (getChildFragmentManager().findFragmentById(binding.masterView.getId()) == null) {
            getChildFragmentManager().beginTransaction()
                    .add(binding.masterView.getId(), RepositoryListViewFragment.newInstance())
                    .commit();
        }
        if (getChildFragmentManager().findFragmentById(binding.detailsView.getId()) == null) {
            getChildFragmentManager().beginTransaction()
                    .add(binding.detailsView.getId(), RepositoryPagerViewFragment.newInstance())
                    .commit();
        }
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(binding.toolbar);
        ActionBar supportActionBar = activity.getSupportActionBar();
        assert supportActionBar != null;
        supportActionBar.setDisplayHomeAsUpEnabled(true);
        supportActionBar.setTitle(mUsername);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        mViewModel.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mViewModel.onSave(outState);
    }

    @Override
    public void onPause() {
        mViewModel.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mComponent = null;
        mViewModel = null;
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.repository_split, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_about:
                return mViewModel.onAboutOptionsItemSelected();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean hideDetailsView() {
        return mViewModel.onHideDetailsView();
    }

    @Override
    public RepositoryPagerViewFragment.Component repositoryPagerViewComponent(
            RepositoryPagerViewFragment fragment) {
        return mComponent.repositoryPagerViewComponent(fragment);
    }

    @Override
    public RepositoryListViewFragment.Component repositoryListViewComponent(
            RepositoryListViewFragment fragment) {
        return mComponent.repositoryListViewComponent(fragment);
    }

    public interface Component {
        RepositoryListViewFragment.Component repositoryListViewComponent(
                RepositoryListViewFragment fragment);

        RepositoryPagerViewFragment.Component repositoryPagerViewComponent(
                RepositoryPagerViewFragment fragment);

        void inject(RepositorySplitViewFragment fragment);
    }

    public interface FragmentHost {
        Component repositorySplitViewComponent(RepositorySplitViewFragment fragment);
    }
}
