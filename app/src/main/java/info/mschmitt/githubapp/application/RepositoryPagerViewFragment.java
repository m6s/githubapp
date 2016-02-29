package info.mschmitt.githubapp.application;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import info.mschmitt.githubapp.adapters.RepositoryPagerAdapter;
import info.mschmitt.githubapp.android.presentation.FragmentUtils;
import info.mschmitt.githubapp.databinding.RepositoryPagerViewBinding;
import info.mschmitt.githubapp.viewmodels.RepositoryPagerViewModel;

/**
 * @author Matthias Schmitt
 */
public class RepositoryPagerViewFragment extends Fragment
        implements RepositoryDetailsViewFragment.FragmentHost {
    @Inject Component mComponent;
    @Inject RepositoryPagerViewModel mViewModel;
    private RepositoryPagerAdapter mAdapter;

    public static RepositoryPagerViewFragment newInstance() {
        return new RepositoryPagerViewFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentUtils.getParent(this, FragmentHost.class).inject(this);
        mViewModel.onLoad(savedInstanceState);
        mAdapter =
                new RepositoryPagerAdapter(getChildFragmentManager(), mViewModel.getRepositories());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        RepositoryPagerViewBinding binding =
                RepositoryPagerViewBinding.inflate(inflater, container, false);
        binding.setViewModel(mViewModel);
        mAdapter.onCreateView(savedInstanceState);
        binding.setAdapter(mAdapter);
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
    public void onDestroyView() {
        mAdapter.onDestroyView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mComponent = null;
        mViewModel = null;
        mAdapter = null;
        super.onDestroy();
    }

    @Override
    public void inject(RepositoryDetailsViewFragment fragment) {
        mComponent.inject(fragment);
    }

    public interface Component {
        void inject(RepositoryDetailsViewFragment fragment);
    }

    public interface FragmentHost {
        void inject(RepositoryPagerViewFragment fragment);
    }
}
