package com.assettrack.service;

import com.assettrack.domain.repository.AssetRepository;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class SyncForegroundService_MembersInjector implements MembersInjector<SyncForegroundService> {
  private final Provider<AssetRepository> repositoryProvider;

  public SyncForegroundService_MembersInjector(Provider<AssetRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  public static MembersInjector<SyncForegroundService> create(
      Provider<AssetRepository> repositoryProvider) {
    return new SyncForegroundService_MembersInjector(repositoryProvider);
  }

  @Override
  public void injectMembers(SyncForegroundService instance) {
    injectRepository(instance, repositoryProvider.get());
  }

  @InjectedFieldSignature("com.assettrack.service.SyncForegroundService.repository")
  public static void injectRepository(SyncForegroundService instance, AssetRepository repository) {
    instance.repository = repository;
  }
}
