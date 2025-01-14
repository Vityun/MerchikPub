package ua.com.merchik.merchik.dialogs.EKL

class EKLDataHolder private constructor() {

    var usersPTTid: Int? = null
    var usersPTTName: String? = null
    var usersPTTWorkAddressId: Int? = null
    var usersPTTClientId: Int? = null
    var usersPTTNumberTel1: String? = null
    var usersPTTNumberTel2: String? = null



    companion object {
        private var instance: EKLDataHolder? = null
        fun instance(): EKLDataHolder {
            if (instance == null) {
                instance = EKLDataHolder()
            }
            return instance!!
        }
    }

    fun init() {
        usersPTTid = null
        usersPTTName = null
        usersPTTWorkAddressId = null
        usersPTTClientId = null
        usersPTTNumberTel1 = null
        usersPTTNumberTel2 = null
    }
}