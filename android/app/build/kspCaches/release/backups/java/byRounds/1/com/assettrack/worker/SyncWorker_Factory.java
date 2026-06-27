package com.assettrack.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.assettrack.data.MasterBarangManager;
import com.assettrack.domain.repository.AssetRepository;
import dagger.internal.DaggerGenerated;
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
public final class SyncWorker_Factory {
  private final Provider<AssetRepository> repositoryProvider;

  private final Provider<MasterBarangManager> masterBarangManagerProvider;

  public SyncWorker_Factory(Provider<AssetRepository> repositoryProvider,
      Provider<MasterBarangManager> masterBarangManagerProvider) {
    this.repositoryProvider = repositoryProvider;
    this.masterBarangManagerProvider = masterBarangManagerProvider;
  }

  public SyncWorker get(Context context, WorkerParameters workerParams) {
    return newInstance(context, workerParams, repositoryProvider.get(), masterBarangManagerProvider.get());
  }

  public static SyncWorker_Factory create(Provider<AssetRepository> repositoryProvider,
      Provider<MasterBarangManager> masterBarangManagerProvider) {
    return new SyncWorker_Factory(repositoryProvider, masterBarangManagerProvider);
  }

  public static SyncWorker newInstance(Context context, WorkerParameters workerParams,
      AssetRepository repository, MasterBarangManager masterBarangManager) {
    return new SyncWorker(context, workerParams, repository, masterBarangManager);
  }
}
