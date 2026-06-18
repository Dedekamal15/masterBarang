package com.assettrack.di;

import com.assettrack.data.local.AssetTrackDatabase;
import com.assettrack.data.local.dao.AssetDao;
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
public final class AppModule_ProvideAssetDaoFactory implements Factory<AssetDao> {
  private final Provider<AssetTrackDatabase> dbProvider;

  public AppModule_ProvideAssetDaoFactory(Provider<AssetTrackDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public AssetDao get() {
    return provideAssetDao(dbProvider.get());
  }

  public static AppModule_ProvideAssetDaoFactory create(Provider<AssetTrackDatabase> dbProvider) {
    return new AppModule_ProvideAssetDaoFactory(dbProvider);
  }

  public static AssetDao provideAssetDao(AssetTrackDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideAssetDao(db));
  }
}
