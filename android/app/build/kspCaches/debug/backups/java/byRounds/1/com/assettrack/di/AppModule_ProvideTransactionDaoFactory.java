package com.assettrack.di;

import com.assettrack.data.local.AssetTrackDatabase;
import com.assettrack.data.local.dao.TransactionDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AppModule_ProvideTransactionDaoFactory implements Factory<TransactionDao> {
  private final Provider<AssetTrackDatabase> dbProvider;

  public AppModule_ProvideTransactionDaoFactory(Provider<AssetTrackDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public TransactionDao get() {
    return provideTransactionDao(dbProvider.get());
  }

  public static AppModule_ProvideTransactionDaoFactory create(
      Provider<AssetTrackDatabase> dbProvider) {
    return new AppModule_ProvideTransactionDaoFactory(dbProvider);
  }

  public static TransactionDao provideTransactionDao(AssetTrackDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideTransactionDao(db));
  }
}
