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

  public MasterBarangManager_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public MasterBarangManager get() {
    return newInstance(contextProvider.get());
  }

  public static MasterBarangManager_Factory create(Provider<Context> contextProvider) {
    return new MasterBarangManager_Factory(contextProvider);
  }

  public static MasterBarangManager newInstance(Context context) {
    return new MasterBarangManager(context);
  }
}
