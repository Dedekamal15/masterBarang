package com.assettrack.domain.repository;

import com.assettrack.data.local.dao.AssetDao;
import com.assettrack.data.local.dao.TransactionDao;
import com.assettrack.data.remote.api.AssetTrackApiService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class AssetRepository_Factory implements Factory<AssetRepository> {
  private final Provider<AssetDao> assetDaoProvider;

  private final Provider<TransactionDao> transactionDaoProvider;

  private final Provider<AssetTrackApiService> apiProvider;

  public AssetRepository_Factory(Provider<AssetDao> assetDaoProvider,
      Provider<TransactionDao> transactionDaoProvider, Provider<AssetTrackApiService> apiProvider) {
    this.assetDaoProvider = assetDaoProvider;
    this.transactionDaoProvider = transactionDaoProvider;
    this.apiProvider = apiProvider;
  }

  @Override
  public AssetRepository get() {
    return newInstance(assetDaoProvider.get(), transactionDaoProvider.get(), apiProvider.get());
  }

  public static AssetRepository_Factory create(Provider<AssetDao> assetDaoProvider,
      Provider<TransactionDao> transactionDaoProvider, Provider<AssetTrackApiService> apiProvider) {
    return new AssetRepository_Factory(assetDaoProvider, transactionDaoProvider, apiProvider);
  }

  public static AssetRepository newInstance(AssetDao assetDao, TransactionDao transactionDao,
      AssetTrackApiService api) {
    return new AssetRepository(assetDao, transactionDao, api);
  }
}
