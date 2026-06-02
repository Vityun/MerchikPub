package ua.com.merchik.merchik.Activities.TaskAndReclamations.TasksActivity

class TarPhotoDataHolder private constructor() {

    var photoToId: Int? = null

    companion object {
        private var instance: TarPhotoDataHolder? = null
        fun instance(): TarPhotoDataHolder {
            if (instance == null) {
                instance = TarPhotoDataHolder()
            }
            return instance!!
        }
    }

    fun init() {
        photoToId = null
    }
}