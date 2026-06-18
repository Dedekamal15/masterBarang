package com.assettrack.presentation.screens.transaction;

import com.assettrack.data.LocationHelper;
import com.assettrack.data.MasterBarangManager;
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
public final class TransactionViewModel_Factory implements Factory<TransactionViewModel> {
  private final Provider<AssetRepository> repositoryProvider;

  private final Provider<LocationHelper> locationHelperProvider;

  private final Provider<MasterBarangManager> masterBarangManagerProvider;

  public TransactionViewModel_Factory(Provider<AssetRepository> repositoryProvider,
      Provider<LocationHelper> locationHelperProvider,
      Provider<MasterBarangManager> masterBarangManagerProvider) {
    this.repositoryProvider = repositoryProvider;
    this.locationHelperProvider = locationHelperProvider;
    this.masterBarangManagerProvider = masterBarangManagerProvider;
  }

  @Override
  public TransactionViewModel get() {
    return newInstance(repositoryProvider.get(), locationHelperProvider.get(), masterBarangManagerProvider.get());
  }

  public static TransactionViewModel_Factory create(Provider<AssetRepository> repositoryProvider,
      Provider<LocationHelper> locationHelperProvider,
      Provider<MasterBarangManager> masterBarangManagerProvider) {
    return new TransactionViewModel_Factory(repositoryProvider, locationHelperProvider, masterBarangManagerProvider);
  }

  public static TransactionViewModel newInstance(AssetRepository repository,
      LocationHelper locationHelper, MasterBarangManager masterBarangManager) {
    return new TransactionViewModel(repository, locationHelper, masterBarangManager);
  }
}
