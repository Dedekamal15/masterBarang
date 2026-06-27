package com.assettrack;

import androidx.hilt.work.HiltWorkerFactory;
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
public final class AssetTrackApp_MembersInjector implements MembersInjector<AssetTrackApp> {
  private final Provider<HiltWorkerFactory> workerFactoryProvider;

  public AssetTrackApp_MembersInjector(Provider<HiltWorkerFactory> workerFactoryProvider) {
    this.workerFactoryProvider = workerFactoryProvider;
  }

  public static MembersInjector<AssetTrackApp> create(
      Provider<HiltWorkerFactory> workerFactoryProvider) {
    return new AssetTrackApp_MembersInjector(workerFactoryProvider);
  }

  @Override
  public void injectMembers(AssetTrackApp instance) {
    injectWorkerFactory(instance, workerFactoryProvider.get());
  }

  @InjectedFieldSignature("com.assettrack.AssetTrackApp.workerFactory")
  public static void injectWorkerFactory(AssetTrackApp instance, HiltWorkerFactory workerFactory) {
    instance.workerFactory = workerFactory;
  }
}
