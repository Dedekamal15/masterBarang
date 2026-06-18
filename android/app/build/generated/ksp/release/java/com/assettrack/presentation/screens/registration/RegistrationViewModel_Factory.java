package com.assettrack.presentation.screens.registration;

import com.assettrack.data.CsvParser;
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
public final class RegistrationViewModel_Factory implements Factory<RegistrationViewModel> {
  private final Provider<AssetRepository> repositoryProvider;

  private final Provider<CsvParser> csvParserProvider;

  public RegistrationViewModel_Factory(Provider<AssetRepository> repositoryProvider,
      Provider<CsvParser> csvParserProvider) {
    this.repositoryProvider = repositoryProvider;
    this.csvParserProvider = csvParserProvider;
  }

  @Override
  public RegistrationViewModel get() {
    return newInstance(repositoryProvider.get(), csvParserProvider.get());
  }

  public static RegistrationViewModel_Factory create(Provider<AssetRepository> repositoryProvider,
      Provider<CsvParser> csvParserProvider) {
    return new RegistrationViewModel_Factory(repositoryProvider, csvParserProvider);
  }

  public static RegistrationViewModel newInstance(AssetRepository repository, CsvParser csvParser) {
    return new RegistrationViewModel(repository, csvParser);
  }
}
