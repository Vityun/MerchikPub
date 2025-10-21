package ua.com.merchik.merchik.features.maps.di



import javax.inject.Qualifier


@Target(AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION, AnnotationTarget.TYPE)
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class FromMaps


@Target(AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION, AnnotationTarget.TYPE)
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class FromWPdata