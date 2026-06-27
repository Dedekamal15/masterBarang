package com.assettrack.presentation.screens.dashboard;

import androidx.work.WorkManager;
import com.assettrack.domain.repository.AssetRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class DashboardViewModel_Factory implements Factory<DashboardViewModel> {
  private final Provider<AssetRepository> repositoryProvider;

  private final Provider<WorkManager> workManagerProvider;

  public DashboardViewModel_Factory(Provider<AssetRepository> repositoryProvider,
      Provider<WorkManager> workManagerProvider) {
    this.repositoryProvider = repositoryProvider;
    this.workManagerProvider = workManagerProvider;
  }

  @Override
  public DashboardViewModel get() {
    return newInstance(repositoryProvider.get(), workManagerProvider.get());
  }

  public static DashboardViewModel_Factory create(Provider<AssetRepository> repositoryProvider,
      Provider<WorkManager> workManagerProvider) {
    return new DashboardViewModel_Factory(repositoryProvider, workManagerProvider);
  }

  public static DashboardViewModel newInstance(AssetRepository repository,
      WorkManager workManager) {
    return new DashboardViewModel(repository, workManager);
  }
}
