package com.assettrack.data;

import android.content.Context;
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
public final class MasterBarangManager_Factory implements Factory<MasterBarangManager> {
  private final Provider<Context> contextProvider;

  private final Provider<ImageCompressor> imageCompressorProvider;

  public MasterBarangManager_Factory(Provider<Context> contextProvider,
      Provider<ImageCompressor> imageCompressorProvider) {
    this.contextProvider = contextProvider;
    this.imageCompressorProvider = imageCompressorProvider;
  }

  @Override
  public MasterBarangManager get() {
    return newInstance(contextProvider.get(), imageCompressorProvider.get());
  }

  public static MasterBarangManager_Factory create(Provider<Context> contextProvider,
      Provider<ImageCompressor> imageCompressorProvider) {
    return new MasterBarangManager_Factory(contextProvider, imageCompressorProvider);
  }

  public static MasterBarangManager newInstance(Context context, ImageCompressor imageCompressor) {
    return new MasterBarangManager(context, imageCompressor);
  }
}
