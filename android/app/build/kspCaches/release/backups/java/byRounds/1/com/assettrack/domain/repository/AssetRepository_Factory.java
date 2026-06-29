package com.assettrack.domain.repository;

import android.content.Context;
import com.assettrack.data.SyncPreferences;
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
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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

  private final Provider<Context> contextProvider;

  private final Provider<SyncPreferences> syncPrefsProvider;

  public AssetRepository_Factory(Provider<AssetDao> assetDaoProvider,
      Provider<TransactionDao> transactionDaoProvider, Provider<AssetTrackApiService> apiProvider,
      Provider<Context> contextProvider, Provider<SyncPreferences> syncPrefsProvider) {
    this.assetDaoProvider = assetDaoProvider;
    this.transactionDaoProvider = transactionDaoProvider;
    this.apiProvider = apiProvider;
    this.contextProvider = contextProvider;
    this.syncPrefsProvider = syncPrefsProvider;
  }

  @Override
  public AssetRepository get() {
    return newInstance(assetDaoProvider.get(), transactionDaoProvider.get(), apiProvider.get(), contextProvider.get(), syncPrefsProvider.get());
  }

  public static AssetRepository_Factory create(Provider<AssetDao> assetDaoProvider,
      Provider<TransactionDao> transactionDaoProvider, Provider<AssetTrackApiService> apiProvider,
      Provider<Context> contextProvider, Provider<SyncPreferences> syncPrefsProvider) {
    return new AssetRepository_Factory(assetDaoProvider, transactionDaoProvider, apiProvider, contextProvider, syncPrefsProvider);
  }

  public static AssetRepository newInstance(AssetDao assetDao, TransactionDao transactionDao,
      AssetTrackApiService api, Context context, SyncPreferences syncPrefs) {
    return new AssetRepository(assetDao, transactionDao, api, context, syncPrefs);
  }
}
