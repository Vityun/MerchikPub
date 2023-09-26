package ua.com.merchik.merchik.retrofit


const val UPLOAD_PHOTO_KEY = "UPLOAD_PHOTO_KEY"

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Timeout(val key: String)
